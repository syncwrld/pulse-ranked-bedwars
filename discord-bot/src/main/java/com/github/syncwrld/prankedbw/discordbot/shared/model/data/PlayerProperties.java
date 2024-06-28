package com.github.syncwrld.prankedbw.discordbot.shared.model.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlayerProperties {
	private int eloPoints;
	private boolean discordSynchronized;
	private String discordUserId;
}
