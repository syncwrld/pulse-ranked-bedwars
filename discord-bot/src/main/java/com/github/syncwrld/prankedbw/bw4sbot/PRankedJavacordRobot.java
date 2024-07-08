package com.github.syncwrld.prankedbw.bw4sbot;

import com.github.syncwrld.prankedbw.bw4sbot.database.Repositories;
import com.github.syncwrld.prankedbw.bw4sbot.event.bukkit.ConnectionTrafficListener;
import com.github.syncwrld.prankedbw.bw4sbot.event.bukkit.TokenInputListener;
import com.github.syncwrld.prankedbw.bw4sbot.event.bukkit.game.MatchListener;
import com.github.syncwrld.prankedbw.bw4sbot.event.bukkit.game.TeamFormationListener;
import com.github.syncwrld.prankedbw.bw4sbot.event.javacord.BindCommandListener;
import com.github.syncwrld.prankedbw.bw4sbot.model.config.RobotConfiguration;
import com.github.syncwrld.prankedbw.bw4sbot.task.MatchAvailabilityLabor;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.Getter;
import me.syncwrld.booter.ApplicationBootstrapper;
import org.bukkit.Bukkit;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.user.UserStatus;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.util.logging.FallbackLoggerConfiguration;

@Getter(AccessLevel.PUBLIC)
public class PRankedJavacordRobot implements ApplicationBootstrapper {
	private final PRankedSpigotPlugin plugin;
	private DiscordApi api;
	private RobotConfiguration configuration;
	
	public PRankedJavacordRobot(PRankedSpigotPlugin plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void enable() {
		/*
		Desabilitando o debug e o tracer do logger do Javacord
		 */
		FallbackLoggerConfiguration.setDebug(false);
		FallbackLoggerConfiguration.setTrace(false);
		
		/*
		Conectando ao banco de dados e fazendo o setup do cache que armazena os dados na memória
		 */
		Repositories.setup(this.plugin);
		this.plugin.getCaches().setup(this.plugin);
		
		/*
		Criando a configuração do bot
		 */
		this.configuration = RobotConfiguration.of(this.plugin.getConfiguration());
		
		/*
		Iniciando o bot do Discord
		 */
		String botToken = this.plugin.getConfiguration().getString("discord-bot-token");
		
		if (Strings.isNullOrEmpty(botToken)) {
			this.plugin.log("&bDISCORD! Falta o token do bot no arquivo de configuração. Desabilitando.");
			this.disablePlugin();
			return;
		}
		
		this.plugin.log("&bDISCORD! &fBot iniciado com sucesso.");
		this.plugin.log("&bGERAL! &fCarregando sistemas...");
		
		/*
		Registrando eventos (Bukkit)
		 */
		this.plugin.registerListeners(
			new ConnectionTrafficListener(this.plugin),
			new TokenInputListener(this.plugin),
			new TeamFormationListener(this.plugin),
			new MatchListener(this.plugin)
		);
		
		this.api = new DiscordApiBuilder()
			.setAllIntents()
			.setToken(botToken)
			.login()
			.join();
		
		api.updateActivity(ActivityType.PLAYING, "Ranked 4S com outros jogadores!");
		Bukkit.getScheduler().runTaskTimer(this.plugin, new MatchAvailabilityLabor(this), 0, 80);
		
		/*
		Criando o comando /bind no Discord
		 */
		SlashCommand bindCommand = SlashCommand.with(Configuration.BIND_COMMAND, Configuration.BIND_COMMAND_DESCRIPTION).addOption(SlashCommandOption.createStringOption("Nickname", "Seu nickname do Minecraft", true, false)).createGlobal(this.api).join();
		this.api.addSlashCommandCreateListener(new BindCommandListener(this.plugin));
		
		this.plugin.log("&bGERAL! &fCarregamento concluido com sucesso.");
	}
	
	@Override
	public void disable() {
		if (this.api != null) {
			this.api.disconnect().join();
		}
	}
	
	public void disablePlugin() {
		try {
			this.plugin.getPluginLoader().disablePlugin(this.plugin);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
