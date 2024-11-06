package shared;

import java.util.*;

public class Message {
	private String contents;
	private Date dateSent;
	private String toUserName;
	private int toUserID;
	private String fromUserName;
	private int fromUserID;
	private int toChatroom;
	private static int messageIDCounter;
	private	int messageID;
	private MessageType messageType;
	
	public Message(MessageCreator messageCreator) {
		
    
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
