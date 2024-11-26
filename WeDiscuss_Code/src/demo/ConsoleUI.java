package demo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import server.User;
import shared.*;

public class ConsoleUI {
	private ArrayList<Message> userMessages = null;
	private ConcurrentHashMap<Integer, Chatroom> chatrooms = null;
	private ConcurrentHashMap<Integer, String> userMap = null;
	
	private User user;
	private Boolean operationCheck = false;
	private ConsoleClient client;
	
	private CountDownLatch serverResponse = new CountDownLatch(1); // So that if we need a response before continuing, we can block!
	
	public ConsoleUI() {
		client = new ConsoleClient(this); // Init Client w/ this GUI
	}
	
	public static void main(String[] args) throws IOException {
		ConsoleUI ui = new ConsoleUI();
		
		try (Scanner input = new Scanner(System.in)) {
			ui.doInitUI(input);
		
			ui.doLogin(input);
		
			
			while(true) {
				System.out.println("Welcome!");
				
				
				// Display the command options
				System.out.println("[0] - LOGOUT");
				System.out.println("[1] - UTU");
				System.out.println("[2] - {///}");
				System.out.print("Command: "); // Prompt for command input
				
				int command = input.nextInt();
				
				switch (command) {
				case 0:
				    if(ui.doLogout()) {
				    	System.out.println("Goodbye!");
				    	return;
				    }
				    System.out.println("Error logging out!");
				    break;
				case 1:
				    System.out.println("UTU...");
				    ui.doUTU();
				    break;
				case 2:
				    System.out.println("Executing: ///");

				    break;
				default:
				    System.out.println("Invalid command. Please try again.");
				    break;
				}
			
	        
				System.out.println();
			}
		}
	}
	
	private void doInitUI(Scanner input) {
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
	
	private void doLogin(Scanner input) {
		
		while(true) {
			System.out.println("User: ");
			String userName = input.nextLine();
			System.out.println("Password: ");
			String password = input.nextLine();
				
			try {
				client.sendLoginRequest(userName, password);
				serverResponse.await(); // Wait for server authentication response
				break;
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
	
	private Boolean doLogout() {
		try {
			client.sendLogoutRequest();
			serverResponse.await();
			
			if(operationCheck) return true;
		}
		catch(IOException e) {
			System.err.println("Logout Request Error!");
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			System.err.println("Logout process interrupted.");
		}
		
		operationCheck = true;
		return false;
	}
	
	private void doUTU() throws IOException {
		MessageCreator messageCreator = new MessageCreator(MessageType.UTU);
		messageCreator.setContents("TEST");
		
		client.sendMessageToUser("Hey", "Bana", 123);
	}
	
	public void initUpdate(Message message) {
		// Failed login, release lock do nothing!
		if(!message.getContents().equals("SUCCESS")) {
			System.out.println("Invalid Credentials!");
			serverResponse.countDown(); // Releases blocking lock
			return;
		}
		
		// Init everything
		// user = message.getUser();
		// userMessages = user.getMessages();
		// chatrooms = message.getChatrooms();
		// userMap = message.getUserMap();
		printMessage(message);
		
		
		serverResponse.countDown(); // Releases blocking lock
		
	}
	
	/* Gets & process message client got from server */
	public void update(Message message) {
		
		printMessage(message);
		
		if(message.getMessageType().equals(MessageType.LOGOUT)) {
			if(message.getContents().equals("SUCCESS")) {
				operationCheck = true;
				serverResponse.countDown(); // Releases blocking lock
				return;
			}
			operationCheck = false;
			serverResponse.countDown(); // Releases blocking lock
			return;
		}
		
		/*
		 * Any other messageType special logic should go here. Probably can make a SWITCH
		 */
		
		operationCheck = true;
		serverResponse.countDown(); // Releases blocking lock
	}
	
	public void printMessage(Message message) {
		System.out.println("Type: " + message.getMessageType());
		System.out.println("Contents: " + message.getContents());
		System.out.println("!\n");
	}
}
