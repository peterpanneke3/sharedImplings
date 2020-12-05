package be.sharedimplings.overlay;

import be.sharedimplings.ImplingType;
import net.runelite.api.ItemID;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.worldmap.WorldMapPoint;
import net.runelite.client.util.ImageUtil;

import java.awt.*;
import java.awt.image.BufferedImage;

class ImplingWorldMapPoint extends WorldMapPoint
{
	private final BufferedImage implingWorldImage;
	private final Point implingWorldPoint;
	private BufferedImage mapArrow;
	private ImplingType implingType;
	private ItemManager itemManager;

	BufferedImage getMapArrow()
	{
		if (mapArrow != null)
		{
			return mapArrow;
		}

		mapArrow = ImageUtil.getResourceStreamFromClass(getClass(), "/util/clue_arrow.png");

		return mapArrow;
	}

	public BufferedImage getImplingImage()
	{
		switch (implingType) {
			case DRAGON:
				return itemManager.getImage(ItemID.DRAGON_IMPLING_JAR);
			case LUCKY:
				return itemManager.getImage(ItemID.LUCKY_IMPLING_JAR);
			default:
				return itemManager.getImage(ItemID.CAKE_OF_GUIDANCE);
		}
	}

	ImplingWorldMapPoint(final WorldPoint worldPoint, ImplingType implingType, ItemManager itemManager)
	{
		super(worldPoint, null);
		this.implingType = implingType;
		this.itemManager = itemManager;

		implingWorldImage = new BufferedImage(getMapArrow().getWidth(), getMapArrow().getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics graphics = implingWorldImage.getGraphics();
		graphics.drawImage(getMapArrow(), 0, 0, null);
		graphics.drawImage(getImplingImage(), 0, 0, null);
		implingWorldPoint = new Point(
			implingWorldImage.getWidth() / 2,
			implingWorldImage.getHeight());

		this.setSnapToEdge(true);
		this.setJumpOnClick(true);
		this.setImage(implingWorldImage);
		this.setImagePoint(implingWorldPoint);
	}

	@Override
	public void onEdgeSnap()
	{
		this.setImage(getImplingImage());
		this.setImagePoint(null);
	}

	@Override
	public void onEdgeUnsnap()
	{
		this.setImage(implingWorldImage);
		this.setImagePoint(implingWorldPoint);
	}

}
