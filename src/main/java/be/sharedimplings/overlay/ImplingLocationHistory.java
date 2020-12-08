package be.sharedimplings.overlay;

import be.sharedimplings.ImpDespawn;
import be.sharedimplings.ImpSighting;
import be.sharedimplings.ImplingType;
import lombok.Builder;
import lombok.Value;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@Value
public class ImplingLocationHistory {

    ImplingType implingType;
    int world;
    List<LocationAge> previousLocations;
    boolean reportedAsDespawned;


    public static ImplingLocationHistory createFor(List<ImpSighting> impSightings, List<ImpDespawn> impDespawns, int currentTick) {
        impDespawns = impDespawns == null ? Collections.emptyList() : impDespawns;
        return ImplingLocationHistory.builder()
                .implingType(impSightings.get(0).getImplingType())
                .world(impSightings.get(0).getWorld())
                .previousLocations(mapToLocationAges(impSightings, currentTick))
                .reportedAsDespawned(!impDespawns.isEmpty())
                .build();
    }

    private static List<LocationAge> mapToLocationAges(List<ImpSighting> impSightings, int currentTick) {
        return impSightings.stream()
                .map(
                        sighting -> LocationAge.builder()
                                .tickAge(currentTick - sighting.getReceivedAtTick())
                                .worldPoint(sighting.getWorldLocation()).build()
                ).collect(Collectors.toList());
    }


    public String locationDescription() {
        LocationAge mostRecentLocation = getMostRecentLocation();
        return DescriptionProvider.getDescriptionFor(mostRecentLocation.getWorldPoint().getRegionID());
    }

    public int ageInSeconds(){
        return (int) (getMostRecentLocation().getTickAge() * 0.6);
    }

    public LocationAge getMostRecentLocation() {
        return previousLocations.stream()
                .min(Comparator.comparing(LocationAge::getTickAge))
                .orElseThrow(RuntimeException::new);
    }


}
