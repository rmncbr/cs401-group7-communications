package test;


import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import shared.*;
import server.User;

public class MessageCreatorTester {
    private MessageCreator messageCreator;
    private User user;
    private Chatroom chatroom;

    @BeforeEach
    public void setUp() {
        this.messageCreator = new MessageCreator(MessageType.UTU);
        this.user = new User("1meza", "password123", false, 01);
        this.chatroom = new Chatroom(1, this.user.getID());
    }

    @Test
    public void testSetAndGetContents() {
        String contents = "Hello, this is a Test!";
        this.messageCreator.setContents(contents);
        assertEquals(contents, this.messageCreator.getContents());
    }

    @Test
    public void testSetAndGetToUserName() {
        String toUserName = "NewUser";
        this.messageCreator.setToUserName(toUserName);
        assertEquals(toUserName, this.messageCreator.getToUserName());
    }

    @Test
    public void testSetAndGetToUserID() {
        int toUserID = 02;
        this.messageCreator.setToUserID(toUserID);
        assertEquals(toUserID, this.messageCreator.getToUserID());
    }

    @Test
    public void testSetAndGetFromUserName() {
        String fromUserName = this.user.getUsername();
        this.messageCreator.setFromUserName(fromUserName);
        assertEquals(fromUserName, this.messageCreator.getFromUserName());
    }

    @Test
    public void testSetAndGetFromUserID() {
        int fromUserID = this.user.getID();
        this.messageCreator.setFromUserID(fromUserID);
        assertEquals(fromUserID, this.messageCreator.getFromUserID());
    }

    @Test
    public void testSetAndGetChatroomID() {
        int chatroomID = this.chatroom.getChatroomID();
        this.messageCreator.setToChatroom(chatroomID);
        assertEquals(chatroomID, this.messageCreator.getChatroomID());
    }

    @Test
    public void testSetAndGetUser() {
        this.messageCreator.setUser(this.user);
        assertEquals(this.user, this.messageCreator.getUser());
    }

    @Test
    public void testSetAndGetChatroom() {
        this.messageCreator.setChatroom(this.chatroom);
        assertEquals(this.chatroom, this.messageCreator.getChatroom());
    }

    @Test
    public void testCreateMessage() {
        this.messageCreator.setContents("This is a test message");
        this.messageCreator.setFromUserName(this.user.getUsername());
        this.messageCreator.setToUserName("NewUser");
        this.messageCreator.setFromUserID(this.user.getID());
        this.messageCreator.setToUserID(2);
        this.messageCreator.setToChatroom(3);
        this.messageCreator.setUser(this.user);
        this.messageCreator.setChatroom(this.chatroom);

        Message message = messageCreator.createMessage();

        assertEquals("This is a test message", message.getContents());
        assertEquals(this.user.getUsername(), message.getFromUserName());
        assertEquals("NewUser", message.getToUserName());
        assertEquals(this.user.getID(), message.getFromUserID());
        assertEquals(2, message.getToUserID());
        assertEquals(3, message.getToChatroomID());
        assertEquals(this.user, message.getUser());
        assertEquals(this.chatroom, message.getChatroom());
    }
}
