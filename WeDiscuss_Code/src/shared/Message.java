package shared;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

import server.*;

public class Message implements Serializable{
	private String contents;
	private Date dateSent;
	private String toUserName;
	private int toUserID;
	private String fromUserName;
	private int fromUserID;
	private int toChatroomID;
	private static int messageIDCounter = 0;
	private	int messageID;
	private MessageType messageType;
	
	
	private User user;
	private ConcurrentMap<Integer, Chatroom> chatroomMap;
	private ConcurrentMap<Integer, String> userMap;
	private Chatroom chatroom;
	
	
	
	public Message(MessageCreator messageCreator) {
		this.contents = messageCreator.getContents();
		this.dateSent = new Date();
		this.toUserName = messageCreator.getToUserName();
		this.toUserID = messageCreator.getToUserID();
		this.fromUserName = messageCreator.getFromUserName();
		this.toChatroomID = messageCreator.getChatroomID();
		this.messageID = messageIDCounter++;
		this.messageType = messageCreator.getMessageType();
		this.user = messageCreator.getUser();
		this.chatroom = messageCreator.getChatroom();
		this.chatroomMap = messageCreator.getChatroomMap();
		this.userMap = messageCreator.getUserMap();
		
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
	
	public int getToChatroomID() {
		return this.toChatroomID;
	}
	
	public int getMessageID() {
		return this.messageID;
	}
	
	public MessageType getMessageType() {
		return this.messageType;
	}
	
	public User getUser() {
		return this.user;
	}
	
	public Chatroom getChatroom() {
		return this.chatroom;
	}
	
	public ConcurrentMap<Integer, Chatroom> getChatroomMap() {
		return this.chatroomMap;
	}
	
	public ConcurrentMap<Integer, String> getUserMap() {
		return this.userMap;
	}
	
	public void outputMessage(MessageType messageType) {
		System.out.println("Message type: " + messageType);
	}
	
	public String toString()
	{
		String result ="";
		if (this.messageType == MessageType.UTU)
		{
			result = "FROM " + this.fromUserName + ": " + this.contents;
		}
		
		else if(this.messageType == MessageType.UTC)
		{
			result = "FROM: " + this.fromUserName + ": " + this.contents;
		}
		
		else if(this.messageType == MessageType.GUL)
		{
			result = "FROM: " + this.fromUserName + ": " + this.contents + "  /TO "+ this.toUserName;
		}
		
		else if(this.messageType == MessageType.GCL)
		{
			result = "FROM: " + this.fromUserName + ": " + this.contents + "  /TO Chatroom ID:"+ this.toChatroomID;
		}
		return result;
	}
	
	
}
