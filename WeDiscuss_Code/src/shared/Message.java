package shared;

import java.io.Serializable;
import java.util.*;

public class Message implements Serializable{
	private String contents;
	private Date dateSent;
	private String toUserName;
	private int toUserID;
	private String fromUserName;
	private int fromUserID;
	private int toChatroom;
	private static int messageIDCounter = 0;
	private	int messageID;
	private MessageType messageType;
	
	/*
	private User user;
	private ConcurrentMap<Integer, Chatroom> chatroomMap;
	private ConcurrentMap<Integer, String> userMap;
	*/
	
	public Message(MessageCreator messageCreator) {
		this.contents = messageCreator.getContents();
		this.dateSent = new Date();
		this.toUserName = messageCreator.getToUserName();
		this.toUserID = messageCreator.getToUserID();
		this.fromUserName = messageCreator.getFromUserName();
		this.toChatroom = messageCreator.getChatroom();
		this.messageID = messageIDCounter++;
		this.messageType = messageCreator.getMessageType();
    
	}
	
	public String getContents() {
		return this.contents;
	}
	
	public Date getDateSent() {
		return this.dateSent;
	}
	
	public String getToUserName() {
		return this.toUserName;
	}
	
	public int getToUserID() {
		return this.toUserID;
	}
	
	public String getFromUserName() {
		return this.fromUserName;
	}
	
	public int getFromUserID() {
		return this.fromUserID;
	}
	
	public int getToChatroom() {
		return this.toChatroom;
	}
	
	public int getMessageID() {
		return this.messageID;
	}
	
	public MessageType getMessageType() {
		return this.messageType;
	}
	
	public void setToID(int toID) {
		this.toUserID = toID;
	}
	
	public void outputMessage(MessageType messageType) {
		System.out.println("Message type: " + messageType);
	}
	
}
