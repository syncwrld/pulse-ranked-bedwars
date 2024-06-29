package com.github.syncwrld.prankedbw.discordbot.discord.command;

import com.github.syncwrld.prankedbw.discordbot.discord.Listener;
import com.github.syncwrld.prankedbw.discordbot.discord.PRankedRobotBootstrapper;
import com.github.syncwrld.prankedbw.discordbot.shared.cache.Caches;
import com.github.syncwrld.prankedbw.discordbot.shared.gen.AuthCodeGenerator;
import com.github.syncwrld.prankedbw.discordbot.shared.mapping.PlayerAuthMapper;
import com.github.syncwrld.prankedbw.discordbot.shared.model.PlayerAccount;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

import java.awt.*;
import java.util.Optional;

public class BindCommand implements Listener<MessageCreateEvent> {
	private final PRankedRobotBootstrapper bootstrapper;
	
	public BindCommand(PRankedRobotBootstrapper bootstrapper) {
		this.bootstrapper = bootstrapper;
	}
	
	@Override
	public void handle(MessageCreateEvent event) {
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
		Message message = event.getMessage();
		String content = message.getContent();
		
		String[] split = content.split(" ");
		
		if (split.length != 1) {
			event.getMessage().reply("O uso correto é '!bind <usernaameDoMinecraft>'").join();
			return;
		}
		
		String username = split[0];
		
		if (username.isEmpty()) {
			event.getMessage().reply("O uso correto é '!bind <usernaameDoMinecraft>'").join();
			return;
		}
		
		PlayerAccount account = Caches.USER_CACHE.findByDiscordId("" + member.getId());
		
		if (account == null) {
			event.getMessage().reply("Não foi possível encontrar o usuário no banco de dados. Por favor, verifique se o nome do usuário está correto.").join();
			return;
		}
		
		if (account.getProperties().isDiscordSynchronized() || account.getDiscordId() != null) {
			event.getMessage().reply("Este usuário já está vinculado ao Discord.").join();
			return;
		}
		
		String authCode = AuthCodeGenerator.create();
		PlayerAuthMapper.setAuthCode(username, authCode);
		
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setColor(Color.MAGENTA);
		embedBuilder.setTitle("Vinculação de usuário");
		embedBuilder.setDescription("Tudo certo! Agora para completar a vinculação, copie seu código de autorização abaixo e envie no chat do jogo no lobby do Bedwars.");
		embedBuilder.addField("Código de autorização", authCode, true);
		embedBuilder.setFooter("Este código de autorização é válido por 5 minutos.");
		
		event.getMessage().reply(embedBuilder.build()).join();
	}
}
