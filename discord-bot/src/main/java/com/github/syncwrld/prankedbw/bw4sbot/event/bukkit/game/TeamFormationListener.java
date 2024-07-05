package com.github.syncwrld.prankedbw.bw4sbot.event.bukkit.game;

import com.github.syncwrld.prankedbw.bw4sbot.Configuration;
import com.github.syncwrld.prankedbw.bw4sbot.PRankedSpigotPlugin;
import com.github.syncwrld.prankedbw.bw4sbot.api.event.MatchAvailableEvent;
import com.github.syncwrld.prankedbw.bw4sbot.api.event.TeamsAvailableEvent;
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
		Set<Player> players = playerManager.getPlayers(users);
		
		/*
		Se o número de jogadores disponíveis para a partida
		forem menores que o número necessário para formar
		2 times completos
		 */
		if ((players.size() / 2) < Configuration.PLAYERS_PER_TEAM) {
			return;
		}
		
		Pair<Pair<List<Player>, List<User>>, Pair<List<Player>, List<User>>> teams = createTeams(users);
		
		List<User> u1 = teams.getKey().getValue();
		List<User> u2 = teams.getValue().getValue();
		
		List<Player> t1 = teams.getKey().getKey();
		List<Player> t2 = teams.getValue().getKey();
		
		String randomChannelId = RandomStringUtils.randomAlphanumeric(5);
		
		ServerVoiceChannel t1_channel = createVoiceChannel(randomChannelId, users);
		ServerVoiceChannel t2_channel = createVoiceChannel(randomChannelId, users);
		
		Team team1 = new Team(u1, t1, t1_channel);
		Team team2 = new Team(u2, t2, t2_channel);
		
		Match match = new Match(team1, team2);
		
		Bukkit.getScheduler().runTaskLater(
			this.plugin,
			() -> {
				team1.moveAllToChannel();
				team2.moveAllToChannel();
			}, 40L);
		
		MatchAvailableEvent matchAvailableEvent = new MatchAvailableEvent(match);
		BukkitPlugin.callEvent(matchAvailableEvent);
	}
	
	private Pair<Pair<List<Player>, List<User>>, Pair<List<Player>, List<User>>> createTeams(Set<User> users) {
		Set<Player> players = playerManager.getPlayers(users);
		int teamSize = Configuration.PLAYERS_PER_TEAM;
		
		Map<User, Player> userMap = new HashMap<>();
		
		// Converte o Set<Player> para uma List<Player> e embaralha a lista
		List<Player> allPlayers = new ArrayList<>(players);
		Collections.shuffle(allPlayers, ThreadLocalRandom.current());
		
		// Cria duas sublistas para os times
		List<Player> t1 = new ArrayList<>();
		List<Player> t2 = new ArrayList<>();
		List<User> u1 = new ArrayList<>();
		List<User> u2 = new ArrayList<>();
		
		// Itera sobre os jogadores e os distribui nos times, além de preencher o userMap
		for (Player player : allPlayers) {
			// Adiciona o jogador ao primeiro time se não estiver cheio
			if (t1.size() < teamSize) {
				t1.add(player);
				users.stream()
					.filter(user -> user.getName().equals(player.getName()))
					.findFirst()
					.ifPresent(u1::add);
			}
			// Adiciona o jogador ao segundo time se não estiver cheio
			else if (t2.size() < teamSize) {
				t2.add(player);
				users.stream()
					.filter(user -> user.getName().equals(player.getName()))
					.findFirst()
					.ifPresent(u2::add);
			}
		}
		
		return new Pair<>(new Pair<>(t1, u1), new Pair<>(t2, u2));
	}
	
	
	private ServerVoiceChannel createVoiceChannel(String channelName, Set<User> users) {
		RobotConfiguration configuration = this.plugin.getBootstrapper().getConfiguration();
		String matchCategoryId = configuration.getMatchCategoryId();
		
		Optional<ChannelCategory> category = this.plugin.getBootstrapper().getApi().getChannelCategoryById(matchCategoryId);
		
		if (category.isEmpty()) {
			this.plugin.log("&cA categoria de partidas não foi encontrada. Por favor, verifique suas configurações.");
			return null;
		}
		
		Server server = category.get().getServer();
		CompletableFuture<ServerVoiceChannel> builderFuture = server.createVoiceChannelBuilder().setCategory(category.get()).setName(channelName).setUserlimit(users.size()).create();
		
		builderFuture.thenAccept((channel) -> {
			Role everyoneRole = server.getEveryoneRole();
			channel.createUpdater()
				.addPermissionOverwrite(everyoneRole,
					new PermissionsBuilder().setDenied(PermissionType.CONNECT).build())
				.update().join();
			
			for (User user : users) {
				channel.createUpdater().addPermissionOverwrite(user,
						new PermissionsBuilder()
							.setAllowed(PermissionType.CONNECT).build())
					.update().join();
			}
		}).exceptionally((e) -> {
			this.plugin.log("&cUm erro ocorreu ao criar o canal de voz. Por favor, verifique suas configurações.");
			return null;
		});
		
		return builderFuture.join();
	}
	
}
