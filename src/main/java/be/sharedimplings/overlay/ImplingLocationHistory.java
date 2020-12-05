package be.sharedimplings.overlay;

import be.sharedimplings.ImpSighting;
import be.sharedimplings.ImplingType;
import lombok.Builder;
import lombok.Value;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@Value
public class ImplingLocationHistory {

    ImplingType implingType;
    int world;
    List<LocationAge> previousLocations;


    public static ImplingLocationHistory createFor(List<ImpSighting> impSightings, int currentTick) {
        return ImplingLocationHistory.builder()
                .implingType(impSightings.get(0).getImplingType())
                .world(impSightings.get(0).getWorld())
                .previousLocations(mapToLocationAges(impSightings, currentTick))
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
        String regionName = DescriptionProvider.getDescriptionFor(mostRecentLocation.getWorldPoint().getRegionID());
        return regionName + " " + mostRecentLocation.getTickAge();
    }

    public LocationAge getMostRecentLocation() {
        return previousLocations.stream()
                .min(Comparator.comparing(LocationAge::getTickAge))
                .orElseThrow(RuntimeException::new);
    }
}
