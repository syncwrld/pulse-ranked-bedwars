package com.github.syncwrld.prankedbw.discordbot.shared.cache;

import com.github.syncwrld.prankedbw.discordbot.shared.cache.impl.PreferenceCache;
import com.github.syncwrld.prankedbw.discordbot.shared.cache.loader.GeneralCacheLoader;
import com.github.syncwrld.prankedbw.discordbot.spigot.PRankedSpigotPlugin;

public class Caches {
	public static final PreferenceCache PREFERENCE_CACHE = new PreferenceCache();
	
	public static void setup(PRankedSpigotPlugin plugin) {
		GeneralCacheLoader generalCacheLoader = new GeneralCacheLoader(plugin);
		generalCacheLoader.loadPreferences();
	}
}
