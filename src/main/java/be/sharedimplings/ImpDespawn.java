package be.sharedimplings;

import lombok.AllArgsConstructor;
import lombok.Value;
import net.runelite.api.coords.WorldPoint;

@AllArgsConstructor
@Value
public class ImpDespawn {

    int world;
    ImplingType implingType;
    int npcIndex;
    WorldPoint worldLocation;
    int receivedAtTick;

    public  int getNpcIndexAndWorldUniqueness() {
        return npcIndex * 1000 + world; //ensures uniqueness if theres less than 1k worlds
    }
}
