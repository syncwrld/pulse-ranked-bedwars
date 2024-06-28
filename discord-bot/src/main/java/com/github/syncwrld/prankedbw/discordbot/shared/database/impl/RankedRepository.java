package com.github.syncwrld.prankedbw.discordbot.shared.database.impl;

import com.github.syncwrld.prankedbw.discordbot.shared.SharedConstants;
import com.github.syncwrld.prankedbw.discordbot.shared.model.PlayerAccount;
import com.github.syncwrld.prankedbw.discordbot.shared.model.data.PlayerProperties;
import me.syncwrld.booter.database.DatabaseHelper;
import me.syncwrld.booter.database.IdentifiableRepository;
import me.syncwrld.booter.database.TableComponent;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

public class RankedRepository implements IdentifiableRepository, DatabaseHelper {
	private final Connection connection;
	
	public RankedRepository(Connection connection) {
		this.connection = connection;
	}
	
	@Override
	public String getName() {
		return "pranked_4s_users";
	}
	
	@Override
	public boolean hasMoreThanOneTable() {
		return false;
	}
	
	@Override
	public void createTables() {
		this.createTable(this.connection, this.getName(), new TableComponent(TableComponent.Type.VARCHAR_16, "username", true), new TableComponent(TableComponent.Type.JSON, "properties"));
	}
	
	public PlayerAccount createAccount(Player player) {
		PlayerProperties properties = new PlayerProperties(0, false, null);
		
		this.insert(
			this.getName(),
			this.connection,
			player.getName(),
			SharedConstants.GSON.toJson(properties)
		);
		
		return new PlayerAccount(player.getName(), properties);
	}
	
	public PlayerAccount getAccount(String username) {
		String query = "select * from ? where username = ?";
		
		try (PreparedStatement statement = this.prepare(this.connection, query)) {
			statement.setString(1, this.getName());
			statement.setString(2, username);
			
			try (ResultSet resultSet = result(statement)) {
				if (resultSet.next()) {
					return new PlayerAccount(username, SharedConstants.GSON.fromJson(resultSet.getString("properties"), PlayerProperties.class));
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
		return null;
	}
	
	public void updateAccount(PlayerAccount account) {
		String query = "replace into ? values (?, ?)";
		
		try (PreparedStatement statement = this.prepare(this.connection, query)) {
			statement.setString(1, this.getName());
			statement.setString(2, account.getUsername());
			statement.setString(3, SharedConstants.GSON.toJson(account.getProperties()));
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void deleteAccount(String username) {
		String query = "delete from ? where username = ?";
		
		try (PreparedStatement statement = this.prepare(this.connection, query)) {
			statement.setString(1, this.getName());
			statement.setString(2, username);
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public Map<String, Integer> getTableIDs() {
		return Collections.emptyMap();
	}
}
