package testing;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import java.util.concurrent.ConcurrentLinkedQueue;

import shared.*;

public class ConsoleClient {
	
	private ConsoleUI consoleUI;
	private Socket serverSocket;
	private String serverIP;
	private int serverPort;
	private ObjectOutputStream toServer;
	private ObjectInputStream fromServer;
	

	private ConcurrentLinkedQueue<Message> messageQueue = new ConcurrentLinkedQueue<Message>();
	
	public ConsoleClient(ConsoleUI ui) {
		consoleUI = ui;
	}
	
	/* Attempts to establish a two way connection with the server */
	public void connectToServer(String serverIP, int serverPort) throws IOException {
		try {
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
			System.out.println("Failed to Connect to Server!");
			throw e;
		}
		
	}
	
	public void sendLoginRequest(String userName, String password) throws IOException {
		MessageCreator messageCreator = new MessageCreator(MessageType.LOGIN);
		messageCreator.setContents(userName + "|" + password);
		
		Message message = messageCreator.createMessage();
		
		sendMessage(message);
		
	}
	
	private void sendMessage(Message message) throws IOException {
		try {
			toServer.writeObject(message);
		}
		catch(IOException e) {
			System.out.println("Failed sending message!");
			throw e;
		}
	}
	
	private void reconnect() throws IOException{
		// Close the old socket
		if(serverSocket != null && !serverSocket.isClosed()) {
			serverSocket.close();
		}
		
		serverSocket = new Socket(serverIP, serverPort);
		toServer = new ObjectOutputStream(serverSocket.getOutputStream());
		fromServer = new ObjectInputStream(serverSocket.getInputStream());
		
		System.out.println("Reconnected to: " + serverSocket.getInetAddress() + ", " + serverSocket.getPort());
		
		// Listener thread to listen for messages
		new Thread(new listenForMessages()).start();
		// Reader thread to read incoming messages
		new Thread(new readMessages()).start();
		
	}
	
	/* Once connected to the sever, a separate thread only listens for messages from server, adds them to a queue for readMessages to consume */
	private class listenForMessages implements Runnable {

		@Override
		public void run(){
			try {
				while(true) {
					Message message = (Message) fromServer.readObject();
					messageQueue.add(message);
				}
			}
			catch(IOException | ClassNotFoundException e) {
				System.out.println("Lost connection to server!");
			}
			
		}
	}
	
	/* Takes Messages from the messageQueue & Processes them based on Message Type */
	private class readMessages implements Runnable {
		
		@Override
		public void run() {
			while(true) {
				Message message = messageQueue.poll();
				if(message == null) continue;
				
				MessageType type = message.getMessageType();
				
				/* NOTE: Should the logic be in the switch statements? */
				switch(type) {
					case LOGIN:
						// Login fail so try reconnect to server
						if(!message.getContents().equals("SUCCESS")) {
							try {
								reconnect();
							} catch (IOException e) {
								System.err.println("Error reconnecting to Server");
								e.printStackTrace();
							}
						}
						
						consoleUI.initUpdate(message);
						break;
					case LOGOUT:
						// confirm message
						consoleUI.update(message);
						break;
					case ADDUSER:
						// confirm message	
						consoleUI.update(message);
						break;
					case DELUSER:
						// confirm message
						consoleUI.update(message);
						break;
					case CPWD:
						// confirm message
						consoleUI.update(message);
						break;
					case GUL:
						// message w/ log contents
						consoleUI.update(message);
						break;
					case GCL:
						// message w/ log contents
						consoleUI.update(message);
						break;
					case CC:
						// messge w/ chatroom id
						consoleUI.update(message);
						break;
					case IUC:
						// message w/ chatroom
						consoleUI.update(message);
						break;
					case JC:
						// message w/ chatroom
						consoleUI.update(message);
						break;
					case LC:
						// confirm message
						consoleUI.update(message);
						break;
					case UTU:
						// message w/ message contents & info
						consoleUI.update(message);
						break;
					case UTC:
						// message w/ message contents add to chatroom
						consoleUI.update(message);
						break;
					default:
						break;
				}
					
			}
		}
	}

	
	
	
	
	
	/* Various Getters */
	
}
