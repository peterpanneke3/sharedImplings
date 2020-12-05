package be.sharedimplings.overlay;

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

    public ImplingWorldOverlay() {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPreferredSize(new Dimension(300, 20));
        setDragTargetable(true);
    }


    @Override
    public Dimension render(Graphics2D graphics) {
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
}
