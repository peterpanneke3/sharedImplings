package be.sharedimplings.overlay;

import be.sharedimplings.ImpSighting;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class GroupedImplingState {


    private final List<ImplingLocationHistory> implingLocationHistories;


    public static GroupedImplingState createFor(List<ImpSighting> impSightings, int currentTick) {
        Map<Integer, List<ImpSighting>> sightingsPerImp = impSightings.stream()
                .collect(Collectors.groupingBy(ImpSighting::getNpcIndexAndWorldUniqueness));
        List<ImplingLocationHistory> impStates = sightingsPerImp.values()
                .stream()
                .map((List<ImpSighting> impSightings1) -> ImplingLocationHistory.createFor(impSightings1, currentTick))
                .collect(Collectors.toList());
        return new GroupedImplingState(impStates);
    }


}
