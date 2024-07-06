package com.github.syncwrld.prankedbw.bw4sbot.hook;

import com.github.syncwrld.prankedbw.bw4sbot.PRankedSpigotPlugin;
import com.tomkeuper.bedwars.api.BedWars;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class BedwarsHook {
	
	private final PRankedSpigotPlugin plugin;
	private BedWars api;
	
	public BedwarsHook(PRankedSpigotPlugin plugin) {
		this.plugin = plugin;
		setup();
	}
	
	public void setup() {
		Plugin bedwars2023 = Bukkit.getPluginManager().getPlugin("BedWars2023");
		
		if (bedwars2023 != null) {
			RegisteredServiceProvider<BedWars> registration = Bukkit.getServicesManager().getRegistration(BedWars.class);
			if (registration != null) {
				this.api = registration.getProvider();
			} else {
				plugin.log("BedWars1058 está instalado porém a BedWars API não está registrada.");
			}
			return;
		}
		
		plugin.log("BedWars1058 não está instalado.");
	}
	
	public BedWars getApi() {
		return api;
	}
}
