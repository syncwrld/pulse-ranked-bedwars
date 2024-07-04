package com.github.syncwrld.prankedbw.bw4sbot.cache.impl;

import me.syncwrld.booter.minecraft.tool.Pair;
import me.syncwrld.booter.minecraft.tool.Triple;

import java.time.Instant;
import java.util.HashMap;

public class TokenCache implements Runnable {
	
	/*
	K = nome do jogador que será vinculado (mc)
	V = Pair<Pair<discord_id, discord_name, token>, Tempo de expiração>
	 */
	private HashMap<String, Pair<Triple<String, String, String>, Instant>> tokens = new HashMap<>();
	
	public String getToken(String minecraftName) {
		return tokens.get(minecraftName).getKey().getValue2();
	}
	
	public void setToken(String minecraftName, String discordId, String discordName, String token) {
		tokens.put(minecraftName, new Pair<>(
			new Triple<>(discordId, discordName, token),
			Instant.now().plusSeconds(60)
		));
	}
	
	public void removeToken(String minecraftName) {
		tokens.remove(minecraftName);
	}
	
	public void clear() {
		tokens.clear();
	}
	
	public boolean isAlreadyWaiting(String minecraftName) {
		return tokens.containsKey(minecraftName);
	}
	
	@Override
	public void run() {
		for (String key : tokens.keySet()) {
			if (Instant.now().isAfter(tokens.get(key).getValue().plusSeconds(300))) {
				removeToken(key);
			}
		}
	}
}
