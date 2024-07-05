package com.github.syncwrld.prankedbw.bw4sbot.model.game;

import com.github.syncwrld.prankedbw.bw4sbot.PRankedSpigotPlugin;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.user.User;

import java.util.List;

public class Team {
	private final List<User> users;
	private final List<Player> players;
	private final ServerVoiceChannel voiceChannel;
	
	public Team(List<User> users, List<Player> players, ServerVoiceChannel voiceChannel) {
		this.users = users;
		this.players = players;
		this.voiceChannel = voiceChannel;
	}
	
	public List<Player> getPlayers() {
		return players;
	}
	
	public int getSize() {
		return players.size();
	}
	
	public ServerVoiceChannel getVoiceChannel() {
		return voiceChannel;
	}
	
	public void moveAllToChannel() {
		this.users.forEach(user -> {
			user.move(this.voiceChannel)
				.exceptionally((e) -> {
					JavaPlugin.getPlugin(PRankedSpigotPlugin.class)
						.log("&cUm erro ocorreu ao mover o usuário para o canal de voz. Por favor, verifique suas configurações. E = <" + e.getMessage() + ">");
					return null;
				})
				.join();
		});
	}
	
	public List<User> getUsers() {
		return users;
	}
}
