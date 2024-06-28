package com.github.syncwrld.prankedbw.discordbot.discord.task;

import com.github.syncwrld.prankedbw.discordbot.discord.PRankedRobotBootstrapper;
import com.github.syncwrld.prankedbw.discordbot.shared.cache.Caches;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

import java.util.List;

public class MatchAvailabilityLabor implements Runnable {
	private final PRankedRobotBootstrapper robot;
	
	public MatchAvailabilityLabor(PRankedRobotBootstrapper robot) {
		this.robot = robot;
	}
	
	@Override
	public void run() {
		String waitChannelId = Caches.PREFERENCE_CACHE.getWaitChannelId();
		VoiceChannel voiceChannel = this.robot.getJda().getVoiceChannelById(waitChannelId);
		
		if (voiceChannel == null) {
			this.robot.getPlugin().log("§cNão foi possível encontrar o canal de espera de partidas.");
			return;
		}
		
		List<Member> members = voiceChannel.getMembers();
		
	}
}
