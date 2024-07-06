package com.github.syncwrld.prankedbw.bw4sbot.event.bukkit.game;

import com.github.syncwrld.prankedbw.bw4sbot.PRankedSpigotPlugin;
import com.github.syncwrld.prankedbw.bw4sbot.cache.Caches;
import com.github.syncwrld.prankedbw.bw4sbot.manager.GameManager;
import com.github.syncwrld.prankedbw.bw4sbot.model.data.PlayerAccount;
import com.github.syncwrld.prankedbw.bw4sbot.model.game.Match;
import com.tomkeuper.bedwars.api.events.gameplay.GameEndEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerBedBreakEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerKillEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GameListener implements Listener {
	
	private final PRankedSpigotPlugin plugin;
	private final Caches caches;
	private final GameManager gameManager;
	
	public GameListener(PRankedSpigotPlugin plugin) {
		this.plugin = plugin;
		this.caches = plugin.getCaches();
		this.gameManager = plugin.getGameManager();
	}
	
	@EventHandler
	public void onBedDestroy(PlayerBedBreakEvent event) {
		Player player = event.getPlayer();
		int points = randomPoints(4, 16);
		
		player.sendMessage("§a+ §e" + points + " ELO!");
		
		addEloPoints(player, points);
	}
	
	@EventHandler
	public void onGameEnd(GameEndEvent event) {
		List<Player> members = event.getTeamWinner().getMembers();
		for (Player player : members) {
			int points = randomPoints(10, 20);
			player.sendMessage("§a+ §e" + points + " ELO!");
			
			addEloPoints(player, points);
		}
		
		Match match = null;
		int index = 0;
		
		while (match == null && index < members.size()) {
			match = plugin.getCaches().getMatchesCache().findMatch(members.get(index));
			index++;
		}
		
		if (match != null) {
			plugin.getGameManager().finishMatch(match);
		}
	}
	
	@EventHandler
	public void onPlayerKill(PlayerKillEvent event) {
		Player player = event.getKiller();
		int points = randomPoints(1, 8);
		
		player.sendMessage("§a+ §e" + points + " ELO!");
		
		addEloPoints(player, points);
	}
	
	private void addEloPoints(Player player, int points) {
		PlayerAccount account = caches.getAccountCache().getAccount(player);
		
		if (account == null) {
			return;
		}
		
		account.setEloPoints(account.getEloPoints() + points);
		caches.getAccountCache().setAccount(player, account);
	}
	
	private int randomPoints(int min, int max) {
		return ThreadLocalRandom.current().nextInt(min, max);
	}
}
