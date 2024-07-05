package com.github.syncwrld.prankedbw.bw4sbot.database.impl;

import com.github.syncwrld.prankedbw.bw4sbot.Constants;
import com.github.syncwrld.prankedbw.bw4sbot.model.data.AccountData;
import com.github.syncwrld.prankedbw.bw4sbot.model.data.PlayerAccount;
import me.syncwrld.booter.database.DatabaseHelper;
import me.syncwrld.booter.database.IdentifiableRepository;
import me.syncwrld.booter.database.TableComponent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class RankedRepository implements IdentifiableRepository, DatabaseHelper {
	private final Connection connection;
	
	public RankedRepository(Connection connection) {
		this.connection = connection;
	}
	
	@Override
	public String getName() {
		return "ranked4s";
	}
	
	@Override
	public boolean hasMoreThanOneTable() {
		return false;
	}
	
	@Override
	public void createTables() {
		/*
		Estrutura da tabela:
		 * VARCHAR(16) PK - minecraft_name (user do minecraft)
		 * STRING - username (usuário do discord)
		 * STRING (JSON) - properties (propriedades do jogador)
		 */
		this.createTable(
			connection,
			this.getName(),
			new TableComponent(TableComponent.Type.VARCHAR_16, "minecraft_name", true),
			new TableComponent(TableComponent.Type.STRING, "username", false),
			new TableComponent(TableComponent.Type.STRING, "account_data", true)
		);
	}
	
	public String findDiscordUsername(String minecraftName) {
		/*
		 * SELECT username FROM 4s_ranked WHERE minecraft_name = minecraftName
		 */
		return this.get(
			this.getName(),
			connection,
			"username",
			"minecraft_name",
			minecraftName,
			String.class
		);
	}
	
	public String findMinecraftUsername(String discordUsername) {
		/*
		 * SELECT minecraft_name FROM 4s_ranked WHERE username = discordUsername
		 */
		return this.get(
			this.getName(),
			connection,
			"minecraft_name",
			"username",
			discordUsername,
			String.class
		);
	}
	
	public void updateDiscordUsername(String minecraftName, String discordUsername) {
		/*
		 * UPDATE 4s_ranked SET username = discordUsername WHERE minecraft_name = minecraftName
		 */
		
		String query = "UPDATE " + this.getName() + " SET username = ? WHERE minecraft_name = ?";
		
		try (PreparedStatement statement = prepare(connection, query)) {
			statement.setString(1, discordUsername);
			statement.setString(2, minecraftName);
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void updateMinecraftUsername(String discordUsername, String minecraftName) {
		/*
		 * UPDATE 4s_ranked SET minecraft_name = minecraftName WHERE username = discordUsername
		 */
		
		String query = "UPDATE " + this.getName() + " SET minecraft_name = ? WHERE username = ?";
		
		try (PreparedStatement statement = prepare(connection, query)) {
			statement.setString(1, minecraftName);
			statement.setString(2, discordUsername);
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public AccountData findAccountData(String minecraftName) {
		/*
		 * SELECT * FROM 4s_ranked WHERE minecraft_name = minecraftName
		 */
		
		String query = "SELECT * FROM " + this.getName() + " WHERE minecraft_name = ?";
		
		try (PreparedStatement statement = prepare(connection, query)) {
			statement.setString(1, minecraftName);
			
			try (ResultSet result = statement.executeQuery()) {
				if (!result.next()) {
					return AccountData.createNull();
				}
				
				return AccountData.create(
					result.getString("username"),
					result.getInt("elo_points")
				);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public PlayerAccount getOrCreateAccount(String minecraftName) {
		/*
		 * SELECT * FROM 4s_ranked WHERE minecraft_name = minecraftName
		 */
		
		String query = "SELECT * FROM " + this.getName() + " WHERE minecraft_name = ?";
		
		try (PreparedStatement statement = prepare(connection, query)) {
			statement.setString(1, minecraftName);
			
			try (ResultSet result = statement.executeQuery()) {
				if (!result.next()) {
					return PlayerAccount.createEmpty(minecraftName);
				}
				
				return new PlayerAccount(
					result.getString("minecraft_name"),
					result.getString("username"),
					Constants.GSON.fromJson(result.getString("properties"), AccountData.class)
				);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public Map<String, Integer> getTableIDs() {
		return Map.of(this.getName(), 0);
	}
}
