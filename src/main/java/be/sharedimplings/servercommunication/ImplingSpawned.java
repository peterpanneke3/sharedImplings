package be.sharedimplings.servercommunication;

import lombok.Value;

@Value
public class ImplingSpawned {
    private final ImplingSpawnedData data;
    private final String action = "reportlocation";

    public ImplingSpawned(ImplingSpawnedData data) {
        this.data = data;
    }
}
