package com.github.syncwrld.prankedbw.discordbot.shared.mapping;

import lombok.Getter;
import me.syncwrld.booter.minecraft.tool.Pair;

import java.time.Instant;
import java.util.HashMap;

public class PlayerAuthMapper {
	
	@Getter
	private static final HashMap<String, Pair<String, Instant>> authCodes = new HashMap<>();
	
	public static String getAuthCode(String username) {
		return authCodes.get(username).getKey();
	}
	
	public static void setAuthCode(String username, String authCode) {
		authCodes.put(username, new Pair<>(authCode, Instant.now()));
	}
	
	public static void invalidate(String username) {
		authCodes.remove(username);
	}
	
	public static void invalidateAll() {
		authCodes.clear();
	}
	
	public static boolean invalid(String username, String authCode) {
		return !authCodes.get(username).getKey().equals(authCode);
	}
	
}
