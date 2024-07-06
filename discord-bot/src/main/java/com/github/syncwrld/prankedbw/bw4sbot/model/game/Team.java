package com.github.syncwrld.prankedbw.bw4sbot.model.game;

import me.syncwrld.booter.minecraft.loader.BukkitPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
	
	public void moveAllToChannel(BukkitPlugin plugin) {
		if (users == null || users.isEmpty()) {
			plugin.log("&cNenhum usuário disponível para mover para o canal de voz.");
			return;
		}
		
		if (voiceChannel == null) {
			plugin.log("&cCanal de voz não especificado.");
			return;
		}
		
		CompletableFuture<Void> allMoves = CompletableFuture.allOf(
			users.stream()
				.map(user -> user.move(voiceChannel)
					.exceptionally(throwable -> {
						plugin.log("&cUm erro ocorreu ao mover o usuário para o canal de voz: " + user.getName());
						throwable.printStackTrace();
						return null;
					})
				)
				.toArray(CompletableFuture[]::new)
		);
		
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> new Runnable() {
			@Override
			public void run() {
				try {
					allMoves.join();
				} catch (Exception e) {
					plugin.log("&cErro ao mover usuários para o canal de voz.");
					e.printStackTrace();
				}
			}
		});
	}
	
	public List<User> getUsers() {
		return users;
	}
}
