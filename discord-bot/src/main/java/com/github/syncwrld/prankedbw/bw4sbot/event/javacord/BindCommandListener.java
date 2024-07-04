package com.github.syncwrld.prankedbw.bw4sbot.event.javacord;

import com.github.syncwrld.prankedbw.bw4sbot.PRankedSpigotPlugin;
import com.github.syncwrld.prankedbw.bw4sbot.cache.impl.AccountCache;
import com.github.syncwrld.prankedbw.bw4sbot.cache.impl.TokenCache;
import org.apache.commons.lang3.RandomStringUtils;
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
					
					/*
					Verificando se o nickname já está vinculado ao Minecraft
					 */
					if (accountCache.hasAccount(nickname)) {
						commandInteraction.createImmediateResponder()
							.setContent("Esse nickname já está vinculado a uma conta.")
							.respond().join();
						return;
					}
					
					TokenCache tokenCache = plugin.getCaches().getTokenCache();
					
					/*
					Verificando se o nickname já está aguardando para ser vinculado
					 */
					if (tokenCache.isAlreadyWaiting(nickname)) {
						commandInteraction.createImmediateResponder()
							.setContent("Esse nickname já está aguardando para ser vinculado.")
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
						.respond().join();
					
					/*
					Abrindo uma nova DM com o usuário e enviando o token de vinculação
					 */
					user.openPrivateChannel()
						.thenAccept((privateChannel) -> {
							EmbedBuilder embed = new EmbedBuilder()
								.setTitle("Ranked 4S - Vinculação")
								.setDescription("Complete sua vinculação para desbloquear o modo Ranked 4S.")
								.addField("Código de vinculação", token, false)
								.setColor(Color.MAGENTA);
							
							privateChannel.sendMessage(embed)
								/*
								Caso a DM esteja aberta, enviar o token de vinculação,
								caso contrário, mostrar um erro para o usuário
								 */
								.exceptionally((throwable) -> {
									commandInteraction.createImmediateResponder()
										.setContent("Ocorreu um erro ao enviar o código de vinculação. Verifique se sua DM está aberta.")
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
						.respond().join();
				}
			);
	}
}
