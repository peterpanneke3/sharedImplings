package be.sharedimplings.servercommunication;

import lombok.Value;
import net.runelite.http.api.ws.WebsocketMessage;

@Value
public class ReportImplingDespawn extends WebsocketMessage {
    private final ImplingDespawnData data;
    //private final String action = "reportdespawn";
    //This is kind of a hack.
    //reportlocation is the only route configured in the websocket gateway, but it'll correclty handle despawn messages
    //TODO should be moved to a different route since logic in the lambda's will change when allowing groups to share internaly
    private final String action = "reportlocation";

    public ReportImplingDespawn(ImplingDespawnData data) {
        this.data = data;
    }

    public boolean isValid() {
        return data != null && data.isValid();
    }
}
