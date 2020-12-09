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
        if (!message.isValid()) {
            log.debug("received invalid message" + message.toString());
            return;
        }
        ImplingDespawnData despawnData = message.getData();
        ImpDespawn impDespawn = new ImpDespawn(despawnData.getWorld(), despawnData.getImplingType(), despawnData.getNpcIndex(), despawnData.getWorldLocation(), client.getTickCount());
        receivedImpSightings.despawned(impDespawn);
    }

    @Subscribe
    public void onReportImplingSighting(ReportImplingSighting message) {
        if (!message.isValid()) {
            log.debug("received invalid message" + message.toString());
            return;
        }
        ImplingSightingData impLocationMessage = message.getData();

        boolean interestedInSighting = configIsInterestedInSighting(impLocationMessage);
        if (interestedInSighting) {

            notifyIfNeeded(impLocationMessage);

            ImpSighting impSighting = new ImpSighting(impLocationMessage.getWorld(), impLocationMessage.getImplingType(), impLocationMessage.getNpcIndex(), impLocationMessage.getWorldLocation(), client.getTickCount());

            receivedImpSightings.addSighting(impSighting);
        } else {
            log.debug("not interested in received message for " + impLocationMessage.getImplingType());
        }
    }

    private boolean configIsInterestedInSighting(ImplingSightingData impLocationMessage) {
        boolean isPuroPuro = impLocationMessage.getWorldLocation().getRegionID() == 10307;
        ImplingType implingType = impLocationMessage.getImplingType();
        boolean isCurrentWorld = client.getWorld() == impLocationMessage.getWorld();

        switch (implingType) {
            case DRAGON:
                return testConfigFilter(isPuroPuro, isCurrentWorld, config.receiveDragon(), config.receiveDragonOnlyCurrentWorld());
            case LUCKY:
                return testConfigFilter(isPuroPuro, isCurrentWorld, config.receiveLucky(), config.receiveLuckyOnlyCurrentWorld());
            case NINJA:
                return testConfigFilter(isPuroPuro, isCurrentWorld, config.receiveNinja(), config.receiveNinjaOnlyCurrentWorld());
            case MAGPIE:
                return testConfigFilter(isPuroPuro, isCurrentWorld, config.receiveMagpie(), config.receiveMagpieOnlyCurrentWorld());
            default:
                return false;
        }
    }

    private boolean configWantsToNotifySighting(ImplingSightingData impLocationMessage) {
        boolean isPuroPuro = impLocationMessage.getWorldLocation().getRegionID() == 10307;
        ImplingType implingType = impLocationMessage.getImplingType();
        boolean isCurrentWorld = client.getWorld() == impLocationMessage.getWorld();

        switch (implingType) {
            case DRAGON:
                return testConfigFilter(isPuroPuro, isCurrentWorld, config.receiveDragonNotificationFilter(), config.receiveDragonOnlyCurrentWorld());
            case LUCKY:
                return testConfigFilter(isPuroPuro, isCurrentWorld, config.receiveLuckyNotificationFilter(), config.receiveLuckyOnlyCurrentWorld());
            case NINJA:
                return testConfigFilter(isPuroPuro, isCurrentWorld, config.receiveNinjaNotificationFilter(), config.receiveNinjaOnlyCurrentWorld());
            case MAGPIE:
                return testConfigFilter(isPuroPuro, isCurrentWorld, config.receiveMagpieNotificationFilter(), config.receiveMagpieOnlyCurrentWorld());
            default:
                return false;
        }
    }

    private boolean testConfigFilter(boolean isPuroPuro, boolean isCurrentWorld, LocationOption locationFilter, boolean onlyCurrentWorld) {
        boolean worldOk = isCurrentWorld || !onlyCurrentWorld;
        boolean locationOk = testLocationFilter(isPuroPuro, locationFilter);
        return worldOk && locationOk;
    }

    private boolean testLocationFilter(boolean isPuroPuro, LocationOption locationFilter) {
        switch (locationFilter) {
            case EXCLUDE_PURO_PURO:
                return !isPuroPuro;
            case EVERYWHERE:
                return true;
            default:
                return false;
        }
    }

    private void notifyIfNeeded(ImplingSightingData impLocationMessage) {
        boolean configAccepts = configWantsToNotifySighting(impLocationMessage);
        if (!configAccepts) {
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
                    Impling impling = new Impling(impType, npc);
                    if (shouldReport(impling)) {
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

    private boolean shouldReport(Impling impling) {
        boolean isPuroPuro = impling.getNpc().getWorldLocation().getRegionID() == 10307;
        ImplingType implingType = impling.getImplingType();

        switch (implingType) {
            case DRAGON:
                return testLocationFilter(isPuroPuro, config.reportDragon());
            case LUCKY:
                return testLocationFilter(isPuroPuro, config.reportLucky());
            case NINJA:
                return testLocationFilter(isPuroPuro, config.reportNinja());
            case MAGPIE:
                return testLocationFilter(isPuroPuro, config.reportMagpie());
            default:
                return false;
        }
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
                    if (despawnHappenedNearPlayer(npcDespawned)) {
                        reportDespawnOf(new Impling(implingType, npcDespawned.getNpc()));
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
        } catch (Exception e) {
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


    @Schedule(period = 10, unit = ChronoUnit.SECONDS, asynchronous = true)
    public void autoReconnect() {
        if (stateHolder.getState() == ConnectionState.DISCONNECTED) {
            socketClient.connect();
        }
    }

    public static boolean devMode = false;
    public static int devIndex = 0;

    @Schedule(period = 20, unit = ChronoUnit.SECONDS)
    public void fakeReceiveImplingLocation() {
        if (devMode && client.getGameState() == GameState.LOGGED_IN) {
            devIndex = (devIndex + 1) % 4;
            ReportImplingSighting fakeSighting = new ReportImplingSighting(ImplingSightingData.builder()
                    .npcIndex(devIndex)
                    .implingType(indexToImp(devIndex))
                    .worldLocation(client.getLocalPlayer().getWorldLocation())
                    .world(client.getWorld())
                    .build());
            onReportImplingSighting(fakeSighting);
        }
    }

    private ImplingType indexToImp(int rng) {
        switch (rng) {
            case 0:
                return ImplingType.LUCKY;
            case 1:
                return ImplingType.DRAGON;
            case 2:
                return ImplingType.NINJA;
            default:
                return ImplingType.MAGPIE;
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