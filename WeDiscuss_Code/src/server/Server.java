package server;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;


public class Server {
	private UserManager userManager;
	private LogManager logManager;
	private ChatroomManager chatroomManager;
	private Map<Integer, Socket> listOfClients;
	
	private ServerSocket serverSocket;
	private int port;
	private boolean running;
	private ExecutorService executorService;
	private static final int MAX_THREADS = 10;
	
	
	
	public Server(int port) {
		this.port = port;
		this.running = false;
		this.userManager = new UserManager();
		this.logManager = new LogManager();
		this.chatroomManager = new ChatroomManager();
		this.listOfClients = new HashMap<>();
	
		this.executorService = Executors.newFixedThreadPool(MAX_THREADS);
	}
	
	
	public void start() {
		try {
			serverSocket = new ServerSocket(port);
			running = true;
			System.out.println("Server started on port: " + port);
			
			while (running) {
				System.out.println("Waiting for client connection...");
				
				//Accept client connection
				Socket clientSocket = serverSocket.accept();
				System.out.println("Client connected: " + clientSocket.getInetAddress());
				
				//create input/output streams for object transmission
				ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
				ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());
				
				/*
				try {
					
					//read message object from client
					Message recievedMessage = (Message) input.readObject();
					System.out.println("Message recieved from client: " + recievedMessage);
					
					//create and send response message
					Message response = new Message();
					response.setContent("Message successfully recieved by server");
					output.writeObject(response);
					output.flush();
					
				} catch (ClassNotFoundException e) {
					System.err.println("Error reading message object: " + e.getMessage());
				} finally {
					//Close streams and socket
					input.close();
					output.close();
					clientSocket.close();
					
				}
				*/
			}
		} catch (IOException e) {
			System.err.println("Server error: " + e.getMessage());
			
		} finally {
			stop();
		}
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
	
	public Map<Integer, Socket> getListOfClients() {
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
			while (true) {
				Socket clientSocket = serverSocket.accept();
				Thread clientThread = new Thread(() -> processResponse());
				clientThread.start();
			}
		} catch (IOException e) {
			System.err.println("Server connection error: " + e.getMessage());
		}
	}
	
	
	public void processResponse() {
		//Create appropriate Message objects and route them 
		//to the correct handler based on message type
		
	}
	
	
	public static void main(String[] args) {
		Server server = new Server(8080); //temp port for testing
		server.start();
	}
	
	

}
