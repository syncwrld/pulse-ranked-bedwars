package com.github.syncwrld.prankedbw.discordbot.discord.task;

import com.github.syncwrld.prankedbw.discordbot.discord.PRankedRobotBootstrapper;
import com.github.syncwrld.prankedbw.discordbot.shared.cache.Caches;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.VoiceChannel;
import org.javacord.api.entity.user.User;

import java.util.Optional;
import java.util.Set;

public class MatchAvailabilityLabor implements Runnable {
	private final PRankedRobotBootstrapper robot;
	
	public MatchAvailabilityLabor(PRankedRobotBootstrapper robot) {
		this.robot = robot;
	}
	
	@Override
	public void run() {
		final String waitChannelId = Caches.PREFERENCE_CACHE.getWaitChannelId();
		Optional<VoiceChannel> possibleVoiceChannel = this.robot.getClient().getVoiceChannelById(waitChannelId);
		
		if (!possibleVoiceChannel.isPresent()) {
			this.robot.getPlugin().log("&cO canal de espera deve ser um canal de voz. Verifique seu servidor.");
			this.robot.getPlugin().log("&cNão foi possível encontrar o canal de espera. O plugin será desabilitado.");
			this.robot.getPlugin().getServer().getPluginManager().disablePlugin(this.robot.getPlugin());
			return;
		}
		
		VoiceChannel voiceChannel = possibleVoiceChannel.get();
		Optional<ServerVoiceChannel> possibleServerVC = voiceChannel.asServerVoiceChannel();
	
		if (!possibleServerVC.isPresent()) {
			this.robot.getPlugin().log("&cO canal de espera deve ser um canal de voz do servidor configurado. Verifique seu servidor.");
			this.robot.getPlugin().getServer().getPluginManager().disablePlugin(this.robot.getPlugin());
			return;
		}
		
		ServerVoiceChannel serverVoiceChannel = possibleServerVC.get();
		Set<User> connectedUsers = serverVoiceChannel.getConnectedUsers();
	}
}
