package com.github.syncwrld.prankedbw.discordbot.discord.task;

import com.github.syncwrld.prankedbw.discordbot.discord.PRankedRobotBootstrapper;
import com.github.syncwrld.prankedbw.discordbot.shared.api.event.MatchmakingDoneEvent;
import com.github.syncwrld.prankedbw.discordbot.shared.cache.Caches;
import com.github.syncwrld.prankedbw.discordbot.shared.cache.impl.UserCache;
import com.github.syncwrld.prankedbw.discordbot.shared.model.PlayerAccount;
import lombok.SneakyThrows;
import me.syncwrld.booter.minecraft.tool.Pair;
import org.bukkit.Bukkit;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.ServerVoiceChannelBuilder;
import org.javacord.api.entity.channel.VoiceChannel;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class MatchAvailabilityLabor implements Runnable {
	private final PRankedRobotBootstrapper robot;
	private final int teamSize = 1;
	private Server server;
	
	public MatchAvailabilityLabor(PRankedRobotBootstrapper robot) {
		this.robot = robot;
	}
	
	@SneakyThrows
	@Override
	public void run() {
		Optional<VoiceChannel> optionalVoiceChannel = getWaitChannel();
		if (!optionalVoiceChannel.isPresent()) {
			handleShutdown("O canal de espera deve ser um canal de voz. Verifique seu servidor.");
			return;
		}
		
		ServerVoiceChannel serverVoiceChannel = optionalVoiceChannel.get().asServerVoiceChannel().orElse(null);
		if (serverVoiceChannel == null) {
			handleShutdown("O canal de espera deve ser um canal de voz do servidor configurado. Verifique seu servidor.");
			return;
		}
		
		Set<User> connectedUsers = serverVoiceChannel.getConnectedUsers();
		if (connectedUsers.size() < (teamSize * 2)) {
			return;
		}
		
		List<Pair<User, PlayerAccount>> synchronizedPlayers = getSynchronizedPlayers(connectedUsers);
		if (synchronizedPlayers.size() < (teamSize * 2)) {
			return;
		}
		
		HashMap<User, PlayerAccount> teamOne = new HashMap<>();
		HashMap<User, PlayerAccount> teamTwo = new HashMap<>();
		assignTeams(synchronizedPlayers, teamOne, teamTwo);
		
		if (teamOne.size() != teamSize || teamTwo.size() != teamSize) {
			robot.getPlugin().log("Não foi possível encontrar os jogadores para a partida (" + teamOne.size() + " x " + teamTwo.size() + "). Verifique se os jogadores estão online.");
			return;
		}
		
		Optional<Server> optionalServer = robot.getClient().getServerById(serverVoiceChannel.getServer().getIdAsString());
		if (!optionalServer.isPresent()) {
			handleShutdown("Não foi possível encontrar o servidor. O plugin será desabilitado.");
			return;
		}
		
		int randoMatchID = ThreadLocalRandom.current().nextInt(0, 999999999);
		this.server = optionalServer.get();
		Pair<ServerVoiceChannel, ServerVoiceChannel> voiceChannelsAndMovePlayers = createVoiceChannelsAndMovePlayers(randoMatchID, server1, teamOne, teamTwo);
		
		callMatchmakingDoneEvent(randoMatchID, teamOne, teamTwo, voiceChannelsAndMovePlayers.getKey(), voiceChannelsAndMovePlayers.getValue());
	}
	
	private Optional<VoiceChannel> getWaitChannel() {
		final String waitChannelId = Caches.PREFERENCE_CACHE.getWaitChannelId();
		return robot.getClient().getVoiceChannelById(waitChannelId);
	}
	
	private void handleShutdown(String message) throws InterruptedException {
		robot.getPlugin().log(message);
		Thread.sleep(3000);
		Bukkit.shutdown();
	}
	
	private List<Pair<User, PlayerAccount>> getSynchronizedPlayers(Set<User> connectedUsers) {
		UserCache userCache = Caches.USER_CACHE;
		List<Pair<User, PlayerAccount>> synchronizedPlayers = new ArrayList<>();
		for (User user : connectedUsers) {
			PlayerAccount playerAccount = userCache.findByDiscordId(String.valueOf(user.getId()));
			if (playerAccount != null && playerAccount.getProperties().isDiscordSynchronized()) {
				synchronizedPlayers.add(new Pair<>(user, playerAccount));
			}
		}
		return synchronizedPlayers;
	}
	
	private void assignTeams(List<Pair<User, PlayerAccount>> synchronizedPlayers, HashMap<User, PlayerAccount> teamOne, HashMap<User, PlayerAccount> teamTwo) {
		Random random = new Random();
		for (int i = 0; i < (teamSize * 2); i++) {
			int randomIndex = random.nextInt(synchronizedPlayers.size());
			Pair<User, PlayerAccount> playerPair = synchronizedPlayers.get(randomIndex);
			
			if (random.nextBoolean() && teamOne.size() < teamSize) {
				teamOne.put(playerPair.getKey(), playerPair.getValue());
			}
			
			if (teamTwo.size() < teamSize) {
				teamTwo.put(playerPair.getKey(), playerPair.getValue());
			}
		}
	}
	
	private Pair<ServerVoiceChannel, ServerVoiceChannel> createVoiceChannelsAndMovePlayers(int randomID, Server server, HashMap<User, PlayerAccount> teamOne, HashMap<User, PlayerAccount> teamTwo) throws Exception {
		Optional<ChannelCategory> channelCategory = server.getChannelCategoryById(robot.getVoiceChannelCategoryId());
		if (!channelCategory.isPresent()) {
			handleShutdown("Não foi possível encontrar a categoria de salas de voz. O plugin será desabilitado.");
			return new Pair<>(null, null);
		}
		
		ServerVoiceChannel vcTeamOne = createVoiceChannel(server, "T1", randomID, channelCategory.get());
		ServerVoiceChannel vcTeamTwo = createVoiceChannel(server, "T2", randomID, channelCategory.get());
		handleChannelPermissions(vcTeamOne, teamOne);
		handleChannelPermissions(vcTeamTwo, teamTwo);
		
		Bukkit.getScheduler().runTaskLater(robot.getPlugin(), () -> {
			movePlayersToChannels(teamOne, vcTeamOne);
			movePlayersToChannels(teamTwo, vcTeamTwo);
		}, 60);
		
		return new Pair<>(vcTeamOne, vcTeamTwo);
	}
	
	private void handleChannelPermissions(ServerVoiceChannel voiceChannel, HashMap<User, PlayerAccount> team) {
		team.keySet().forEach(user -> {
			voiceChannel.createUpdater().addPermissionOverwrite(user, new PermissionsBuilder().setAllowed(PermissionType.CONNECT).build()).update();
		});
		
		Role everyoneRole = server.getEveryoneRole();
		voiceChannel.createUpdater()
			.addPermissionOverwrite(everyoneRole, new PermissionsBuilder().setDenied(PermissionType.CONNECT).build()).update();
	}
	
	private ServerVoiceChannel createVoiceChannel(Server server, String teamName, int randomID, ChannelCategory category) throws Exception {
		return new ServerVoiceChannelBuilder(server).setName("4s [" + teamName + " - " + randomID + "]").setUserlimit(teamSize).setCategory(category).create().join();
	}
	
	private void movePlayersToChannels(HashMap<User, PlayerAccount> team, ServerVoiceChannel voiceChannel) {
		team.keySet().forEach(user -> user.move(voiceChannel));
	}
	
	private void callMatchmakingDoneEvent(int randomMatchID, HashMap<User, PlayerAccount> teamOne, HashMap<User, PlayerAccount> teamTwo, ServerVoiceChannel teamOneChannel, ServerVoiceChannel teamTwoChannel) {
		HashMap<User, PlayerAccount> allPlayers = new HashMap<>(teamOne);
		allPlayers.putAll(teamTwo);
		Bukkit.getPluginManager().callEvent(new MatchmakingDoneEvent(allPlayers, teamOne, teamTwo, teamOneChannel, teamTwoChannel, randomMatchID));
		robot.getPlugin().log("Nova partida iniciando em instantes! RdMatchID -> " + randomMatchID);
	}
	
}
