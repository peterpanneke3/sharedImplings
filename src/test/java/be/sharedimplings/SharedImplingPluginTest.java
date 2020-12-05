package be.sharedimplings;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class SharedImplingPluginTest
{
	public static void main(String[] args) throws Exception
	{
		SharedImplingsPlugin.devMode = true;
		ExternalPluginManager.loadBuiltin(SharedImplingsPlugin.class);
		RuneLite.main(args);
	}
}