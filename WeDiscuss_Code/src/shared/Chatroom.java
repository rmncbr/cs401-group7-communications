package shared;
import java.util.*;

public class Chatroom extends Message {
	private int id;
	private List<Integer> members;
	private List<Message> messages;
	
	public Chatroom(int id, int creatorID) {
		super(new MessageCreator(MessageType.CC));
		this.id = id;
		this.members = new ArrayList<Integer>();
		this.members.add(creatorID);
		this.messages = new ArrayList<Message>();
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
	
	public void findMember(int userID) {
		if (this.members.contains(userID)) {
            System.out.println("Member found!");
        } else {
            System.out.println("Member not found!");
        }
    }
	
	public void addMember(int userID) {
		this.members.add(userID);
	}
	
	public void removeMember(int userID) {
		this.members.remove(userID);
	}
}



