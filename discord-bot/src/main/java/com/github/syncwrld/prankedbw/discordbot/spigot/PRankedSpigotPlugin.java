package com.github.syncwrld.prankedbw.discordbot.spigot;

import com.github.syncwrld.prankedbw.discordbot.discord.PRankedRobotBootstrapper;
import lombok.AccessLevel;
import lombok.Getter;
import me.syncwrld.booter.minecraft.loader.BukkitPlugin;

@Getter(AccessLevel.PUBLIC)
public class PRankedSpigotPlugin extends BukkitPlugin {
	
	private final PRankedRobotBootstrapper bootstrapper = new PRankedRobotBootstrapper(this);
	
	@Override
	protected void whenLoad() {
		this.setPrefix("§d[PRanked- 4S BOT]");
		this.setConfigurationAsDefault(true);
		
		this.saveDefaultConfig();
	}
	
	@Override
	protected void whenEnable() {
		this.bootstrapper.enable();
	}
	
	@Override
	protected void whenDisable() {
		this.bootstrapper.disable();
	}
}
