package com.github.syncwrld.prankedbw.bw4sbot.api;

import com.github.syncwrld.prankedbw.bw4sbot.cache.Caches;
import com.github.syncwrld.prankedbw.bw4sbot.manager.PlayerManager;
import com.github.syncwrld.prankedbw.bw4sbot.model.data.PlayerAccount;
import com.github.syncwrld.prankedbw.bw4sbot.model.game.Match;
import org.bukkit.entity.Player;

public interface Ranked4SApi {
	public Match findMatch(Player player);
	
	public boolean isBind(String discordId);
	
	public boolean isBindByMinecraftName(String minecraftName);
	
	public boolean isWaitingToBind(String discordId);
	
	public boolean isWaitingToBindByMinecraftName(String minecraftName);
	
	public PlayerManager getPlayerManager();
	
	public Caches getCaches();
	
	public PlayerAccount getAccount(Player player);
	
	public PlayerAccount getAccountByDiscordId(String discordId);
	
	public int getElo(Player player);
	
	public int getEloByDiscordId(String discordId);
	
	public void setElo(Player player, int elo);
	
	public void setEloByDiscordId(String discordId, int elo);
}
