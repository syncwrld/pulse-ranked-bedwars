package com.github.syncwrld.prankedbw.bw4sbot.model.game;

import com.github.syncwrld.prankedbw.bw4sbot.PRankedSpigotPlugin;
import org.bukkit.entity.Player;
import org.javacord.api.entity.channel.ServerTextChannel;

import java.time.Instant;
import java.util.HashMap;

public class Match {
	private final PRankedSpigotPlugin plugin;
	private final String id;
	private final Team team1;
	private final Team team2;
	private final ServerTextChannel matchChannel;
	private final HashMap<Player, Integer> eloChanges;
	private Instant startTime;
	private boolean strikeable;
	
	public Match(PRankedSpigotPlugin plugin, Team team1, Team team2, ServerTextChannel matchChannel) {
		this.plugin = plugin;
		this.startTime = Instant.now();
		this.id = createId();
		this.team1 = team1;
		this.team2 = team2;
		this.matchChannel = matchChannel;
		this.strikeable = true;
		this.eloChanges = new HashMap<>();
	}
	
	private String createId() {
		int currentMatchId = plugin.getConfiguration().getInt("current-match-id");
		int nextId = currentMatchId + 1;
		
		plugin.getConfiguration().set("current-match-id", nextId);
		plugin.getConfiguration().save();
		plugin.getConfiguration().reload();
		
		return String.format("bw4s-%d", nextId);
	}
	
	public Team getTeam1() {
		return team1;
	}
	
	public Team getTeam2() {
		return team2;
	}
	
	public String getId() {
		return id;
	}
	
	public ServerTextChannel getMatchChannel() {
		return matchChannel;
	}
	
	public boolean isStrikeable() {
		return strikeable;
	}
	
	public void setStrikeable(boolean strikeable) {
		this.strikeable = strikeable;
	}
	
	public Instant getStartTime() {
		return startTime;
	}
	
	public void setStartTime(Instant startTime) {
		this.startTime = startTime;
	}
	
	public HashMap<Player, Integer> getEloChanges() {
		return eloChanges;
	}
	
	public void setEloPoints(Player player, int points) {
		eloChanges.put(player, points);
	}
	
	public void addEloPoints(Player player, int points) {
		eloChanges.put(player, eloChanges.getOrDefault(player, 0) + points);
	}
	
	public void removeEloPoints(Player player, int points) {
		eloChanges.put(player, eloChanges.getOrDefault(player, 0) - points);
	}
}
