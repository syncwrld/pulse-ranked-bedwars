package com.github.syncwrld.prankedbw.bw4sbot.manager;

import com.github.syncwrld.prankedbw.bw4sbot.PRankedSpigotPlugin;
import com.github.syncwrld.prankedbw.bw4sbot.cache.impl.AccountCache;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.javacord.api.entity.user.User;

import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PlayerManager {
	private final PRankedSpigotPlugin plugin;
	
	public PlayerManager(PRankedSpigotPlugin plugin) {
		this.plugin = plugin;
	}
	
	public boolean isOnlineInGame(String discordUsername) {
		AccountCache accountCache = this.plugin.getCaches().getAccountCache();
		String minecraftUsername = accountCache.getMinecraftUsername(discordUsername);
		
		return minecraftUsername != null && this.plugin.getServer().getPlayer(minecraftUsername) != null;
	}
	
	public int howManyAreBind(Set<User> users) {
		AccountCache accountCache = plugin.getCaches().getAccountCache();
		Predicate<User> hasAccount = user -> accountCache.hasAccountById("" + user.getId());
		
		return (int) users.stream()
			.filter(hasAccount)
			.count();
	}
	
	public int howManyAreBindAndInGame(Set<User> users) {
		return howManyAreBind(users) + howManyAreInGame(users);
	}
	
	private int howManyAreInGame(Set<User> users) {
		return (int) users.stream()
			.filter(user -> isOnlineInGame(user.getName()))
			.count();
	}
	
	public Set<User> chopToEight(Set<User> users) {
		return users.stream()
			.limit(8)
			.collect(Collectors.toSet());
	}
	
	public Set<Player> getPlayers(Set<User> users) {
		return users.stream()
			.map(user -> Bukkit.getPlayerExact(
				plugin.getCaches().getAccountCache().getMinecraftUsername(user.getName())
			))
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
	}
	
	public Set<Player> getAvailablePlayers(PRankedSpigotPlugin plugin, Set<User> users) {
		return users.stream()
			.map(user -> Bukkit.getPlayerExact(
				plugin.getCaches().getAccountCache().getMinecraftUsername(user.getName())
			))
			.filter(Objects::nonNull)
			.filter(player -> !plugin.getCaches().getMatchesCache().isPlaying(plugin, player))
			.collect(Collectors.toSet());
	}
	
}
