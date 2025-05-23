package com.github.syncwrld.prankedbw.bw4sbot.task;

import com.github.syncwrld.prankedbw.bw4sbot.Configuration;
import com.github.syncwrld.prankedbw.bw4sbot.PRankedJavacordRobot;
import com.github.syncwrld.prankedbw.bw4sbot.api.event.other.TeamsAvailableEvent;
import com.github.syncwrld.prankedbw.bw4sbot.manager.GameManager;
import com.github.syncwrld.prankedbw.bw4sbot.manager.PlayerManager;
import com.github.syncwrld.prankedbw.bw4sbot.model.data.PlayerAccount;
import me.syncwrld.booter.minecraft.loader.BukkitPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class MatchAvailabilityLabor implements Runnable {
	private final PRankedJavacordRobot robot;
	private final PlayerManager playerManager;
	private final GameManager gameManager;
	
	private final ArrayList<User> alreadySentWarning = new ArrayList<>();
	
	public MatchAvailabilityLabor(PRankedJavacordRobot robot) {
		this.robot = robot;
		this.playerManager = new PlayerManager(this.robot.getPlugin());
		this.gameManager = this.robot.getPlugin().getGameManager();
	}
	
	@Override
	public void run() {
		final String channelId = this.robot.getConfiguration().getWaitChannelId();
		final String registeredRoleId = this.robot.getConfiguration().getRegisteredRoleId();
		
		Optional<Role> roleById = this.robot.getApi().getRoleById(registeredRoleId);
		
		if (roleById.isEmpty()) {
			this.robot.getPlugin().log("&cO cargo com o id informado não foi encontrado. Desabilitando...");
			this.robot.disablePlugin();
			return;
		}
		
		this.robot.getApi().getVoiceChannelById(channelId).ifPresentOrElse(
			(voiceChannel) -> {
				ServerVoiceChannel serverVoiceChannel = voiceChannel.asServerVoiceChannel().orElse(null);
				
				if (serverVoiceChannel == null) {
					this.robot.getPlugin().log("&cO canal com o id informado não é um canal de voz. Desabilitando...");
					this.robot.disablePlugin();
					return;
				}
				
				Set<User> connectedUsers = serverVoiceChannel.getConnectedUsers();
				Set<Player> availablePlayers = playerManager.getAvailablePlayers(robot.getPlugin(), connectedUsers);
				Role registeredRole = roleById.get();
				
				Set<User> playersWithRole = filterUserWithRole(connectedUsers, serverVoiceChannel.getServer(), registeredRole);
				
				int bindAmount = this.playerManager.howManyAreBind(playersWithRole);
				int playersPerTeam = Configuration.PLAYERS_PER_TEAM;
				
				playersWithRole.forEach(roledPlayer -> {
					PlayerAccount roledPlayerAccount = this.robot.getPlugin().getCaches().getAccountCache().getAccountByDiscordId(roledPlayer.getIdAsString());
					
					if (roledPlayerAccount != null) {
						roledPlayer.updateNickname(
							registeredRole.getServer(),
							roledPlayer.getName() + " [" + roledPlayerAccount.getEloPoints() + "]"
						).thenCompose(ignored_ -> null);
					}
				});
				
                /*
                Se o número de jogadores disponíveis para a partida
                forem menores que o número necessário para formar
                2 times completos
                 */
				if ((bindAmount / 2) < playersPerTeam) {
					return;
				}
				
				if ((availablePlayers.size() / 2) < playersPerTeam) {
					return;
				}
				
				playersWithRole.forEach(user -> {
					if (!playerManager.isOnlineInGame(user.getName())) {
						if (!alreadySentWarning.contains(user)) {
							alreadySentWarning.add(user);
							
							user.openPrivateChannel()
								.thenAccept(channel -> {
									channel.sendMessage("Você está aguardando uma partida, porém não online dentro do jogo, você não será incluso em nenhum jogo. Entre no servidor do Minecraft e fique na chamada.")
										.exceptionally(ignored_ -> null)
										.join();
								})
								.exceptionally(ignored_ -> (Void) null)
								.join();
						}
					}
				});
				
				if (playersWithRole.isEmpty()) {
					return;
				}
				
				TeamsAvailableEvent event = new TeamsAvailableEvent(playersWithRole);
				
				// Execute the event call on the main server thread
				Bukkit.getScheduler().runTask(this.robot.getPlugin(), () -> {
					BukkitPlugin.callEvent(event);
				});
			}, () -> {
				this.robot.getPlugin().log("&cO canal com o id informado não foi encontrado. Desabilitando...");
				this.robot.disablePlugin();
			}
		);
	}
	
	private Set<User> filterUserWithRole(Set<User> users, Server server, Role role) {
		return users.stream()
			.filter(user -> user.getRoles(server).contains(role))
			.collect(Collectors.toSet());
	}
}
