package com.github.syncwrld.prankedbw.bw4sbot.task;

import com.github.syncwrld.prankedbw.bw4sbot.Configuration;
import com.github.syncwrld.prankedbw.bw4sbot.PRankedJavacordRobot;
import com.github.syncwrld.prankedbw.bw4sbot.manager.PlayerManager;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class MatchAvailabilityLabor implements Runnable {
	private final PRankedJavacordRobot robot;
	private final PlayerManager playerManager;
	
	public MatchAvailabilityLabor(PRankedJavacordRobot robot) {
		this.robot = robot;
		this.playerManager = new PlayerManager(this.robot.getPlugin());
	}
	
	@Override
	public void run() {
		final String channelId = this.robot.getConfiguration().getWaitChannelId();
		final String registeredRoleId = this.robot.getConfiguration().getRegisteredRoleId();
		
		Optional<Role> roleById = this.robot.getApi().getRoleById(registeredRoleId);
		
		if (roleById.isEmpty()) {
			this.robot.getPlugin().log("&cO cargo com o id informado não foi encontrado.");
			return;
		}
		
		this.robot.getApi().getVoiceChannelById(channelId).ifPresentOrElse(
			(voiceChannel) -> {
				ServerVoiceChannel serverVoiceChannel = voiceChannel.asServerVoiceChannel().orElse(null);
				
				if (serverVoiceChannel == null) {
					this.robot.getPlugin().log("&cO canal com o id informado não é um canal de voz.");
					return;
				}
				
				Set<User> connectedUsers = serverVoiceChannel.getConnectedUsers();
				Role registeredRole = roleById.get();
				
				Set<User> playersWithRole = filterUserWithRole(connectedUsers, serverVoiceChannel.getServer(), registeredRole);
				
				int bindAmount = this.playerManager.howManyAreBind(playersWithRole);
				int playersPerTeam = Configuration.PLAYERS_PER_TEAM;
				
				/*
				Se o número de jogadores disponíveis para a partida
				forem menores que o número necessário para formar
				2 times completos
				 */
				if ((bindAmount / 2) < playersPerTeam) {
					return;
				}
			}, () -> {
				this.robot.getPlugin().log("&cO canal com o id informado não foi encontrado.");
			}
		);
	}
	
	private Set<User> filterUserWithRole(Set<User> users, Server server, Role role) {
		return users.stream()
			.filter(user -> user.getRoles(server).contains(role))
			.collect(Collectors.toSet());
	}
}
