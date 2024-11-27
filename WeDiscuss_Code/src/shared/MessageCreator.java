package shared;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

import server.*;

public class MessageCreator {
	private String contents;
	private String toUserName;
	private int toUserID;
	private String fromUserName;
	private int fromUserID;
	private int toChatroomID;
	private MessageType messageType;
	private User user;
	private Chatroom chatroom;
	private ConcurrentMap<Integer, Chatroom> chatroomMapping;
	private ConcurrentMap<Integer, String> userMap;
	
	public MessageCreator(MessageType messageType) {
		this.messageType = messageType;
		this.contents = null;
		this.toUserName = null;
		this.toUserID = -1;
		this.fromUserName = null;
		this.fromUserID = -1;
		this.toChatroomID = -1;
		this.user = null;
		this.chatroom = null;
		this.chatroomMapping = null;
		this.userMap = null;
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
	
	public void setToChatroom(int toChatroomid) {
		this.toChatroomID = toChatroomid;
	}
	
	public void setUser(User user) {
        this.user = user;
    }
	
	public void setChatroom(Chatroom chatroom) {
        this.chatroom = chatroom;
    }
	
	public void setChatroomMap( ConcurrentMap<Integer, Chatroom> chatroomMapping ) {
		this.chatroomMapping = chatroomMapping;
	}
	
	public void setUserMap(ConcurrentMap<Integer, String> userMap) {
		this.userMap = userMap;
	}
	
	public String getContents() {
		return this.contents;
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
	
	public MessageType getMessageType() {
		return this.messageType;
	}
	
	public int getChatroomID() {
		return this.toChatroomID;
	}
	
	public User getUser() {
        return this.user;
    }
	
	public Chatroom getChatroom() {
		return this.chatroom;
	}
	
	public ConcurrentMap<Integer, Chatroom> getChatroomMap() {
		return this.chatroomMapping;
	}
	
	public ConcurrentMap<Integer, String> getUserMap() {
		return this.userMap;
	}
	
	// get Chat room Map Function
	
	public Message createMessage() {
		Message message = new Message(this);
		return message;
	}
	
	
	
	
}
