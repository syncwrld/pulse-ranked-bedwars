package com.github.syncwrld.prankedbw.discordbot.discord.event;

import com.github.syncwrld.prankedbw.discordbot.discord.PRankedRobotBootstrapper;
import com.github.syncwrld.prankedbw.discordbot.shared.api.event.MatchmakingDoneEvent;
import com.github.syncwrld.prankedbw.discordbot.shared.model.PlayerAccount;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;

import java.awt.*;
import java.util.HashMap;
import java.util.stream.Collectors;

public class MatchmakingListener implements Listener {
	
	private final PRankedRobotBootstrapper robot;
	
	public MatchmakingListener(PRankedRobotBootstrapper robot) {
		this.robot = robot;
	}
	
	@EventHandler
	public void whenMatchmakingIsDone(MatchmakingDoneEvent event) {
		HashMap<User, PlayerAccount> players = event.getPlayers();
		sendEmbedMessage(event, players);
	}
	
	private void sendEmbedMessage(MatchmakingDoneEvent event, HashMap<User, PlayerAccount> players) {
		players.forEach((user, account) -> {
			this.robot.getClient().getUserById(user.getId()).thenAccept(acceptedUser -> {
				acceptedUser.openPrivateChannel().thenAccept(channel -> {
						EmbedBuilder embedBuilder = new EmbedBuilder();
						embedBuilder.setColor(Color.MAGENTA);
						
						embedBuilder.setTitle("NOVA PARTIDA!");
						embedBuilder.setDescription("Uma nova partida foi iniciada e você está em um dos times.");
						embedBuilder.addField("Time A", formatTeam(event.getTeamOne()), true);
						embedBuilder.addField("Time B", formatTeam(event.getTeamTwo()), true);
						
						channel.sendMessage(embedBuilder)
							.exceptionally(ignored_ -> null)
							.join();
					}).exceptionally(ignored_ -> null)
					.join();
			});
		});
	}
	
	private String formatTeam(HashMap<User, PlayerAccount> team) {
		return team.values().stream().map(PlayerAccount::getUsername).collect(Collectors.joining(", "));
	}
	
}
