package com.github.syncwrld.prankedbw.discordbot.discord.task;

import com.github.syncwrld.prankedbw.discordbot.discord.PRankedRobotBootstrapper;
import com.github.syncwrld.prankedbw.discordbot.shared.cache.Caches;
import com.github.syncwrld.prankedbw.discordbot.shared.cache.impl.UserCache;
import com.github.syncwrld.prankedbw.discordbot.shared.model.PlayerAccount;
import lombok.SneakyThrows;
import me.syncwrld.booter.minecraft.tool.Pair;
import org.bukkit.Bukkit;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.ServerVoiceChannelBuilder;
import org.javacord.api.entity.channel.VoiceChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class MatchAvailabilityLabor implements Runnable {
	private final PRankedRobotBootstrapper robot;
	
	public MatchAvailabilityLabor(PRankedRobotBootstrapper robot) {
		this.robot = robot;
	}
	
	@SneakyThrows
	@Override
	public void run() {
		final String serverId = this.robot.getGuildId();
		final String waitChannelId = Caches.PREFERENCE_CACHE.getWaitChannelId();
		final DiscordApi client = this.robot.getClient();
		
		Optional<VoiceChannel> possibleVoiceChannel = client.getVoiceChannelById(waitChannelId);
		
		if (!possibleVoiceChannel.isPresent()) {
			this.robot.getPlugin().log("O canal de espera deve ser um canal de voz. Verifique seu servidor.");
			this.robot.getPlugin().log("Não foi possível encontrar o canal de espera. O servidor será desabilitado.");
			
			Thread.sleep(3000);
			
			Bukkit.shutdown();
			return;
		}
		
		VoiceChannel voiceChannel = possibleVoiceChannel.get();
		Optional<ServerVoiceChannel> possibleServerVC = voiceChannel.asServerVoiceChannel();
		
		if (!possibleServerVC.isPresent()) {
			this.robot.getPlugin().log("O canal de espera deve ser um canal de voz do servidor configurado. Verifique seu servidor. O servidor será desabilitado.");
			this.robot.getPlugin().getServer().getPluginManager().disablePlugin(this.robot.getPlugin());
			
			Thread.sleep(3000);
			
			Bukkit.shutdown();
			return;
		}
		
		ServerVoiceChannel serverVoiceChannel = possibleServerVC.get();
		Set<User> connectedUsers = serverVoiceChannel.getConnectedUsers();
		
		if (connectedUsers.size() < 8) {
			return;
		}
		
		UserCache userCache = Caches.USER_CACHE;
		ArrayList<PlayerAccount> synchronizedPlayersList = new ArrayList<>();
		HashMap<Integer, Pair<User, PlayerAccount>> synchronizedPlayers = new HashMap<>();
		int synchronizedPlayerCount = 0;
		
		int userIndex = 0;
		for (User user : connectedUsers) {
			PlayerAccount playerAccount = userCache.findByDiscordId("" + user.getId());
			if (playerAccount != null && playerAccount.getProperties().isDiscordSynchronized()) {
				synchronizedPlayers.put(userIndex, new Pair<>(user, playerAccount));
				synchronizedPlayersList.add(playerAccount);
				synchronizedPlayerCount++;
				userIndex++;
			}
		}
		
		if (synchronizedPlayerCount < 8) {
			return;
		}
		
		int teamSize = 4;
		HashMap<User, PlayerAccount> teamOne = new HashMap<>();
		HashMap<User, PlayerAccount> teamTwo = new HashMap<>();
		
		for (int i = 0; i < 8; i++) {
			ThreadLocalRandom random = ThreadLocalRandom.current();
			int randomIndex = random.nextInt(0, synchronizedPlayers.size());
			
			boolean isTeamOne = random.nextBoolean();
			for (int j = 0; j < teamSize; j++) {
				PlayerAccount playerAccount = synchronizedPlayersList.get(randomIndex);
				
				if (isTeamOne && teamOne.size() < teamSize) {
					teamOne.put(synchronizedPlayers.get(j).getKey(), playerAccount);
					continue;
				}
				
				if (teamTwo.size() < teamSize) {
					teamTwo.put(synchronizedPlayers.get(j).getKey(), playerAccount);
					continue;
				}
			}
		}
		
		if (teamOne.size() != 8 && teamTwo.size() != 8) {
			this.robot.getPlugin().log("Não foi possível encontrar os jogadores para a partida (" + teamOne.size() + " x " + teamTwo.size() + "). Verifique se os jogadores estão online.");
			return;
		}
		
		Optional<Server> optionalServer = client.getServerById(serverId);
		
		if (!optionalServer.isPresent()) {
			this.robot.getPlugin().log("Não foi possível encontrar o servidor. O plugin será desabilitado.");
			this.robot.getPlugin().getServer().getPluginManager().disablePlugin(this.robot.getPlugin());
			return;
		}
		
		Server server = optionalServer.get();
		int randomT1ID = ThreadLocalRandom.current().nextInt(0, 9999999);
		int randomT2ID = ThreadLocalRandom.current().nextInt(0, 9999999);
		
		Optional<ChannelCategory> channelCategoryById = server.getChannelCategoryById(this.robot.getVoiceChannelCategoryId());
		
		if (!channelCategoryById.isPresent()) {
			this.robot.getPlugin().log("Não foi possível encontrar a categoria de salas de voz. O plugin será desabilitado.");
			this.robot.getPlugin().getServer().getPluginManager().disablePlugin(this.robot.getPlugin());
			return;
		}
		
		ChannelCategory channelCategory = channelCategoryById.get();
		
		ServerVoiceChannelBuilder teamOneVCBuilder = server.createVoiceChannelBuilder();
		teamOneVCBuilder.setName("4s [T1 x " + randomT1ID + "]");
		teamOneVCBuilder.setUserlimit(teamSize);
		teamOneVCBuilder.setCategory(channelCategory);
		
		ServerVoiceChannelBuilder teamTwoVCBuilder = server.createVoiceChannelBuilder();
		teamTwoVCBuilder.setName("4s [T2 x " + randomT2ID + "]");
		teamOneVCBuilder.setUserlimit(teamSize);
		teamOneVCBuilder.setCategory(channelCategory);
		
		ServerVoiceChannel vcTeamOne = teamOneVCBuilder.create().join();
		ServerVoiceChannel vcTeamTwo = teamTwoVCBuilder.create().join();
		
		for (int i = 0; i < teamSize; i++) {
			synchronizedPlayers.forEach((index, account) -> {
				User accountKey = account.getKey();
				
				if (teamOne.containsKey(accountKey)) {
					accountKey.move(vcTeamOne);
					return;
				}
				
				if (teamTwo.containsKey(accountKey)) {
					accountKey.move(vcTeamTwo);
					return;
				}
			});
		}
		
		this.robot.getPlugin().log(
			"Nova partida iniciando em instantes! ID = T1 x " + randomT1ID + " | ID = T2 x " + randomT2ID
		);
	}
}


