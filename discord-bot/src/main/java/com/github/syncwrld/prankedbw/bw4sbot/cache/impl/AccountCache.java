package com.github.syncwrld.prankedbw.bw4sbot.cache.impl;

import com.github.syncwrld.prankedbw.bw4sbot.database.Repositories;
import com.github.syncwrld.prankedbw.bw4sbot.database.impl.RankedRepository;
import com.github.syncwrld.prankedbw.bw4sbot.model.data.PlayerAccount;
import me.syncwrld.booter.minecraft.loader.BukkitPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

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
		return !getMinecraftUsername(discordUsername).equals("bw4sinvalidname");
	}
	
	public void clear() {
		accounts.clear();
	}
	
	public String getMinecraftUsername(String discordUsername) {
		return accounts.entrySet().stream()
			.filter(entry -> entry.getValue().getDiscordUsername() != null && entry.getValue().getDiscordUsername().equals(discordUsername))
			.map(entry -> entry.getKey().getName())
			.findFirst()
			.orElse("bw4sinvalidname");
	}
	
	public String getDiscordUsername(String minecraftUsername) {
		return accounts.entrySet().stream()
			.filter(entry -> entry.getValue().getMinecraftName() != null && entry.getValue().getMinecraftName().equals(minecraftUsername))
			.map(entry -> entry.getKey().getName())
			.findFirst()
			.orElse(null);
	}
	
	public void setup(BukkitPlugin plugin) {
		this.accounts.clear();
		
		AtomicInteger loadedAccounts = new AtomicInteger();
		
		RankedRepository ranked = Repositories.RANKED;
		ranked.getAllAccounts().forEach(account -> {
			try {
				loadedAccounts.getAndIncrement();
				this.accounts.put(Bukkit.getOfflinePlayer(account.getMinecraftName()).getPlayer(), account);
			} catch (Exception ignored) {}
		});
		
		plugin.log("&6CACHE! &aCarregadas " + loadedAccounts.get() + " contas do banco de dados.");
	}
	
	public void save() {
		RankedRepository ranked = Repositories.RANKED;
		this.accounts.forEach((ignored_, account) -> {
			ranked.updateAccount(account);
		});
	}
	
}
