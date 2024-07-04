package com.github.syncwrld.prankedbw.bw4sbot.cache;

import com.github.syncwrld.prankedbw.bw4sbot.cache.impl.AccountCache;
import com.github.syncwrld.prankedbw.bw4sbot.cache.impl.TokenCache;
import me.syncwrld.booter.minecraft.loader.BukkitPlugin;

public class Caches {
	private final AccountCache accountCache = new AccountCache();
	private final TokenCache tokenCache = new TokenCache();
	
	public AccountCache getAccountCache() {
		return accountCache;
	}
	
	public TokenCache getTokenCache() {
		return tokenCache;
	}
	
	public void setup(BukkitPlugin plugin) {
		accountCache.clear();
		tokenCache.clear();
		
		plugin.startRepeatingRunnable(tokenCache, 100);
	}
}
