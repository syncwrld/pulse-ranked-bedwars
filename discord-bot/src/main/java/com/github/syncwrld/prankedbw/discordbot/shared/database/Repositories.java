package com.github.syncwrld.prankedbw.discordbot.shared.database;

import com.github.syncwrld.prankedbw.discordbot.shared.database.impl.RankedRepository;
import com.github.syncwrld.prankedbw.discordbot.spigot.PRankedSpigotPlugin;

import java.sql.Connection;

public class Repositories {
	public static RankedRepository RANKED_REPOSITORY;
	
	public static void setup(PRankedSpigotPlugin plugin) {
		Connection connection = DatabaseHandler.getConnection(plugin);
		RANKED_REPOSITORY = new RankedRepository(connection);
	}
}
