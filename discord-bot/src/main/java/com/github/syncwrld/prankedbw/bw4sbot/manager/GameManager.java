package com.github.syncwrld.prankedbw.bw4sbot.manager;

import com.github.syncwrld.prankedbw.bw4sbot.Configuration;
import com.github.syncwrld.prankedbw.bw4sbot.PRankedSpigotPlugin;
import com.github.syncwrld.prankedbw.bw4sbot.cache.impl.MatchesCache;
import com.github.syncwrld.prankedbw.bw4sbot.event.bukkit.game.GameListener;
import com.github.syncwrld.prankedbw.bw4sbot.hook.BedwarsHook;
import com.github.syncwrld.prankedbw.bw4sbot.model.game.Match;
import com.github.syncwrld.prankedbw.bw4sbot.model.game.Team;
import com.tomkeuper.bedwars.api.BedWars;
import com.tomkeuper.bedwars.api.arena.GameState;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.arena.team.ITeamAssigner;
import com.tomkeuper.bedwars.api.events.gameplay.TeamAssignEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerJoinArenaEvent;
import com.tomkeuper.bedwars.api.events.server.ArenaEnableEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class GameManager implements Listener, ITeamAssigner {
	
	private final PRankedSpigotPlugin plugin;
	
	public GameManager(PRankedSpigotPlugin plugin) {
		this.plugin = plugin;
		this.plugin.registerListener(new GameListener(plugin));
	}
	
	public boolean initializeMatch(Match match) {
		Team team1 = match.getTeam1();
		Team team2 = match.getTeam2();
		
		BedwarsHook bedwars = plugin.getBedwars();
		BedWars.ArenaUtil arenaUtil = bedwars.getApi().getArenaUtil();
		
		String arenaGroup = plugin.getPreferenceConfiguration().getArenaGroup();
		
		List<Player> players = new ArrayList<>();
		players.addAll(team1.getPlayers());
		players.addAll(team2.getPlayers());
		
		for (Player player : players) {
			if (!arenaUtil.joinRandomFromGroup(player, arenaGroup)) {
				plugin.log("&cUm erro ocorreu ao iniciar a partida (provavelmente o id do grupo das arenas está errado). Por favor, verifique suas configurações.");
				return false;
			}
		}
		
		return true;
	}
	
	@EventHandler
	public void onArenaLoad(ArenaEnableEvent event) {
		if (event.getArena().getGroup().equals(this.plugin.getPreferenceConfiguration().getArenaGroup())) {
			event.getArena().setTeamAssigner(this);
		}
	}
	
	@EventHandler
	public void onArenaJoin(PlayerJoinArenaEvent event) {
		MatchesCache matchesCache = plugin.getCaches().getMatchesCache();
		Player player = event.getPlayer();
		
		Match match = matchesCache.findMatch(player);
		if (match != null) {
			IArena arena = event.getArena();
			arena.setTeamAssigner(this);
			
			arena.getTeamAssigner().assignTeams(arena);
			
			List<Player> players = arena.getPlayers();
			
			int size = players.size();
			if ((size + 1) != (Configuration.PLAYERS_PER_TEAM * 2)) {
				arena.changeStatus(GameState.waiting);
			} else {
				arena.changeStatus(GameState.starting);
				arena.getStartingTask().setCountdown(10);
				
				com.tomkeuper.bedwars.BedWars.debug = true;
			}
		}
	}
	
	private void removeOtherTeams(IArena arena) {
		ITeam greenTeam = arena.getTeam("Verde");
		if (!greenTeam.getMembers().isEmpty()) {
			for (Player teamPlayer : greenTeam.getMembers()) {
				arena.removePlayer(teamPlayer, false);
			}
		}
		
		ITeam yellowTeam = arena.getTeam("Amarelo");
		if (!yellowTeam.getMembers().isEmpty()) {
			for (Player teamPlayer : yellowTeam.getMembers()) {
				arena.removePlayer(teamPlayer, false);
			}
		}
	}
	
	@EventHandler
	public void onTeamAssignment(TeamAssignEvent event) {
		MatchesCache matchesCache = plugin.getCaches().getMatchesCache();
		Player player = event.getPlayer();
		
		Match match = matchesCache.findMatch(player);
		
		if (match != null) {
			IArena arena = event.getArena();
			
			ITeam teamOne = arena.getTeam("Vermelho");
			ITeam teamTwo = arena.getTeam("Azul");
			
			match.getTeam1().getPlayers().forEach(teamOne::addPlayers);
			match.getTeam2().getPlayers().forEach(teamTwo::addPlayers);
			
			removeOtherTeams(arena);
		}
	}
	
	public boolean anyIsPlaying(List<Player> players) {
		return players.stream()
			.anyMatch(player -> plugin.getCaches().getMatchesCache().isPlaying(plugin, player));
	}
	
	@Override
	public void assignTeams(IArena arena) {
		List<Player> players = arena.getPlayers();
		
		if (players.isEmpty()) {
			return;
		}
		
		Match match = null;
		
		int index = 0;
		
		while (match == null && index < players.size()) {
			match = plugin.getCaches().getMatchesCache().findMatch(players.get(index));
			index++;
		}
		
		if (match == null) {
			return;
		}
		
		Team team1 = match.getTeam1();
		Team team2 = match.getTeam2();
		
		ITeam teamOne = arena.getTeam("Vermelho");
		ITeam teamTwo = arena.getTeam("Azul");
		
		if (teamOne != null) {
			for (Player teamPlayer : team1.getPlayers()) {
				teamOne.addPlayers(teamPlayer);
			}
		}
		
		if (teamTwo != null) {
			team2.getPlayers().forEach(teamTwo::addPlayers);
		}
		
		removeOtherTeams(arena);
	}
	
	public void finishMatch(Match match) {
		Team team1 = match.getTeam1();
		Team team2 = match.getTeam2();
		
		MatchesCache matchesCache = plugin.getCaches().getMatchesCache();
		matchesCache.remove(match);
		
		List<Player> players = team1.getPlayers();
		players.addAll(team2.getPlayers());
		
		for (Player player : players) {
			matchesCache.remove(player);
		}
		
		match.getMatchChannel().createUpdater().setTopic("Este canal será deletado em 25 segundos").update().join();
		match.getMatchChannel().deleteAfter(Duration.ofSeconds(25)).join();
		
		team1.getVoiceChannel().deleteAfter(Duration.ofSeconds(2)).join();
		team2.getVoiceChannel().deleteAfter(Duration.ofSeconds(2)).join();
	}
	
	public IArena getArena(Match match) {
		BedwarsHook bedwars = plugin.getBedwars();
		
		Player randomPlayer = null;
		int index = 0;
		
		while (randomPlayer == null && index < match.getTeam1().getPlayers().size()) {
			randomPlayer = match.getTeam1().getPlayers().get(index);
			index++;
		}
		
		if (randomPlayer == null) {
			index = 0;
			
			while (randomPlayer == null && index < match.getTeam2().getPlayers().size()) {
				randomPlayer = match.getTeam2().getPlayers().get(index);
				index++;
			}
		}
		
		
		return bedwars.getApi().getArenaUtil().getArenaByPlayer(randomPlayer);
	}
}
