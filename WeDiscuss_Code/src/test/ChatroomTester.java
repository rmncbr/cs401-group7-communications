package test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import shared.*;
import server.User;

import java.util.List;

public class ChatroomTester {
    private Chatroom chatroom;
    private Message message;

    @BeforeEach
    public void setUp() {
        chatroom = new Chatroom(1, 02);
        message = new Message(new MessageCreator(MessageType.UTU));
    }

    @Test
    public void testGetChatroomID() {
        assertEquals(1, chatroom.getChatroomID());
    }

    @Test
    public void testAddMember() {
        int userID = 01;
        chatroom.addMember(userID);
        assertTrue(chatroom.findMember(userID));
    }

    @Test
    public void testRemoveMember() {
        int userID = 01;
        chatroom.addMember(userID);
        chatroom.removeMember(userID);
        assertFalse(chatroom.findMember(userID));
    }

    @Test
    public void testAddMessage() {
        chatroom.addMessage(message);
        List<Message> messages = chatroom.getMessages();
        assertEquals(1, messages.size());
        assertEquals(message, messages.get(0));
    }

    @Test
    public void testGetMessages() {
        Message message1 = new Message(new MessageCreator(MessageType.UTU));
        Message message2 = new Message(new MessageCreator(MessageType.UTC));
        chatroom.addMessage(message1);
        chatroom.addMessage(message2);

        List<Message> messages = chatroom.getMessages();
        assertEquals(2, messages.size());
        assertEquals(message1, messages.get(0));
        assertEquals(message2, messages.get(1));
    }

    @Test
    public void testFindMember() {
        int userID = 456;
        chatroom.addMember(userID);
        assertTrue(chatroom.findMember(userID));
        assertFalse(chatroom.findMember(789));
    }
}

