package com.github.syncwrld.prankedbw.bw4sbot.model.data;

import me.syncwrld.booter.minecraft.Serializable;

/*
* Objeto que guarda os dados do jogador
* Armazenado em JSON
 */
public class AccountData implements Serializable {
	
	private String discordId;
	private int eloPoints;
	
	private AccountData(String discordId, int eloPoints) {
		this.discordId = discordId;
		this.eloPoints = eloPoints;
	}
	
	public static AccountData create(String discordId, int eloPoints) {
		return new AccountData(discordId, eloPoints);
	}
	
	public static AccountData createNull() {
		return new AccountData(null, 0);
	}
	
	public String getDiscordId() {
		return discordId;
	}
	
	public void setDiscordId(String discordId) {
		this.discordId = discordId;
	}
	
	public int getEloPoints() {
		return eloPoints;
	}
	
	public void setEloPoints(int eloPoints) {
		this.eloPoints = eloPoints;
	}
	
}
