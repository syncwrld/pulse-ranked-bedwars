package com.github.syncwrld.prankedbw.bw4sbot.database;

import com.github.syncwrld.prankedbw.bw4sbot.PRankedSpigotPlugin;
import me.syncwrld.booter.database.connector.SimpleBukkitDatabaseConnector;
import me.syncwrld.booter.database.connector.SimpleDatabaseConnector;
import me.syncwrld.booter.database.connector.sample.DatabaseType;

import java.io.File;
import java.sql.Connection;
import java.util.function.Function;

public class SQLConnector {
	
	/*
	Conectando ao banco usando um implementador do BukkitPlugin
	 */
	public Connection connect(PRankedSpigotPlugin engine) {
		DatabaseType databaseType = getDatabaseType(engine.getConfiguration().getString("database.type"));
		SimpleDatabaseConnector connector;
		
		switch (databaseType) {
			case MYSQL_HIKARICP:
			case MYSQL:
				connector = SimpleBukkitDatabaseConnector.construct(engine, engine.getConfigOf("configuration.yml"));
				break;
			case SQLITE:
				File sqliteFile = new File(engine.getDataFolder(), engine.getConfiguration().getString("database.sqlite-file"));
				connector = new SimpleDatabaseConnector(databaseType, sqliteFile);
				break;
			default:
				throw new IllegalArgumentException("Invalid database type: " + databaseType);
		}
		
		if (!connector.connect()) {
			engine.log("&eDATABASE! &4Falha ao conectar ao banco de dados.");
			return null;
		}
		
		engine.log("&eDATABASE! &aConectado ao banco de dados. (" + databaseType.getName() + ")");
		return connector.getConnection();
	}
	
	/*
	Pegando o tipo de banco de dados a partir do input de uma string
	 */
	private DatabaseType getDatabaseType(String type) {
		switch (type.toUpperCase()) {
			case "MYSQL":
				return DatabaseType.MYSQL;
			case "MYSQL-HIKARI":
				return DatabaseType.MYSQL_HIKARICP;
			case "SQLITE":
				return DatabaseType.SQLITE;
			default:
				throw new IllegalArgumentException("Invalid database type: " + type);
		}
	}
}
