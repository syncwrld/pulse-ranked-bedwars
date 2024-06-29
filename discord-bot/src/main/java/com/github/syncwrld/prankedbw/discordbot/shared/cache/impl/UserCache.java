package com.github.syncwrld.prankedbw.discordbot.shared.cache.impl;

import com.github.syncwrld.prankedbw.discordbot.shared.model.PlayerAccount;
import lombok.Data;

import java.util.ArrayList;

@Data
public class UserCache {
	private final ArrayList<PlayerAccount> accounts;
	
	public UserCache() {
		this.accounts = new ArrayList<>();
	}
	
	public PlayerAccount findByDiscordId(String discordId) {
		return this.accounts.stream()
			.filter(account -> account.getDiscordId().equals(discordId))
			.findFirst().orElse(null);
	}
	
	public void add(PlayerAccount account) {
		this.accounts.add(account);
	}
	
	public boolean isRegistered(String discordId) {
		return this.findByDiscordId(discordId) != null;
	}
	
}
