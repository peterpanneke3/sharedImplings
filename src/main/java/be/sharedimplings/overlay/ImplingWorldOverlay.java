package be.sharedimplings.overlay;

import be.sharedimplings.ImplingType;
import be.sharedimplings.SharedImplingsConfig;
import be.sharedimplings.servercommunication.ConnectionState;
import be.sharedimplings.servercommunication.ConnectionStateHolder;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

import static be.sharedimplings.ImplingType.*;

public class ImplingWorldOverlay extends OverlayPanel {


    @Inject
    private Client client;

    @Inject
    private ReceivedImpSightings implings;

    @Inject
    private ConnectionStateHolder connectionStateHolder;

    @Inject
    private SharedImplingsConfig config;

    public ImplingWorldOverlay() {
        setPosition(OverlayPosition.TOP_CENTER);
        setPriority(OverlayPriority.MED);
    }


    @Override
    public Dimension render(Graphics2D graphics) {
        if (!config.showOverlay()) {
            return super.render(graphics);
        }
        if (config.showOverlayConnected() || connectionStateHolder.getState() != ConnectionState.CONNECTED) {
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text(connectionStateHolder.getState().name())
                    .color(colorOf(connectionStateHolder.getState()))
                    .build());
        }

        GroupedImplingState stateToRender = implings.getStateRelevantAt(client.getTickCount());
        List<ImplingLocationHistory> dragonImpsToRender = getImps(DRAGON, stateToRender);
        if (!dragonImpsToRender.isEmpty()) {
            renderImps(DRAGON, dragonImpsToRender);
        }
        List<ImplingLocationHistory> luckyImpsToRender = getImps(LUCKY, stateToRender);
        if (!luckyImpsToRender.isEmpty()) {
            renderImps(LUCKY, luckyImpsToRender);
        }
        List<ImplingLocationHistory> ninjaImpsToRender = getImps(NINJA, stateToRender);
        if (!ninjaImpsToRender.isEmpty()) {
            renderImps(NINJA, ninjaImpsToRender);
        }
        List<ImplingLocationHistory> magpieImpsToRender = getImps(MAGPIE, stateToRender);
        if (!magpieImpsToRender.isEmpty()) {
            renderImps(MAGPIE, magpieImpsToRender);
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
                    String caughtText = impLocationHistory.isReportedAsDespawned() ?  "(CAUGHT)" : "";
                    String directionText = buildDirectionText(impLocationHistory.getMostRecentLocation(), inCurrentWorld);
                    panelComponent.getChildren().add(TitleComponent.builder()
                            .text(impLocationHistory.locationDescription() + worldText + " " + impLocationHistory.ageInSeconds() + "s ago " + caughtText + directionText)
                            .color(inCurrentWorld ? (impLocationHistory.isReportedAsDespawned() ? Color.ORANGE : Color.GREEN) : Color.RED)
                            .build());
                });

    }

    private String buildDirectionText(LocationAge mostRecentLocation, boolean inCurrentWorld) {
        WorldPoint impPoint = mostRecentLocation.getWorldPoint();
        WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
        boolean nearEnough = playerLocation.distanceTo(impPoint) < 60;
        if(!nearEnough || !inCurrentWorld){
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int yDiff = impPoint.getY() - playerLocation.getY();
        if(yDiff > 0){
            sb.append(yDiff).append("N");
        }
        if(yDiff < 0){
            sb.append(-yDiff).append("S");
        }


        int xDiff = impPoint.getX() - playerLocation.getX();
        if(xDiff > 0){
            sb.append(xDiff).append("E");
        }
        if(xDiff < 0) {
            sb.append(-xDiff).append("W");
        }
        return sb.toString();
    }

    private List<ImplingLocationHistory> getImps(ImplingType implingType, GroupedImplingState stateToRender) {
        return stateToRender.getImplingLocationHistories()
                .stream().filter(lh -> lh.getImplingType() == implingType)
                .collect(Collectors.toList());

    }

    private Color colorOf(ConnectionState state) {
        switch (state) {
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
