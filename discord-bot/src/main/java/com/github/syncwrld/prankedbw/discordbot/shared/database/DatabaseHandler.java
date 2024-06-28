package com.github.syncwrld.prankedbw.discordbot.shared.database;

import me.syncwrld.booter.database.connector.SimpleBukkitDatabaseConnector;
import me.syncwrld.booter.database.connector.SimpleDatabaseConnector;
import me.syncwrld.booter.database.connector.sample.DatabaseType;
import me.syncwrld.booter.minecraft.loader.BukkitPlugin;

import java.sql.Connection;

public class DatabaseHandler {
	public static DatabaseType getDatabaseType(String rawType) {
		switch (rawType.toUpperCase()) {
			case "MYSQL":
				return DatabaseType.MYSQL;
			case "MYSQL-HIKARI":
				return DatabaseType.MYSQL_HIKARICP;
			case "SQLITE":
				return DatabaseType.SQLITE;
			default:
				throw new IllegalArgumentException("Invalid database type: " + rawType);
		}
	}
	
	public static Connection getConnection(BukkitPlugin plugin) {
		SimpleDatabaseConnector databaseConnector = SimpleBukkitDatabaseConnector.construct(plugin, plugin.getConfigOf("configuration.yml"));
		if (!databaseConnector.connect()) {
			throw new RuntimeException("Failed to connect to database | plugin.name = " + plugin.getName());
		}
		return databaseConnector.getConnection();
	}
}