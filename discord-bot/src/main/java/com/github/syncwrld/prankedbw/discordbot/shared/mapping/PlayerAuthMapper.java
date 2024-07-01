package com.github.syncwrld.prankedbw.discordbot.shared.mapping;

import lombok.Getter;
import me.syncwrld.booter.minecraft.tool.Pair;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class PlayerAuthMapper {
	
	@Getter
	private static final HashMap<String, Pair<String, Instant>> authCodes = new HashMap<>();
	private static final HashMap<String, String> discordIds = new HashMap<>();
	
	public static String getAuthCode(String username) {
		return authCodes.get(username).getKey();
	}
	
	public static String getDiscordId(String username) {
		return discordIds.entrySet().stream()
			.filter(entry -> entry.getValue().equals(username))
			.map(Map.Entry::getKey)
			.findFirst()
			.orElse(null);
	}
	
	public static void setAuthCode(String username, String authCode, String discordId) {
		authCodes.put(username, new Pair<>(authCode, Instant.now()));
		discordIds.put(discordId, username);
	}
	
	public static void invalidate(String username) {
		authCodes.remove(username);
		discordIds.remove(username);
	}
	
	public static void invalidateAll() {
		authCodes.clear();
		discordIds.clear();
	}
	
	public static boolean invalid(String username, String authCode) {
		return !authCodes.get(username).getKey().equals(authCode);
	}
	
	public static boolean isValidating(String username) {
		return authCodes.containsKey(username);
	}
	
}
