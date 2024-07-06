package com.github.syncwrld.prankedbw.bw4sbot.model.data;

import org.bukkit.entity.Player;

import java.io.Serializable;
import java.util.UUID;

/*
 * Objeto principal que guarda as informações de uma conta do jogador
 */
public class PlayerAccount implements Serializable {
	
	private final UUID minecraftUuid;
	private final String discordId;
	private String minecraftName;
	private String discordUsername;
	private int eloPoints;
	
	public PlayerAccount(UUID minecraftUuid, String minecraftName, String discordId, String discordUsername, int eloPoints) {
		this.minecraftUuid = minecraftUuid;
		this.minecraftName = minecraftName;
		this.discordId = discordId;
		this.discordUsername = discordUsername;
		this.eloPoints = eloPoints;
	}
	
	public static PlayerAccount createEmpty(Player player) {
		return new PlayerAccount(player.getUniqueId(), player.getName(), null, null, 0);
	}
	
	public String getMinecraftName() {
		return minecraftName;
	}
	
	public void setMinecraftName(String minecraftName) {
		this.minecraftName = minecraftName;
	}
	
	public String getDiscordUsername() {
		return discordUsername;
	}
	
	public void setDiscordUsername(String discordUsername) {
		this.discordUsername = discordUsername;
	}
	
	public UUID getMinecraftUuid() {
		return minecraftUuid;
	}
	
	public int getEloPoints() {
		return eloPoints;
	}
	
	public void setEloPoints(int eloPoints) {
		this.eloPoints = eloPoints;
	}
	
	public String getDiscordId() {
		return discordId;
	}
}
