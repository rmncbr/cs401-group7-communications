package client;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import server.User;
import shared.*;

public class ClientUI {
	private ArrayList<Message> userMessages = new ArrayList<Message>();
	private ConcurrentHashMap<Integer, Chatroom> chatrooms = new ConcurrentHashMap<Integer, Chatroom>();
	private ConcurrentHashMap<Integer, String> userMap = new ConcurrentHashMap<Integer, String>();
	
	private Client client;
	
	public ClientUI() {
		client = new Client(this); // Init Client w/ this GUI
	}
	
	public static void main(String[] args) {
		
		
	}
	
	public void initUpdate(Message message) {
		
	}
	
	public void update(Message message) {

	}
}
