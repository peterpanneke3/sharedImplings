package be.sharedimplings.servercommunication;

import lombok.Value;
import net.runelite.http.api.ws.WebsocketMessage;

@Value
public class ReportImplingSighting extends WebsocketMessage {
    private final ImplingSightingData data;
    private final String action = "reportlocation";

    public ReportImplingSighting(ImplingSightingData data) {
        this.data = data;
    }
}
