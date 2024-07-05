package com.github.syncwrld.prankedbw.bw4sbot.cache.impl;

import com.github.syncwrld.prankedbw.bw4sbot.model.game.Match;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MatchesCache {
	
	private final List<Match> matches = new ArrayList<>();
	
	public void insert(Match match) {
		matches.add(match);
	}
	
	public List<Match> getMatches() {
		return matches;
	}
	
	public Match findMatch(Player player) {
		Optional<Match> matchOptional = matches.stream()
			.filter(match -> match.getTeam1().getPlayers().contains(player) ||
				match.getTeam2().getPlayers().contains(player))
			.findFirst();
		return matchOptional.orElse(null);
	}
	
	public Match findMatch(String id) {
		Optional<Match> matchOptional = matches.stream()
			.filter(match -> match.getId().equals(id))
			.findFirst();
		return matchOptional.orElse(null);
	}
	
}
