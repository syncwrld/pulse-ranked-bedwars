package com.github.syncwrld.prankedbw.discordbot.shared.model;

import com.github.syncwrld.prankedbw.discordbot.shared.model.data.PlayerProperties;
import lombok.Data;

@Data
public class PlayerAccount {
	private final String username;
	private final String discordId;
	private final PlayerProperties properties;
}
