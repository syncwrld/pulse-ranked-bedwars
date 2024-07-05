package com.github.syncwrld.prankedbw.bw4sbot.model.game;

import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class Match {
	private final String id;
	private final Team team1;
	private final Team team2;
	
	public Match(Team team1, Team team2) {
		this.id = createId(team1, team2);
		this.team1 = team1;
		this.team2 = team2;
	}
	
	private String createId(Team team1, Team team2) {
		Player t1_player = team1.getPlayers().get(ThreadLocalRandom.current().nextInt(0, team1.getPlayers().size()));
		Player t2_player = team2.getPlayers().get(ThreadLocalRandom.current().nextInt(0, team2.getPlayers().size()));
		
		UUID t1_randomUUID = UUID.nameUUIDFromBytes(t1_player.getName().getBytes());
		UUID t2_randomUUID = UUID.nameUUIDFromBytes(t2_player.getName().getBytes());
		
		return String.format("bw4s-%s%s", t1_randomUUID.toString().substring(0, 2), t2_randomUUID.toString().substring(0, 2));
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
}
