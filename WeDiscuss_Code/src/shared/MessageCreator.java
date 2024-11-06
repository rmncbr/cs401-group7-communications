package shared;
import java.util.*;

public class MessageCreator {
	private String contents;
	private String toUserName;
	private int toUserID;
	private String fromUserName;
	private int fromUserID;
	private int toChatroom;
	private MessageType messageType;
	// private User user;
	//Map<Integer, User> chatroomMapping;
	Map<Integer, String> userMap;
	
	public MessageCreator(MessageType messageType) {
		this.messageType = messageType;
		//this.chatroomMapping = new HashMap<Integer, User>();
		this.userMap = new HashMap<Integer, String>();
	}
	
	public void setContents(String contents) {
		this.contents = contents;
	}
	
	public void setToUserName(String toUserName) {
		this.toUserName = toUserName;
	}
	
	public void setToUserID(int toUserID) {
		this.toUserID = toUserID;
	}
	
	public void setFromUserName(String fromUserName) {
		this.fromUserName = fromUserName;
	}
	
	public void setFromUserID(int fromUserID) {
		this.fromUserID = fromUserID;
	}
	
	public void setToChatroom(int toChatroom) {
		this.toChatroom = toChatroom;
	}
	
	public void setUser(/*User user*/) {
        //this.user = user;
    }
	
	public void setUserMap(/* Map<Integer, User> chatroomMapping */) {
		// this.chatroomMapping = chatroomMapping;
	}
	
	public void setUserMap(Map<Integer, String> userMap) {
		this.userMap = userMap;
	}
	
	public String getContents() {
		return this.contents;
	}
	
	public String getUserName() {
		return this.toUserName;
	}
	
	public int getUserID() {
		return this.toUserID;
	}
	
	public Integer getChatroom() {
		return this.toChatroom;
	}
	
	// Get user function
	
	public Map<Integer, String> getUserMap() {
		return this.userMap;
	}
	
	// get Chat room Map Function
	
	public Message createMessage() {
		Message message = new Message(this);
		return message;
	}
	
	
	
}
