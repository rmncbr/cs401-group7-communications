package server;
import java.util.*;

import shared.Message;

public class User {
	//static counter to generate unique IDs
	private static int IDCounter = 0;
	
	private String username;
	private String password;
	private int ID;
	private boolean status; //False = offline, True = online
	private boolean adminStatus; //False = non-admin, True = admin
	private List<Message> messageInbox;
	private List<Integer> involvedChatrooms;
	
	//Constructor
	public User(String username, String password, boolean adminStatus) {
		this.username = username;
		this.password = password;
		this.adminStatus = adminStatus;
		this.ID = ++IDCounter;
		this.status = false; //Initially offline
		this.messageInbox = new ArrayList<>();
		this.involvedChatrooms = new ArrayList<>();
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
	public void addChatroom(int chatroomID) {
		if (!involvedChatrooms.contains(chatroomID)) {
			involvedChatrooms.add(chatroomID);
		}
	}
	
	//Set a new password for the user
	public void setPassword(String password) {
		this.password = password;
	}
	
	//Add a message to the user's inbox
	public void addToInbox(Message message) {
		messageInbox.add(message);
	}
	
	//Display all messages in the user's inbox
	public void displayMessageInbox() {
		for (Message message : messageInbox) {
			System.out.println(message.toString());
		}
	}
	
	//Set the user's online status
	protected void setStatus(boolean status) {
		this.status = status;
	}

}
