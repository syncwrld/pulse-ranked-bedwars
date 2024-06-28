package com.github.syncwrld.prankedbw.discordbot.shared.cache.impl;

import lombok.Data;

@Data
public class PreferenceCache {
	private String discordToken;
	private String waitChannelId;
}
