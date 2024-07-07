package com.github.syncwrld.prankedbw.bw4sbot.event.javacord;

import com.github.syncwrld.prankedbw.bw4sbot.PRankedSpigotPlugin;
import com.github.syncwrld.prankedbw.bw4sbot.cache.impl.AccountCache;
import com.github.syncwrld.prankedbw.bw4sbot.cache.impl.TokenCache;
import org.apache.commons.lang3.RandomStringUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;

import java.awt.*;

public class BindCommandListener implements SlashCommandCreateListener {
	private final PRankedSpigotPlugin plugin;
	
	public BindCommandListener(PRankedSpigotPlugin plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void onSlashCommandCreate(SlashCommandCreateEvent event) {
		SlashCommandInteraction commandInteraction = event.getSlashCommandInteraction();
		commandInteraction.getArgumentStringValueByName("Nickname")
			.ifPresentOrElse(nickname -> {
					AccountCache accountCache = plugin.getCaches().getAccountCache();
					String userId = commandInteraction.getUser().getIdAsString();
					
					if (accountCache.hasAccountById(userId)) {
						commandInteraction.createImmediateResponder()
							.setContent("Você já vinculou sua conta.")
							.setFlags(MessageFlag.EPHEMERAL)
							.respond().join();
						return;
					}
					
					/*
					Verificando se o nickname já está vinculado ao Minecraft
					 */
					if (accountCache.hasAccountByMinecraftName(nickname)) {
						commandInteraction.createImmediateResponder()
							.setContent("Esse nickname já está vinculado a uma conta.")
							.setFlags(MessageFlag.EPHEMERAL)
							.respond().join();
						return;
					}
					
					TokenCache tokenCache = plugin.getCaches().getTokenCache();
					
					/*
					Verificando se o nickname já está aguardando para ser vinculado
					 */
					if (tokenCache.isWaiting(nickname)) {
						commandInteraction.createImmediateResponder()
							.setContent("Esse nickname já está aguardando para ser vinculado.")
							.setFlags(MessageFlag.EPHEMERAL)
							.respond().join();
						return;
					}
					
					/*
					Verificando se o discordId já está aguardando para ser vinculado
					 */
					String discordId = "" + commandInteraction.getUser().getId();
					if (tokenCache.isAlreadyWaitingByDiscordId(discordId)) {
						commandInteraction.createImmediateResponder()
							.setContent("Você já está aguardando para ser vinculado.")
							.setFlags(MessageFlag.EPHEMERAL)
							.respond().join();
						return;
					}
					
					User user = commandInteraction.getUser();
					
					/*
					Gerando um token aleatório
					 */
					String token = RandomStringUtils.randomAlphanumeric(8);
					tokenCache.setToken(
						nickname,
						"" + user.getId(),
						user.getName(),
						token
					);
					
					commandInteraction.createImmediateResponder()
						.setContent("Tudo jóia! Seu código de vinculação será enviado por DM.")
						.setFlags(MessageFlag.EPHEMERAL)
						.respond().join();
					
					/*
					Abrindo uma nova DM com o usuário e enviando o token de vinculação
					 */
					user.openPrivateChannel()
						.thenAccept((privateChannel) -> {
							EmbedBuilder embed = new EmbedBuilder()
								.setTitle("Ranked 4S - Vinculação")
								.setDescription("Tudo certo! Agora para completar a vinculação, copie seu código de autorização abaixo e envie no chat do jogo no lobby do Bedwars.")
								.addField("Código de vinculação", token, false)
								.setFooter("Seu código de autorização expira em 5 minutos")
								.setColor(Color.MAGENTA);
							
							privateChannel.sendMessage(embed)
								/*
								Caso a DM esteja aberta, enviar o token de vinculação,
								caso contrário, mostrar um erro para o usuário
								 */
								.exceptionally((throwable) -> {
									commandInteraction.createImmediateResponder()
										.setContent("Ocorreu um erro ao enviar o código de vinculação. Verifique se sua DM está aberta.")
										.setFlags(MessageFlag.EPHEMERAL)
										.respond().join();
									return null;
								})
								.join();
						})
						.exceptionally((throwable) -> {
							return null;
						});
				},
				() -> {
					commandInteraction.createImmediateResponder()
						.setContent("Você precisa informar um nickname.")
						.setFlags(MessageFlag.EPHEMERAL)
						.respond().join();
				}
			);
	}
}
