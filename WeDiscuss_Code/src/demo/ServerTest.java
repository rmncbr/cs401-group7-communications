package demo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import server.User;
import shared.*;

public class ServerTest {
	private ConcurrentHashMap<Integer, ObjectOutputStream> listOfClients;
	private ConcurrentHashMap<Integer, Thread> clientThreads;
	private ServerSocket serverSocket;
	private int port;
	private String serverIP;
	private boolean running;
	private ExecutorService executorService;
	private static final int MAX_THREADS = 10;
	
	private ConcurrentHashMap<Integer, String> userIDToUsername = new ConcurrentHashMap<Integer, String>(); // id to username
	private ConcurrentHashMap<String, Integer> usernameToUserID = new ConcurrentHashMap<String, Integer>(); // username to id
	private ConcurrentHashMap<String, User> allUsers = new ConcurrentHashMap<String, User>(); // map of all existing users to usernames
	
	private ConcurrentHashMap<Integer, Chatroom> listOfChatrooms = new ConcurrentHashMap<Integer, Chatroom>();
	
	private int testUserId = 0;
	
	public ServerTest(int port) throws UnknownHostException {
		this.port = port;
		this.serverIP = InetAddress.getLocalHost().getHostAddress().trim();
		this.running = false;
		this.listOfClients = new ConcurrentHashMap<>();
		this.clientThreads = new ConcurrentHashMap<>();
		this.executorService = Executors.newFixedThreadPool(MAX_THREADS);
	}
	
	public static void main(String[] args) throws IOException, ClassNotFoundException{
		ServerTest server = new ServerTest(8080); //temp port for testing
		server.start();	

		
	}
	
	public void start() {
		MessageCreator messageCreate = new MessageCreator(MessageType.UTC);
		messageCreate.setContents("Hey!");
		Chatroom chatroom1 = new Chatroom(0, 1);
		chatroom1.addMessage(messageCreate.createMessage());
		chatroom1.addMessage(messageCreate.createMessage());
		Chatroom chatroom2 = new Chatroom(1, 1);
		chatroom2.addMessage(messageCreate.createMessage());
		
		listOfChatrooms.put(chatroom1.getChatroomID(), chatroom1);
		listOfChatrooms.put(chatroom2.getChatroomID(), chatroom2);
		
		
		running = true;
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
	
	/*
	public UserManager getUserManager() {
		return userManager;
		
	}
	
	public LogManager getLogManager() {
		return logManager;
	}
	
	public ChatroomManager getChatroomManager() {
		return chatroomManager;
	}
	*/
	
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
		String clientIP = clientSocket.getLocalAddress().getHostAddress().trim();
		
		try {
			
			input = new ObjectInputStream(clientSocket.getInputStream());
			output = new ObjectOutputStream(clientSocket.getOutputStream());
			
			while(running) {
				// If thread stopped for w/e reason (User deletion) stop processing & shutdown
			    if (Thread.interrupted()) {
			        System.out.println("Thread interrupted, shutting down for user: " + userID);
			        closeResources(clientSocket, input, output, userID);
			    }
				
				Message message = (Message) input.readObject();
				
				if(message == null) continue;
				
				System.out.println("Message recieved from client: ");
				printMessage(message);
				
				MessageType type = message.getMessageType();
				
				
				switch(type) {
					case LOGIN:
							System.out.println("Handling login...");
							userID = handleLogin(message, output);
							if(userID != -1) {
								// System.out.println("LOGIN USER ID: " + userID);
								listOfClients.put(userID, output);
								clientThreads.put(userID, Thread.currentThread());
							}
						break;
					case LOGOUT:
							if(handleLogout(message, output, clientIP, userID)) {
								closeResources(clientSocket, input, output, userID);
								clientThreads.remove(userID);
							}
						break;
					case ADDUSER:
							// handleAddUser()
						break;
					case DELUSER:
							int delUser = handleDelUser(message, output);
							if(delUser != -1) {
								Thread clientThread = clientThreads.get(delUser);
								if(clientThread != null) {
									clientThread.interrupt();
								}
							}
						break;
					case CPWD:
							handleChangePassword(message, output);
						break;
					case GUL:
							// log
						break;
					case GCL:
							// log
						break;
					case CC:
							// handleCreateChatroom();
						break;
					case IUC:
							// handleInviteUserToChatroom();
						break;
					case JC:
							// handleJoinChatroom();
						break;
					case LC:
							// handleLeaveChatroom();
						break;
					case UTU:
							handleSendUserMessage(message, output);
						break;
					case UTC:
							handleSendChatroomMessage(message, listOfClients);
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
			clientThreads.remove(userID);
			closeResources(clientSocket, input, output, userID);
		}
		
	}
		
	private Integer handleLogin(Message message, ObjectOutputStream out) {
		try {
			//message variable
			Message Send;
			MessageCreator create;
			create = new MessageCreator(MessageType.LOGIN);
			create.setContents("Error"); 
			Send = create.createMessage();// have message ready to return a deny
			
			String input = message.getContents();
			
			if(input == null) {
				System.out.println("No Message Contents!");
				sendMessage(Send, out);
				return -1;
			}
			
			String[] split = input.split("\\s+");
		    if (split.length != 2)
		    {
		    	System.out.println("Not Enough Message Contents!");
		    	sendMessage(Send, out);
				return -1;
		    }
		    
		    User user = new User(split[0], split[1], true, testUserId++);
		    
		    System.out.println(user.getID() + ", " + user.getUsername());
		    
		    create.setUser(user);
		    create.setContents("Success");
		    create.setToUserID(user.getID());
		    create.setToUserName(user.getUsername());
		    
		    userIDToUsername.put(user.getID(), user.getUsername());
		    usernameToUserID.put(user.getUsername(), user.getID());
		    allUsers.put(user.getUsername(), user);
		    
		    create.setUserMap(userIDToUsername);
		    create.setChatroomMap(listOfChatrooms);
		    
		    sendMessage(create.createMessage(), out);
		    sendUserMapUpdates(user.getID(), true);
		    
		    return user.getID();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	private Boolean handleLogout(Message message, ObjectOutputStream out, String clientIP, Integer userID) {
		MessageCreator messageCreator = new MessageCreator(MessageType.LOGOUT);
		messageCreator.setContents("Success");
		try {
			sendMessage(messageCreator.createMessage(), out);
			System.out.println("Ending connection with: ");
			System.out.println(clientIP);
			return true;
		} catch (IOException e) {
			System.err.println("Error during logout processing!");
			messageCreator.setContents("Error");
			try {
				sendMessage(messageCreator.createMessage(), out);
				return false;
			} catch (IOException e1) {
				System.err.println("Error during logout processing!");
				e1.printStackTrace();
				return false;
			}
		}
	}
	
	private Integer handleDelUser(Message message, ObjectOutputStream out) {
		try {
			//message variable
			Message Send;
			MessageCreator create;
			create = new MessageCreator(MessageType.DELUSER);
			create.setContents("Error"); 
			Send = create.createMessage();// have message ready to return a deny
			
			
			String input = message.getContents();
			
			//check for bad input
			if (input == null)
			{
				sendMessage(Send, out);
				return -1;
			}
			
		    //split the given string by using space as delimiter
		    String[] split = input.split("\\s+");
		    //check for bad input (username)
		    if (split.length != 1)
		    {
		    	sendMessage(Send, out);
				return -1;
		    }
		    
		    String removeName = split[0];
		    //check if already exists inorder to remove
		    if (allUsers.containsKey(split[0]))
		    {
		    	//get extra details of account being deleted
		    	int removeID = usernameToUserID.get(removeName);
		    	
		    	ObjectOutputStream delOut = listOfClients.get(removeID);
		    	create.setContents("Disconnect");
		    	delOut.writeObject(create.createMessage());
		    	clientThreads.get(removeID).interrupt();
		    	
		    	//remove from all local data
		    	sendUserMapUpdates(removeID, false);
		    	clientThreads.remove(removeID);
		    	
		    	//return a success message
		    	create.setContents("Success");
		    	sendMessage(create.createMessage(), out);
		    	
		    	//return the removed user id
		    	return removeID;
		    }
		    
		    sendMessage(Send, out);
		    return -1;//send deny if not an existing account
		    
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return -1;
	}
	
	private void handleSendUserMessage(Message message, ObjectOutputStream outFrom) {
		try {
			//message variable
			Message Send;
			MessageCreator create;
			create = new MessageCreator(MessageType.UTU);
			create.setToUserID(message.getToUserID());
			create.setToUserName(message.getToUserName());
			create.setFromUserID(message.getFromUserID());
			create.setFromUserName(message.getFromUserName());
			create.setContents("Error"); 
			Send = new Message(create);// have message ready to return a deny
			
			String input = message.getContents();
			
			if (input == null)
			{
				//don't send a message
				System.out.println("No Message Contents!");
				sendMessage(Send, outFrom); //send the deny message
				return;
			}
			
			if(userIDToUsername.containsKey(message.getToUserID())) {
				if(listOfClients.containsKey(message.getToUserID())) {
					create.setContents(input);
					ObjectOutputStream outTo = listOfClients.get(message.getToUserID());
					sendMessage(new Message(create), outTo);
				}
				else {
					// Add to User Inbox
				}
				create.setContents("Success");
			    sendMessage(new Message(create), outFrom);
			    return;
			}

			create.setContents("Error");
			sendMessage(new Message(create), outFrom); //send the deny message
			
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void handleSendChatroomMessage(Message message, ConcurrentHashMap<Integer, ObjectOutputStream> listOfClients) {
		
		// Synchronizes the sending of the update message!
		listOfClients.values().parallelStream().forEach(output ->{
			try {
				output.writeObject(message);
				output.flush();
			}
			catch(IOException e) {
				System.err.println("Error sending message to a client!");
			}
			
		});
		
		listOfChatrooms.get(message.getToChatroomID()).addMessage(message);
		
	}
	
	private void handleChangePassword(Message message, ObjectOutputStream out) {
		try {
			
			//message variable
			Message Send;
			MessageCreator create;
			create = new MessageCreator(MessageType.CPWD);
			create.setContents("Error"); 
			Send = new Message(create);// have message ready to return a deny
			
			
			String input = message.getContents();
			
			//check for bad input
			if (input == null)
			{
				sendMessage(Send, out);
				return;
			}
			
		    //split the given string by using space as delimiter
		    String[] split = input.split("\\s+");
		    //check for bad input (username password)
		    if (split.length != 2)
		    {
		    	sendMessage(Send, out);
				return;
		    }
		    
		    
		    //check if already exists inorder to change
		    if (allUsers.containsKey(split[0]))
		    {
		    	User toChange = allUsers.get(split[0]);
		    	toChange.setPassword(split[1]);
		    	
		    	create.setContents("Success");
				Send = new Message(create);// create an accept message
				sendMessage(Send, out);
		    	return;
		    }
		    
		    sendMessage(Send, out);
		    
			}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	// Someone disconnects, name can be changed
	private void sendUserMapUpdates(Integer userID, Boolean addUser) {
		MessageCreator messageCreator = new MessageCreator(MessageType.UPDATEUM);
		messageCreator.setFromUserID(userID);
		if(addUser) {	
			messageCreator.setContents("Add");
			messageCreator.setFromUserName(userIDToUsername.get(userID));
			
			// Synchronizes the sending of the update message!
			listOfClients.values().parallelStream().forEach(output ->{
				try {
					output.writeObject(messageCreator.createMessage());
					output.flush();
				}
				catch(IOException e) {
					System.err.println("Error sending update to a client!");
				}
				
			});
		}
		else {
			listOfClients.remove(userID);
			usernameToUserID.remove(userIDToUsername.get(userID));
			allUsers.remove(userIDToUsername.get(userID));
			userIDToUsername.remove(userID);
			messageCreator.setContents("Remove");
			
			
			// Synchronizes the sending of the update message!
			listOfClients.values().parallelStream().forEach(output ->{
				try {
					output.writeObject(messageCreator.createMessage());
					output.flush();
				}
				catch(IOException e) {
					System.err.println("Error sending update to a client!");
				}
				
			});
		}
		
	}
		
	private void sendMessage(Message message, ObjectOutputStream out) throws IOException {
		// Send a message to client
		System.out.println("Sending...");
		printMessage(message);
		out.writeObject(message);
		out.flush();
	}
	
	public void closeResources(Socket clientSocket, ObjectInputStream input, ObjectOutputStream output, Integer UserID) {
		try {
			if(clientSocket != null) clientSocket.close();
			if(input != null) input.close();
			if(output != null) output.close();
			sendUserMapUpdates(UserID, false);
		}
		catch(IOException e) {
			System.err.println("Error closing resources!");
			e.printStackTrace();
		}
	}
	
	private void printMessage(Message msg) {
		System.out.println("From : " + msg.getFromUserName());
		System.out.println("ID : " + msg.getFromUserID());
		System.out.println("To : " + msg.getToUserName());
		System.out.println("ID : " + msg.getToUserID());
		System.out.println("Type : " + msg.getMessageType());
		System.out.println("Contents : " + msg.getContents());
		System.out.println("--------------------------------\n");
	}
}
