package com.github.syncwrld.prankedbw.bw4sbot.api.event;

import com.github.syncwrld.prankedbw.bw4sbot.model.game.Match;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MatchAvailableEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	
	private final Match match;
	
	public MatchAvailableEvent(Match match) {
		this.match = match;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public Match getMatch() {
		return match;
	}
}
