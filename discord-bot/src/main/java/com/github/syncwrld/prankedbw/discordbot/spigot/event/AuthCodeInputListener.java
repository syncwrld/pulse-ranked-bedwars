package com.github.syncwrld.prankedbw.discordbot.spigot.event;

import com.github.syncwrld.prankedbw.discordbot.discord.PRankedRobotBootstrapper;
import com.github.syncwrld.prankedbw.discordbot.shared.cache.Caches;
import com.github.syncwrld.prankedbw.discordbot.shared.mapping.PlayerAuthMapper;
import com.github.syncwrld.prankedbw.discordbot.shared.model.PlayerAccount;
import com.github.syncwrld.prankedbw.discordbot.shared.model.data.PlayerProperties;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;

public class AuthCodeInputListener implements Listener {
	
	private final PRankedRobotBootstrapper robot;
	
	public AuthCodeInputListener(PRankedRobotBootstrapper robot) {
		this.robot = robot;
	}
	
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
			
			PlayerAccount account = new PlayerAccount(player.getName(), PlayerAuthMapper.getDiscordId(player.getName()), new PlayerProperties(0, true, "waitingSynchronization"));
			Caches.USER_CACHE.add(account);
			
			player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);
			player.sendMessage("§aCódigo de autorização válido! Você foi vinculado ao Discord.");
			sendEmbedMessage(account);
		}
	}
	
	private void sendEmbedMessage(PlayerAccount account) {
		this.robot.getClient().getUserById(account.getDiscordId()).thenAccept(user -> {
			user.openPrivateChannel().thenAccept(channel -> {
				EmbedBuilder embedBuilder = new EmbedBuilder();
				embedBuilder.setColor(Color.MAGENTA);
				embedBuilder.setTitle("GG! Agora sua conta está vinculada.");
				embedBuilder.setDescription("Nosso sistema acabou de receber a sua autorização de vinculação, agora você pode explorar nosso modo de jogo 4S via Discord. Parabéns!");
				embedBuilder.setFooter("© PulseMC 2024");
				
				channel.sendMessage(embedBuilder).join();
			}).exceptionally(ignored_ -> null).join();
		});
	}
}
