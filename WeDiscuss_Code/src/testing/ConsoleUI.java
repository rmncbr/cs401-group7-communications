package testing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import server.User;
import shared.*;

public class ConsoleUI {
	private User user = null;
	private ArrayList<Message> userMessages = null;
	private ConcurrentHashMap<Integer, Chatroom> chatrooms = null;
	private ConcurrentHashMap<Integer, String> userMap = null;
	
	private Boolean connect = false;
	private ConsoleClient client;
	
	private Scanner input = new Scanner(System.in);
	
	private CountDownLatch serverResponse = new CountDownLatch(1); // So that if we need a response before continuing, we can block!
	
	public ConsoleUI() {
		client = new ConsoleClient(this); // Init Client w/ this GUI
	}
	
	public static void main(String[] args) {
		ConsoleUI ui = new ConsoleUI();
		
		ui.doInitUI();
		
		ui.doLogin();
		
		System.out.println("Welcome!");
		
		/// runMainLoop()
		
		
		
		
		
	}
	
	private void doInitUI() {
		while(true) {
			System.out.print("IP: ");
			String ip = input.nextLine();
			System.out.print("Port: ");
			int port = input.nextInt();
			input.nextLine();
			
			try {
				client.connectToServer(ip, port); // Attempt connection
				break; // Break if successful
			}
			catch(IOException e) {
				System.err.println("Refused Connection!");
			}
		}
		System.out.println("Connected!");
	}
	
	private void doLogin() {
		while(true) {
			System.out.println("User: ");
			String userName = input.nextLine();
			System.out.println("Password: ");
			String password = input.nextLine();
			
			try {
				client.sendLoginRequest(userName, password);
				serverResponse.await(); // Wait for server authentication response
				
				if(connect) { // Updated to True if credentials valid!
					break;
				}
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
		// Failed login, release lock do nothing!
		if(!message.getContents().equals("SUCCESS")) {
			System.out.println("Invalid Credentials!");
			serverResponse.countDown(); // Releases blocking lock
			return;
		}
		
		// Init everything
		// user = message.getUser();
		connect = true;
		System.out.println("Login Success!");
		System.out.println("Type: " + message.getMessageType());
		System.out.println("Contents: " + message.getContents());
		System.out.println("!");
		
		
		serverResponse.countDown(); // Releases blocking lock
		
	}
	
	/* Gets & Process Message client got from server */
	public void update(Message message) {
		
		System.out.println("Type: " + message.getMessageType());
		System.out.println("Contents: " + message.getContents());
		
		System.out.println("!");
	}
	
}
