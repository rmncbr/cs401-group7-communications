package shared;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

import server.*;

public class Message implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
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
		this.dateSent = messageCreator.getDate();
		this.toUserName = messageCreator.getToUserName();
		this.toUserID = messageCreator.getToUserID();
		this.fromUserName = messageCreator.getFromUserName();
		this.fromUserID = messageCreator.getFromUserID();
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
			result = "FROM: " + this.fromUserName + "/ " + this.contents;
		}
		
		else if(this.messageType == MessageType.UTC)
		{
			result = "FROM: " + this.fromUserName + "/ " + this.contents;
		}
		
		return result;
	}
	
	public String buildUserLog() {
		String result = "";
		
		result = "FROM: " + this.fromUserID + "/ " + this.contents + this.toUserName;
		
		return result;
	}
	
	public String buildChatLog() {
		String result = "";
		result = "FROM: " + this.fromUserID + "/ " + this.contents;
		
		return result;
	}
	
	public String storeChatroomMessage()
	{
		String result = "";
		result += this.contents + "|";
		result += this.dateSent.getTime() + "|";
		result += Integer.toString(this.toChatroomID) + "|";
		result += this.fromUserName + "|";
		result += Integer.toString(this.fromUserID) + "|";
		
		String type = typeToString(this.messageType);
		
		result += type;
		
		return result;
	}
	
	public String storeInboxMessage()
	{
		String result = "";
		result += this.contents + "|";
		result += this.dateSent.getTime() + "|";
		result += this.toUserName + "|";
		result += Integer.toString(this.toUserID) + "|";
		result += this.fromUserName + "|";
		result += Integer.toString(this.fromUserID) + "|";
		
		String type = typeToString(this.messageType);
		
		result += type;
		
		return result;
	}
	
	public String storeUserLogMessage() {
		String result = "";
		result += Integer.toString(this.fromUserID) + "|";
		result += this.contents + "|";
		result += this.dateSent.getTime() + "|";
		result += this.toUserName + "|";
		
		String type = typeToString(this.messageType);
		
		result += type;
		
		return result;
	}
	
	public String storeChatLogMessage() {
		String result = "";
		result += Integer.toString(this.fromUserID) + "|";
		result += this.contents + "|";
		result += this.dateSent.getTime() + "|";
		
		
		String type = typeToString(this.messageType);
		
		result += type;
		
		result += "|" + Integer.toString(this.toChatroomID);
		
		return result;
	}
	
	public String typeToString(MessageType type)
	{
		switch(type) {
		
			case LOGIN:
				return "LOGIN";
				
			case LOGOUT:
				return "LOGOUT";
	
			case ADDUSER:
				return "ADDUSER";
				
			case DELUSER:
				return "DELUSER";
				
			case CPWD:
				return "CPWD";
				
			case GUL:
				return "GUL";
	
			case GCL:
				return "GCL";
				
			case CC:
				return "CC";
	
			case IUC:
				return "IUC";
				
			case JC:
				return "JC";
				
			case LC:
				return "LC";
				
			case UTU:
				return "UTU";
				
			case UTC:
				return "UTC";
				
			default:
				return "ERROR";
		}
	}
	
}