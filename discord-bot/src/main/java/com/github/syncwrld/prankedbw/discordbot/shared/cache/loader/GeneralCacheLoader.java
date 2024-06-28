package com.github.syncwrld.prankedbw.discordbot.shared.cache.loader;

import com.github.syncwrld.prankedbw.discordbot.shared.cache.Caches;
import com.github.syncwrld.prankedbw.discordbot.spigot.PRankedSpigotPlugin;
import org.bukkit.configuration.file.FileConfiguration;

public class GeneralCacheLoader {
	
	private final PRankedSpigotPlugin plugin;
	
	public GeneralCacheLoader(PRankedSpigotPlugin plugin) {
		this.plugin = plugin;
	}
	
	public void loadPreferences() {
		FileConfiguration configuration = this.plugin.getConfigOf("configuration");
		
		Caches.PREFERENCE_CACHE.setDiscordToken(configuration.getString("discord-bot-token"));
		Caches.PREFERENCE_CACHE.setWaitChannelId(configuration.getString("wait-channel-id"));
	}
	
}
