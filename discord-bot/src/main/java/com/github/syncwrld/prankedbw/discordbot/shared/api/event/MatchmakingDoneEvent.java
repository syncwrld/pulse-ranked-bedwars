package com.github.syncwrld.prankedbw.discordbot.shared.api.event;

import com.github.syncwrld.prankedbw.discordbot.shared.model.PlayerAccount;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.user.User;

import java.util.HashMap;

@EqualsAndHashCode(callSuper = true)
@Data @AllArgsConstructor
public class MatchmakingDoneEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private final HashMap<User, PlayerAccount> players;
	private final HashMap<User, PlayerAccount> teamOne;
	private final HashMap<User, PlayerAccount> teamTwo;
	private final ServerVoiceChannel teamOneChannel;
	private final ServerVoiceChannel teamTwoChannel;
	private int matchId;
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
}
