package com.github.syncwrld.prankedbw.bw4sbot.model.data;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/*
 * Objeto principal que guarda as informações de uma conta do jogador
 */
public class PlayerAccount implements Serializable {
	
	private String minecraftName;
	private String discordUsername;
	private AccountData accountData;
	
	public PlayerAccount(String minecraftName, String discordUsername, AccountData accountData) {
		this.minecraftName = minecraftName;
		this.discordUsername = discordUsername;
		this.accountData = accountData;
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
	
	public AccountData getAccountData() {
		return accountData;
	}
	
	public void setAccountData(AccountData accountData) {
		this.accountData = accountData;
	}
	
	public static PlayerAccount createEmpty(String minecraftName) {
		return new PlayerAccount(minecraftName, null, AccountData.createNull());
	}
	
}
