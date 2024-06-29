package com.github.syncwrld.prankedbw.discordbot.shared.mapping.labor;

import com.github.syncwrld.prankedbw.discordbot.shared.mapping.PlayerAuthMapper;
import me.syncwrld.booter.minecraft.tool.Pair;

import java.time.Instant;
import java.util.HashMap;

public class PlayerAuthMapperLabor implements Runnable {
	@Override
	public void run() {
		HashMap<String, Pair<String, Instant>> authCodes = PlayerAuthMapper.getAuthCodes();
		
		for (String username : authCodes.keySet()) {
			Pair<String, Instant> pair = authCodes.get(username);
			
			if (Instant.now().isAfter(pair.getValue().plusSeconds(300))) {
				PlayerAuthMapper.invalidate(username);
			}
		}
	}
}
