package com.github.syncwrld.prankedbw.bw4sbot.model.game;

import com.github.syncwrld.prankedbw.bw4sbot.PRankedSpigotPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.user.User;

import java.util.Iterator;
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
		PRankedSpigotPlugin plugin = JavaPlugin.getPlugin(PRankedSpigotPlugin.class);
		Bukkit.getScheduler().runTaskTimer(plugin, new BukkitRunnable() {
			private final Iterator<User> userIterator = users.iterator();
			
			@Override
			public void run() {
				if (userIterator.hasNext()) {
					User user = userIterator.next();
					user.move(voiceChannel)
						.exceptionally(e -> {
							plugin.log("&cUm erro ocorreu ao mover o usuário para o canal de voz. Por favor, verifique suas configurações. E = <" + e.getMessage() + ">");
							return null;
						})
						.join();
				} else {
					this.cancel();
				}
			}
		}, 0L, 3L);
	}
	
	public List<User> getUsers() {
		return users;
	}
}
