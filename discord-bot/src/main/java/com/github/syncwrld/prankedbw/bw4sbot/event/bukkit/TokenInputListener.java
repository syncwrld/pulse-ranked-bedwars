package com.github.syncwrld.prankedbw.bw4sbot.event.bukkit;

import com.github.syncwrld.prankedbw.bw4sbot.PRankedSpigotPlugin;
import com.github.syncwrld.prankedbw.bw4sbot.cache.impl.AccountCache;
import com.github.syncwrld.prankedbw.bw4sbot.cache.impl.TokenCache;
import com.github.syncwrld.prankedbw.bw4sbot.model.data.AccountData;
import com.github.syncwrld.prankedbw.bw4sbot.model.data.PlayerAccount;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.javacord.api.entity.message.embed.EmbedBuilder;

public class TokenInputListener implements Listener {
	private final PRankedSpigotPlugin plugin;
	
	public TokenInputListener(PRankedSpigotPlugin plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onTokenInput(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		TokenCache tokenCache = this.plugin.getCaches().getTokenCache();
		
		String playerName = player.getName();
		if (!tokenCache.isWaiting(playerName)) {
			return;
		}
		
		String message = event.getMessage();
		String expectedToken = tokenCache.getToken(playerName);
		String discordId = tokenCache.getDiscordId(playerName);
		
		if (message.equals(expectedToken)) {
			event.setCancelled(true);
			player.sendMessage("§aVocê foi vinculado com sucesso!");
			
			AccountCache accountCache = plugin.getCaches().getAccountCache();
			PlayerAccount oldAccount = accountCache.getAccount(player);
			PlayerAccount newAccount = new PlayerAccount(playerName, tokenCache.getDiscordName(playerName), AccountData.create(discordId, oldAccount.getAccountData().getEloPoints()));
			accountCache.setAccount(player, newAccount);
			
			this.plugin.getBootstrapper().getApi()
				.getUserById(discordId)
				.thenAcceptAsync(user -> {
					tokenCache.removeToken(playerName);
					
					user.openPrivateChannel()
						.thenAccept(channel -> {
							EmbedBuilder embed = new EmbedBuilder()
								.setTitle("Ranked 4S - Vinculação")
								.setDescription("Você foi vinculado com sucesso!")
								.addField("Nickname do Minecraft", playerName, false)
								.addField("Nickname no Discord", user.getName(), false)
								.setFooter("© PulseMC, 2024. Todos os direitos reservados.");
							
							channel.sendMessage(embed)
								.exceptionally(ignored -> null)
								.join();
						})
						.exceptionally(ignored -> (Void) null)
						.join();
				})
				.exceptionally(ignored -> (Void) null)
				.join();
		}
	}
}
