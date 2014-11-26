package uk.ac.cam.rds46.fjava.tick5;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import uk.ac.cam.cl.fjava.messages.RelayMessage;

public class CorrectDatabase {

	private Connection connection;

	public CorrectDatabase(String databasePath) throws SQLException {
		try {

			// Establish connection:
			{
				Class.forName("org.hsqldb.jdbcDriver");
				connection = DriverManager.getConnection("jdbc:hsqldb:file:"
						+ databasePath, "SA", "");

				Statement delayStmt = connection.createStatement();
				try {
					delayStmt.execute("SET WRITE_DELAY FALSE");
				} 
				// Always update data on disk:
				finally {
					delayStmt.close();
				}
			}

			// Reset autocommit:
			connection.setAutoCommit(false);

			// Create new messages table:
			{
				Statement sqlStmt = connection.createStatement();
				try {
					sqlStmt.execute("CREATE TABLE messages(nick VARCHAR(255) NOT NULL,"
							+ "message VARCHAR(4096) NOT NULL,timeposted BIGINT NOT NULL)");
				} catch (SQLException e) {
					System.out.println("Warning: Database table \"messages\" already exists.");
				} finally {
					sqlStmt.close();
				}
			}

			// Create new stats table:
			{
				Statement sqlStmt = connection.createStatement();
				try {
					sqlStmt.execute("CREATE TABLE statistics(key VARCHAR(255),value INT)");
					String stmt1 = "INSERT INTO statistics(key,value) VALUES ('Total messages',0)";
					PreparedStatement insertMessage1 = connection
							.prepareStatement(stmt1);
					String stmt2 = "INSERT INTO statistics(key,value) VALUES ('Total logins',0)";
					PreparedStatement insertMessage2 = connection
							.prepareStatement(stmt2);
					try {
						insertMessage1.executeUpdate();
						insertMessage2.executeUpdate();
					} finally {
						insertMessage1.close();
						insertMessage2.close();
					}
				} catch (SQLException e) {
					System.out
							.println("Warning: Database table \"statistics\" already exists.");
				} finally {
					sqlStmt.close();
				}
			}

			// Commit:
			connection.commit();

		} catch (ArrayIndexOutOfBoundsException e) {
			System.err
					.println("Usage: java uk.ac.cam.crsid.fjava.tick5.Database <database name>");
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void close() throws SQLException {
		connection.close();
	}

	public void incrementLogins() throws SQLException {
		String stmt = "UPDATE statistics SET value = value+1 WHERE key='Total logins'";
		PreparedStatement insertMessage = connection.prepareStatement(stmt);
		try {
			insertMessage.executeUpdate();
		} finally {
			insertMessage.close();
		}
		connection.commit();
	}

	public void addMessage(RelayMessage m) throws SQLException {

		// Insert message:
		String stmt = "INSERT INTO MESSAGES(nick,message,timeposted) VALUES (?,?,?)";
		PreparedStatement insertMessage = connection.prepareStatement(stmt);
		try {
			insertMessage.setString(1, m.getFrom());
			insertMessage.setString(2, m.getMessage());
			insertMessage.setLong(3, System.currentTimeMillis());
			insertMessage.executeUpdate();
		} finally {
			insertMessage.close();
		}

		// Update message:
		String stmt2 = "UPDATE statistics SET value = value+1 WHERE key='Total messages'";
		PreparedStatement updateMessage = connection.prepareStatement(stmt2);
		try {
			updateMessage.executeUpdate();
		} finally {
			updateMessage.close();
		}
		connection.commit();
	}

	public List<RelayMessage> getRecent() throws SQLException {
		String stmt = "SELECT nick,message,timeposted FROM messages "
				+ "ORDER BY timeposted DESC LIMIT 10";
		PreparedStatement recentMessages = connection.prepareStatement(stmt);
		try {
			ResultSet rs = recentMessages.executeQuery();
			try {
				List<RelayMessage> list = new ArrayList<RelayMessage>(10);
				while (rs.next()) {
					list.add(new RelayMessage(rs.getString(1), rs.getString(2),
							new Date(rs.getLong(3))));
					System.out.println("message retreived: " + rs.getString(2));
				}
				return list;
			} finally {
				rs.close();
			}
		} finally {
			recentMessages.close();
		}
	}

	public static void main(String[] args) {
		try {
			
			// Establish connection:
			Class.forName("org.hsqldb.jdbcDriver");
			Connection connection = DriverManager
					.getConnection("jdbc:hsqldb:file:"
							+ args[0], "SA", "");

			Statement delayStmt = connection.createStatement();
			try {
				delayStmt.execute("SET WRITE_DELAY FALSE");
			} 
			// Always update data on disk
			finally {
				delayStmt.close();
			}

			// Reset autocommit:
			connection.setAutoCommit(false);

			// Create new messages table:
			Statement sqlStmt = connection.createStatement();
			try {
				sqlStmt.execute("CREATE TABLE messages(nick VARCHAR(255) NOT NULL,"
						+ "message VARCHAR(4096) NOT NULL,timeposted BIGINT NOT NULL)");
			} catch (SQLException e) {
				System.out
						.println("Warning: Database table \"messages\" already exists.");
			} finally {
				sqlStmt.close();
			}

			// Insert row in table:
			String stmt = "INSERT INTO MESSAGES(nick,message,timeposted) VALUES (?,?,?)";
			PreparedStatement insertMessage = connection.prepareStatement(stmt);
			try {
				insertMessage.setString(1, "Alastair"); // set value of first "?" to "Alastair"
				insertMessage.setString(2, "Hello, Andy");
				insertMessage.setLong(3, System.currentTimeMillis());
				insertMessage.executeUpdate();
			} finally { // Notice use of finally clause here to finish statement
				insertMessage.close();
			}

			// Commit
			connection.commit();

			// Print SQL request:
			stmt = "SELECT nick,message,timeposted FROM messages "
					+ "ORDER BY timeposted DESC LIMIT 10";
			PreparedStatement recentMessages = connection
					.prepareStatement(stmt);
			try {
				ResultSet rs = recentMessages.executeQuery();
				try {
					while (rs.next())
						System.out.println(rs.getString(1) + ": "
								+ rs.getString(2) + " [" + rs.getLong(3) + "]");
				} finally {
					rs.close();
				}
			} finally {
				recentMessages.close();
			}
		} catch (SQLException | ClassNotFoundException e) {
			System.err.println(e.getMessage());
		} catch (ArrayIndexOutOfBoundsException a) {
			System.err.println("Insufficient arguments");
		}
	}
}