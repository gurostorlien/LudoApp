package no.ntnu.imt3281.ludo.server;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import no.ntnu.imt3281.ludo.server.ChatServer.Client;

/**
 * Master-server who controls DB, chats & games  
 * 
 */
public class ServerController {

	/** The 'url' to our database (local) */
	private String url = "jdbc:derby:BadgerDB;";
	/** Unique ID for each chat */
	//private int chatID = 0;
	//private int gameID = 0;
	/** Socket that all communication goes through */
	private ServerSocket serverSocket;
	/** ArrayList wit all logged in clients */
	private ArrayList<Client> clients = new ArrayList<Client>();
	/** A vector all chats */
	private ArrayList<Chat> chats = new ArrayList<Chat>();
	/** A vector all chats */
	private ArrayList<Game> games = new ArrayList<Game>();
	   
	
	
	private ArrayBlockingQueue<String> messages = new ArrayBlockingQueue<String>(50);
    private ServerSocket server;
    private ExecutorService executorService;
    private boolean shutdown = false;
    private JTextArea status;
	
	/** Starts up an object of ServerController */
	public static void main(String[] args) {
		ServerController servercontroller = new ServerController();
	}
	
	
	public ServerController() {		
		
		try {
			Database db = new Database(url);		// tries to connect to DB	
		} catch(SQLException sqle) {			
			url += "create=true";				
			sqle.printStackTrace();
			try {
				Database db = new Database(url);	// if DB not found, adds "create=true" to make DB
			} catch(SQLException sqle2) {
				System.err.println("No DB after 2nd try"); 
				sqle2.printStackTrace();
			}
		}
		
		// ServerController have only one masterChat
		Chat masterChat = new Chat("masterChat", 1);
		chats.add(masterChat);
		// TODO
		// alle nye chats som skal legges til sjekkes at det ikke er duplikater
		//  av chatname og ID før chatserver blir laget
		
		
		try {
            serverSocket = new ServerSocket(12345);
            executorService = Executors.newCachedThreadPool();
            startLoginMonitor();		// Handle login requests in a separate thread
            startMessageSender();		// Send same message to all clients, handled in a separate thread
            startMessageListener();		// Check clients for new messages
            executorService.shutdown();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(1);
        }
		
		
	}
	
	private void startLoginMonitor() {
        executorService.execute(() -> {
            while (!shutdown) {
            	// If message type = LOGIN 
            	translateMessage(msg, 5).equals("LOGIN");
                try {
                    Socket s = serverSocket.accept();
                    Client c = new Client(s);
                    //messages.add ("Login,"+getClientID() + ",0,LOGIN:");
                    synchronized (clients) {
                    	clients.add(c);
                    	Iterator<Client> i = clients.iterator();
	                    while (i.hasNext()) {		// Send message to all clients that a new person has joined
	                        Client c1 = i.next();
	                        if (c!=c1)
	                        	try {
	                        		chats.get(0).addParticipant(c);		// Legger Klient til i masterChat 
	                        		c.sendText("CHAT,1,"+c.name+","+c.name+"logged inn");	
	                        	} catch (IOException ioelocal) {
	                        		// Lost connection, but doesn't bother to handle it here
	                        	}
	                    }
                    }
                    displayMessage("CLIENT CONNECTED:" + c.name + "\n");
                    try {
                        messages.put("LOGIN:" + c.name);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (IOException ioe) {
                    displayMessage("CONNECTION ERROR: " + ioe + "\n");
                }
            }
        });
    }
	
    private void startMessageListener() {
        executorService.execute(() -> {			// Thread
            while (!shutdown) {
                try {
                	synchronized (clients) {	// Only one thread at a time might use the clients object 
	                    Iterator<Client> i = clients.iterator();	// Iterate throug all clients
	                    while (i.hasNext()) {			
	                        Client c = i.next();		// TODO SA - hopper over første?
	                        try {
		                        String msg = c.read();
		                        
		                        
		                       
		                        if (msg != null && !msg.equals(">>>LOGOUT<<<"))
		                            messages.put(c.name+"> "+msg);
		                        else if (msg != null) {	// >>>LOGOUT<<< received, remove the client
		                            i.remove();
		                            messages.put("LOGOUT:"+c.name);
		                            messages.put(c.name+" logged out");
		                        }
	                        } catch (IOException ioe) {	// Unable to communicate with the client, remove it
	                        	i.remove();
	                            messages.put("LOGOUT:"+c.name);
	                            messages.put(c.name+" got lost in hyperspace");
	                        }
	                    }
                	}
                } catch (InterruptedException ie) {
                	ie.printStackTrace();
                } 
           }
        });
    }

    private void startMessageSender() {
        executorService.execute(() -> {
            while (!shutdown) {
                try {
                    String message = messages.take();
                    displayMessage("Sending '" + message + "' to "
                            + clients.size() + " clients\n");
                    synchronized (clients) {		// Only one thread at a time might use the clients object
	                    Iterator<Client> i = clients.iterator();
	                    while (i.hasNext()) {
	                        Client c = i.next();
	                        try {
	                        	c.sendText(message);
	                        } catch (IOException ioe) {	// Unable to communicate with the client, remove it
	                        	i.remove();
	                        	messages.add("LOGOUT:"+c.name);
	                        	messages.add(c.name+" got lost in hyperspace");
	                        }
	                    }
                    }
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        });
    }
	private void displayMessage(String text) {
		SwingUtilities.invokeLater(() -> status.append(text));
	}
	
	private String translateMessage(String msg , int nr) {
		 String[] parts = msg.split(",");        
         return parts[nr];
	}
	
	
	
	/**
     * A new object of this class is created for all new clients.
     * When a socket is created by the serverSockets accept method
     * a new object of this class is created based on that socket.
     * This object will then contain the socket itself, a bufferedReader,
     * a bufferedWriter and the nickname of the user using the connected
     * client.
     * 
     * @author okolloen
     *
     */
    class Client {
        private String name;
        private Socket connection;
        private BufferedReader input;
        private BufferedWriter output;

        /**
         * Construct a new Client object based on the given socket object.
         * A buffered reader and a buffered writer will be created based on the
         * input stream and output stream of the given socket object. Then
         * the nickname of the user using the connecting client will be read.
         * If no LOGIN:username message can be read from the client
         * an IOException is thrown. 
         * 
         * @param connection the socket object from the server sockets accept call.
         * @throws IOException if any errors occurs during the initial IO operations
         */
        public Client(Socket connection) throws IOException {
            this.connection = connection;
            input = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            output = new BufferedWriter(new OutputStreamWriter(
                    connection.getOutputStream()));
            name = input.readLine();		// TODO ikke hele linja
            if (!name.startsWith("LOGIN:"))
                throw new IOException("No login received from client");
            name = name.substring(6);
        }

        /**
         * Closes the buffered reader, the buffered writer and the socket
         * connection.
         * 
         * @throws IOException
         */
        public void close() throws IOException {
            input.close();
            output.close();
            connection.close();
        }

        /**
         * Send the given message to the client. Ensures that all messages
         * have a trailing newline and are flushed.
         * 
         * @param text the message to send
         * @throws IOException if an error occurs when sending the message 
         */
        public void sendText(String text) throws IOException {
            output.write(text);
            output.newLine();
            output.flush();
        }

        /**
         * Non blocking read from the client, if no data is available then null 
         * will be returned. Checks to see if a line can be read from the client
         * and if so reads and returns that line (message). If no message is 
         * available null is returned.
         * 
         * @return a String with message if available, otherwise null
         * @throws IOException if an error occurs during reading
         */
        public String read() throws IOException {
            if (input.ready())
                return input.readLine();
            return null;
        }
    }
	
    
    class Chat {
    	private String chatName;
    	private int ID;
    	private Vector<Client> participants;
    	
    	public Chat(String chatname, int ID) {   	 
    		this.ID = ID; 			//participantID.add(ID);
    		this.chatName = chatname; 			
    	}
    	public String getChatName() {
    		return this.chatName; 			
    	}
    	
    	public void addParticipant(Client c) {
    		participants.add(c);
    		
    	}
    	public void removeParticipant(Client c) {
    		participants.removeElement(c);
    		
    	}
    	public Vector<Client> getParticipant() {
    		return participants; 
    		
    	}
    	// TODO, trengs en funskjon
    }
    
    class Game {
    	private int ID;
    	private Vector<Client> participants;
    	
    	public Game(int ID) {
    		this.ID = ID; 	
    	}
    	
    	public void addParticipant(Client c) {
    		participants.add(c);
    		
    	}
    	public void removeParticipant(Client c) {
    		participants.removeElement(c);
    		
    	}
    	public Vector<Client> getParticipant() {
    		return participants; 
    		
    	}
    	// TODO, trengs en funskjon for kommunikasjon?
    }
	
	
	
	
	
}