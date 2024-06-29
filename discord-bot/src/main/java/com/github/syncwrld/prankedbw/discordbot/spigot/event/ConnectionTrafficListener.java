package com.github.syncwrld.prankedbw.discordbot.spigot.event;

import com.github.syncwrld.prankedbw.discordbot.shared.cache.Caches;
import com.github.syncwrld.prankedbw.discordbot.shared.database.Repositories;
import com.github.syncwrld.prankedbw.discordbot.shared.database.impl.RankedRepository;
import com.github.syncwrld.prankedbw.discordbot.shared.model.PlayerAccount;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ConnectionTrafficListener implements Listener {
	
	@EventHandler
	public void whenConnectionStart(PlayerJoinEvent event) {
		try {
			Player player = event.getPlayer();
			RankedRepository rankedRepository = Repositories.RANKED_REPOSITORY;

			PlayerAccount account = rankedRepository.findByMCNickname(player.getName());

			if (account == null) {
				account = rankedRepository.createAccount(player);
			}

			Caches.USER_CACHE.add(account);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
