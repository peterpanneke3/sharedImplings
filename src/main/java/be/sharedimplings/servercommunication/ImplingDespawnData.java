package be.sharedimplings.servercommunication;

import be.sharedimplings.ImplingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import net.runelite.api.coords.WorldPoint;

@Builder
@Value
@AllArgsConstructor
public class ImplingDespawnData {
    int world;
    ImplingType implingType;
    int npcIndex;
    WorldPoint worldLocation;

    public boolean isValid() {
        return implingType != null && worldLocation != null && npcIndex > -1 && world > -1;
    }
}
