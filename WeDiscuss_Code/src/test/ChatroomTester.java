package test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import shared.*;


import java.util.List;

public class ChatroomTester {
    private Chatroom chatroom;
    private Message message;

    @BeforeEach
    public void setUp() {
        chatroom = new Chatroom(100);
        message = new Message(new MessageCreator(MessageType.UTU));
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up test files after each test
        int chatroomID = chatroom.getChatroomID();
        new java.io.File(chatroomID + "Messages.txt").delete();
        new java.io.File(chatroomID + "Members.txt").delete();
    }

    @Test
    public void testGetChatroomID() {
        assertTrue(chatroom.getChatroomID() > 0);
    }

    @Test
    public void testAddMember() {
        int userID = 1;
        chatroom.addMember(userID);
        assertTrue(chatroom.findMember(userID));
    }

    @Test
    public void testRemoveMember() {
        int userID = 12;
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
        int userID = 4;
        chatroom.addMember(userID);
        assertTrue(chatroom.findMember(userID));
        assertFalse(chatroom.findMember(7));
    }
    
    @Test
    public void testFileCreationOnInit() {
        int chatroomID = chatroom.getChatroomID();
        assertTrue(new java.io.File(chatroomID + "Messages.txt").exists());
        assertTrue(new java.io.File(chatroomID + "Members.txt").exists());
    }
    
    @Test
	public void testDisplayMessages() {
		Message message1 = new Message(new MessageCreator(MessageType.UTU));
		Message message2 = new Message(new MessageCreator(MessageType.UTC));
		chatroom.addMessage(message1);
		chatroom.addMessage(message2);

		chatroom.displayMessages();
	}
    
    @Test
    public void testSaveMembersToFile() {
        int userID = 1;
        chatroom.addMember(userID);

        // Check file content
        try (java.util.Scanner scanner = new java.util.Scanner(new java.io.File(chatroom.getChatroomID() + "Members.txt"))) {
            assertTrue(scanner.hasNextLine());
            assertEquals(Integer.toString(userID), scanner.nextLine());
        } catch (Exception e) {
            fail("Exception while reading members file: " + e.getMessage());
        }
    }
    
    
   

    
}
