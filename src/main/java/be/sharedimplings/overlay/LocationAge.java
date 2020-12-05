package be.sharedimplings.overlay;

import lombok.Builder;
import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

@Builder
@Getter
public class LocationAge {

    private WorldPoint worldPoint;
    private int tickAge;

}
