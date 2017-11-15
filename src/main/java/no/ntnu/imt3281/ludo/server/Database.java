package no.ntnu.imt3281.ludo.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

/**
 * Database class that handles the interaction with the database
 * for storing userdata and chatlogs
 */
public class Database {
	private Connection con;
	
	/** The amount of colums in the usertable */
	private final static int USERCOLUMNS = 3;
	
	/** The amount of columns in the logtable */
	private final static int LOGCOLUMNS = 4;
	
	/** The 'url' to our database (local) */
	//private final static String url = "jdbc:derby:BadgerDB;create=true";
	
	
	/**
	 * Constructor that tries to create the needed tables
	 * in the database. 
	 */
	public Database(String url) throws SQLException {
		
		con = DriverManager.getConnection(url);


		Statement stmt = con.createStatement();
		
		//stmt.execute("DROP TABLE userdb");
		//stmt.execute("DROP TABLE chat");
		//stmt.execute("DROP TABLE UserToChat");
		//stmt.execute("DROP TABLE test");
		//stmt.execute("DROP TABLE test1");
		
		try {
			System.err.println("user");
			
			stmt.execute("CREATE TABLE userdb ("
					+ "id bigint NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
					+ "nickname varchar(20) NOT NULL,"
					+ "password varchar(128) NOT NULL,"
					+ "PRIMARY KEY (id))");
			
			System.err.println("User table created!");
		}
		catch (SQLException sqle) {
			System.err.println("Table excists!");
			//sqle.printStackTrace();
		}
		
		try {
			System.err.println("chat");
			// time: 13.11.2017 10:11
			stmt.execute( "CREATE TABLE chat ("
						+ "id bigint NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
						//+ "chatName varchar(10) NOT NULL,"
						+ "PRIMARY KEY (id))");
			
			System.err.println("Chat table created!");
		}	
		catch (SQLException sqle) {
			System.err.println("Chat already exists!");
			//sqle.printStackTrace();
		}
		
		try {
			System.err.println("message");
			stmt.execute("CREATE TABLE message ("
						+ "chatId bigint NOT NULL,"
						+ "userId bigint NOT NULL,"
						+ "time timestamp NOT NULL,"
						+ "message varchar(3000) NOT NULL,"
						+ "PRIMARY KEY (chatId, userId),"
						+ "FOREIGN KEY chatId REFERENCES chat(id),"
						+ "FOREIGN KEY userId REFERENCES userdb(id)");
			
			System.err.println("Message table created!");
		}
		catch (SQLException sqle) {
			System.err.println("Message already exitsts");
		}
		
		//con.close();
	}
	
	
	/**
	 * The main method just for testing
	 * @param args cmd-line args
	 */
	
	/*
	public static void main(String[] args) {
		Database db = new Database();
		
		db.addUser("Skjare", "123");
		
		//System.err.println(db.getUser(701));
		System.err.println(db.getUser("Skjare")[0]);
		System.err.println(db.getUser("Skjare")[1]);
		System.err.println(db.getUser("Skjare")[2]);
		db.close();
	}
	*/
	
	/**
	 * Tries to add a user to the userdb table
	 * @param nickname The nickname of the user
	 * @param password The password of the user
	 */
	public void addUser(String nickname, String password) {
		
		// TODO: sjekk om om bruker finnes fra før
		// evt implementere unike brukernavn
		
		// TODO: sjekk for 'null' argument
		try {
			Statement stmt = con.createStatement();
			
			System.err.println("addUser");
			stmt.execute( "INSERT INTO userdb (nickname, password)"
						+ "VALUES ('" + nickname + "', '" + password + "')");
		}
		catch (SQLException sqle) {
			sqle.printStackTrace();
		}
	}
	
	
	/**
	 * Tries to enter a logentry into the log-table
	 * @param userId Id of the user who sent the message
	 * @param chatId Id of the chat the given user spoke in
	 * @param message The message to be logged
	 */
	public void logMessage(int userId, int chatId, String message) {
		try {
			Statement stmt = con.createStatement();
			
			stmt.execute("INSERT INTO message ("
						+ "chatId, userId, time, message)"
						+ " VALUES ("
						+ "'" + userId + "', "
						+ "'" + chatId + "', "
						+ "CURRENT_TIMESTAMP, "
						+ "'" + message + "')");
			
		}
		catch (SQLException sqle) {
			sqle.printStackTrace();
		}
	}
	
	
	/**
	 * @deprecated Because of weird id increments
	 * @param id the id of the user
	 * @return An array with (index: content): <br>
	 * <ul>
	 *   <li>0: The users id</li>
	 *   <li>1: The users nickname</li>
	 *   <li>2: The users password</li>
	 * </ul>
	 */
	@Deprecated
	public String[] getUser(int id) {
		String[] userdata = new String[USERCOLUMNS];
		
		try {
			Statement stmt = con.createStatement();
		
			System.err.println("getUser1");
			ResultSet resultSet = stmt.executeQuery("SELECT * FROM userdb");
			
			while(resultSet.next()) {
				if(id == Integer.parseInt(resultSet.getString("id"))) {
					for(int i = 0; i < USERCOLUMNS; i++) {
						userdata[i] = resultSet.getString(i + 1);
					}
				} else {
					userdata = null;
				}
			}
		
			/*
			while(resultSet.next()) {
				userdata[0] = resultSet.getString(0);
				String nick = resultSet.getString("nick");
				System.err.println("Nick: " + nick);
			}*/
		}
		catch (SQLException sqle) {
			sqle.printStackTrace();
			userdata = null;
		}
		
		return userdata;
	}
	
	
	/**
	 * Gets the data of the given user
	 * @param nickname The user to get
	 * @return An array with (index: content): <br>
	 * <ul>
	 *   <li>0: The users id</li>
	 *   <li>1: The users nickname</li>
	 *   <li>2: The users password</li>
	 * </ul>
	 */
	public String[] getUser(String nickname) {
		String[] userdata = new String[3];
		
		
		try {
			Statement stmt = con.createStatement();
		
			System.err.println("getUser2");
			ResultSet resultSet = stmt.executeQuery("SELECT * FROM userdb");
			
			while(resultSet.next()) {
				if(nickname.equals(resultSet.getString("nickname"))) {
					for(int i = 0; i < USERCOLUMNS; i++) {
						userdata[i] = resultSet.getString(i + 1);
					} // for
					
				} else {
					userdata = null;
				} // if
			} // while
		} // try
		catch (SQLException sqle) {
			sqle.printStackTrace();
			userdata = null;
		} // catch
		
		return userdata;
	} // func end
	
	
	/**
	 * Closes the database
	 */
	private void close() {
		try {
			con.close();
		}
		catch (SQLException sqle) {
			
		}
	}
	
	private void display() {
		try {
			Statement stmt = con.createStatement();
			
			ResultSet res = stmt.executeQuery("SELECT * FROM message");
			
			System.err.print("USER\t| CHAT\t| TIME\t\t| MESSAGE\n");
			while(res.next()) {
				int userId = res.getInt("userId");
				int chatId = res.getInt("chatId");
				Timestamp ts = res.getTimestamp("time");
				String msg = res.getString("message");
				
				
				System.err.print(userId + "\t| " + chatId + "\t| "
							+ ts + "\t\t| " + msg + "\n");
			}
			
			System.err.println("\n\n");
			
			res = stmt.executeQuery("SELECT * FROM chat");
			
			System.err.print("CHAT\t");
			while(res.next()) {
				
			}
		}
		catch (SQLException sqle) {
			
		}
		
	}
}
