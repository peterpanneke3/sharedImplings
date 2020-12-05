package be.sharedimplings.overlay;

import be.sharedimplings.ImpSighting;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.worldmap.WorldMapPointManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class ReceivedImpSightings {

    @Inject
    private ItemManager itemManager;
    @Inject
    private WorldMapPointManager worldMapPointManager;

    private final List<ImpSighting> impSightings = new ArrayList<>();
    private final List<ImplingWorldMapPoint> implingWorldPoints = new ArrayList<>();


    private int cacheIsForTick = -1;
    private GroupedImplingState cachedImplingState = GroupedImplingState.createFor(Collections.emptyList(), 0);

    public GroupedImplingState getStateRelevantAt(int currentTick) {
        if (cacheIsForTick == currentTick) {
            return cachedImplingState;
        }
        List<ImpSighting> recentSightings = impSightings.stream()
                .filter(sighting -> currentTick - sighting.getReceivedAtTick() < 30) //TODO allow to configure?
                .collect(Collectors.toList());

        GroupedImplingState state = GroupedImplingState.createFor(recentSightings, currentTick);
        cachedImplingState = state;
        cacheIsForTick = currentTick;
        return state;
    }

    public void addSighting(ImpSighting sighting) {
        impSightings.add(sighting);
    }

    public void updateWorldpoints() {
        //TODO move - change concept
        worldMapPointManager.removeIf(wp -> implingWorldPoints.remove(wp));

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
}
