package com.github.syncwrld.prankedbw.bw4sbot.event.bukkit;

import com.github.syncwrld.prankedbw.bw4sbot.PRankedSpigotPlugin;
import com.github.syncwrld.prankedbw.bw4sbot.database.Repositories;
import com.github.syncwrld.prankedbw.bw4sbot.model.data.PlayerAccount;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Objects;

public class ConnectionTrafficListener implements Listener {
	
	private final PRankedSpigotPlugin plugin;
	
	public ConnectionTrafficListener(PRankedSpigotPlugin plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onConnect(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		PlayerAccount account = Repositories.RANKED.getOrCreateAccount(player);
		
		if (!Objects.equals(account.getMinecraftName(), player.getName())) {
			Repositories.RANKED.setMinecraftNameChanges(account.getMinecraftName(), player.getName());
			account.setMinecraftName(player.getName());
		}
		
		this.plugin.getCaches().getAccountCache().setAccount(player, account);
	}
	
	@EventHandler
	public void onDisconnect(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		this.plugin.getCaches().getAccountCache().removeAccount(player);
	}
	
}
