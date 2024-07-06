package com.github.syncwrld.prankedbw.bw4sbot;

import com.github.syncwrld.prankedbw.bw4sbot.api.Ranked4SApi;
import com.github.syncwrld.prankedbw.bw4sbot.api.impl.Ranked4SApiImpl;
import com.github.syncwrld.prankedbw.bw4sbot.cache.Caches;
import com.github.syncwrld.prankedbw.bw4sbot.hook.BedwarsHook;
import com.github.syncwrld.prankedbw.bw4sbot.manager.GameManager;
import com.github.syncwrld.prankedbw.bw4sbot.model.config.PreferenceConfiguration;
import lombok.AccessLevel;
import lombok.Getter;
import me.syncwrld.booter.minecraft.loader.BukkitPlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;

@Getter(AccessLevel.PUBLIC)
public class PRankedSpigotPlugin extends BukkitPlugin {
	
	private final Caches caches = new Caches();
	private PreferenceConfiguration preferenceConfiguration;
	private PRankedJavacordRobot bootstrapper;
	private BedwarsHook bedwars;
	private GameManager gameManager;
	
	@Override
	protected void whenLoad() {
		this.setPrefix("§d[PRanked- 4S BOT]");
		this.setConfigurationAsDefault(true);
		
		this.saveDefaultConfig();
	}
	
	@Override
	protected void whenEnable() {
		try {
			Class.forName("com.github.syncwrld.packedjc.PackedJCPlugin");
		} catch (ClassNotFoundException e) {
			this.log(
				"O plugin 'PackedJC' não foi encontrado. Por favor, instale-o.",
				"Você pode encontrar o plugin no repositório do projeto.",
				"Se você já instalou, certifique-se de que ele está habilitado.",
				"O sistema inteiro depende dele, o bot será desabilitado, pois o funcionamento está comprometido."
			);
			
			this.getPluginLoader().disablePlugin(this);
			return;
		}
		
		try {
			Class.forName("com.tomkeuper.bedwars.api.BedWars");
		} catch (ClassNotFoundException e) {
			this.log("Cadê o 'BedWars2023'? O sistema precisa dele.");
			this.log("Instale-o e reinicie o servidor. O plugin não será carregado.");
			
			this.getPluginLoader().disablePlugin(this);
			return;
		}
		
		this.preferenceConfiguration = new PreferenceConfiguration(this.getConfiguration().getString("arena-group"));
		
		this.bedwars = new BedwarsHook(this);
		this.gameManager = new GameManager(this);
		
		this.registerListener(this.gameManager);
		
		this.bootstrapper = new PRankedJavacordRobot(this);
		this.bootstrapper.enable();
		
		this.registerApi();
	}
	
	private void registerApi() {
		Ranked4SApi api = new Ranked4SApiImpl(this);
		this.getServer().getServicesManager().register(
			Ranked4SApi.class,
			api,
			this,
			ServicePriority.Normal
		);
	}
	
	@Override
	protected void whenDisable() {
		this.caches.save();
		this.bootstrapper.disable();
	}
	
	@Override
	public void log(String message) {
		Bukkit.getConsoleSender().sendMessage("§b[Ranked4S-BOT] " + message.replace("&", "§"));
	}
}
