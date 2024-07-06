package com.github.syncwrld.prankedbw.bw4sbot.cache.impl;

import com.github.syncwrld.prankedbw.bw4sbot.PRankedSpigotPlugin;
import com.github.syncwrld.prankedbw.bw4sbot.model.game.Match;
import com.github.syncwrld.prankedbw.bw4sbot.model.game.Team;
import com.tomkeuper.bedwars.api.BedWars;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class MatchesCache {
	
	private final List<Match> matches = new ArrayList<>();
	private final HashMap<Player, Match> matchMap = new HashMap<>();
	
	public void insert(Match match) {
		matches.add(match);
	}
	
	public void insert(Player player, Match match) {
		matchMap.put(player, match);
	}
	
	public List<Match> getMatches() {
		return matches;
	}
	
	public void remove(Match match) {
		matches.remove(match);
	}
	
	public void remove(Player player) {
		matchMap.remove(player);
	}
	
	public Match findMatch(Player player) {
		Optional<Match> matchOptionalt1 = matches.stream()
			.filter(match -> match.getTeam1().getPlayers().contains(player))
			.findAny();
		
		Optional<Match> matchOptionalt2 = matches.stream()
			.filter(match -> match.getTeam2().getPlayers().contains(player))
			.findAny();
		
		return matchOptionalt1.orElse(matchOptionalt2.orElse(matchMap.get(player)));
	}
	
	public Match findMatch(String id) {
		Optional<Match> matchOptional = matches.stream()
			.filter(match -> match.getId().equals(id))
			.findFirst();
		return matchOptional.orElse(null);
	}
	
	public boolean isPlayingBedwars(PRankedSpigotPlugin plugin, Player player) {
		BedWars api = plugin.getBedwars().getApi();
		return api.getArenaUtil().isPlaying(player);
	}
	
	public boolean isPlaying(PRankedSpigotPlugin plugin, Player player) {
		return findMatch(player) != null ||
			isPlayingBedwars(plugin, player) ||
			matches.stream()
				.anyMatch(
						match -> match.getTeam1().getPlayers().contains(player) ||
						match.getTeam2().getPlayers().contains(player)
				);
	}
	
	public void save() {
		this.matches.forEach(match -> {
			Team team1 = match.getTeam1();
			Team team2 = match.getTeam2();
			
			match.getMatchChannel().delete().join();
			team1.getVoiceChannel().delete().join();
			team2.getVoiceChannel().delete().join();
		});
	}
	
	public HashMap<Player, Match> getMatchMap() {
		return matchMap;
	}
}
