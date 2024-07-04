package com.github.syncwrld.prankedbw.bw4sbot.database;

import com.github.syncwrld.prankedbw.bw4sbot.PRankedSpigotPlugin;
import com.github.syncwrld.prankedbw.bw4sbot.database.impl.RankedRepository;

public class Repositories {
	public static RankedRepository RANKED;
	
	/*
	Criando conexão com o banco de dados
	 */
	public static void setup(PRankedSpigotPlugin plugin) {
		RANKED = new RankedRepository((new SQLConnector().connect(plugin)));
		RANKED.createTables();
	}
}
