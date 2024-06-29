package com.github.syncwrld.prankedbw.discordbot.shared.cache;

import com.github.syncwrld.prankedbw.discordbot.shared.cache.impl.PreferenceCache;
import com.github.syncwrld.prankedbw.discordbot.shared.cache.impl.UserCache;
import com.github.syncwrld.prankedbw.discordbot.shared.cache.loader.GeneralCacheLoader;
import com.github.syncwrld.prankedbw.discordbot.shared.database.Repositories;
import com.github.syncwrld.prankedbw.discordbot.shared.database.impl.RankedRepository;
import com.github.syncwrld.prankedbw.discordbot.shared.model.PlayerAccount;
import com.github.syncwrld.prankedbw.discordbot.spigot.PRankedSpigotPlugin;

import java.util.HashSet;

public class Caches {
	public static final PreferenceCache PREFERENCE_CACHE = new PreferenceCache();
	public static final UserCache USER_CACHE = new UserCache();
	
	public static void setup(PRankedSpigotPlugin plugin) {
		GeneralCacheLoader generalCacheLoader = new GeneralCacheLoader(plugin);
		generalCacheLoader.loadPreferences();
		
		loadAccountsInCache(plugin);
	}
	
	private static void loadAccountsInCache(PRankedSpigotPlugin plugin) {
		RankedRepository rankedRepository = Repositories.RANKED_REPOSITORY;
		HashSet<PlayerAccount> playerAccounts = rankedRepository.fetchAll();
		playerAccounts.forEach(USER_CACHE::add);
			
		plugin.log("Carregado " + playerAccounts.size() + " usuários no banco de dados.");
	}
	
	public static void save() {
		RankedRepository rankedRepository = Repositories.RANKED_REPOSITORY;
		
		USER_CACHE.getAccounts().forEach(rankedRepository::updateAccount);
	}
}
