package com.github.syncwrld.prankedbw.bw4sbot.event.bukkit.game;

import com.github.syncwrld.prankedbw.bw4sbot.PRankedSpigotPlugin;
import com.github.syncwrld.prankedbw.bw4sbot.cache.Caches;
import com.github.syncwrld.prankedbw.bw4sbot.model.data.PlayerAccount;
import com.github.syncwrld.prankedbw.bw4sbot.model.game.Match;
import com.tomkeuper.bedwars.api.BedWars;
import com.tomkeuper.bedwars.api.events.gameplay.GameEndEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerBedBreakEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerKillEvent;
import me.syncwrld.booter.minecraft.tool.Pair;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class GameListener implements Listener {
	
	private final PRankedSpigotPlugin plugin;
	private final Caches caches;
	
	private final HashMap<Player, HashMap<String, Integer>> statsMap = new HashMap<>();
	
	public GameListener(PRankedSpigotPlugin plugin) {
		this.plugin = plugin;
		this.caches = plugin.getCaches();
	}
	
	@EventHandler
	public void onBedDestroy(PlayerBedBreakEvent event) {
		Player player = event.getPlayer();
		Match match = caches.getMatchesCache().findMatch(player);
		
		if (match != null) {
			HashMap<String, Integer> stats = this.statsMap.getOrDefault(player, new HashMap<>());
			stats.put("beds", stats.getOrDefault("beds", 0) + 1);
			this.statsMap.put(player, stats);
			
			int points = randomPoints(4, 16);
			
			player.sendMessage("§a+ §e" + points + " ELO!");
			addEloPoints(player, points);
		}
	}
	
	@EventHandler
	public void onGameEnd(GameEndEvent event) {
		List<Player> winners = event.getTeamWinner().getMembers();
		Match match = null;
		int index = 0;
		
		while (match == null && index < winners.size()) {
			match = plugin.getCaches().getMatchesCache().findMatch(winners.get(index));
			index++;
		}
		
		if (match != null) {
			for (Player player : winners) {
				this.statsMap.remove(player);
				
				int points = randomPoints(10, 20);
				player.sendMessage("§a+ §e" + points + " ELO!");
				
				addEloPoints(player, points);
			}
			
			List<UUID> losers = event.getLosers();
			List<Player> loserPlayers = losers.stream()
				.map(Bukkit::getPlayer)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
			
			for (Player loserPlayer : loserPlayers) {
				this.statsMap.remove(loserPlayer);
				
				int points = randomPoints(20, 30);
				loserPlayer.sendMessage("§c- §e" + points + " ELO!");
				
				removeEloPoints(loserPlayer, points);
			}
			
			EmbedBuilder embedBuilder = new EmbedBuilder()
				.setTitle("PARTIDA FINALIZADA!")
				.setDescription("Mais uma partida finalizada com sucesso!")
				.setFooter("© PulseMC, 2024. Todos os direitos reservados.", "https://i.imgur.com/3SWa1TY.png")
				.addField("Ganhadores", transform(winners), true)
				.addField("Perdedores", transform(loserPlayers), true)
				.setColor(Color.YELLOW);
			
			StringBuilder topKillsString = new StringBuilder();
			HashMap<Integer, Pair<Player, Integer>> topKillers = this.getTopKillers(match);
			
			for (int i = 0; i < 3; i++) {
				Pair<Player, Integer> topKiller = topKillers.get(i);
				if (topKiller != null) {
					topKillsString.append(topKiller.getKey().getName()).append(" - ").append(topKiller.getValue()).append(" kills\n");
				}
			}
			
			embedBuilder.addField("TOP KILLS", topKillsString.toString(), false);
			
			HashMap<Integer, Pair<Player, Integer>> topBedBreakers = this.getTopBedBreakers(match);
			StringBuilder topBedBreakersString = new StringBuilder();
			
			for (int i = 0; i < 3; i++) {
				Pair<Player, Integer> topBedBreaker = topBedBreakers.get(i);
				if (topBedBreaker != null) {
					topBedBreakersString.append(topBedBreaker.getKey().getName()).append(" - ").append(topBedBreaker.getValue()).append(" beds\n");
				}
			}
			
			embedBuilder.addField("TOP CAMAS QUEBRADAS", topBedBreakersString.toString(), false);
			
			match.getMatchChannel().sendMessage(embedBuilder).join();
			
			plugin.getGameManager().finishMatch(match);
		}
	}
	
	
	@EventHandler
	public void onPlayerKill(PlayerKillEvent event) {
		Player player = event.getKiller();
		Match match = caches.getMatchesCache().findMatch(player);
		
		if (match != null) {
			HashMap<String, Integer> stats = this.statsMap.getOrDefault(player, new HashMap<>());
			stats.put("kills", stats.getOrDefault("kills", 0) + 1);
			this.statsMap.put(player, stats);
			
			int points = randomPoints(1, 8);
			
			player.sendMessage("§a+ §e" + points + " ELO!");
			addEloPoints(player, points);
			
			if (match.isStrikeable()) {
				caches.getMatchesCache().remove(match);
				
				Match newMatch = new Match(match.getTeam1(), match.getTeam2(), match.getMatchChannel());
				newMatch.setStrikeable(false);
				
				caches.getMatchesCache().insert(newMatch);
			}
		}
	}
	
	@EventHandler
	public void onStrikeCommandExecuted(PlayerCommandPreprocessEvent event) {
		String command = event.getMessage();
		command = command.startsWith("/") ? command.substring(1) : command;
		
		if (command.equalsIgnoreCase("strike")) {
			event.setCancelled(true);
			
			Player player = event.getPlayer();
			Match match = caches.getMatchesCache().findMatch(player);
			BedWars api = plugin.getBedwars().getApi();
			
			if (match != null) {
				if (match.isStrikeable()) {
				
				}
				
				player.sendMessage("§cA partida atual não pode mais ser cancelada.");
				return;
			}
			
			player.sendMessage("§cVocê não está em um uma partida de Ranked 4S para executar um strike.");
		}
	}
	
	private void addEloPoints(Player player, int points) {
		PlayerAccount account = caches.getAccountCache().getAccount(player);
		
		if (account == null) {
			return;
		}
		
		account.setEloPoints(account.getEloPoints() + points);
		caches.getAccountCache().setAccount(player, account);
	}
	
	private void removeEloPoints(Player player, int points) {
		PlayerAccount account = caches.getAccountCache().getAccount(player);
		
		if (account == null) {
			return;
		}
		
		int newEloPoints = account.getEloPoints() - points;
		
		if (newEloPoints < 0) {
			newEloPoints = 0;
		}
		
		account.setEloPoints(newEloPoints);
		caches.getAccountCache().setAccount(player, account);
	}
	
	private int randomPoints(int min, int max) {
		return ThreadLocalRandom.current().nextInt(min, max);
	}
	
	private HashMap<Integer, Pair<Player, Integer>> getTopThreePlayers(HashMap<Player, Integer> playerCounts) {
		return playerCounts.entrySet()
			.stream()
			.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
			.limit(3)
			.collect(Collectors.toMap(
				entry -> playerCounts.size() - playerCounts.entrySet().stream()
					.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
					.collect(Collectors.toList()).indexOf(entry),
				entry -> new Pair<>(entry.getKey(), entry.getValue()),
				(e1, e2) -> e1,
				HashMap::new
			));
	}
	
	public HashMap<Integer, Pair<Player, Integer>> getTopKillers(Match match) {
		List<Player> allPlayers = new ArrayList<>(match.getTeam1().getPlayers());
		allPlayers.addAll(new ArrayList<>(match.getTeam2().getPlayers()));
		
		HashMap<Player, Integer> bedBreakCounts = new HashMap<>();
		
		for (Player player : allPlayers) {
			HashMap<String, Integer> stats = statsMap.getOrDefault(player, new HashMap<>());
			bedBreakCounts.put(player, stats.getOrDefault("kills", 0));
		}
		
		return getTopThreePlayers(bedBreakCounts);
	}
	
	public HashMap<Integer, Pair<Player, Integer>> getTopBedBreakers(Match match) {
		List<Player> allPlayers = new ArrayList<>(match.getTeam1().getPlayers());
		allPlayers.addAll(new ArrayList<>(match.getTeam2().getPlayers()));
		
		HashMap<Player, Integer> bedBreakCounts = new HashMap<>();
		
		for (Player player : allPlayers) {
			HashMap<String, Integer> stats = statsMap.getOrDefault(player, new HashMap<>());
			bedBreakCounts.put(player, stats.getOrDefault("beds", 0));
		}
		
		return getTopThreePlayers(bedBreakCounts);
	}
	
	
	private String transform(List<Player> players) {
		return players.stream()
			.map(HumanEntity::getName)
			.collect(Collectors.joining(", "));
	}
	
}
