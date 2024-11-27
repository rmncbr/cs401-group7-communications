package server;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import shared.*;


public class Server {
	private UserManager userManager;
	private LogManager logManager;
	private ChatroomManager chatroomManager;
	private ConcurrentHashMap<Integer, ObjectOutputStream> listOfClients;
	private ConcurrentHashMap<Integer, Thread> clientThreads; // NEW!
	private ServerSocket serverSocket;
	private int port;
	private String serverIP;
	private boolean running;
	private ExecutorService executorService;
	private static final int MAX_THREADS = 10;
	
	public Server(int port) throws UnknownHostException {
		this.port = port;
		this.serverIP = InetAddress.getLocalHost().getHostAddress().trim();
		this.running = false;
		this.userManager = new UserManager();
		this.logManager = new LogManager();
		this.chatroomManager = new ChatroomManager();
		this.listOfClients = new ConcurrentHashMap<>();
		this.clientThreads = new ConcurrentHashMap<>();
		this.executorService = Executors.newFixedThreadPool(MAX_THREADS);
	}
	
	
	public void start() {
		running = true;
		// Starts thread that listens for incoming client connections
		Thread listenThread = new Thread(() -> listenForConnections());
		listenThread.start();
	}
	
	public void stop() {
		running = false;
		
		try {
			if (serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
				System.out.println("Server stopped.");
			}
		} catch (IOException e) {
			System.out.println("Error closing server: " + e.getMessage());
		}
	}
	
	public UserManager getUserManager() {
		return userManager;
		
	}
	
	public LogManager getLogManager() {
		return logManager;
	}
	
	public ChatroomManager getChatroomManager() {
		return chatroomManager;
	}
	
	public ConcurrentHashMap<Integer, ObjectOutputStream> getListOfClients() {
		return listOfClients;
	}
	
	public ObjectOutputStream getSocketForUser(Integer userID) {
		return listOfClients.get(userID);
	}
	
	public ServerSocket getServerSocket() {
		return serverSocket;
	}
	
	public void listenForConnections() {
		try {
			serverSocket = new ServerSocket(port);
			System.out.println("Server started on IPV4 Address: " +  serverIP + " Port: " + port);
			while (true) {
				Socket clientSocket = serverSocket.accept();
				System.out.println("New Connection! From: " + clientSocket.getLocalSocketAddress());
				Thread processThread = new Thread(() -> processResponse(clientSocket));
				executorService.submit(processThread);
			}
		} catch (IOException e) {
			System.err.println("Server connection error: " + e.getMessage());
		}
		finally {
			stop();
		}
	}
	
	
	public void processResponse(Socket clientSocket) {
		//Create appropriate Message objects and route them 
		//to the correct handler based on message type
		int userID = -1;
		ObjectInputStream input = null;
		ObjectOutputStream output = null;
		try {
			
			input = new ObjectInputStream(clientSocket.getInputStream());
			output = new ObjectOutputStream(clientSocket.getOutputStream());
			
			while(running) {
				// If thread stopped for w/e reason (User deletion) stop processing & shutdown
			    if (Thread.interrupted()) {
			        System.out.println("Thread interrupted, shutting down for user: " + userID);
			        closeResources(clientSocket, input, output);
			    }
				
				Message message = (Message) input.readObject();
				
				if(message == null) continue;
				
				System.out.println("Message recieved from client: " + message.getMessageType());
				
				MessageType type = message.getMessageType();
				
				// In the switch, new thread created for server component handler (logManager, userManager...)
				// Or maybe this thread itself just does the work?
				switch(type) {
					case LOGIN:
							userID = userManager.authUser(output, message);
							if(userID != -1) {
								 listOfClients.put(userID, output);
								 clientThreads.put(userID, Thread.currentThread());
							}
						break;
					case LOGOUT:
							if(userManager.logout(output, message)) {
								closeResources(clientSocket, input, output);
								clientThreads.remove(userID);
								return;
							}
						break;
					case ADDUSER:
							userManager.addUser(output, message);
						break;
					case DELUSER:
							int delUser = userManager.deleteUser(output, message);
							// Then we need to remove the user from any chatrooms that they were involved in
							// So maybe we add a method in chatroomManager that does that?
							// Then if we do that, we have to notify EVERY client that is part of each chatroom the user is gone
							// chatroomManager.removeUserfromChatroom(delUser, listOfClients);
							if(delUser != -1) {
								Thread clientThread = clientThreads.get(delUser);
								if(clientThread != null) {
									clientThread.interrupt();
								}
							}
						break;
					case CPWD:
							userManager.changeUserPassword(output, message);
						break;
					case GUL:
							logManager.getUserMessages(output, message);
						break;
					case GCL:
							logManager.getChatroomMessages(output, message);
						break;
					case CC:
							chatroomManager.createChatroom(output, message);
						break;
					case IUC:
							chatroomManager.addUsertoChatroom(output, message);
							// add user to chatroom w/ chatroomManager & send chatroom to User
							// Also need to add the ChatroomID to the list of involved chatrooms in the UserObject
						break;
					case JC:
							chatroomManager.addUsertoChatroom(output, message);
							// Same thing as IUC
						break;
					case LC:
							// This should only be called when the Client itself wants to be removed
							chatroomManager.removeUserfromChatroom(output, message);
						break;
					case UTU:
							// Find userID w/ userManager method
							userManager.sendMessage(output, message, toUser);
						break;
					case UTC:
							chatroomManager.sendMessageToChatroom(output, message, listOfClients);
						break;
					default:
						break;
				}	
			}
			
			System.out.println("All messages processed, ending server");
			stop();
		}
		catch(IOException | ClassNotFoundException e) {
			System.err.println("Error processing Client Message: " + e.getMessage());
		}
		finally {
			if(userID != -1) {
				listOfClients.remove(userID);
				clientThreads.remove(userID);
			}
			closeResources(clientSocket, input, output);
		}
		
	}
	
	public void closeResources(Socket clientSocket, ObjectInputStream input, ObjectOutputStream output) {
		try {
			if(clientSocket != null) clientSocket.close();
			if(input != null) input.close();
			if(output != null) output.close();
		}
		catch(IOException e) {
			System.err.println("Error closing resources!");
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws UnknownHostException {
		Server server = new Server(8080); //temp port for testing
		server.start();
	}
	
	

}
