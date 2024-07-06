package com.github.syncwrld.prankedbw.bw4sbot.event.bukkit.game;

import com.github.syncwrld.prankedbw.bw4sbot.Configuration;
import com.github.syncwrld.prankedbw.bw4sbot.PRankedSpigotPlugin;
import com.github.syncwrld.prankedbw.bw4sbot.api.event.MatchAvailableEvent;
import com.github.syncwrld.prankedbw.bw4sbot.api.event.TeamsAvailableEvent;
import com.github.syncwrld.prankedbw.bw4sbot.cache.impl.AccountCache;
import com.github.syncwrld.prankedbw.bw4sbot.manager.PlayerManager;
import com.github.syncwrld.prankedbw.bw4sbot.model.config.RobotConfiguration;
import com.github.syncwrld.prankedbw.bw4sbot.model.game.Match;
import com.github.syncwrld.prankedbw.bw4sbot.model.game.Team;
import me.syncwrld.booter.minecraft.loader.BukkitPlugin;
import me.syncwrld.booter.minecraft.tool.Pair;
import org.apache.commons.lang3.RandomStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class TeamFormationListener implements Listener {
	
	private final PRankedSpigotPlugin plugin;
	private final PlayerManager playerManager;
	
	public TeamFormationListener(PRankedSpigotPlugin plugin) {
		this.plugin = plugin;
		this.playerManager = new PlayerManager(plugin);
	}
	
	@EventHandler
	public void onTeamsAvailable(TeamsAvailableEvent event) {
		Set<User> users = event.getUsers();
		Set<Player> players = playerManager.getAvailablePlayers(plugin, users);
		
		if ((players.size() / 2) < Configuration.PLAYERS_PER_TEAM) {
			return;
		}
		
		Pair<Pair<List<Player>, List<User>>, Pair<List<Player>, List<User>>> teams = createTeams(users);
		
		List<User> u1 = teams.getKey().getValue();
		List<User> u2 = teams.getValue().getValue();
		
		List<Player> t1 = teams.getKey().getKey();
		List<Player> t2 = teams.getValue().getKey();
		
		String randomChannelId = "BW4S-" + RandomStringUtils.randomAlphanumeric(5);
		
		createTextChannel(randomChannelId, users).thenAccept(textChannel -> {
			createVoiceChannel(randomChannelId + " [TIME 1]", new HashSet<>(u1)).thenAccept(voiceChannel -> {
				if (voiceChannel == null) {
					return;
				}
				
				Team team1 = new Team(u1, t1, voiceChannel);
				
				createVoiceChannel(randomChannelId + " [TIME 2]", new HashSet<>(u2)).thenAccept(voiceChannelT2 -> {
					if (voiceChannelT2 == null) {
						return;
					}
					
					Team team2 = new Team(u2, t2, voiceChannelT2);
					Match match = new Match(team1, team2, textChannel);
					
					Bukkit.getScheduler().runTask(this.plugin, () -> {
						MatchAvailableEvent matchAvailableEvent = new MatchAvailableEvent(match);
						BukkitPlugin.callEvent(matchAvailableEvent);
					});
				});
			});
		});
	}
	
	private Pair<Pair<List<Player>, List<User>>, Pair<List<Player>, List<User>>> createTeams(Set<User> users) {
		Set<Player> players = playerManager.getPlayers(users);
		int teamSize = Configuration.PLAYERS_PER_TEAM;
		
		List<Player> allPlayers = new ArrayList<>(players);
		Collections.shuffle(allPlayers, ThreadLocalRandom.current());
		
		List<Player> players_t1 = allPlayers.subList(0, teamSize);
		List<Player> players_t2 = allPlayers.subList(teamSize, allPlayers.size());
		List<User> u1 = new ArrayList<>();
		List<User> u2 = new ArrayList<>();
		
		AccountCache accountCache = plugin.getCaches().getAccountCache();
		
		for (Player player : players_t1) {
			String discordUsername = accountCache.getDiscordUsername(player.getName());
			if (discordUsername != null) {
				users.stream()
					.filter(user -> user.getName().equals(discordUsername))
					.findFirst()
					.ifPresent(u1::add);
			}
		}
		
		for (Player player : players_t2) {
			String discordUsername = accountCache.getDiscordUsername(player.getName());
			if (discordUsername != null) {
				users.stream()
					.filter(user -> user.getName().equals(discordUsername))
					.findFirst()
					.ifPresent(u2::add);
			}
		}
		
		return new Pair<>(new Pair<>(players_t1, u1), new Pair<>(players_t2, u2));
	}
	
	private CompletableFuture<ServerVoiceChannel> createVoiceChannel(String channelName, Set<User> users) {
		RobotConfiguration configuration = this.plugin.getBootstrapper().getConfiguration();
		String matchCategoryId = configuration.getMatchCategoryId();
		
		Optional<ChannelCategory> category = this.plugin.getBootstrapper().getApi().getChannelCategoryById(matchCategoryId);
		
		if (category.isEmpty()) {
			this.plugin.log("&cA categoria de partidas não foi encontrada. Por favor, verifique suas configurações.");
			return CompletableFuture.failedFuture(new IllegalStateException("Categoria não encontrada"));
		}
		
		Server server = category.get().getServer();
		
		if (server != null) {
			List<ServerVoiceChannel> voiceChannelsByName = server.getVoiceChannelsByName(channelName);
			if (voiceChannelsByName != null && !voiceChannelsByName.isEmpty()) {
				return CompletableFuture.completedFuture(voiceChannelsByName.get(0));
			}
		}
		
		assert server != null;
		return server.createVoiceChannelBuilder()
			.setCategory(category.get())
			.setName(channelName)
			.setUserlimit(users.size())
			.create()
			.thenCompose(channel -> {
				Role everyoneRole = server.getEveryoneRole();
				
				CompletableFuture<Void> updateFuture = channel.createUpdater()
					.addPermissionOverwrite(everyoneRole,
						new PermissionsBuilder()
							.setDenied(PermissionType.VIEW_CHANNEL, PermissionType.SPEAK, PermissionType.CONNECT).build())
					.update();
				
				for (User user : users) {
					updateFuture = updateFuture.thenCompose(v -> channel.createUpdater()
						.addPermissionOverwrite(user,
							new PermissionsBuilder()
								.setAllowed(PermissionType.CONNECT, PermissionType.VIEW_CHANNEL, PermissionType.SPEAK).build())
						.update());
				}
				
				return updateFuture.thenApply(v -> channel);
			})
			.exceptionally(e -> {
				this.plugin.log("&cUm erro ocorreu ao criar o canal de voz. Por favor, verifique suas configurações.");
				return null;
			});
	}
	
	private CompletableFuture<ServerTextChannel> createTextChannel(String channelName, Set<User> users) {
		RobotConfiguration configuration = this.plugin.getBootstrapper().getConfiguration();
		String matchCategoryId = configuration.getMatchCategoryId();
		
		Optional<ChannelCategory> category = this.plugin.getBootstrapper().getApi().getChannelCategoryById(matchCategoryId);
		
		if (category.isEmpty()) {
			this.plugin.log("&cA categoria de partidas não foi encontrada. Por favor, verifique suas configurações.");
			return CompletableFuture.failedFuture(new IllegalStateException("Categoria não encontrada"));
		}
		
		Server server = category.get().getServer();
		
		if (server != null) {
			List<ServerTextChannel> textChannelsByName = server.getTextChannelsByName(channelName);
			if (textChannelsByName != null && !textChannelsByName.isEmpty()) {
				return CompletableFuture.completedFuture(textChannelsByName.get(0));
			}
		}
		
		assert server != null;
		return server.createTextChannelBuilder()
			.setCategory(category.get())
			.setName(channelName)
			.create()
			.thenCompose(channel -> {
				Role everyoneRole = server.getEveryoneRole();
				
				CompletableFuture<Void> updateFuture = channel.createUpdater()
					.addPermissionOverwrite(everyoneRole,
						new PermissionsBuilder()
							.setDenied(PermissionType.VIEW_CHANNEL, PermissionType.SEND_MESSAGES).build())
					.update();
				
				for (User user : users) {
					updateFuture = updateFuture.thenCompose(v -> channel.createUpdater()
						.addPermissionOverwrite(user,
							new PermissionsBuilder()
								.setAllowed(PermissionType.VIEW_CHANNEL, PermissionType.SEND_MESSAGES)
								.setDenied(PermissionType.MENTION_EVERYONE, PermissionType.MANAGE_THREADS)
								.build())
						.update());
				}
				
				return updateFuture.thenApply(v -> channel);
			})
			.exceptionally(e -> {
				this.plugin.log("&cUm erro ocorreu ao criar o canal de texto. Por favor, verifique suas configurações.");
				return null;
			});
	}
	
	
}
