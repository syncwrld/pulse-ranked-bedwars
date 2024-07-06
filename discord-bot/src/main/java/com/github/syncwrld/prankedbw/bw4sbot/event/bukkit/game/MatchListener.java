package com.github.syncwrld.prankedbw.bw4sbot.event.bukkit.game;

import com.github.syncwrld.prankedbw.bw4sbot.Configuration;
import com.github.syncwrld.prankedbw.bw4sbot.PRankedSpigotPlugin;
import com.github.syncwrld.prankedbw.bw4sbot.api.event.MatchAvailableEvent;
import com.github.syncwrld.prankedbw.bw4sbot.cache.impl.MatchesCache;
import com.github.syncwrld.prankedbw.bw4sbot.manager.GameManager;
import com.github.syncwrld.prankedbw.bw4sbot.model.game.Match;
import com.github.syncwrld.prankedbw.bw4sbot.model.game.Team;
import com.tomkeuper.bedwars.api.arena.IArena;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
		
		GameManager gameManager = plugin.getGameManager();
		
		if (gameManager.anyIsPlaying(team1.getPlayers()) || gameManager.anyIsPlaying(team2.getPlayers())) {
			return;
		}
		
		plugin.log("&bGAME! &aCriando nova partida... (" + match.getId() + ")");
		
		MatchesCache matchesCache = this.plugin.getCaches().getMatchesCache();
		matchesCache.insert(match);
		
		List<Player> allPlayers = new ArrayList<>();
		allPlayers.addAll(team1.getPlayers());
		allPlayers.addAll(team2.getPlayers());
		
		allPlayers.forEach(player -> matchesCache.insert(player, match));
		
		if (!gameManager.initializeMatch(match)) {
			matchesCache.remove(match);
			
			for (Player player : allPlayers) {
				matchesCache.remove(player);
			}
			
			return;
		}
		
		plugin.log("&bGAME! &aPartida criada com sucesso." + " (" + match.getId() + ")");
		
		IArena arena = gameManager.getArena(match);
		String name = arena.getDisplayName();
		
		match.getTeam1().getVoiceChannel().createUpdater().setName(match.getId() + " [TIME 1]").update().join();
		match.getTeam2().getVoiceChannel().createUpdater().setName(match.getId() + " [TIME 2]").update().join();
		match.getMatchChannel().createUpdater().setName(match.getId()).update().join();
		
		EmbedBuilder embedBuilder = new EmbedBuilder()
			.setTitle("NOVA PARTIDA!")
			.setDescription("Uma nova partida foi iniciada! | ID: " + match.getId())
			.addField("Mapa", name, false)
			.addField("Modo", "Ranked 4S", false)
			.addField("Time 1", transform(team1.getPlayers()), false)
			.addField("Time 2", transform(team2.getPlayers()), false)
			.setColor(Color.CYAN);
		match.getMatchChannel().sendMessage(embedBuilder).join();
		
		team1.moveAllToChannel(plugin);
		team2.moveAllToChannel(plugin);
		
		plugin.log("&bGAME! &aOs jogadores já foram teleportados para a arena e já estão na sala de voz dos seus respectivos times.");
	}
	
	public String transform(List<Player> players) {
		return players.stream()
			.map(HumanEntity::getName)
			.collect(Collectors.joining(", "));
	}
	
}
