package com.github.syncwrld.prankedbw.bw4sbot.api.event.game;

import com.github.syncwrld.prankedbw.bw4sbot.model.game.Match;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MatchEndEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	
	private final Match match;
	private final boolean striked;
	
	public MatchEndEvent(Match match, boolean striked) {
		this.match = match;
		this.striked = striked;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public boolean wasStriked() {
		return striked;
	}
	
	public Match getMatch() {
		return match;
	}
}
