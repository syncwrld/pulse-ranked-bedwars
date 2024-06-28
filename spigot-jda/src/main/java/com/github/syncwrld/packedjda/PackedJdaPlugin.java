package com.github.syncwrld.packedjda;

import net.dv8tion.jda.internal.utils.JDALogger;
import org.bukkit.plugin.java.JavaPlugin;

public class PackedJdaPlugin extends JavaPlugin {
	
	@Override
	public void onEnable() {
		JDALogger.setFallbackLoggerEnabled(false);
		this.getLogger().info("PackedJDA is enabled!");
	}
	
}