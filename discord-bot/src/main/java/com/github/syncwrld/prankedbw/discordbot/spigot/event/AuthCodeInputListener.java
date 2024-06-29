package com.github.syncwrld.prankedbw.discordbot.spigot.event;

import com.github.syncwrld.prankedbw.discordbot.shared.mapping.PlayerAuthMapper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class AuthCodeInputListener implements Listener {
	
	@EventHandler
	public void whenAuthCodeInput(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		
		if (!PlayerAuthMapper.isValidating(player.getName())) {
			return;
		}
		
		String authCode = PlayerAuthMapper.getAuthCode(player.getName());
		
		if (authCode == null) {
			return;
		}
		
		String message = event.getMessage();
		
		if (message.equals(authCode)) {
			PlayerAuthMapper.invalidate(player.getName());
			event.setCancelled(true);
			
			player.sendMessage("Código de autorização válido! Você foi vinculado ao Discord.");
		}
	}
}
