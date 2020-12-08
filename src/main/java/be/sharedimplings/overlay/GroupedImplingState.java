package be.sharedimplings.overlay;

import be.sharedimplings.ImpDespawn;
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


    public static GroupedImplingState createFor(List<ImpSighting> impSightings, List<ImpDespawn> despawns, int currentTick) {
        Map<Integer, List<ImpSighting>> sightingsPerImp = impSightings.stream()
                .collect(Collectors.groupingBy(ImpSighting::getNpcIndexAndWorldUniqueness));
        Map<Integer, List<ImpDespawn>> despawnsPerImp = despawns.stream()
                .collect(Collectors.groupingBy(ImpDespawn::getNpcIndexAndWorldUniqueness));

        List<ImplingLocationHistory> impStates = sightingsPerImp.entrySet().stream()
                .map(entry -> ImplingLocationHistory.createFor(entry.getValue(), despawnsPerImp.get(entry.getKey()), currentTick))
                .collect(Collectors.toList());
        return new GroupedImplingState(impStates);
    }


}
