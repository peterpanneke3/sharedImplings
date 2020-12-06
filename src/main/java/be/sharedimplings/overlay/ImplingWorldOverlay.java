package be.sharedimplings.overlay;

import be.sharedimplings.ImplingType;
import be.sharedimplings.servercommunication.ConnectionState;
import be.sharedimplings.servercommunication.ConnectionStateHolder;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

import static be.sharedimplings.ImplingType.DRAGON;
import static be.sharedimplings.ImplingType.LUCKY;

public class ImplingWorldOverlay extends OverlayPanel {


    @Inject
    private Client client;

    @Inject
    private ReceivedImpSightings implings;

    @Inject
    private ConnectionStateHolder connectionStateHolder;

    public ImplingWorldOverlay() {
        setPosition(OverlayPosition.TOP_CENTER);
        setPriority(OverlayPriority.MED);
    }


    @Override
    public Dimension render(Graphics2D graphics) {
        if(connectionStateHolder.getState() != ConnectionState.CONNECTED) {
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text(connectionStateHolder.getState().name())
                    .color(colorOf(connectionStateHolder.getState()))
                    .build());
        }

        GroupedImplingState stateToRender = implings.getStateRelevantAt(client.getTickCount());


        List<ImplingLocationHistory> dragonImpsToRender = getImps(DRAGON, stateToRender);
        if(!dragonImpsToRender.isEmpty()){
            renderImps(DRAGON, dragonImpsToRender);
        }


        List<ImplingLocationHistory> luckyImpsToRender = getImps(LUCKY, stateToRender);
        if(!luckyImpsToRender.isEmpty()){
            renderImps(LUCKY, luckyImpsToRender);
        }


        return super.render(graphics);
    }

    private void renderImps(ImplingType implingType, List<ImplingLocationHistory> impsOfType) {
        panelComponent.getChildren().add(TitleComponent.builder()
                .text(implingType.name())
                .color(Color.ORANGE)
                .build());

        impsOfType
                .forEach(impLocationHistory -> {
                    boolean inCurrentWorld = impLocationHistory.getWorld() == client.getWorld();
                    String worldText = inCurrentWorld ? "" : " (W" + impLocationHistory.getWorld() + ")";
                    panelComponent.getChildren().add(TitleComponent.builder()
                            .text(impLocationHistory.locationDescription() + worldText)
                            .color(inCurrentWorld ? Color.GREEN : Color.RED)
                            .build());
                });

    }

    private List<ImplingLocationHistory> getImps(ImplingType implingType, GroupedImplingState stateToRender) {
        return stateToRender.getImplingLocationHistories()
                .stream().filter(lh -> lh.getImplingType() == implingType)
                .collect(Collectors.toList());

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
