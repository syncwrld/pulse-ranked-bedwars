package com.github.syncwrld.prankedbw.bw4sbot.event.bukkit.game;

import com.github.syncwrld.prankedbw.bw4sbot.Configuration;
import com.github.syncwrld.prankedbw.bw4sbot.PRankedSpigotPlugin;
import com.github.syncwrld.prankedbw.bw4sbot.api.event.MatchAvailableEvent;
import com.github.syncwrld.prankedbw.bw4sbot.cache.impl.MatchesCache;
import com.github.syncwrld.prankedbw.bw4sbot.model.game.Match;
import com.github.syncwrld.prankedbw.bw4sbot.model.game.Team;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MatchListener implements Listener {
	
	private final PRankedSpigotPlugin plugin;
	
	public MatchListener(PRankedSpigotPlugin plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onMatchAvailable(MatchAvailableEvent event) {
		Match match = event.getMatch();
		
		Team team1 = event.getMatch().getTeam1();
		Team team2 = event.getMatch().getTeam2();
		
		if (team1.getSize() < Configuration.PLAYERS_PER_TEAM) {
			return;
		}
		
		if (team2.getSize() < Configuration.PLAYERS_PER_TEAM) {
			return;
		}
		
		MatchesCache matchesCache = this.plugin.getCaches().getMatchesCache();
		matchesCache.insert(match);
	}
	
}
