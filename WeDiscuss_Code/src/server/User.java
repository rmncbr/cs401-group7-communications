package server;
import java.util.*;
import java.io.*;



import shared.Message;

public class User implements Serializable{
	//static counter to generate unique IDs
	//private static int IDCounter = 0;
	
	private String username;
	private String password;
	private int ID;
	private boolean status; //False = offline, True = online
	private boolean adminStatus; //False = non-admin, True = admin
	private List<Message> messageInbox;
	private List<Integer> involvedChatrooms;
	

	
	//Constructor
	public User(String username, String password, boolean adminStatus, int userID) {
		this.username = username;
		this.password = password;
		this.adminStatus = adminStatus;
		this.ID = validateUID(userID);
		this.status = false; //Initially offline
		this.messageInbox = loadMessageInbox();
		this.involvedChatrooms = loadChatrooms();
	}

	//Method to validate userID
	private int validateUID(Integer userID) {

		if (userID == null) {
			throw new IllegalArgumentException("User ID cannot be null");
		}

		if (userID < 0) {
			throw new IllegalArgumentException("User ID cannot be negative");
		}

		//update IDCounter if incoming ID is higher
		if (userID >= IDCounter) {
			IDCounter = userID + 1;
		}

		return userID;
	}

	//get the next available user ID
	//return current highest IDCounter
	public static int getNextAvailableID() {
		return IDCounter;
	}
		
		
	//Load messages from inbox file
	private List<Message> loadMessageInbox() {
		List<Message> messages = new ArrayList<>();
		String filename = username + ID + "inbox.text";
			
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
				while (true) {
					try {
						Message message = (Message) ois.readObject();
						messages.add(message);
					} catch (EOFException e) {
						break;
					}
				}
		} catch (FileNotFoundException e) {
			System.err.out.println("Error. File name not found: " + e.getMessage());
		} catch (IOException | ClassNotFoundException e) {
			System.err.println("Error reading inbox file: " + e.getMessage());
		}
			
		return messages;
	}
		

	//Load chatrooms IDs from the charooms file
	private List<Integer> loadChatrooms() {
		
		List<Integer> chatrooms = new ArrayList<>();			
		String filename = username + ID + "chatrooms.text";
				
		try(BufferedReader reader = new BufferedReader(new FileReader(filename))){
				
			String line;		
			while ((line = reader.readLine()) != null) {
					
				try {				
					chatrooms.add(Integer.parseInt(line.trim()));			
				} catch (NumberFormatException e) {				
					System.err.println("Invcalid chatroom ID in file: " + line);			
				}			
			}
						
		} catch (FileNotFoundException e) {		
			System.err.println("Error. File name not found: " + e.getMessage());
		} catch (IOException e) {	
			System.err.println("Error reading chatrooms file: " + e.getMessage());
		}
		
		return chatrooms;
		
	}
	
	
	
	//Save message to inbox file
	private void saveMessageToFile(Message message) {
		String filename = username + ID + "inbox.text";
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename, true))) {
			oos.writeObject(message);
		} catch (IOException e) {
			System.err.println("Error writing to inbox file: " + e.getMessage());
		}
	}
		
		
	//save chatroom ID to chatrooms file
	private void saveChatroomToFile(int chatroomID) {
		String filename = username + ID + "chatrooms.text";
		try (PrintWriter writer = new PrintWriter(new FileWriter(filename, true))) {
			writer.println();
		} catch (IOException e) {
			System.err.println("Error writing to chatrooms file: " + e.getMessage());
		}
	}	
		
	//display messages in inbox with console formatting for testing without GUI use
	//thought this could be useful 
	public void displayMessageInboxToConsole() {
		for (Message message : messageInbox) {
			System.out.println("From: " + message.getFromUserName());
			System.out.println("Date: " + message.getDateSent());
			System.out.println("Type: " + message.getMessageType());
			System.out.println("Content: " + message.getContents());
			System.out.println("--------------------");
		}
	}
	
	
	
	
	//Get user's username
	//return username
	public String getUsername() {
		return username;
	}
	
	//Get the user's password
	//return password
	public String getPassword() {
		return password;
	}
	
	//Get the user's ID
	//return ID
	public int getID() {
		return ID;
	}
	
	//Get the user's online status
	//return true if online, false if offline
	public boolean getStatus() {
		return status;
	}
	
	//Get the user's admin status
	//return true if admin, false if non-admin
	public boolean getAdminStatus() {
		return adminStatus;
	}
	
	//Get list of chatrooms the user is involved in
	//return list of chatroom IDs
	public List<Integer> getChatrooms() {
		return new ArrayList<>(involvedChatrooms);
	}
	
	//Add a chatroom to the user's involved chatrooms list
	//Update: added functionality to save inovolved chatroomID to chatroom file
	public void addChatroom(int chatroomID) {
		if (!involvedChatrooms.contains(chatroomID)) {
			involvedChatrooms.add(chatroomID);
			saveChatroomToFile(chatroomID);
		}
	}
	
	//Add a message to the user's inbox
	//Update: added ability to save message to file
	public void addToInbox(Message message) {
		messageInbox.add(message);
		saveMessageToFile(message);
	}
	
	//Display all messages in the user's inbox
	public void displayMessageInbox() {
		for (Message message : messageInbox) {
			System.out.println(message.toString());
		}
	}
	
	//Set a new password for the user
	public void setPassword(String password) {
		this.password = password;
	}
		
	//Set the user's online status
	protected void setStatus(boolean status) {
		this.status = status;
	}
	
	
	
	
}
