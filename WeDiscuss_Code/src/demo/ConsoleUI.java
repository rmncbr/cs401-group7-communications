package demo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

import server.User;
import shared.*;

public class ConsoleUI {
	private ConsoleClient client;

	private boolean isLoggedIn = false;
	private volatile boolean operationCheck = false;
	
	private User user;
	
	// User and chatroom maps caches
	private ConcurrentMap<Integer, Chatroom> chatrooms = new ConcurrentHashMap<Integer, Chatroom>();
	private ConcurrentMap<Integer, String> userMap = new ConcurrentHashMap<Integer, String>();
	// Should have one for the other way as well? (<String, Integer>)
	
	private ArrayList<String> userLog;
	private ArrayList<String> chatroomLog;
	
	private ConcurrentLinkedQueue<Message> clientMessages = new ConcurrentLinkedQueue<Message>();
	
	private CountDownLatch serverResponse = new CountDownLatch(1);
	private CountDownLatch displayUpdate = new CountDownLatch(1);
	
	private Thread listenThread;
	// Maybe start another thread that is responsible for displaying chatroom messages?
	// That way when a User "Joins" A chatroom, the display of all the chatroom messages is constantly
	// refreshed when a new message comes in
	
	public ConsoleUI() {
		client = new ConsoleClient(this);
	}
	
	public static void main(String [] args) {	
		ConsoleUI clientUI = new ConsoleUI();
		Scanner input = new Scanner(System.in);
		
		clientUI.showInitDialog(input);
		clientUI.showLoginDialog(input);
		
		clientUI.start();
		
		while(true) {
			System.out.println("Welcome!");
			
			// Display the command options
			System.out.println("[0] - LOGOUT");
			System.out.println("[1] - Display Chatrooms");
			System.out.println("[2] - Display Users");
			System.out.println("[3] - Send Mesage To User");
			System.out.println("[4] - Join Chatroom");
			System.out.println("[5] - Leave Chatroom");
			System.out.println("[6] - Password Change Request");
			System.out.println("[7] - Admin Commands");
			System.out.println("[8] - Display User Inbox");
			System.out.print("Command: "); // Prompt for command input
			
			int command = -1;
			if(input.hasNextInt()) {
				command = input.nextInt();
				input.nextLine();
				switch (command) {
					case 0:
						if(clientUI.doLogout()) {
							System.out.println("Goodbye!");
							return;
						}
						System.out.println("Error Logging Out!");
						break;
					case 1:
						clientUI.doDisplayChatrooms();
						break;
					case 2:
						clientUI.doDisplayUsers();
						break;
					case 3:
						clientUI.doSendUserMessage(input);
						break;
					case 4:
						clientUI.doJoinChatroom(input);
						break;
					case 5:
						clientUI.doLeaveChatroom(input);
						break;
					case 6:
						clientUI.doPasswordChange();
						break;
					case 7:
						clientUI.doAdminCommands();
						break;
					case 8:
						clientUI.doDisplayUserInbox();
						break;
					default:
						System.out.println("Invalid Command!");
						break;
				}
			}
			else {
				input.nextLine();
				System.out.println("Invalid Input!");
			}
			
			System.out.println();
		}
		
	}
	
	public void start() {
		listenThread = new Thread(() -> processMessages());
		
		isLoggedIn = true;
        
        listenThread.start();
        
	}
	
	private void showInitDialog(Scanner input) {
		while(true) {
			System.out.print("IP: ");
			String ip = input.nextLine();
			System.out.print("Port: ");
			int port = input.nextInt();
			input.nextLine();
				
			try {
				client.connectToServer(ip, port); // Attempt connection
				break;
			}
			catch(IOException e) {
				System.err.println("Refused Connection!");
			}
		}
		System.out.println("Connected!");
	}

	private void showLoginDialog(Scanner input) {
		while(true) {
			System.out.println("User: ");
			String userName = input.nextLine();
			System.out.println("Password: ");
			String password = input.nextLine();
				
			try {
				client.sendLoginRequest(userName, password);
				operationCheck = false;
				serverResponse = new CountDownLatch(1);
				serverResponse.await(); // Wait for server authentication response
				if(operationCheck) {
					break;
				}
				System.out.println("Invalid Credentials!");
			}
			catch(IOException e) {
				System.err.println("Login Request Error!");
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				System.err.println("Login process interrupted.");
			}
		}
	}
	
	public void initUpdate(Message message) {
		System.out.println("Received Client Message: " + message.getMessageType());
		System.out.println(message.getContents());
		
		if(message.getContents().equals("Success")) {
			// Init everything
			user = message.getUser();
			if(!(message.getChatroomMap() == null)) {
				chatrooms = message.getChatroomMap();
			}
			userMap = message.getUserMap();
			// printMessage(message);
			userMap.put(user.getID(), user.getUsername());
			operationCheck = true;
		}
		
		serverResponse.countDown();
	}
	
	private Boolean doLogout() {
		try {
			client.sendLogoutRequest();
			serverResponse.await();
			
			if(operationCheck) {
				listenThread.interrupt();
				return true;
			}
		}
		catch(IOException e) {
			System.err.println("Logout Request Error!");
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			System.err.println("Logout process interrupted.");
		}
		
		operationCheck = false;
		return false;
	}
	
	private void doSendUserMessage(Scanner input) {
		doDisplayUsers();
		
		System.out.print("Username of user to send message to: ");
		String toUser = input.nextLine();
		Integer toUserID = -1;
		Boolean found = false;
		for(Integer user : userMap.keySet()) {
			if(userMap.get(user).equals(toUser)) {
				found = true;
				toUserID = user;
				break;
			}
		}
		
		if(found) {
			System.out.print("Message: ");
			String message = input.nextLine();

			try {
				operationCheck = false;
				serverResponse = new CountDownLatch(1);
				client.sendMessageToUser(message, userMap.get(toUserID), toUserID);
				serverResponse.await();
				if(operationCheck) {
					System.out.println("Sent message!");
				}
				else {
					System.out.println("Couldn't send message!");
				}
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
		else {
			System.out.println("User not found!");
		}
	}
	
	private void doJoinChatroom(Scanner input) {
		
	}
	
	private void doLeaveChatroom(Scanner input) {
		doDisplayChatrooms();
		System.out.println("Which chatroom would you like to leave?");
		int chatroomID = -1;
		System.out.print("Enter: ");
		if(input.hasNextInt()) {
			chatroomID = input.nextInt();
			input.nextLine();
			if(!chatrooms.containsKey(chatroomID)) {
				System.out.println("You are not apart of chatroom: " + chatroomID + "!");
				return;
			}
			try {
				client.leaveChatroom(chatroomID);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			input.nextLine();
			System.out.println("Invalid Input!");
		}
	}
	
	private void doPasswordChange() {
		
	}
	
	private void doAdminCommands() {
		
	}
	
	public void addToMessageQueue(Message message) {
		clientMessages.add(message);
	}
	
	private void processMessages() {
		while(true) {
			
			if(Thread.interrupted()) {
				break;
			}
			Message message = clientMessages.poll();
			if(message == null) continue;
			
			MessageType type = message.getMessageType();
			System.out.println("Received Client Message: " + type);
			
			switch(type) {
			case LOGOUT:
				processLogout(message);
				break;
			case ADDUSER:
				processAddUser(message);
				break;
			case DELUSER:
				processDelUser(message);
				break;
			case CPWD:
				processChangePassword(message);
				break;
			case GUL:
				processGetUserLogs(message);
				break;
			case GCL:
				processGetChatroomLogs(message);
				break;
			case CC:
				processCreateChatroom(message);
				break;
			case IUC:
				processInviteUserToChatroom(message);
				break;
			case JC:
				processJoinChatroom(message);
				break;
			case LC:
				processLeaveChatroom(message);
				break;
			case UTU:
				processUserMessage(message);
				break;
			case UTC:
				processChatroomMessage(message);
				break;
			case UPDATEUM:
				processUserMapUpdate(message);
				break;
			case UPDATECM:
				//chatroomMap.remove(message.getToChatroomID());
				break;
			default:
				break;
			}
			
		}
	}
	
	private void processLogout(Message message) {
		try {
			client.sendLogoutRequest();
		} catch (IOException e) {
			e.printStackTrace();
		}
		serverResponse.countDown();
	}
	
	private void processAddUser(Message message) {
		
	}
	
	private void processDelUser(Message message) {
		
	}
	
	private void processChangePassword(Message message) {
		
	}
	
	private void processGetUserLogs(Message message) {
		
	}
	
	private void processGetChatroomLogs(Message message) {
		
	}
	
	private void processCreateChatroom(Message message) {
		
	}
	
	private void processJoinChatroom(Message message) {
		// Here we would call a new thread of DisplayChatMessages until the user returns to Main Menu
	}
	
	private void displayChatMessages() {
		
		while(true) {
			doDisplayChatrooms();
			// Some logic to wait until interrupted/ countdownlatch when new chatroom message comes in
		}
	}
	
	private void processLeaveChatroom(Message message) {
		
	}
	
	private void processInviteUserToChatroom(Message message) {
		
	}
	
	private void processUserMessage(Message message) {
		// Sent a message, server sent it
		if(message.getContents().equals("Success")) {
			operationCheck = true;
			serverResponse.countDown();
			return;
		}
		
		// Sent a message, server couldn't send it
		if(message.getContents().equals("Error")) {
			serverResponse.countDown();
			return;
		}

		// Received a message
		user.addToInbox(message);
		System.out.println("New Message!");
		return;

	}
	
	private void processChatroomMessage(Message message) {
		
	}
	
	private void processUserMapUpdate(Message message) {
		if(message.getContents().equals("Add")) {
			userMap.put(message.getFromUserID(), message.getFromUserName());
		}
		else {
			userMap.remove(message.getFromUserID());
		}
		System.out.println("List of Users Update!");
	}
	
	private void doDisplayChatrooms() {
		
		System.out.println("Chatrooms");
		System.out.println("-----------------------------"); 
		for(Integer chatroomID : chatrooms.keySet()) {
			System.out.println("Chatroom ID: " + chatroomID);
		}
		System.out.println("-----------------------------"); 
	}
	
	private void doDisplayUsers() {
		System.out.println("Users");
		System.out.println("-----------------------------"); 
		for(Integer userID : userMap.keySet()) {
			System.out.println("User ID: " + userID + " | Username: " + userMap.get(userID));
		}
		System.out.println("-----------------------------"); 
	}
	
	private void doDisplayUserInbox() {
		System.out.println("Inbox:");
		System.out.println("-----------------------------"); 
		user.displayMessageInbox();
		System.out.println("-----------------------------"); 
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
