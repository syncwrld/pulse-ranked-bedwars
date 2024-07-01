package com.github.syncwrld.prankedbw.discordbot.shared.database.impl;

import com.github.syncwrld.prankedbw.discordbot.shared.SharedConstants;
import com.github.syncwrld.prankedbw.discordbot.shared.model.PlayerAccount;
import com.github.syncwrld.prankedbw.discordbot.shared.model.data.PlayerProperties;
import me.syncwrld.booter.database.DatabaseHelper;
import me.syncwrld.booter.database.IdentifiableRepository;
import me.syncwrld.booter.database.TableComponent;
import me.syncwrld.booter.database.connector.sample.DatabaseType;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

public class RankedRepository implements IdentifiableRepository, DatabaseHelper {
	private final Connection connection;
	private final DatabaseType databaseType;
	
	public RankedRepository(Connection connection, DatabaseType databaseType) {
		this.connection = connection;
		this.databaseType = databaseType;
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
		this.createTable(this.connection, this.getName(), new TableComponent(TableComponent.Type.VARCHAR_16, "username", true), new TableComponent(TableComponent.Type.STRING, "discord_id"), new TableComponent(TableComponent.Type.JSON, "properties"));
	}
	
	public PlayerAccount createAccount(Player player) {
		PlayerProperties properties = new PlayerProperties(0, false, "");
		
		String query = this.databaseType == DatabaseType.MYSQL ? "insert ignore into " + this.getName() + " values (?, ?, ?)" : "insert or ignore into " + this.getName() + " values (?, ?, ?)";
		
		try (PreparedStatement statement = this.prepare(this.connection, query)) {
			statement.setString(1, player.getName());
			statement.setString(2, SharedConstants.GSON.toJson(properties));
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
		return new PlayerAccount(player.getName(), null, properties);
	}
	
	public PlayerAccount findByMCNickname(String username) {
		String query = "select * from " + this.getName() + " where username = ?";
		
		try (PreparedStatement statement = this.prepare(this.connection, query)) {
			statement.setString(1, username);
			
			try (ResultSet resultSet = result(statement)) {
				if (resultSet.next()) {
					return new PlayerAccount(username, resultSet.getString("discord_id"), SharedConstants.GSON.fromJson(resultSet.getString("properties"), PlayerProperties.class));
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
		return null;
	}
	
	public PlayerAccount findByDiscordId(String discordId) {
		String query = "select * from " + this.getName() + " where discord_id = ?";
		
		try (PreparedStatement statement = this.prepare(this.connection, query)) {
			statement.setString(1, this.getName());
			statement.setString(2, discordId);
			
			try (ResultSet resultSet = result(statement)) {
				if (resultSet.next()) {
					return new PlayerAccount(resultSet.getString("username"), discordId, SharedConstants.GSON.fromJson(resultSet.getString("properties"), PlayerProperties.class));
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
		return null;
	}
	
	public void updateAccount(PlayerAccount account) {
		String query = "replace into " + this.getName() + " values (?, ?, ?)";
		
		try (PreparedStatement statement = this.prepare(this.connection, query)) {
			;
			statement.setString(1, account.getUsername());
			statement.setString(2, account.getDiscordId());
			statement.setString(3, SharedConstants.GSON.toJson(account.getProperties()));
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void deleteAccount(String username) {
		String query = "delete from " + this.getName() + " where username = ?";
		
		try (PreparedStatement statement = this.prepare(this.connection, query)) {
			statement.setString(1, username);
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void bindDiscord(String discordId, String username) {
		String query = "replace into " + this.getName() + " values (?, ?, ?)";
		
		PlayerAccount playerAccount = findByMCNickname(username);
		PlayerProperties properties = playerAccount.getProperties();
		properties.setDiscordSynchronized(true);
		properties.setDiscordUsername("waitingSynchronization");
		
		try (PreparedStatement statement = this.prepare(this.connection, query)) {
			statement.setString(1, username);
			statement.setString(2, discordId);
			
			statement.setString(3, SharedConstants.GSON.toJson(properties));
			
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public HashSet<PlayerAccount> fetchAll() {
		String query = "select * from " + this.getName();
		
		try (PreparedStatement statement = this.prepare(this.connection, query)) {
			try (ResultSet resultSet = result(statement)) {
				HashSet<PlayerAccount> accounts = new HashSet<>(resultSet.getFetchSize());
				
				while (resultSet.next()) {
					accounts.add(new PlayerAccount(resultSet.getString("username"), resultSet.getString("discord_id"), SharedConstants.GSON.fromJson(resultSet.getString("properties"), PlayerProperties.class)));
				}
				return accounts;
			}
		} catch (SQLException | NullPointerException e) {
			return new HashSet<>(0);
		}
	}
	
	@Override
	public Map<String, Integer> getTableIDs() {
		return Collections.emptyMap();
	}
}
