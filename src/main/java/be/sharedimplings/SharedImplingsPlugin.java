package be.sharedimplings;

import be.sharedimplings.overlay.DescriptionProvider;
import be.sharedimplings.overlay.ImplingWorldOverlay;
import be.sharedimplings.overlay.ReceivedImpSightings;
import be.sharedimplings.servercommunication.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
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
@Slf4j
public class SharedImplingsPlugin extends Plugin {


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
    public void onReportImplingDespawn(ReportImplingDespawn message) {
        if(!message.isValid()){
            log.debug("received invalid message" + message.toString());
            return;
        }
        ImplingDespawnData despawnData = message.getData();
        ImpDespawn impDespawn = new ImpDespawn(despawnData.getWorld(), despawnData.getImplingType(), despawnData.getNpcIndex(), despawnData.getWorldLocation(), client.getTickCount());
        receivedImpSightings.despawned(impDespawn);
    }

    @Subscribe
    public void onReportImplingSighting(ReportImplingSighting message) {
        if(!message.isValid()){
            log.debug("received invalid message" + message.toString());
            return;
        }
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
            log.debug("not interested in received message for " + impLocationMessage.getImplingType());
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
        NPC npc = npcDespawned.getNpc();
        Optional<ImplingType> implingTypeOpt = ImplingType.forNpcId(npc.getId());
        implingTypeOpt.ifPresent(
                implingType -> {
                    //if the imp moves out of sight, it should not be reported as gone,
                    //because it might still be there
                    //happens when player teleports, or the player/imp walk away
                    if(despawnHappenedNearPlayer(npcDespawned)){
                        reportDespawnOf(new Impling( implingType, npcDespawned.getNpc()));
                    }
                }

        );

        reportNewLocationsOf = reportNewLocationsOf.stream()
                .filter(tracked -> tracked.getNpc().getIndex() != npcDespawned.getNpc().getIndex())
                .collect(Collectors.toSet());
    }

    private boolean despawnHappenedNearPlayer(NpcDespawned npcDespawned) {
        try {
            WorldPoint despawnLocation = npcDespawned.getNpc().getWorldLocation();
            WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
            return despawnLocation.distanceTo(playerLocation) < 15;
        }catch (Exception e){
            return false;
        }
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


    private void reportDespawnOf(Impling impling) {
        ReportImplingDespawn impDespawn = new ReportImplingDespawn(ImplingDespawnData.builder()
                .npcIndex(impling.getNpc().getIndex())
                .implingType(impling.getImplingType())
                .worldLocation(impling.getNpc().getWorldLocation())
                .world(client.getWorld())
                .build());
        socketClient.send(impDespawn);
    }

    public static boolean devMode = false;

    @Schedule(period = 10, unit = ChronoUnit.SECONDS, asynchronous = true)
    public void autoReconnect(){
        if(stateHolder.getState() == ConnectionState.DISCONNECTED){
            socketClient.connect();
        }
    }



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

    @Schedule(period = 17, unit = ChronoUnit.SECONDS)
    public void fakeReceiveImplingDespawn() {
        if (devMode && client.getGameState() == GameState.LOGGED_IN) {
            ReportImplingDespawn fakeDespawn = new ReportImplingDespawn(ImplingDespawnData.builder()
                    .npcIndex(10)
                    .implingType(ImplingType.DRAGON)
                    .worldLocation(client.getLocalPlayer().getWorldLocation())
                    .world(client.getWorld())
                    .build());
            onReportImplingDespawn(fakeDespawn);
        }
    }
}