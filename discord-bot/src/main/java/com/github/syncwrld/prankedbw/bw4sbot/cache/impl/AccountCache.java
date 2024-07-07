package com.github.syncwrld.prankedbw.bw4sbot.cache.impl;

import com.github.syncwrld.prankedbw.bw4sbot.database.Repositories;
import com.github.syncwrld.prankedbw.bw4sbot.database.impl.RankedRepository;
import com.github.syncwrld.prankedbw.bw4sbot.model.data.PlayerAccount;
import me.syncwrld.booter.minecraft.loader.BukkitPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

public class AccountCache {
	
	private final HashSet<PlayerAccount> accounts = new HashSet<>();
	private final HashMap<Player, PlayerAccount> accountMap = new HashMap<>();
	
	public PlayerAccount getAccount(Player player) {
		return accountMap.get(player);
	}
	
	public PlayerAccount getAccountByDiscordId(String discordId) {
		return accounts.stream()
			.filter(account -> account.getDiscordId() != null && account.getDiscordId().equals(discordId))
			.findFirst()
			.orElse(null);
	}
	
	public void setAccount(Player player, PlayerAccount account) {
		accountMap.put(player, account);
		accounts.add(account);
	}
	
	public void removeAccount(Player player) {
		accountMap.remove(player);
		accounts.remove(accountMap.get(player));
	}
	
	public boolean hasAccount(Player player) {
		return accountMap.containsKey(player);
	}
	
	public boolean hasAccountById(String discordId) {
		return accounts.stream()
			.anyMatch(account -> account.getDiscordId() != null && account.getDiscordId().equals(discordId));
	}
	
	public boolean hasAccountByName(String discordUsername) {
		String minecraftUsernameById = getMinecraftUsername(discordUsername);
		System.out.println(minecraftUsernameById);
		
		return minecraftUsernameById != null && !minecraftUsernameById.equals("bw4sinvalidname");
	}
	
	public String getMinecraftUsernameById(String discordId) {
		return this.accountMap.entrySet().stream()
			.filter(entry -> entry.getValue().getDiscordId() != null && entry.getValue().getDiscordId().equals(discordId))
			.map(entry -> entry.getKey() == null ? "bw4sinvalidname" : entry.getValue().getMinecraftName())
			.findFirst()
			.orElse("bw4sinvalidname");
	}
	
	public void clear() {
		accounts.clear();
		accountMap.clear();
	}
	
	public String getMinecraftUsername(String discordUsername) {
		return accounts.stream()
			.filter(account -> discordUsername.equals(account.getDiscordUsername()))
			.map(PlayerAccount::getMinecraftName)
			.findFirst()
			.orElse("bw4sinvalidname");
	}
	
	
	public String getDiscordUsername(String minecraftUsername) {
		return accounts.stream()
			.filter(account -> account.getMinecraftName() != null && account.getMinecraftName().equals(minecraftUsername))
			.map(PlayerAccount::getDiscordUsername)
			.findFirst()
			.orElse(null);
	}
	
	public String getDiscordId(String minecraftUsername) {
		return accounts.stream()
			.filter(account -> account.getMinecraftName() != null && account.getMinecraftName().equals(minecraftUsername))
			.map(PlayerAccount::getDiscordId)
			.findFirst()
			.orElse(null);
	}
	
	public void setup(BukkitPlugin plugin) {
		this.accountMap.clear();
		
		AtomicInteger loadedAccounts = new AtomicInteger();
		
		RankedRepository ranked = Repositories.RANKED;
		ranked.getAllAccounts().forEach(account -> {
			try {
				loadedAccounts.getAndIncrement();
				this.accountMap.put(Bukkit.getPlayerExact(account.getMinecraftName()), account);
				this.accounts.add(account);
			} catch (Exception e) {
				throw new RuntimeException("Failed to load account " + account.getMinecraftName(), e);
			}
		});
		
		plugin.log("&6CACHE! &aCarregadas " + loadedAccounts.get() + " contas do banco de dados.");
	}
	
	public void save() {
		RankedRepository ranked = Repositories.RANKED;
		this.accountMap.forEach((ignored_, account) -> {
			ranked.updateAccount(account);
		});
	}
	
	public boolean hasAccountByMinecraftName(String nickname) {
		return this.accounts.stream()
			.anyMatch(account -> account.getMinecraftName() != null && account.getMinecraftName().equals(nickname) && account.getDiscordId() != null && !(account.getDiscordId().isEmpty()));
	}
}
