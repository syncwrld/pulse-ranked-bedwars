package com.github.syncwrld.prankedbw.discordbot.discord;

import com.github.syncwrld.prankedbw.discordbot.discord.command.BindCommand;
import com.github.syncwrld.prankedbw.discordbot.discord.task.MatchAvailabilityLabor;
import com.github.syncwrld.prankedbw.discordbot.shared.cache.Caches;
import com.github.syncwrld.prankedbw.discordbot.shared.database.Repositories;
import com.github.syncwrld.prankedbw.discordbot.shared.mapping.labor.PlayerAuthMapperLabor;
import com.github.syncwrld.prankedbw.discordbot.spigot.PRankedSpigotPlugin;
import lombok.AccessLevel;
import lombok.Getter;
import me.syncwrld.booter.ApplicationBootstrapper;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.listener.GloballyAttachableListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Getter(AccessLevel.PUBLIC)
public class PRankedRobotBootstrapper implements ApplicationBootstrapper {
	
	private final PRankedSpigotPlugin plugin;
	private DiscordApi client;
	
	private String voiceChannelCategoryId;
	private String guildId;
	
	public PRankedRobotBootstrapper(PRankedSpigotPlugin plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void enable() {
		final String token = this.plugin.getConfiguration().getString("discord-bot-token");
		
		if (token == null || token.isEmpty()) {
			this.plugin.log("§7Defina o token do bot no arquivo de configuração. O plugin será desabilitado.");
			this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
			return;
		}
		
		try {
			this.client = new DiscordApiBuilder().setAllIntents().setToken(token).login().join();
			this.handleReady();
		} catch (Exception e) {
			this.plugin.log("§cNão foi possível iniciar o bot do Discord.");
			throw new RuntimeException(e);
		}
		
		this.guildId = this.plugin.getConfiguration().getString("guild-id");
		this.voiceChannelCategoryId = this.plugin.getConfiguration().getString("voice-channel-category-id");
	}
	
	@Override
	public void disable() {
	
	}
	
	public void handleReady() {
		User robotYourself = this.client.getYourself();
		
		this.plugin.log((
			"Logado com sucesso! (@" + robotYourself.getName() + ") | Em caso de dúvidas, utilize 'pr!help'"
		));
		
		this.loadSystems();
	}
	
	private void loadSystems() {
		/*
		Carregando algumas coisas em memória e conectando ao banco de dados
		 */
		Repositories.setup(this.plugin);
		Caches.setup(this.plugin);
		
		/*
		Inicializando as tarefas constantes (trabalhos)
		 */
		createScheduler().scheduleAtFixedRate(new MatchAvailabilityLabor(this), 0, 3, TimeUnit.SECONDS);
		createScheduler().scheduleAtFixedRate(new PlayerAuthMapperLabor(), 0, 3, TimeUnit.SECONDS);
		
		/*
		Registrando ouvintes de eventos
		 */
		this.registerListeners(new BindCommand());
	}
	
	public ScheduledExecutorService createScheduler() {
		return Executors.newSingleThreadScheduledExecutor();
	}
	
	public ExecutorService createExecutorService() {
		return Executors.newCachedThreadPool();
	}
	
	public void registerListeners(GloballyAttachableListener... listeners) {
		for (GloballyAttachableListener listener : listeners) {
			this.client.addListener(listener);
		}
	}
}
