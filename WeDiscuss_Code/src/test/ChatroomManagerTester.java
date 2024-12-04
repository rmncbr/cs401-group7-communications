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

 

    @AfterEach
    public void tearDown() {
        //cleanup
        chatroomManager = null;
        server = null;
    }


}
