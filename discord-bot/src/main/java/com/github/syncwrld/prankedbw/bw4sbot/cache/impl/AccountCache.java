package com.github.syncwrld.prankedbw.bw4sbot.cache.impl;

import com.github.syncwrld.prankedbw.bw4sbot.model.data.PlayerAccount;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class AccountCache {
	
	private final HashMap<Player, PlayerAccount> accounts = new HashMap<>();
	
	public PlayerAccount getAccount(Player player) {
		return accounts.get(player);
	}
	
	public void setAccount(Player player, PlayerAccount account) {
		accounts.put(player, account);
	}
	
	public void removeAccount(Player player) {
		accounts.remove(player);
	}
	
	public boolean hasAccount(Player player) {
		return accounts.containsKey(player);
	}
	
	public boolean hasAccount(String discordUsername) {
		return getMinecraftUsername(discordUsername) != null;
	}
	
	public void clear() {
		accounts.clear();
	}
	
	public String getMinecraftUsername(String discordUsername) {
		return accounts.entrySet().stream()
			.filter(entry -> entry.getValue().getDiscordUsername().equals(discordUsername))
			.map(entry -> entry.getKey().getName())
			.findFirst()
			.orElse(null);
	}
	
	public String getDiscordUsername(String minecraftUsername) {
		return accounts.entrySet().stream()
			.filter(entry -> entry.getValue().getMinecraftName().equals(minecraftUsername))
			.map(entry -> entry.getKey().getName())
			.findFirst()
			.orElse(null);
	}
	
}
