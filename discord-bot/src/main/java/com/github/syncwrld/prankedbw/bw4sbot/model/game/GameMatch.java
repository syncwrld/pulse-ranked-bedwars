package com.github.syncwrld.prankedbw.bw4sbot.model.game;

import com.tomkeuper.bedwars.arena.Arena;

public class GameMatch {
	private final Match match;
	private final Arena arena;
	
	public GameMatch(Match match, Arena arena) {
		this.match = match;
		this.arena = arena;
	}
	
	public Arena getArena() {
		return arena;
	}
	
	public Match getMatch() {
		return match;
	}
}
