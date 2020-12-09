package be.sharedimplings;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("sharedimplings")
public interface SharedImplingsConfig extends Config
{

	//per impling:
	//report: overworld - overworld+puro - nowhere
	//receive:overworld - overworld+puro - nowhere
	//receivenotifications: overworld - overworld+puro - nowhere
	//receiveworld: only-current all  (boolean)

	//layout configs

	//todo overall config (blacklist world types ?)

	@ConfigSection(
		name = "Dragon Impling settings",
		description = "Configuration for dragon implings",
		position = 2
	)
	String DRAGONIMPLINGSECTION = "dragonimplingsection";

	@ConfigItem(
			position = 1,
			keyName = "reportdragonlocationfilter",
			name = "Report Dragon implings",
			description = "If you scout a dragon impling, do you want to share it with others?",
			section = DRAGONIMPLINGSECTION
	)
	default LocationOption reportDragon()
	{
		return LocationOption.EXCLUDE_PURO_PURO;
	}

	@ConfigItem(
			position = 2,
			keyName = "receivedragonlocationfilter",
			name = "Receive Dragon implings",
			description = "If somebody else scouts a dragon impling, which do you want to receive?",
			section = DRAGONIMPLINGSECTION
	)
	default LocationOption receiveDragon()
	{
		return LocationOption.EXCLUDE_PURO_PURO;
	}

	@ConfigItem(
			position = 3,
			keyName = "receivedragonworldfilter",
			name = "Only in current world",
			description = "If somebody else scouts a dragon impling, do you want to only receive if it's in your current world?",
			section = DRAGONIMPLINGSECTION
	)
	default boolean receiveDragonOnlyCurrentWorld()
	{
		return false;
	}

	@ConfigItem(
			position = 4,
			keyName = "receivedragonnotifications",
			name = "Trigger notification",
			description = "If somebody else scouts a dragon impling, do you want to only receive if it's in your current world?",
			section = DRAGONIMPLINGSECTION
	)
	default LocationOption receiveDragonNotificationFilter()
	{
		return LocationOption.EXCLUDE_PURO_PURO;
	}


@ConfigSection(
		name = "Lucky Impling settings",
		description = "Configuration for lucky implings",
		position = 1
	)
	String LUCKYIMPLINGSECTION = "luckyimplingsection";

	@ConfigItem(
			position = 1,
			keyName = "reportluckylocationfilter",
			name = "Report Lucky implings",
			description = "If you scout a lucky impling, do you want to share it with others?",
			section = LUCKYIMPLINGSECTION
	)
	default LocationOption reportLucky()
	{
		return LocationOption.EXCLUDE_PURO_PURO;
	}

	@ConfigItem(
			position = 2,
			keyName = "receiveluckylocationfilter",
			name = "Receive Lucky implings",
			description = "If somebody else scouts a lucky impling, which do you want to receive?",
			section = LUCKYIMPLINGSECTION
	)
	default LocationOption receiveLucky()
	{
		return LocationOption.EXCLUDE_PURO_PURO;
	}

	@ConfigItem(
			position = 3,
			keyName = "receiveluckyworldfilter",
			name = "Only in current world",
			description = "If somebody else scouts a lucky impling, do you want to only receive if it's in your current world?",
			section = LUCKYIMPLINGSECTION
	)
	default boolean receiveLuckyOnlyCurrentWorld()
	{
		return false;
	}

	@ConfigItem(
			position = 4,
			keyName = "receiveluckynotifications",
			name = "Trigger notification",
			description = "If somebody else scouts a lucky impling, do you want to only receive if it's in your current world?",
			section = LUCKYIMPLINGSECTION
	)
	default LocationOption receiveLuckyNotificationFilter()
	{
		return LocationOption.EXCLUDE_PURO_PURO;
	}


@ConfigSection(
		name = "Ninja Impling settings",
		description = "Configuration for ninja implings",
		position = 3
	)
	String NINJAIMPLINGSECTION = "ninjaimplingsection";

	@ConfigItem(
			position = 1,
			keyName = "reportninjalocationfilter",
			name = "Report Ninja implings",
			description = "If you scout a ninja impling, do you want to share it with others?",
			section = NINJAIMPLINGSECTION
	)
	default LocationOption reportNinja()
	{
		return LocationOption.EXCLUDE_PURO_PURO;
	}

	@ConfigItem(
			position = 2,
			keyName = "receiveninjalocationfilter",
			name = "Receive Ninja implings",
			description = "If somebody else scouts a ninja impling, which do you want to receive?",
			section = NINJAIMPLINGSECTION
	)
	default LocationOption receiveNinja()
	{
		return LocationOption.EXCLUDE_PURO_PURO;
	}

	@ConfigItem(
			position = 3,
			keyName = "receiveninjaworldfilter",
			name = "Only in current world",
			description = "If somebody else scouts a ninja impling, do you want to only receive if it's in your current world?",
			section = NINJAIMPLINGSECTION
	)
	default boolean receiveNinjaOnlyCurrentWorld()
	{
		return false;
	}

	@ConfigItem(
			position = 4,
			keyName = "receiveninjanotifications",
			name = "Trigger notification",
			description = "If somebody else scouts a ninja impling, do you want to only receive if it's in your current world?",
			section = NINJAIMPLINGSECTION
	)
	default LocationOption receiveNinjaNotificationFilter()
	{
		return LocationOption.EXCLUDE_PURO_PURO;
	}


@ConfigSection(
		name = "Magpie Impling settings",
		description = "Configuration for dragon implings",
		position = 4
	)
	String MAGPIEIMPLINGSECTION = "magpieimplingsection";

	@ConfigItem(
			position = 1,
			keyName = "reportmagpielocationfilter",
			name = "Report Magpie implings",
			description = "If you scout a magpie impling, do you want to share it with others?",
			section = MAGPIEIMPLINGSECTION
	)
	default LocationOption reportMagpie()
	{
		return LocationOption.EXCLUDE_PURO_PURO;
	}

	@ConfigItem(
			position = 2,
			keyName = "receivemagpielocationfilter",
			name = "Receive Magpie implings",
			description = "If somebody else scouts a magpie impling, which do you want to receive?",
			section = MAGPIEIMPLINGSECTION
	)
	default LocationOption receiveMagpie()
	{
		return LocationOption.EXCLUDE_PURO_PURO;
	}

	@ConfigItem(
			position = 3,
			keyName = "receivemagpieworldfilter",
			name = "Only in current world",
			description = "If somebody else scouts a magpie impling, do you want to only receive if it's in your current world?",
			section = MAGPIEIMPLINGSECTION
	)
	default boolean receiveMagpieOnlyCurrentWorld()
	{
		return false;
	}

	@ConfigItem(
			position = 4,
			keyName = "receivemagpienotifications",
			name = "Trigger notification",
			description = "If somebody else scouts a magpie impling, do you want to only receive if it's in your current world?",
			section = MAGPIEIMPLINGSECTION
	)
	default LocationOption receiveMagpieNotificationFilter()
	{
		return LocationOption.EXCLUDE_PURO_PURO;
	}





	@ConfigSection(
			name = "Layout",
			description = "Layout options",
			position = 5
	)
	String LAYOUTSECTION = "layoutsection";

	@ConfigItem(
			position = 1,
			keyName = "showsimpoverlay",
			name = "Show the overlay",
			description = "Toggles wether the overlay is show",
			section = LAYOUTSECTION
	)
	default boolean showOverlay()
	{
		return true;
	}

	@ConfigItem(
			position = 2,
			keyName = "showsimpoverlayconnected",
			name = "Show when CONNECTED",
			description = "Toggles wether to show CONNECTED",
			section = LAYOUTSECTION
	)
	default boolean showOverlayConnected()
	{
		return true;
	}

}
