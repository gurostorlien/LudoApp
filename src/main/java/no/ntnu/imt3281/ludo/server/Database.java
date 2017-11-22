package no.ntnu.imt3281.ludo.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import org.apache.derby.shared.common.error.DerbySQLIntegrityConstraintViolationException;

/**
 * Database class that handles the interaction with the database
 * for storing userdata and chatlogs
 */
public class Database {
	private Connection con;
	
	/** The amount of colums in the usertable */
	private final static int USERCOLUMNS = 3;
	
	
	/**
	 * Constructor that tries to create the needed tables
	 * in the database. If the tables already exists no new tables
	 * will be created.
	 * 
	 * @param url - Filepath to the database
	 * @throws SQLException If the url doesn't lead to any valid
	 * database
	 */
	public Database(String url) throws SQLException {		
		// gets a connection to the database
		con = DriverManager.getConnection(url);

		// gets a statement from the connection
		Statement stmt = con.createStatement();
		
	
		// manual deletes
		//stmt.execute("DROP TABLE message");
		//stmt.execute("DROP TABLE usertable");
		//stmt.execute("DROP TABLE chat");
		
		/* Creating the usertable
		 * - autoincremented id : int
		 * - username : string
		 * - encrypted password : string
		 */ 
		try {
			System.err.println("user");
			
			stmt.execute("CREATE TABLE usertable ("
					+ "id bigint NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
					+ "username varchar(20) NOT NULL UNIQUE,"
					+ "password varchar(128) NOT NULL,"
					+ "CONSTRAINT usertable_PK PRIMARY KEY (id))");
		
			
			System.err.println("User table created!");
		}
		catch (SQLException sqle) {
			System.err.println("Table excists!");
			//sqle.printStackTrace();
		}
		
		/* Create the chat table
		 * - autoincremented id : int
		 * - chatname : string
		 */
		try {
			System.err.println("chat");
			// time: 13.11.2017 10:11
			stmt.execute( "CREATE TABLE chat ("
						+ "id bigint NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
						+ "chatName varchar(20) NOT NULL,"
						+ "CONSTRAINT chat_PK PRIMARY KEY (id))");
			
			System.err.println("Chat table created!");
		}	
		catch (SQLException sqle) {
			System.err.println("Chat already exists!");
			//sqle.printStackTrace();
		}
		
		/* Create the message table. This is our actual log-entries
		 * 
		 * - chatId : int | The id of the chat that the message is written in
		 * - userId : int | The id of the user that wrote the message
		 * - time : timestamp | an autogenerated time of the log
		 * - message : string | The actual message that was sendt
		 */
		try {
			System.err.println("message");
			stmt.execute("CREATE TABLE message ("
						+ "chatId bigint NOT NULL,"
						+ "userId bigint NOT NULL,"
						+ "time timestamp NOT NULL,"
						+ "message varchar(3000) NOT NULL,"
						+ "CONSTRAINT message_PK PRIMARY KEY (chatId, userId),"
						+ "CONSTRAINT message_chatId FOREIGN KEY (chatId) REFERENCES chat(id)"
						+ " ON DELETE CASCADE ON UPDATE RESTRICT,"
						+ "CONSTRAINT message_userId FOREIGN KEY (userId) REFERENCES usertable(id)"
						+ " ON DELETE CASCADE ON UPDATE RESTRICT)");
			
			System.err.println("Message table created!");
		}
		catch (SQLException sqle) {
			//sqle.printStackTrace();
			System.err.println("Message already exitsts");
		}
		
		//con.close();
	}
	
	
	// ONLY FOR TESTING
	/*
	public static void main(String[] args) {
		User user1 = new User("BobKaare", "123");
	
		try {
			Database db = new Database("jdbc:derby:BadgerDB;");
			
			db.addChat("NotSoYoloPary");
			db.addUser(new User("Sam", "1123"));
			
			int u = db.getUserID("Sam");
			int c = db.getChatID("NotSoYoloPary");
			
			db.logMessage(u, c, "Halla på re");
			
			db.logMessage(u, c, "Din korthåra lomtjuv");
	
			db.addUser(user1);
			db.display();
			db.close();
		} catch(SQLException sqle) {
			sqle.printStackTrace();
		}
	}
	// */
	
	
	/**
	 * Tries to add a user to the usertable table
	 * @param username The username of the user
	 * @param password The hashed password
	 * @return True if user where found, false otherwise
	 */
	public boolean addUser(String username, String password) {
		
		boolean added = true;
		
		try {
			Statement stmt = con.createStatement();
			
			System.err.println("addUser");
			stmt.execute( "INSERT INTO usertable (username, password)"
						+ "VALUES ('" + username + "', '" + password + "')");
			
			System.err.println("added: " + username);
		}
		catch (DerbySQLIntegrityConstraintViolationException dicve) {
			System.err.println("Constraint error: " + dicve.getMessage());
			added = false;
		}
		catch (SQLException sqle) {
			sqle.printStackTrace();
		}
		
		return added;
	}
	
	
	/**
	 * Tries to enter a logentry into the log-table
	 * @param userId Id of the user who sent the message
	 * @param chatId Id of the chat the given user spoke in
	 * @param message The message to be logged
	 */
	public void logMessage(int userId, int chatId, String message) {
		System.err.println("logMessage");
		System.err.println(userId + ", " + chatId);
		
		try {
			Statement stmt = con.createStatement();
			
			stmt.execute("INSERT INTO message ("
						+ "chatId, userId, time, message)"
						+ " VALUES ("
						+ userId + ", "
						+ chatId + ", "
						+ "CURRENT_TIMESTAMP, "
						+ "'" + message + "')");
			
		}
		catch (DerbySQLIntegrityConstraintViolationException dicve) {
			System.err.println("Constraint error: " + dicve.getMessage());
		}
		catch (SQLException sqle) {
			sqle.printStackTrace();
		}
	}
	
	
	/**
	 * 
	 * @param id the id of the user
	 * @return An array with (index: content): <br>
	 * <ul>
	 *   <li>0: The users id</li>
	 *   <li>1: The users username</li>
	 *   <li>2: The users password</li>
	 * </ul>
	 */
	
	public String getUserName(int id) {
		String userName = null;
		
		try {
			Statement stmt = con.createStatement();
		
			System.err.println("getUser1");
			ResultSet resultSet = stmt.executeQuery("SELECT ID, USERNAME FROM usertable");
			
			while(resultSet.next()) {
				if(id == resultSet.getInt("id")) {
					userName = resultSet.getString("USERNAME");
				}
			}
		}
		catch (SQLException sqle) {
			sqle.printStackTrace();
			
		}
		
		return userName;
	}
	
	
	/**
	 * Gets the data of the given user
	 * @param username The user to get
	 * @return An array with (index: content): <br>
	 * <ul>
	 *   <li>0: The users id</li>
	 *   <li>1: The users username</li>
	 *   <li>2: The users password</li>
	 * </ul>
	 */
	public String[] getUser(String username) {
		String[] userdata = new String[3];
		
		
		try {
			Statement stmt = con.createStatement();
		
			System.err.println("getUser2");
			ResultSet resultSet = stmt.executeQuery("SELECT * FROM usertable");
			
			while(resultSet.next()) {
				if(username.equals(resultSet.getString("username"))) {
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
	 * Gets the id of the given user
	 * @param username The username of a user
	 * @return The users id or -1 if none where found
	 */
	public int getUserID(String username) {
		int userId = -1;
		
		try {
			Statement stmt = con.createStatement();
			
			ResultSet res = stmt.executeQuery("SELECT id, username FROM usertable "
										+ "WHERE username = '" + username + "'");
			
			// res starts BEFORE the actual first row
			// need to move it along
			res.next();
			userId = res.getInt("id");
		}
		catch(SQLException sqle) {
			sqle.printStackTrace();
		}
		
		return userId;
	}
	
	/**
	 * Gets the id of a given chat
	 * @param chatname The name of the chat
	 * @return The id of the chat or -1 if none where found
	 */
	public int getChatID(String chatname) {
		int chatid = -1;
		
		try {
			Statement stmt = con.createStatement();
			
			ResultSet res = stmt.executeQuery("SELECT id, chatname FROM chat "
									+ "WHERE chatname = '" + chatname + "'");

			// res starts BEFORE the actual first row
			// need to move it along
			res.next();
			chatid = res.getInt("id");
		}
		catch(SQLException sqle) {
			sqle.printStackTrace();
		}
		
		return chatid;
	}
	
	/**
	 * This is used to check valid login parameters
	 * @param username The username of the user
	 * @param password The password of the user (should be encryptet)
	 * @return The id of the user or -1 if login credential was a missmatch
	 */
	public int checkLogin(String username, String password) {
		
		int userid = -1;
		
		try {
			Statement stmt = con.createStatement();
			
			ResultSet res = stmt.executeQuery("SELECT id FROM usertable "
									+ "WHERE username = '" + username + "'"
									+ "AND password = '" + password + "'");
			
			if(res.next()) {
				userid = res.getInt("id");
			}
			else System.out.println("Login unsseccsssfull");
		}
		catch (SQLException sqle) {
			sqle.printStackTrace();
		}
		
		return userid;
	}
	
	
	/**
	 * Closes the database
	 */
	public void close() {
		try {
			con.close();
		}
		catch (SQLException sqle) {
			
		}
	}
	
	
	/**
	 * Add a new chat to the database
	 * @param chatname The name of the chat
	 */
	public void addChat(String chatname) {
		try {
			Statement stmt = con.createStatement();
			stmt.execute("INSERT INTO chat (chatname)"
					+ "VALUES ('" + chatname + "')");
			
			System.err.println("added chat: " + chatname);
		}
		catch (SQLException sqle) {
			sqle.printStackTrace();
		}
	}
	
	
	
	/**
	 * Displays the contents of the database
	 * TODO: Should probably take the SELECT as arg
	 * 		and maybe the table
	 */
	private void display() {
		System.err.println("DISPLAY\n");
		try {
			Statement stmt = con.createStatement();
			
			ResultSet res = stmt.executeQuery("SELECT * FROM message");
			
			System.err.print("USER\t| CHAT\t| TIME\t\t\t\t| MESSAGE\n");
			while(res.next()) {
				int userId = res.getInt("userId");
				int chatId = res.getInt("chatId");
				Timestamp ts = res.getTimestamp("time");
				String msg = res.getString("message");
				
				
				System.err.print(userId + "\t| " + chatId + "\t| "
							+ ts + "\t| " + msg + "\n");
			}
			
			System.err.println("\n\n");
			
			res = stmt.executeQuery("SELECT * FROM chat");
			
			System.err.print("CHAT \t| NAME\n");
			while(res.next()) {
				int chatId = res.getInt("id");
				String name = res.getString("chatname");
				
				System.err.print(chatId + " \t| " + name + "\n");
			}
			
			System.err.println("\n\n");
			
			res = stmt.executeQuery("SELECT * FROM usertable");
			
			System.err.print("USER \t| USERNAME \t| PASSWORD\n");
			while(res.next()) {
				int userId = res.getInt("id");
				String username = res.getString("username");
				String password = res.getString("password");
				
				System.err.print(userId + " \t| " + username +
						" \t\t| " + password + "\n");
			}
		}
		catch (SQLException sqle) {
			sqle.printStackTrace();
		}
		
	}
}
