package com.github.syncwrld.prankedbw.discordbot.discord;

public interface Listener<T> {
	public void handle(T event);
}
