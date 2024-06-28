package com.github.syncwrld.prankedbw.discordbot.discord;

import com.github.syncwrld.prankedbw.discordbot.discord.task.MatchAvailabilityLabor;
import com.github.syncwrld.prankedbw.discordbot.shared.cache.Caches;
import com.github.syncwrld.prankedbw.discordbot.spigot.PRankedSpigotPlugin;
import com.google.common.base.Stopwatch;
import lombok.AccessLevel;
import lombok.Getter;
import me.syncwrld.booter.ApplicationBootstrapper;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Getter(AccessLevel.PUBLIC)
public class PRankedRobotBootstrapper implements ApplicationBootstrapper, EventListener {
	
	private final PRankedSpigotPlugin plugin;
	private JDA jda;
	
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
			this.jda = JDABuilder.createDefault(token)
				.enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
				.build();
		} catch (Exception e) {
			this.plugin.log("§cNão foi possível iniciar o bot do Discord.");
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void disable() {
	
	}
	
	@Override
	public void onEvent(@NotNull GenericEvent event) {
		if (event instanceof ReadyEvent) {
			this.plugin.log("§aO bot ficou online com sucesso.");
			this.plugin.log("§7Iniciando carregamento dos sistemas...");
			
			Stopwatch stopwatch = Stopwatch.createStarted();
			this.loadSystems();
			this.plugin.log("§aCarregamento finalizado em " + stopwatch.stop() + ". (Em caso de dúvidas, utilize 'pr!help')");
		}
	}
	
	private void loadSystems() {
		/*
		Carregando algumas coisas em memória
		 */
		Caches.setup(this.plugin);
		
		/*
		Inicializando as tarefas constantes (trabalhos)
		 */
		createScheduler().scheduleAtFixedRate(new MatchAvailabilityLabor(this), 0, 1, TimeUnit.SECONDS);
		
		/*
		Registrando eventos
		 */
	}
	
	public ScheduledExecutorService createScheduler() {
		return Executors.newSingleThreadScheduledExecutor();
	}
	
	public ExecutorService createExecutorService() {
		return Executors.newCachedThreadPool();
	}
}
