package com.github.syncwrld.prankedbw.bw4sbot.cache;

import com.github.syncwrld.prankedbw.bw4sbot.cache.impl.AccountCache;
import com.github.syncwrld.prankedbw.bw4sbot.cache.impl.MatchesCache;
import com.github.syncwrld.prankedbw.bw4sbot.cache.impl.TokenCache;
import me.syncwrld.booter.minecraft.loader.BukkitPlugin;

public class Caches {
	private final AccountCache accountCache = new AccountCache();
	private final TokenCache tokenCache = new TokenCache();
	private final MatchesCache matchesCache = new MatchesCache();
	
	public AccountCache getAccountCache() {
		return accountCache;
	}
	
	public TokenCache getTokenCache() {
		return tokenCache;
	}
	
	public MatchesCache getMatchesCache() {
		return matchesCache;
	}
	
	public void setup(BukkitPlugin plugin) {
		accountCache.clear();
		tokenCache.clear();
		matchesCache.getMatches().clear();
		
		accountCache.setup(plugin);
		
		plugin.startRepeatingRunnable(tokenCache, 100);
	}
	
	public void save() {
		accountCache.save();
		matchesCache.save();
	}
}
