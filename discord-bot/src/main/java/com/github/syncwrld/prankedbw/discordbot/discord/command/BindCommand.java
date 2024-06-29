package com.github.syncwrld.prankedbw.discordbot.discord.command;

import com.github.syncwrld.prankedbw.discordbot.shared.cache.Caches;
import com.github.syncwrld.prankedbw.discordbot.shared.gen.AuthCodeGenerator;
import com.github.syncwrld.prankedbw.discordbot.shared.mapping.PlayerAuthMapper;
import com.github.syncwrld.prankedbw.discordbot.shared.model.PlayerAccount;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.awt.*;
import java.util.Optional;

public class BindCommand implements MessageCreateListener {
	
	@Override
	public void onMessageCreate(MessageCreateEvent event) {
		Message message = event.getMessage();
		String content = message.getContent();
		
		if (!content.startsWith("!bind") || event.getMessageAuthor().isBotUser()) {
			return;
		}
		
		long authorId = event.getMessageAuthor().getId();
		Optional<Server> possibleServer = event.getServer();
		
		if (!(possibleServer.isPresent())) {
			event.getMessage().reply("ta me tirando porra, envia esse caralho no servidor, nao no meu privado. eu em vai se fuder").join();
			return;
		}
		
		Server server = possibleServer.get();
		Optional<User> memberById = server.getMemberById(authorId);
		
		if (!memberById.isPresent()) {
			event.getMessage().reply("ta me tirando porra, envia esse caralho no servidor, nao no meu privado. eu em vai se fuder").join();
			return;
		}
		
		User member = memberById.get();
		
		String[] split = content.split(" ");
		
		if (split.length < 2) {
			event.getMessage().reply("O uso correto é '!bind <usernameDoMinecraft>'").join();
			return;
		}
		
		String username = split[1];
		
		if (username.isEmpty()) {
			event.getMessage().reply("O uso correto é '!bind <usernaameDoMinecraft>'").join();
			return;
		}
		
		PlayerAccount account = Caches.USER_CACHE.findByDiscordId("" + member.getId());
		
		if (account != null) {
			event.getMessage().reply("Este usuário já está vinculado ao Discord.").join();
			return;
		}
		
		String authCode = AuthCodeGenerator.create();
		PlayerAuthMapper.setAuthCode(username, authCode, "" + authorId);
		
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setColor(Color.MAGENTA);
		embedBuilder.setTitle("Vinculação de usuário");
		embedBuilder.setDescription("Tudo certo! Agora para completar a vinculação, copie seu código de autorização abaixo e envie no chat do jogo no lobby do Bedwars.");
		embedBuilder.addField("Código de autorização", authCode, true);
		embedBuilder.setFooter("Este código de autorização é válido por 5 minutos.");
		
		member.openPrivateChannel().thenAccept(channel -> {
			channel.sendMessage(embedBuilder).join();
		}).exceptionally(throwable -> {
			event.getMessage().reply("Não foi possível te enviar o código via DM, por favor abra sua DM para mensagens e tente novamente.").join();
			return null;
		});
		
		event.getMessage().reply("Tudo jóia! Verifique sua caixa de entrada do Discord.").join();
	}
}
