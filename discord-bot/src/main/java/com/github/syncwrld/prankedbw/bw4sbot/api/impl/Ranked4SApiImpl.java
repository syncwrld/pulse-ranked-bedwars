package com.github.syncwrld.prankedbw.bw4sbot.api.impl;

import com.github.syncwrld.prankedbw.bw4sbot.PRankedSpigotPlugin;
import com.github.syncwrld.prankedbw.bw4sbot.api.Ranked4SApi;
import com.github.syncwrld.prankedbw.bw4sbot.cache.Caches;
import com.github.syncwrld.prankedbw.bw4sbot.manager.PlayerManager;
import com.github.syncwrld.prankedbw.bw4sbot.model.data.PlayerAccount;
import com.github.syncwrld.prankedbw.bw4sbot.model.game.Match;
import org.bukkit.entity.Player;

public class Ranked4SApiImpl implements Ranked4SApi {
	private final PRankedSpigotPlugin plugin;
	private final PlayerManager playerManager;
	
	public Ranked4SApiImpl(PRankedSpigotPlugin plugin) {
		this.plugin = plugin;
		this.playerManager = new PlayerManager(plugin);
	}
	
	@Override
	public Match findMatch(Player player) {
		return getCaches().getMatchesCache().findMatch(player);
	}
	
	@Override
	public boolean isBind(String discordId) {
		String minecraftUsernameById = getCaches().getAccountCache().getMinecraftUsernameById(discordId);
		return minecraftUsernameById != null && !minecraftUsernameById.equals("bw4sinvalidname");
	}
	
	@Override @Deprecated
	public boolean isBindByMinecraftName(String minecraftName) {
		return getCaches().getAccountCache().hasAccountByName(minecraftName);
	}
	
	@Override
	public boolean isWaitingToBind(String discordId) {
		return getCaches().getTokenCache().isAlreadyWaitingByDiscordId(discordId);
	}
	
	@Override
	public boolean isWaitingToBindByMinecraftName(String minecraftName) {
		return getCaches().getTokenCache().isWaiting(minecraftName);
	}
	
	@Override
	public PlayerManager getPlayerManager() {
		return this.playerManager;
	}
	
	@Override
	public Caches getCaches() {
		return this.plugin.getCaches();
	}
	
	@Override
	public PlayerAccount getAccount(Player player) {
		return getCaches().getAccountCache().getAccount(player);
	}
	
	@Override
	public PlayerAccount getAccountByDiscordId(String discordId) {
		return getCaches().getAccountCache().getAccountByDiscordId(discordId);
	}
	
	@Override
	public int getElo(Player player) {
		return getAccount(player).getEloPoints();
	}
	
	@Override
	public int getEloByDiscordId(String discordId) {
		return getAccountByDiscordId(discordId).getEloPoints();
	}
	
	@Override
	public void setElo(Player player, int elo) {
		getCaches().getAccountCache().getAccount(player).setEloPoints(elo);
	}
	
	@Override
	public void setEloByDiscordId(String discordId, int elo) {
		getCaches().getAccountCache().getAccountByDiscordId(discordId).setEloPoints(elo);
	}
}
