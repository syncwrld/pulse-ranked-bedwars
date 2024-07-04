package com.github.syncwrld.prankedbw.bw4sbot;

import com.github.syncwrld.prankedbw.bw4sbot.database.Repositories;
import com.github.syncwrld.prankedbw.bw4sbot.event.bukkit.ConnectionTrafficListener;
import com.github.syncwrld.prankedbw.bw4sbot.event.javacord.BindCommandListener;
import com.github.syncwrld.prankedbw.bw4sbot.model.config.RobotConfiguration;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.Getter;
import me.syncwrld.booter.ApplicationBootstrapper;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandOption;

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
		Conectando ao banco de dados
		 */
		Repositories.setup(this.plugin);
		
		/*
		Criando a configuração do bot
		 */
		this.configuration = RobotConfiguration.of(this.plugin.getConfiguration());
		
		/*
		Iniciando o bot do Discord
		 */
		String botToken = this.plugin.getConfiguration().getString("discord-bot-token");
		
		if (Strings.isNullOrEmpty(botToken)) {
			this.plugin.log("&eDISCORD! Falta o token do bot no arquivo de configuração. Desabilitando.");
			this.disablePlugin();
			return;
		}
		
		/*
		Registrando eventos (Bukkit)
		 */
		this.plugin.registerListener(new ConnectionTrafficListener(this.plugin));
		
		this.api = new DiscordApiBuilder()
			.setAllIntents()
			.setToken(botToken)
			.login().join();
		
		/*
		Criando o comando /bind no Discord
		 */
		SlashCommand bindCommand = SlashCommand.with(Configuration.BIND_COMMAND, Configuration.BIND_COMMAND_DESCRIPTION)
			.addOption(SlashCommandOption.createStringOption("Nickname", "Seu nickname do Minecraft", true, false))
			.createGlobal(this.api).join();
		this.api.addSlashCommandCreateListener(new BindCommandListener(this.plugin));
	}
	
	@Override
	public void disable() {
	}
	
	public void disablePlugin() {
		try {
			this.plugin.getPluginLoader().disablePlugin(this.plugin);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
