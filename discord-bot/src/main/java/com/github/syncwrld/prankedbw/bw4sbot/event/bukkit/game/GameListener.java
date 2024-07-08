package com.github.syncwrld.prankedbw.bw4sbot.event.bukkit.game;

import com.github.syncwrld.prankedbw.bw4sbot.PRankedSpigotPlugin;
import com.github.syncwrld.prankedbw.bw4sbot.cache.Caches;
import com.github.syncwrld.prankedbw.bw4sbot.model.data.PlayerAccount;
import com.github.syncwrld.prankedbw.bw4sbot.model.game.Match;
import com.tomkeuper.bedwars.api.arena.GameState;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.events.gameplay.GameEndEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerBedBreakEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerKillEvent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class GameListener implements Listener {
	
	private final PRankedSpigotPlugin plugin;
	private final Caches caches;
	
	private final HashSet<Match> strikedMatches = new HashSet<>();
	private final HashMap<String, Instant> firstBedBreakMap = new HashMap<>();
	private final HashMap<String, Instant> firstKillMap = new HashMap<>();
	
	public GameListener(PRankedSpigotPlugin plugin) {
		this.plugin = plugin;
		this.caches = plugin.getCaches();
	}
	
	@EventHandler
	public void onBedDestroy(PlayerBedBreakEvent event) {
		Player player = event.getPlayer();
		Match match = caches.getMatchesCache().findMatch(player);
		
		if (match != null) {
			int points = randomPoints(4, 16);
			
			match.addEloPoints(player, points);
			
			if (!firstBedBreakMap.containsKey(match.getId())) {
				firstBedBreakMap.put(match.getId(), Instant.now());
			}
			
			if (!firstKillMap.containsKey(match.getId())) {
				firstKillMap.put(match.getId(), Instant.now());
			}
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
		
		if (match != null && !strikedMatches.contains(match)) {
			for (Player player : winners) {
				int points = randomPoints(10, 20);
				match.addEloPoints(player, points);
			}
			
			HashMap<Player, Integer> eloChanges = match.getEloChanges();
			eloChanges.forEach((player, points) -> {
				if (points > 0) {
					player.sendMessage("§a+ §e" + points + " ELO!");
					addEloInCache(player, points);
					return;
				}
				
				player.sendMessage("§c- §e" + Math.abs(points) + " ELO!");
				removeEloInCache(player, Math.abs(points));
			});
			
			List<UUID> losers = event.getLosers();
			List<Player> loserPlayers = losers.stream()
				.map(Bukkit::getPlayer)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
			
			for (Player loserPlayer : loserPlayers) {
				int points = randomPoints(20, 30);
				loserPlayer.sendMessage("§c- §e" + points + " ELO!");
				
				match.removeEloPoints(loserPlayer, points);
			}
			
			Instant startTime = match.getStartTime();
			Instant firstKill = firstKillMap.getOrDefault(match.getId(), Instant.now());
			Instant firstBedBreak = firstBedBreakMap.getOrDefault(match.getId(), Instant.now());
			
			Duration matchDuration = Duration.between(startTime, Instant.now());
			Duration firstKillDuration = Duration.between(startTime, firstKill);
			Duration bedBreakDuration = Duration.between(startTime, firstBedBreak);
			
			EmbedBuilder embedBuilder = new EmbedBuilder()
				.setTitle("PARTIDA FINALIZADA!")
				.setDescription("Mais uma partida finalizada com sucesso!")
				.setFooter("© PulseMC, 2024. Todos os direitos reservados.", "https://i.imgur.com/3SWa1TY.png")
				.addField("Ganhadores", transform(winners), true)
				.addField("Perdedores", transform(loserPlayers), true)
				.addField("Tempo de partida", formatDuration(matchDuration), true)
				.addField("Primeiro Abate/Morte", formatDuration(firstKillDuration), true)
				.addField("Primeira Quebra de Cama", formatDuration(bedBreakDuration), true)
				.setColor(Color.YELLOW);
			
			match.getMatchChannel().sendMessage(embedBuilder).join();
			plugin.getGameManager().finishMatch(match, false);
			
			firstBedBreakMap.remove(match.getId());
			firstKillMap.remove(match.getId());
			
			return;
		}
		
		if (match != null) {
			strikedMatches.remove(match);
		}
	}
	
	@EventHandler
	public void onPlayerKill(PlayerKillEvent event) {
		Player player = event.getKiller();
		Match match = caches.getMatchesCache().findMatch(player);
		
		if (match != null) {
			int points = randomPoints(1, 8);
			
			match.addEloPoints(player, points);
			
			if (match.isStrikeable()) {
				caches.getMatchesCache().remove(match);
				
				Match newMatch = new Match(plugin, match.getTeam1(), match.getTeam2(), match.getMatchChannel());
				newMatch.setStrikeable(false);
				
				caches.getMatchesCache().insert(newMatch);
			}
			
			if (!(firstKillMap.containsKey(match.getId()))) {
				firstKillMap.put(match.getId(), Instant.now());
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
			
			if (match != null) {
				if (match.isStrikeable()) {
					IArena playerArena = plugin.getGameManager().getArena(match);
					
					if (playerArena != null) {
						List<Player> players = playerArena.getPlayers();
						GameState status = playerArena.getStatus();
						
						if (status != GameState.playing) {
							player.sendMessage("§cSó é possível cancelar uma partida quando ela estiver em andamento.");
							return;
						}
						
						strikedMatches.add(match);
						
						players.forEach(arenaPlayer -> {
							if (arenaPlayer == null)
								return;
							
							arenaPlayer.playSound(player.getLocation(), Sound.AMBIENCE_THUNDER, 1, 1);
							
							arenaPlayer.sendMessage(new String[]{
								"",
								"§c§lSTRIKE!",
								"§c ▸ " + player.getName() + " cancelou a partida!",
								"",
								"§7Esta partida não terá a pontuação contada.",
								""
							});
							
							arenaPlayer.performCommand("l");
						});
						
						this.plugin.getGameManager().finishMatch(match, true);
					} else {
						player.sendMessage("§cVocê não está jogando em nenhuma partida.");
						return;
					}
					return;
				}
				
				player.sendMessage("§cA partida atual não pode mais ser cancelada.");
				return;
			}
			
			player.sendMessage("§cVocê não está em um uma partida de Ranked 4S para executar um strike.");
		}
	}
	
	private int randomPoints(int min, int max) {
		return ThreadLocalRandom.current().nextInt(min, max);
	}
	
	
	private String transform(List<Player> players) {
		return players.stream()
			.map(HumanEntity::getName)
			.collect(Collectors.joining(", "));
	}
	
	public String formatDuration(Duration duration) {
		long minutes = duration.toMinutes();
		long seconds = duration.minusMinutes(minutes).getSeconds();
		
		return String.format("%dm %ds", minutes, seconds);
	}
	
	public void addEloInCache(Player player, int points) {
		PlayerAccount account = caches.getAccountCache().getAccount(player);
		
		if (account == null) {
			return;
		}
		
		account.setEloPoints(account.getEloPoints() + points);
		caches.getAccountCache().setAccount(player, account);
	}
	
	public void removeEloInCache(Player player, int points) {
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
	
}
