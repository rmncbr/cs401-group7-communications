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
	private ConcurrentHashMap<Integer, Socket> listOfClients;
	
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
	
	public ConcurrentHashMap<Integer, Socket> getListOfClients() {
		return listOfClients;
	}
	
	public Socket getSocketForUser(Integer userID) {
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
		try {
			
			ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
			ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());
			
			while(running) {
				
				Message message = (Message) input.readObject();
				
				if(message == null) continue;
				
				System.out.println("Message recieved from client: " + message.getMessageType());
				
				MessageType type = message.getMessageType();
				
				// In the switch, new thread created for server component handler (logManager, userManager...)
				// Or maybe this thread itself just does the work?
				switch(type) {
					case LOGIN:
						
						break;
					case LOGOUT:
		
						break;
					case ADDUSER:
		
						break;
					case DELUSER:
		
						break;
					case CPWD:
		
						break;
					case GUL:
						
						break;
					case GCL:
						
						break;
					case CC:
						
						break;
					case IUC:
						
						break;
					case JC:
						
						break;
					case LC:
						
						break;
					case UTU:
						
						break;
					case UTC:
						
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
		
	}
	
	
	public static void main(String[] args) throws UnknownHostException {
		Server server = new Server(8080); //temp port for testing
		server.start();
	}
	
	

}
