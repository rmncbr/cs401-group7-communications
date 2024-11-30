package shared;
import java.io.Serializable;
import java.util.*;

public class Chatroom implements Serializable {
	private int id;
	private List<Integer> members = Collections.synchronizedList(new ArrayList<Integer>());;
	private List<Message> messages = Collections.synchronizedList(new ArrayList<Message>());
	
	public Chatroom(int chatroomID) {
		this.id = chatroomID;
	}
	
	public Chatroom(int chatroomID, int creatorID) {
		this.id = chatroomID;
		this.members.add(creatorID);
	}
	
	public int getChatroomID() {
		return this.id;
	}
	
	public List<Message> getMessages() {
		return this.messages;
	}
	
	public void addMessage(Message message) {
		this.messages.add(message);
	}
	
	public void displayMessages() {
		for (Message message : this.messages) {
			System.out.println(message);
		}
	}
	
	public Boolean findMember(int userID) {
		if (this.members.contains(userID)) {
            return true;        } 
		else {
            return false;
        }
    }
	
	public void addMember(int userID) {
		this.members.add(userID);
	}
	
	public void removeMember(int userID) {
		this.members.remove(userID);
	}
}



