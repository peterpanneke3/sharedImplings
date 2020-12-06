package be.sharedimplings;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("sharedimplings")
public interface SharedImplingsConfig extends Config
{

	@ConfigSection(
		name = "Impling Type Settings",
		description = "Configuration for each type of impling",
		position = 99
	)
	String implingSection = "sharedimplings";

	@ConfigItem(
		position = 1,
		keyName = "reportdragon",
		name = "Report Dragon implings",
		description = "Configures whether or not you want to report Dragon implings to others",
		section = implingSection
	)
	default boolean reportDragon()
	{
		return true;
	}


	@ConfigItem(
			position = 2,
			keyName = "reportlucky",
			name = "Report Lucky implings",
			description = "Configures whether or not you want to report Lucky implings to others",
			section = implingSection
	)
	default boolean reportLucky()
	{
		return true;
	}


	@ConfigItem(
			position = 3,
			keyName = "receivedragon",
			name = "Receive Dragon implings",
			description = "Configures whether or not you want to receive dragon implings from others",
			section = implingSection
	)
	default boolean receiveDragon()
	{
		return true;
	}

	@ConfigItem(
			position = 4,
			keyName = "receivelucky",
			name = "Receive Lucky implings",
			description = "Configures whether or not you want to receive lucky implings from others",
			section = implingSection
	)
	default boolean receiveLucky()
	{
		return true;
	}


	@ConfigItem(
			position = 5,
			keyName = "receiveonlycurrentworldimplings",
			name = "Only receive in current world",
			description = "Yes: only receive implings that are reported in your world",
			section = implingSection
	)
	default boolean receiveOnlyCurrentWorldImplings()
	{
		return false;
	}


	@ConfigItem(
			position = 6,
			keyName = "showsimpoverlay",
			name = "Show the overlay",
			description = "Toggles wether the overlay is show",
			section = implingSection
	)
	default boolean showOverlay()
	{
		return true;
	}

	@ConfigItem(
			position = 7,
			keyName = "showsimpoverlayconnected",
			name = "Show when CONNECTED",
			description = "Toggles wether to show CONNECTED",
			section = implingSection
	)
	default boolean showOverlayConnected()
	{
		return true;
	}

	@ConfigItem(
			position = 8,
			keyName = "notifynewimp",
			name = "Receive notifications",
			description = "Toggles wether to be notified on new imp sightings",
			section = implingSection
	)
	default NotificationConfig notification()
	{
		return NotificationConfig.ALL;
	}
}
