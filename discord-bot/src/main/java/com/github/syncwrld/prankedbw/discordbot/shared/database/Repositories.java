package com.github.syncwrld.prankedbw.discordbot.shared.database;

import com.github.syncwrld.prankedbw.discordbot.shared.database.impl.RankedRepository;
import com.github.syncwrld.prankedbw.discordbot.spigot.PRankedSpigotPlugin;
import me.syncwrld.booter.database.connector.sample.DatabaseType;
import me.syncwrld.booter.minecraft.tool.Pair;

import java.sql.Connection;

public class Repositories {
	public static RankedRepository RANKED_REPOSITORY;
	
	public static void setup(PRankedSpigotPlugin plugin) {
		Pair<Connection, DatabaseType> connResult = DatabaseHandler.createAndConnect(plugin);
		Connection connection = connResult.getKey();
		
		RANKED_REPOSITORY = new RankedRepository(connection, connResult.getValue());
		RANKED_REPOSITORY.createTables();
	}
}
