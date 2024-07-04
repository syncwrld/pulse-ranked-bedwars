package com.github.syncwrld.prankedbw.bw4sbot.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.javacord.api.entity.user.User;

public class TeamsAvailableEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	
	private final User[] users;
	
	public TeamsAvailableEvent(User[] users) {
		this.users = users;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public User[] getUsers() {
		return users;
	}
}
