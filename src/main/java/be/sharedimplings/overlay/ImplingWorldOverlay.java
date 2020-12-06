package be.sharedimplings.overlay;

import be.sharedimplings.servercommunication.ConnectionState;
import be.sharedimplings.servercommunication.ConnectionStateHolder;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class ImplingWorldOverlay extends OverlayPanel {


    @Inject
    private Client client;

    @Inject
    private ReceivedImpSightings implings;

    @Inject
    private ConnectionStateHolder connectionStateHolder;

    public ImplingWorldOverlay() {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPreferredSize(new Dimension(300, 20));
        setDragTargetable(true);
    }


    @Override
    public Dimension render(Graphics2D graphics) {
        panelComponent.getChildren().add(TitleComponent.builder()
                .text(connectionStateHolder.getState().name())
                .color(colorOf(connectionStateHolder.getState()))
                .build());
//figure out a decent way to visualize the data
        GroupedImplingState stateToRender = implings.getStateRelevantAt(client.getTickCount());

        stateToRender.getImplingLocationHistories()
                .forEach(impLocationHistory -> {
                    boolean inCurrentWorld = impLocationHistory.getWorld() == client.getWorld();
                    String worldText = inCurrentWorld ? "" : " (W" + impLocationHistory.getWorld() + ")";
                    panelComponent.getChildren().add(TitleComponent.builder()
                            .text(impLocationHistory.getImplingType() + "," + impLocationHistory.locationDescription() + worldText)
                            .color(inCurrentWorld ? Color.GREEN : Color.RED)
                            .build());


                });
        return super.render(graphics);
    }

    private Color colorOf(ConnectionState state) {
        switch (state){
            case CONNECTED:
                return Color.GREEN;
            case CONNECTING:
                return Color.ORANGE;
            case DISCONNECTED:
                return Color.RED;
            default:
                return Color.BLUE;
        }

    }
}
