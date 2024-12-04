package test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import server.ChatroomManager;
import server.Server;
import shared.Chatroom;
import shared.Message;
import shared.MessageCreator;
import shared.MessageType;

import java.net.UnknownHostException;
import java.util.List;

public class ChatroomManagerTester {
    private ChatroomManager chatroomManager;
    private Server server;

    @BeforeEach
    void setUp() {
        try {
			server = new Server(8080);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} //create new server instance
        chatroomManager = new ChatroomManager(server);
    }

    @Test
    public void testGetAllChatroomIDs() {
        List<Integer> ids = chatroomManager.getAllChatroomIDs();
        assertNotNull(ids, "Chatroom IDs list should not be null");
    }

    @Test
    public void testGetChatroom() {
        //create chatroom
        MessageCreator creator = new MessageCreator(MessageType.CC);
        creator.setToChatroom(1);
        creator.setFromUserID(1);
        Message createMessage = new Message(creator);

        chatroomManager.createChatroom(null, createMessage);

        //test getting chatroom
        Chatroom chatroom = chatroomManager.getChatroom(1);
        assertNotNull(chatroom, "Should return a chatroom for valid ID");
        assertEquals(1, chatroom.getChatroomID(), "Chatroom should match");

        //test non-existing chatroom
        Chatroom nonexistent = chatroomManager.getChatroom(999);
        assertNull(nonexistent, "Should return null for non-existent chatroom");
    }

    @Test
    public void testSaveUsers() {
        //create chatroom
        MessageCreator creator = new MessageCreator(MessageType.CC);
        creator.setToChatroom(1);
        creator.setFromUserID(1);
        Message createMessage = new Message(creator);

        chatroomManager.createChatroom(null, createMessage);

        //Test saving
        chatroomManager.saveUsers();

        //create new ChatroomManager to load from file
        ChatroomManager newManager = new ChatroomManager(server);
        Chatroom loadedChatroom = newManager.getChatroom(1);

        assertNotNull(loadedChatroom, "Chatroom should be loaded from file");
        assertEquals(1, loadedChatroom.getChatroomID(),
            "Loaded chatroom should have correct ID");
    }

    @Test
    public void testCreateChatroom() {
        MessageCreator creator = new MessageCreator(MessageType.CC);
        creator.setToChatroom(1);
        creator.setFromUserID(1);
        Message createMessage = new Message(creator);

        chatroomManager.createChatroom(null, createMessage);

        Chatroom chatroom = chatroomManager.getChatroom(1);
        assertNotNull(chatroom, "Chatroom should be created");
        assertTrue(chatroomManager.getAllChatroomIDs().contains(chatroom.getChatroomID()),
            "Chatroom ID should be in the list of all chatroom IDs");
    }

    @Test
    public void testCreateInvalidChatroom() {
        MessageCreator creator = new MessageCreator(MessageType.CC);
        creator.setFromUserID(1);
        Message createMessage = new Message(creator);

        int initialSize = chatroomManager.getAllChatroomIDs().size();
        chatroomManager.createChatroom(null, createMessage);

        assertEquals(initialSize, chatroomManager.getAllChatroomIDs().size(),
        		"No new chatroom should be created with invalid message");
    }

    @AfterEach
    public void tearDown() {
        //cleanup
        chatroomManager = null;
        server = null;
    }


}
