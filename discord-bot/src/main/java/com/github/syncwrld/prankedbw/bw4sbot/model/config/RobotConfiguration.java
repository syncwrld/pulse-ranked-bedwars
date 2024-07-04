package com.github.syncwrld.prankedbw.bw4sbot.model.config;

import lombok.Data;
import me.syncwrld.booter.minecraft.config.YAML;

@Data
public class RobotConfiguration {
	private final String waitChannelId;
	private final String matchCategoryId;
	private final String botStatusMessage;
	private final String registeredRoleId;
	
	public static RobotConfiguration of(YAML yaml) {
		return new RobotConfiguration(
			yaml.getString("wait-channel-id"),
			yaml.getString("voice-channel-category-id"),
			yaml.getString("bot-status-message"),
			yaml.getString("registered-role-id")
		);
	}
}
