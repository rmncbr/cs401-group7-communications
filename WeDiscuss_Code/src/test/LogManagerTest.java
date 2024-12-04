// LogManagerTest.java
package test;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import server.LogManager;
import shared.Message;
import shared.MessageCreator;
import shared.MessageType;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class LogManagerTest {

    @Test
    public void testStoreMessage_UserMessage() {
        // Initialize LogManager with empty user and chatroom IDs
        List<Integer> allUserIDs = new ArrayList<>();
        List<Integer> allChatroomIDs = new ArrayList<>();
        LogManager logManager = new LogManager(allUserIDs, allChatroomIDs);

        // Create a user-to-user message
        MessageCreator creator = new MessageCreator(MessageType.UTU);
        creator.setFromUserID(1);
        creator.setFromUserName("User1");
        creator.setToUserID(2);
        creator.setToUserName("User2");
        creator.setContents("Hello, User 2!");
        creator.setDate(System.currentTimeMillis());
        Message message = creator.createMessage();

        // Store the message
        logManager.storeMessage(message);

        // Retrieve user message logs
        ConcurrentHashMap<Integer, List<Message>> userLogs = LogManager.getUserMessageLogs();

        // Verify that the message is stored for the correct user
        List<Message> messagesForUser2 = userLogs.get(2);
        assertNotNull(messagesForUser2);
        assertEquals(1, messagesForUser2.size());
        assertEquals("Hello, User 2!", messagesForUser2.get(0).getContents());

        // Clean up any created files (optional)
        new File("2userlog.txt").delete();
    }

    @Test
    public void testStoreMessage_ChatroomMessage() {
        // Initialize LogManager with empty user and chatroom IDs
        List<Integer> allUserIDs = new ArrayList<>();
        List<Integer> allChatroomIDs = new ArrayList<>();
        LogManager logManager = new LogManager(allUserIDs, allChatroomIDs);

        // Create a user-to-chatroom message
        MessageCreator creator = new MessageCreator(MessageType.UTC);
        creator.setFromUserID(1);
        creator.setFromUserName("User1");
        creator.setToChatroom(100);
        creator.setContents("Hello, Chatroom 100!");
        creator.setDate(System.currentTimeMillis());
        Message message = creator.createMessage();

        // Store the message
        logManager.storeMessage(message);

        // Retrieve chatroom message logs
        ConcurrentHashMap<Integer, List<Message>> chatroomLogs = LogManager.getChatroomMessageLogs();

        // Verify that the message is stored for the correct chatroom
        List<Message> messagesForChatroom100 = chatroomLogs.get(100);
        assertNotNull(messagesForChatroom100);
        assertEquals(1, messagesForChatroom100.size());
        assertEquals("Hello, Chatroom 100!", messagesForChatroom100.get(0).getContents());

        // Clean up any created files (optional)
        new File("100chatlog.txt").delete();
    }

    @Test
    public void testLoadUserMessages() {
        // Prepare user IDs and simulate existing messages
        List<Integer> userIDs = new ArrayList<>();
        userIDs.add(1);

        // Write sample data to "1userlog.txt"
        try (FileWriter writer = new FileWriter("1userlog.txt")) {
            // Format: fromUserID|contents|date|toUserName|messageType
            writer.write("1|Hello from user 1|1234567890|User2|UTU\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Initialize LogManager
        LogManager logManager = new LogManager(userIDs, new ArrayList<>());

        // Check that message logs are initialized for user 1
        ConcurrentHashMap<Integer, List<Message>> userLogs = LogManager.getUserMessageLogs();

        List<Message> messagesForUser1 = userLogs.get(1);
        assertNotNull( messagesForUser1);
        assertEquals(1, messagesForUser1.size());
        assertEquals( "Hello from user 1", messagesForUser1.get(0).getContents());
        assertEquals( 1, messagesForUser1.get(0).getFromUserID());
        assertEquals("User2", messagesForUser1.get(0).getToUserName());

        // Clean up the test files
        new File("1userlog.txt").delete();
    }

    @Test
    public void testLoadChatroomMessages() {
        // Prepare chatroom IDs and simulate existing messages
        List<Integer> chatroomIDs = new ArrayList<>();
        chatroomIDs.add(100);

        // Write sample data to "100chatlog.txt"
        try (FileWriter writer = new FileWriter("100chatlog.txt")) {
            // Format: fromUserID|contents|date|ignored|toChatroomID
            writer.write("1|Hello chatroom 100|1234567890|ignored|100\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Initialize LogManager
        LogManager logManager = new LogManager(new ArrayList<>(), chatroomIDs);

        // Check that message logs are initialized for chatroom 100
        ConcurrentHashMap<Integer, List<Message>> chatroomLogs = LogManager.getChatroomMessageLogs();

        List<Message> messagesForChatroom100 = chatroomLogs.get(100);
        assertNotNull( messagesForChatroom100);
        assertEquals( 1, messagesForChatroom100.size());
        assertEquals( "Hello chatroom 100", messagesForChatroom100.get(0).getContents());
        assertEquals( 1, messagesForChatroom100.get(0).getFromUserID());
        assertEquals( 100, messagesForChatroom100.get(0).getToChatroomID());

        // Clean up the test files
        new File("100chatlog.txt").delete();
    }
}
