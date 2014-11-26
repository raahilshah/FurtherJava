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


public class Database {

	private Connection connection;

	public Database(String path) throws SQLException {
		// Connect to the database:
		try {
			Class.forName("org.hsqldb.jdbcDriver");
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
			return;
		}

		connection = DriverManager.getConnection("jdbc:hsqldb:file:"
				+ path,"SA","");

		Statement delayStmt = connection.createStatement();
		try {delayStmt.execute("SET WRITE_DELAY FALSE");}  //Always update data on disk
		finally {delayStmt.close();}

		// Turn on transaction support:
		connection.setAutoCommit(false);

		// Create messages table:
		Statement sqlStmt = connection.createStatement();
		try {
			sqlStmt.execute("CREATE TABLE messages(nick VARCHAR(255) NOT NULL,"+
					"message VARCHAR(4096) NOT NULL,timeposted BIGINT NOT NULL)");
		} catch (SQLException e) {
			System.out.println("Warning: Database table \"messages\" already exists.");
		} finally {
			sqlStmt.close();
		}

		// Create s table:
		Statement sqlStmt2 = connection.createStatement();
		try {
			sqlStmt2.execute("CREATE TABLE statistics(key VARCHAR(255),value INT)");
			PreparedStatement insertMessage1 = 
					connection.prepareStatement("INSERT INTO statistics(key,value) VALUES ('Total messages',0)"),
					insertMessage2 = 
					connection.prepareStatement("INSERT INTO statistics(key,value) VALUES ('Total logins',0)");
			try {
				insertMessage1.executeUpdate();
				insertMessage2.executeUpdate();
			} finally {
				insertMessage1.close();
				insertMessage2.close();
			}
		} catch (SQLException e) {
			System.out.println("Warning: Database table \"statistics\" already exists.");
		} finally {
			sqlStmt.close();
		}

		// Commit:
		connection.commit();
	}

	public void close() throws SQLException {
		connection.close();
	}

	public void incrementLogins() throws SQLException {
		PreparedStatement insertMessage = 
				connection.prepareStatement("UPDATE statistics SET value = value+1 WHERE key='Total logins'");
		try {
			insertMessage.executeUpdate();
		} finally {
			insertMessage.close();
		}
		connection.commit();
	}

	public void addMessage(RelayMessage m) throws SQLException {
		// Add RelayMessage to messages table:
		PreparedStatement insertMessage = 
				connection.prepareStatement("INSERT INTO MESSAGES(nick,message,timeposted) VALUES (?,?,?)");
		try {
			insertMessage.setString(1, m.getFrom());
			insertMessage.setString(2, m.getMessage());
			insertMessage.setLong(3, System.currentTimeMillis());
			insertMessage.executeUpdate();
		} finally {
			insertMessage.close();
		}

		// Update statistics table:
		PreparedStatement updateMessage = 
				connection.prepareStatement("UPDATE statistics SET value = value+1 WHERE key='Total messages'");
		try {
			updateMessage.executeUpdate();
		} finally {
			updateMessage.close();
		}
		connection.commit();
		
		// Commit:
		connection.commit();
	}
	
	public List<RelayMessage> getRecent() throws SQLException {
		PreparedStatement recentMessages = 
				connection.prepareStatement("SELECT nick,message,timeposted FROM messages "
				+ "ORDER BY timeposted DESC LIMIT 10");
		try {
			ResultSet rs = recentMessages.executeQuery();
			try {
				List<RelayMessage> list = new ArrayList<RelayMessage>(10);
				while (rs.next()) {
					list.add(new RelayMessage(rs.getString(1), rs.getString(2),
							new Date(rs.getLong(3))));
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
		// Testing
		// args = new String[] {"/Users/Raahil/Desktop/chat-database"};

		try {
			// Connect to the database:
			Class.forName("org.hsqldb.jdbcDriver");
			Connection connection = DriverManager.getConnection("jdbc:hsqldb:file:"
					+ args[0],"SA","");

			Statement delayStmt = connection.createStatement();
			try {delayStmt.execute("SET WRITE_DELAY FALSE");}  //Always update data on disk
			finally {delayStmt.close();}

			// Turn on transaction support:
			connection.setAutoCommit(false);

			// Create messages table:
			Statement sqlStmt = connection.createStatement();
			try {
				sqlStmt.execute("CREATE TABLE messages(nick VARCHAR(255) NOT NULL,"+
						"message VARCHAR(4096) NOT NULL,timeposted BIGINT NOT NULL)");
			} catch (SQLException e) {
				System.out.println("Warning: Database table \"messages\" already exists.");
			} finally {
				sqlStmt.close();
			}


			// Add a row to the messages table:
			String stmt = "INSERT INTO MESSAGES(nick,message,timeposted) VALUES (?,?,?)";
			PreparedStatement insertMessage = connection.prepareStatement(stmt);
			try {
				insertMessage.setString(1, "Alastair"); //set value of first "?" to "Alastair"
				insertMessage.setString(2, "Hello, Andy");
				insertMessage.setLong(3, System.currentTimeMillis());
				insertMessage.executeUpdate();
			} finally { //Notice use of finally clause here to finish statement
				insertMessage.close();
			}

			// Commit isolated changes:
			connection.commit();

			// Query the database:
			stmt = "SELECT nick,message,timeposted FROM messages "+
					"ORDER BY timeposted DESC LIMIT 10";
			PreparedStatement recentMessages = connection.prepareStatement(stmt);
			try {
				ResultSet rs = recentMessages.executeQuery();
				try {
					while (rs.next())
						System.out.println(rs.getString(1)+": "+rs.getString(2)+
								" ["+rs.getLong(3)+"]");
				} finally {
					rs.close();
				}
			} finally {
				recentMessages.close();
			}

			// Close all database connections:
			connection.close();

		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println("Usage: java uk.ac.cam.crsid.fjava.tick5.Database <database name>");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

}