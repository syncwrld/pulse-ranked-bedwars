package com.github.syncwrld.prankedbw.bw4sbot.api;

import com.github.syncwrld.prankedbw.bw4sbot.cache.Caches;
import com.github.syncwrld.prankedbw.bw4sbot.manager.PlayerManager;
import com.github.syncwrld.prankedbw.bw4sbot.model.game.Match;
import com.github.syncwrld.prankedbw.bw4sbot.model.game.Team;
import org.bukkit.entity.Player;

public interface Ranked4SApi {
	public Match createMatch(Team team1, Team team2);
	
	public Match findMatch(Player player);
	
	public boolean isBind(String discordId);
	
	public boolean isBindByMinecraftName(String minecraftName);
	
	public boolean isWaitingToBind(String discordId);
	
	public boolean isWaitingToBindByMinecraftName(String minecraftName);
	
	public PlayerManager getPlayerManager();
	
	public Caches getCaches();
}
