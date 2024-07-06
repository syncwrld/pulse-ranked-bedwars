package com.github.syncwrld.prankedbw.bw4sbot.database.impl;

import com.github.syncwrld.prankedbw.bw4sbot.model.data.AccountData;
import com.github.syncwrld.prankedbw.bw4sbot.model.data.PlayerAccount;
import me.syncwrld.booter.database.DatabaseHelper;
import me.syncwrld.booter.database.IdentifiableRepository;
import me.syncwrld.booter.database.TableComponent;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
		 * UUID PK - minecraftUuid (uuid do minecraft)
		 * VARCHAR(16) PK - minecraftNickname (user do minecraft)
		 * STRING - discordId (id do discord)
		 * STRING - discordUsername (usuário do discord)
		 * BIGINT - eloPoints (pontos de elo)
		 */
		this.createTable(
			connection,
			this.getName(),
			new TableComponent(TableComponent.Type.UUID, "minecraftUuid", true),
			new TableComponent(TableComponent.Type.VARCHAR_16, "minecraftNickname", false),
			new TableComponent(TableComponent.Type.STRING, "discordId", false),
			new TableComponent(TableComponent.Type.STRING, "discordUsername", false),
			new TableComponent(TableComponent.Type.BIGINT, "eloPoints", false)
		);
	}
	
	public String findDiscordUsername(String minecraftName) {
		/*
		 * SELECT discordUsername FROM 4s_ranked WHERE minecraftNickname = minecraftName
		 */
		return this.get(
			this.getName(),
			connection,
			"discordUsername",
			"minecraftNickname",
			minecraftName,
			String.class
		);
	}
	
	public String findMinecraftUsername(String discordUsername) {
		/*
		 * SELECT minecraftNickname FROM 4s_ranked WHERE discordUsername = discordUsername
		 */
		return this.get(
			this.getName(),
			connection,
			"minecraftNickname",
			"discordUsername",
			discordUsername,
			String.class
		);
	}
	
	public void updateDiscordUsername(String minecraftName, String discordUsername) {
		/*
		 * UPDATE 4s_ranked SET discordUsername = discordUsername WHERE minecraftNickname = minecraftName
		 */
		
		String query = "UPDATE " + this.getName() + " SET discordUsername = ? WHERE minecraftNickname = ?";
		
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
		 * UPDATE 4s_ranked SET minecraftNickname = minecraftName WHERE discordUsername = discordUsername
		 */
		
		String query = "UPDATE " + this.getName() + " SET minecraftNickname = ? WHERE discordUsername = ?";
		
		try (PreparedStatement statement = prepare(connection, query)) {
			statement.setString(1, minecraftName);
			statement.setString(2, discordUsername);
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void setMinecraftNameChanges(String oldMcNickname, String newMcNickname) {
		/*
		 * UPDATE 4s_ranked SET minecraftNickname = newMcNickname WHERE minecraftNickname = oldMcNickname
		 */
		
		String query = "UPDATE " + this.getName() + " SET minecraftNickname = ? WHERE minecraftNickname = ?";
		
		try (PreparedStatement statement = prepare(connection, query)) {
			statement.setString(1, newMcNickname);
			statement.setString(2, oldMcNickname);
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public AccountData findAccountData(String minecraftName) {
		/*
		 * SELECT * FROM 4s_ranked WHERE minecraftNickname = minecraftName
		 */
		
		String query = "SELECT * FROM " + this.getName() + " WHERE minecraftNickname = ?";
		
		try (PreparedStatement statement = prepare(connection, query)) {
			statement.setString(1, minecraftName);
			
			try (ResultSet result = statement.executeQuery()) {
				if (!result.next()) {
					return AccountData.createNull();
				}
				
				return AccountData.create(
					result.getString("discordUsername"),
					result.getInt("elo_points")
				);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public PlayerAccount getOrCreateAccount(Player player) {
		/*
		 * SELECT * FROM 4s_ranked WHERE minecraftUuid = playerUuid
		 */
		
		String query = "SELECT * FROM " + this.getName() + " WHERE minecraftUuid = ?";
		
		try (PreparedStatement statement = prepare(connection, query)) {
			statement.setString(1, player.getUniqueId().toString());
			
			try (ResultSet result = statement.executeQuery()) {
				if (!result.next()) {
					return PlayerAccount.createEmpty(player);
				}
				
				return new PlayerAccount(
					UUID.fromString(result.getString("minecraftUuid")),
					result.getString("minecraftNickname"),
					result.getString("discordId"),
					result.getString("discordUsername"),
					result.getInt("eloPoints"));
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Set<PlayerAccount> getAllAccounts() {
		/*
		 * SELECT * FROM 4s_ranked
		 */
		
		String query = "SELECT * FROM " + this.getName();
		
		try (PreparedStatement statement = prepare(connection, query)) {
			try (ResultSet result = statement.executeQuery()) {
				Set<PlayerAccount> accounts = new HashSet<>();
				
				while (result.next()) {
					accounts.add(new PlayerAccount(
						UUID.fromString(result.getString("minecraftUuid")),
						result.getString("minecraftNickname"),
						result.getString("discordId"),
						result.getString("discordUsername"),
						result.getInt("eloPoints")));
				}
				
				return accounts;
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void updateAccount(PlayerAccount account) {
		/*
		* REPLACE INTO 4s_ranked (minecraftNickname, discordUsername, properties) VALUES (?, ?, ?)
		 */
		
		String query = "REPLACE INTO " + this.getName() + " (minecraftUuid, minecraftNickname, discordId, discordUsername, eloPoints) VALUES (?, ?, ?, ?, ?)";
		
		try (PreparedStatement statement = prepare(connection, query)) {
			statement.setObject(1, account.getMinecraftUuid());
			statement.setString(2, account.getMinecraftName());
			statement.setString(3, account.getDiscordId());
			statement.setString(4, account.getDiscordUsername());
			statement.setInt(5, account.getEloPoints());
			statement.executeUpdate();
 		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public Map<String, Integer> getTableIDs() {
		return Map.of(this.getName(), 0);
	}
}
