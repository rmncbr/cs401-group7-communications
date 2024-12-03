package client;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

//import demo.ConsoleClient.listenForMessages;
//import demo.ConsoleClient.readMessages;
import server.User;
import shared.*;

public class Client {
	
	private ClientUI clientGui; // Reference to GUI, used for updating GUI w/ received messages
	private Socket serverSocket;
	private String serverIP;
	private int serverPort;
	private ObjectOutputStream toServer;
	private ObjectInputStream fromServer;
	private volatile Boolean connected = false;
	private User user = null;

	private ConcurrentLinkedQueue<Message> messageQueue = new ConcurrentLinkedQueue<Message>();
	
	public Client(ClientUI gui) {
		clientGui = gui;
	}
	
	public String getServerIP() {return this.serverIP;}
	
	public int getServerPort() {return this.serverPort;}
	
	/* Attempts to establish a two way connection with the server */
	public void connectToServer(String serverIP, int serverPort) throws IOException {
		try {
			connected = true;
			this.serverIP = serverIP;
			this.serverPort = serverPort;
			serverSocket = new Socket(serverIP, serverPort);
			toServer = new ObjectOutputStream(serverSocket.getOutputStream());
			fromServer = new ObjectInputStream(serverSocket.getInputStream());
			
			System.out.println("Connected to: " + serverSocket.getInetAddress() + ", " + serverSocket.getPort());
			
			// Listener thread to listen for messages
			new Thread(new listenForMessages()).start();
			// Reader thread to read incoming messages
			new Thread(new readMessages()).start();
			
		}
		catch(IOException e) {
			try {
				reconnect();
			}
			catch(IOException e1) {
				System.out.println("Failed to Connect to Server!");
				connected = false;
				throw e;
			}

		}
		
	}
	
	private void reconnect() throws IOException{
		// Close the old socket & Associated resources
		if(serverSocket != null && !serverSocket.isClosed()) {
			closeResources();
		}
		
		connected = true;
		serverSocket = new Socket(serverIP, serverPort);
		toServer = new ObjectOutputStream(serverSocket.getOutputStream());
		fromServer = new ObjectInputStream(serverSocket.getInputStream());
		
		System.out.println("Reconnected to: " + serverSocket.getInetAddress() + ", " + serverSocket.getPort());
		
		
		// Listener thread to listen for messages
		new Thread(new listenForMessages()).start();
		// Reader thread to read incoming messages
		new Thread(new readMessages()).start();
		
	}
	
	public void sendLoginRequest(String userName, String password) throws IOException {
		MessageCreator messageCreator = new MessageCreator(MessageType.LOGIN);
		messageCreator.setContents(userName + " " + password);
		
		sendMessage(messageCreator.createMessage());
		
	}
	
	public void sendLogoutRequest() throws IOException {
		MessageCreator messageCreator = new MessageCreator(MessageType.LOGOUT);
		
		if(user != null) {
			messageCreator.setFromUserName(user.getUsername());
			messageCreator.setFromUserID(user.getID());
		}
		
		sendMessage(messageCreator.createMessage());
	}
	
	public void sendPasswordChangeRequest(String userName, String password) throws IOException{
		MessageCreator messageCreator = new MessageCreator(MessageType.CPWD);
		
		if(user != null) {
			if(user.getAdminStatus() || user.getUsername() == userName) {
				messageCreator.setFromUserName(user.getUsername());
				messageCreator.setFromUserID(user.getID());
			}
			else return; // Not an admin & not the user's account!
		}
		
		messageCreator.setContents(userName + "|" + password);
		
		sendMessage(messageCreator.createMessage());
	}
	
	public void sendMessageToUser(String message, String toUsername, int toUserID) throws IOException {
		MessageCreator messageCreator = new MessageCreator(MessageType.UTU);
		
		messageCreator.setContents(message);
		messageCreator.setToUserName(toUsername);
		messageCreator.setToUserID(toUserID);
		
		if(user != null) {
			messageCreator.setFromUserName(user.getUsername());
			messageCreator.setFromUserID(user.getID());
		}
		
		sendMessage(messageCreator.createMessage());
	}
	
	public void sendMessageToChatroom(String message, int chatroomID) throws IOException, IllegalStateException{
		MessageCreator messageCreator = new MessageCreator(MessageType.UTC);
		
		messageCreator.setContents(message);
		messageCreator.setToChatroom(chatroomID);
		
		
		if(user != null) {
			messageCreator.setFromUserName(user.getUsername());
			messageCreator.setFromUserID(user.getID());
		}
		
		sendMessage(messageCreator.createMessage());
	}
	
	public void getChatroom(int chatroomID) throws IOException {
		MessageCreator messageCreator = new MessageCreator(MessageType.JC);
		messageCreator.setToChatroom(chatroomID);
		
		
		if(user != null) {
			messageCreator.setFromUserName(user.getUsername());
			messageCreator.setFromUserID(user.getID());
		}
		
		
		sendMessage(messageCreator.createMessage());
	}
	
	public void getMessageLogs(String userName) throws IOException {
		MessageCreator messageCreator = new MessageCreator(MessageType.GUL);
		
		if(user != null) {
			if(!user.getAdminStatus()) return; // Not an admin so don't send message!
			messageCreator.setFromUserName(user.getUsername());
			messageCreator.setFromUserID(user.getID());
		}
		
		messageCreator.setToUserName(userName);
		
		sendMessage(messageCreator.createMessage());
	}
	
	public void getChatLogs(int chatroomID) throws IOException {
		MessageCreator messageCreator = new MessageCreator(MessageType.GCL);
		
		if(user != null) {
			if(!user.getAdminStatus()) return; // Not an admin so don't send message!
			messageCreator.setFromUserName(user.getUsername());
			messageCreator.setFromUserID(user.getID());
		}
		
		messageCreator.setToChatroom(chatroomID);
		
		sendMessage(messageCreator.createMessage());
	}
	
	public void addUser(String userName, String password) throws IOException {
		MessageCreator messageCreator = new MessageCreator(MessageType.ADDUSER);
		
		if(user != null) {
			if(!user.getAdminStatus()) return; // Not an admin so don't send message!
			messageCreator.setFromUserName(user.getUsername());
			messageCreator.setFromUserID(user.getID());
		}

		messageCreator.setToUserName(userName);
		
		sendMessage(messageCreator.createMessage());
	}
	
	public void deleteUser(String userName, String password) throws IOException {
		MessageCreator messageCreator = new MessageCreator(MessageType.DELUSER);
		
		if(user != null) {
			if(!user.getAdminStatus()) return; // Not an admin so don't send message!
			messageCreator.setFromUserName(user.getUsername());
			messageCreator.setFromUserID(user.getID());
		}

		messageCreator.setToUserName(userName);
		
		sendMessage(messageCreator.createMessage());
	}
	
	public void createChatroom() throws IOException{
		MessageCreator messageCreator = new MessageCreator(MessageType.CC);
		
		if(user != null) {
			messageCreator.setFromUserName(user.getUsername());
			messageCreator.setFromUserID(user.getID());
		}
		
		sendMessage(messageCreator.createMessage());
	}
	
	public void inviteUserToChatroom(String toUsername, int toUserID, int chatroomID) throws IOException{
		MessageCreator messageCreator = new MessageCreator(MessageType.IUC);
		
		messageCreator.setToUserName(toUsername);
		messageCreator.setToUserID(toUserID);
		messageCreator.setToChatroom(chatroomID);
		if(user != null) {
			messageCreator.setFromUserName(user.getUsername());
			messageCreator.setFromUserID(user.getID());
		}
		
		
		sendMessage(messageCreator.createMessage());
	}
	
	public void joinChatroom(int chatroomID) throws IOException {
		MessageCreator messageCreator = new MessageCreator(MessageType.JC);
		
		messageCreator.setToChatroom(chatroomID);
		
		if(user != null) {
			messageCreator.setFromUserName(user.getUsername());
			messageCreator.setFromUserID(user.getID());
		}
		
		sendMessage(messageCreator.createMessage());
		
	}
	
	public void leaveChatroom(int chatroomID) throws IOException {
		MessageCreator messageCreator = new MessageCreator(MessageType.LC);
		
		messageCreator.setToChatroom(chatroomID);
		
		if(user != null) {
			messageCreator.setFromUserName(user.getUsername());
			messageCreator.setFromUserID(user.getID());
			messageCreator.setToUserID(user.getID());
			messageCreator.setToUserName(user.getUsername());
		}
		
		sendMessage(messageCreator.createMessage());
		
	}
	
	private void sendMessage(Message message) throws IOException {
		try {
			System.out.println(message.getContents() + ", " + message.getMessageType());
			toServer.writeObject(message);
		}
		catch(IOException e) {
			System.out.println("Failed sending message!");
			throw e;
		}
	}
	
	/* Once connected to the sever, a separate thread only listens for messages from server, adds them to a queue for readMessages to consume */
	private class listenForMessages implements Runnable {

		@Override
		public void run(){
			try {
				while(true) {
					if(!connected) break;
					Message message = (Message) fromServer.readObject();
					messageQueue.add(message);
				}
			}
			catch(IOException | ClassNotFoundException e) {
				connected = false;
				System.out.println("Lost connection to server!");
				// e.printStackTrace();
				closeResources();
			}
			
		}
	}
	
	/* Takes Messages from the messageQueue & Processes them based on Message Type */
	private class readMessages implements Runnable {
		
		@Override
		public void run() {
			while(true) {
				if(!connected) break;
				Message message = messageQueue.poll();
				if(message == null) {
					continue;
				}
				
				MessageType type = message.getMessageType();
				
				switch(type) {
					case LOGIN:
						// Login fail
						clientGui.initUpdate(message);
						if(message.getContents().equals("Success")) {
							user = message.getUser();
						}
						break;
					case LOGOUT:
						if(message.getContents().equals("Success")) {
							connected = false;
						}
						clientGui.addToMessageQueue(message);
						break;
					case ADDUSER:
						// confirm message	
						clientGui.addToMessageQueue(message);
						break;
					case DELUSER:
						// confirm message
						clientGui.addToMessageQueue(message);
						break;
					case CPWD:
						// confirm message
						clientGui.addToMessageQueue(message);
						break;
					case GUL:
						// message w/ log contents
						clientGui.addToMessageQueue(message);
						break;
					case GCL:
						// message w/ log contents
						clientGui.addToMessageQueue(message);
						
						break;
					case CC:
						// messge w/ chatroom id
						clientGui.addToMessageQueue(message);;
						break;
					case IUC:
						// message w/ chatroom
						clientGui.addToMessageQueue(message);
						break;
					case JC:
						// message w/ chatroom
						clientGui.addToMessageQueue(message);
						break;
					case LC:
						// confirm message
						clientGui.addToMessageQueue(message);
						break;
					case UTU:
						// message w/ message contents & info
						clientGui.addToMessageQueue(message);
						break;
					case UTC:
						// message w/ message contents add to chatroom
						clientGui.addToMessageQueue(message);
						break;
					case UPDATEUM:
						clientGui.addToMessageQueue(message);
						break;
					case UPDATECM:
						clientGui.addToMessageQueue(message);
						break;
					default:

				}	
			}
			
			System.out.println("Connection with server lost, processing messages in queue...");
			// Connection has been lost, process rest of messages in queue before quitting
			while(!messageQueue.isEmpty()) {
				Message message = messageQueue.poll();
				if(message != null) {
					clientGui.addToMessageQueue(message);
				}
			}
			
			System.out.println("All messages processed, ending client");
			closeResources();
		}
	}

	
	private void closeResources() {
		try {
			if(toServer != null) toServer.close();
			if(fromServer != null) fromServer.close();
			if(serverSocket != null) serverSocket.close();
		}
		catch(IOException e) {
			System.err.println("Error closing resources!");
			e.printStackTrace();
		}
	}
	
}
