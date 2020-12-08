package be.sharedimplings;

import be.sharedimplings.overlay.DescriptionProvider;
import be.sharedimplings.overlay.ImplingWorldOverlay;
import be.sharedimplings.overlay.ReceivedImpSightings;
import be.sharedimplings.servercommunication.ConnectionStateHolder;
import be.sharedimplings.servercommunication.ImplingServerClient;
import be.sharedimplings.servercommunication.ImplingSightingData;
import be.sharedimplings.servercommunication.ReportImplingSighting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.task.Schedule;
import net.runelite.client.ui.overlay.OverlayManager;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@PluginDescriptor(
        name = "Shared Implings",
        description = "Crowdsource implings. Automaticaly report implings. Automaticaly receive impling locations from other users",
        tags = {"hunter", "minimap", "crowdsource", "lucky imp", "dragon imp", "implings"}
)
public class SharedImplingsPlugin extends Plugin {

    private static final Logger logger = LoggerFactory.getLogger(SharedImplingsPlugin.class);

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private ImplingWorldOverlay implingWorldOverlay;

    @Inject
    private Client client;

    @Inject
    private ReceivedImpSightings receivedImpSightings;

    @Inject
    private SharedImplingsConfig config;

    @Inject
    private ConnectionStateHolder stateHolder;

    @Inject
    private Notifier notifier;

    @Inject
    private OkHttpClient okClient;

    @Inject
    private EventBus eventBus;

    private ImplingServerClient socketClient;

    private Gson gson = new GsonBuilder().create();
    ;

    @Provides
    SharedImplingsConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(SharedImplingsConfig.class);
    }


    //TODO split into a reporter (reporter & tracker) and a receiver
    @Override
    protected void startUp() {
        overlayManager.add(implingWorldOverlay);
        socketClient = new ImplingServerClient(okClient, "wss://u2059gjsw9.execute-api.eu-west-2.amazonaws.com/dev", eventBus, stateHolder);
        socketClient.connect();
    }

    @Subscribe
    public void onReportImplingSighting(ReportImplingSighting message) {
        if (!config.receiveDragon() && !config.receiveLucky()) {
            return;
        }

        ImplingSightingData impLocationMessage = message.getData();

        boolean interestedInSighting = (impLocationMessage.getImplingType() == ImplingType.DRAGON && config.receiveDragon()) ||
                (impLocationMessage.getImplingType() == ImplingType.LUCKY && config.receiveLucky());
        interestedInSighting = interestedInSighting && (!config.receiveOnlyCurrentWorldImplings() || client.getWorld() == impLocationMessage.getWorld());
        if (interestedInSighting) {

            notifyIfNeeded(impLocationMessage);

            ImpSighting impSighting = new ImpSighting(impLocationMessage.getWorld(), impLocationMessage.getImplingType(), impLocationMessage.getNpcIndex(), impLocationMessage.getWorldLocation(), client.getTickCount());

            receivedImpSightings.addSighting(impSighting);
        } else {
            logger.debug("not interested in received message for " + impLocationMessage.getImplingType());
        }
    }

    private void notifyIfNeeded(ImplingSightingData impLocationMessage) {
        if (config.notification() == NotificationConfig.NONE) {
            return;
        }

        if (client.getWorld() != impLocationMessage.getWorld() && config.notification() == NotificationConfig.ONLY_CURRENT_WORLD) {
            return;
        }


        if (receivedImpSightings.isNewSighting(impLocationMessage.getWorld(), impLocationMessage.getImplingType(), impLocationMessage.getNpcIndex())) {
            notifier.notify(impLocationMessage.getImplingType() + " impling reported in "
                    + DescriptionProvider.getDescriptionFor(impLocationMessage.getWorldLocation().getRegionID())
                    + (client.getWorld() == impLocationMessage.getWorld() ? " current world." : "in world" + impLocationMessage.getWorld()) + " check the worldmap.");
        }
    }

    private Optional<ImplingSightingData> safeParseImpLocationMessage(String message) {
        try {
            return Optional.of(gson.fromJson(message, ImplingSightingData.class));
        } catch (Exception e) {
            logger.error("Received invalid message on socket", e);
            return Optional.empty();
        }
    }

    @Override
    protected void shutDown() {
        overlayManager.remove(implingWorldOverlay);
        socketClient.close();
        receivedImpSightings.clear();
    }


    @Subscribe
    public void onNpcSpawned(NpcSpawned npcSpawned) {
        NPC npc = npcSpawned.getNpc();
        Optional<ImplingType> implingTypeOpt = ImplingType.forNpcId(npc.getId());

        implingTypeOpt.ifPresent(
                impType -> {
                    if (shouldReport(impType, config)) {
                        Impling impling = new Impling(impType, npc);
                        reportLocationOf(impling);
                        reportNewLocationsOf.add(impling);
                    } else {
                        //TODO refactor this into onconfigchanged
                        //stop reporting - this ensures that changing the config stops tracking
                        reportNewLocationsOf.stream().filter(tracked -> tracked.getNpc().getIndex() == npc.getIndex()).forEach(reportNewLocationsOf::remove);
                    }
                }
        );
    }

    private boolean shouldReport(ImplingType impType, SharedImplingsConfig config) {
        return (impType == ImplingType.DRAGON && config.reportDragon())
                || (impType == ImplingType.LUCKY && config.reportLucky());

    }

    //TODO refactor into ImpTracker
    Set<Impling> reportNewLocationsOf = new HashSet<>();

    @Subscribe
    public void onNpcDespawned(NpcDespawned npcDespawned) {
        reportNewLocationsOf = reportNewLocationsOf.stream()
                .filter(tracked -> tracked.getNpc().getIndex() != npcDespawned.getNpc().getIndex())
                .collect(Collectors.toSet());
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        receivedImpSightings.updateWorldpoints();
        if (client.getTickCount() % 5 == 0) {
            List<NPC> npcs = client.getNpcs();
            reportNewLocationsOf.forEach(
                    imp -> {
                        boolean npcPresent = npcs.stream()
                                .anyMatch(npc -> npc.getIndex() == imp.getNpc().getIndex());
                        if (npcPresent) {
                            reportLocationOf(imp);
                        }
                    });
        }
    }

    private void reportLocationOf(Impling impling) {
        ReportImplingSighting impSpawn = new ReportImplingSighting(ImplingSightingData.builder()
                .npcIndex(impling.getNpc().getIndex())
                .implingType(impling.getImplingType())
                .worldLocation(impling.getNpc().getWorldLocation())
                .world(client.getWorld())
                .build());
        socketClient.send(impSpawn);
    }

    public static boolean devMode = false;

    @Schedule(period = 10, unit = ChronoUnit.SECONDS)
    public void fakeReceiveImplingLocation() {
        if (devMode && client.getGameState() == GameState.LOGGED_IN) {
            ReportImplingSighting fakeSighting = new ReportImplingSighting(ImplingSightingData.builder()
                    .npcIndex(10)
                    .implingType(ImplingType.DRAGON)
                    .worldLocation(client.getLocalPlayer().getWorldLocation())
                    .world(client.getWorld())
                    .build());
            onReportImplingSighting(fakeSighting);
        }
    }
}