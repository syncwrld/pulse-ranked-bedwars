package com.github.syncwrld.prankedbw.discordbot.spigot;

import com.github.syncwrld.prankedbw.discordbot.discord.PRankedRobotBootstrapper;
import com.github.syncwrld.prankedbw.discordbot.spigot.event.AuthCodeInputListener;
import com.github.syncwrld.prankedbw.discordbot.spigot.event.ConnectionTrafficListener;
import lombok.AccessLevel;
import lombok.Getter;
import me.syncwrld.booter.minecraft.loader.BukkitPlugin;

@Getter(AccessLevel.PUBLIC)
public class PRankedSpigotPlugin extends BukkitPlugin {
	
	private PRankedRobotBootstrapper bootstrapper;
	
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
			return;
		}
		
		this.bootstrapper = new PRankedRobotBootstrapper(this);
		this.bootstrapper.enable();
		
		this.registerListeners(new AuthCodeInputListener(), new ConnectionTrafficListener());
	}
	
	@Override
	protected void whenDisable() {
		this.bootstrapper.disable();
	}
	
	@Override
	public void log(String message) {
		this.getLogger().info(message.replace("&", "§"));
	}
}
