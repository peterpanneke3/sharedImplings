package be.sharedimplings.overlay;

import be.sharedimplings.ImpSighting;
import be.sharedimplings.ImplingType;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.worldmap.WorldMapPointManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class ReceivedImpSightings {

    @Inject
    private ItemManager itemManager;
    @Inject
    private WorldMapPointManager worldMapPointManager;

    private List<ImpSighting> impSightings = new ArrayList<>();
    private final List<ImplingWorldMapPoint> implingWorldPoints = new ArrayList<>();


    private int cacheIsForTick = -1;
    private GroupedImplingState cachedImplingState = GroupedImplingState.createFor(Collections.emptyList(), 0);

    public GroupedImplingState getStateRelevantAt(int currentTick) {
        if (cacheIsForTick == currentTick) {
            return cachedImplingState;
        }
        List<ImpSighting> recentSightings = impSightings.stream()
                .filter(sighting -> currentTick - sighting.getReceivedAtTick() < 50) //TODO allow to configure?
                .collect(Collectors.toList());

        GroupedImplingState state = GroupedImplingState.createFor(recentSightings, currentTick);
        cachedImplingState = state;
        cacheIsForTick = currentTick;
        return state;
    }

    public void addSighting(ImpSighting sighting) {
        impSightings.add(sighting);
        impSightings = impSightings.stream()
                .sorted(Comparator.comparingInt(ImpSighting::getReceivedAtTick).reversed())
                .limit(500)
                .collect(Collectors.toList());

    }

    public void updateWorldpoints() {
        //TODO move - change concept
        worldMapPointManager.removeIf(implingWorldPoints::remove);

        cachedImplingState.getImplingLocationHistories()
                .forEach(
                        implingLocationHistory -> {
                            ImplingWorldMapPoint worldMapPoint =
                                    new ImplingWorldMapPoint(implingLocationHistory.getMostRecentLocation().getWorldPoint(), implingLocationHistory.getImplingType(), itemManager);

                            worldMapPointManager.add(worldMapPoint);
                            implingWorldPoints.add(worldMapPoint);
                        }
                );
    }

    public boolean isNewSighting(int world, ImplingType implingType, int npcIndex) {
        return impSightings.stream()
                .noneMatch(
                        sighting ->
                                sighting.getImplingType() == implingType
                                        && sighting.getNpcIndex() == npcIndex
                                        && sighting.getWorld() == world
                );
    }

    public void clear() {
        worldMapPointManager.removeIf(ImplingWorldMapPoint.class::isInstance);
        implingWorldPoints.clear();
    }
}
