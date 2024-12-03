package test;

import static org.junit.jupiter.api.Assertions.*;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.lang.reflect.Field;

import server.User;
import shared.Message;
import shared.MessageCreator;
import shared.MessageType;

import java.util.concurrent.ConcurrentHashMap;

public class UserTester {
	private static final String TEMP_DIR_PREFIX = "user_test_";
    private Path tempDir;
    
    
    @BeforeEach
    public void setUp() throws Exception {
        // Create a temporary directory
        tempDir = Files.createTempDirectory(TEMP_DIR_PREFIX);
        // Change the working directory to the temp directory
        System.setProperty("user.baseDir", tempDir.toString());
        // Reset the IDCounter in User class
        Field idCounterField = User.class.getDeclaredField("IDCounter");
        idCounterField.setAccessible(true);
        idCounterField.setInt(null, 0);
        
    }
    

    @AfterEach
    public void tearDown() throws IOException {
        // Delete the temporary directory and its contents
        Files.walk(tempDir)
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
        System.clearProperty("user.baseDir");
        
    }
    
   
    
    @Test
    public void testUserConstructorWithUserID() {
        // Create a user with specific ID
        int userID = 11;
        User user = new User("testuser", "password123", true, userID);

        // Check that the user properties are set correctly
        assertEquals("testuser", user.getUsername());
        assertEquals("password123", user.getPassword());
        assertTrue(user.getAdminStatus());
        assertEquals(userID, user.getID());
        assertFalse(user.getStatus()); // Should be initialized to false
        assertFalse(user.getChatrooms().isEmpty()); // Should be false since the chatrooms are created in constructor
        assertFalse(user.getMessagesFromUsers().isEmpty()); // Should be false since the messagesInbox are created in constructor
    }
    
    @Test
    public void testUserConstructorWithoutUserID() {
        User user = new User("newuser", "newpassword", false);

        int expectedID = 12; // As per the calculation in User class
        assertEquals("newuser", user.getUsername());
        assertEquals("newpassword", user.getPassword());
        assertFalse(user.getAdminStatus());
        assertEquals(expectedID, user.getID());
        assertFalse(user.getStatus()); // Should be initialized to false
        assertTrue(user.getChatrooms().isEmpty());
        assertTrue(user.getMessagesFromUsers().isEmpty());

        // Check that the Inbox and Chats files are created
        String inboxFileName = user.getID() + "Inbox.txt";
        String chatsFileName = user.getID() + "Chats.txt";

        File inboxFile = new File(inboxFileName);
        File chatsFile = new File(chatsFileName);

        assertTrue(inboxFile.exists());
        assertTrue(chatsFile.exists());
    }

    @Test
    public void testMultipleUsersCreatedWithoutUserID() {
        User user1 = new User("user1", "pass1", false);
        User user2 = new User("user2", "pass2", false);
        User user3 = new User("user3", "pass3", false);

        assertEquals(12, user1.getID());
        assertEquals(18, user2.getID());
        assertEquals(24, user3.getID());
    }

    @Test
    public void testAddChatroom() throws Exception {
        User user = new User("testuser", "password123", false, 11);

        user.addChatroom(2001);
        user.addChatroom(2002);

        List<Integer> chatrooms = user.getChatrooms();
        assertEquals(2, chatrooms.size());
        assertTrue(chatrooms.contains(2001));
        assertTrue(chatrooms.contains(2002));

        // Check that the Chats.txt file contains the chatroom IDs
        String chatsFileName = user.getID() + "Chats.txt";
        File chatsFile = new File(chatsFileName);
        assertTrue(chatsFile.exists());

        // Read the file and check its contents
        List<String> lines = Files.readAllLines(chatsFile.toPath());
        assertEquals(2, lines.size());
        assertTrue(lines.contains("2001"));
        assertTrue(lines.contains("2002"));
    }

    @Test
    public void testSetPassword() {
        User user = new User("testuser", "oldpassword", false, 11);
        assertEquals("oldpassword", user.getPassword());
        user.setPassword("newpassword");
        assertEquals("newpassword", user.getPassword());
    }

    @Test
    public void testSetStatus() {
        User user = new User("testuser", "password", false, 11);
        assertFalse(user.getStatus());
        user.setStatus(true);
        assertTrue(user.getStatus());
    }

    @Test
    public void testAddToInbox() throws Exception {
        User user = new User("testuser", "password123", false, 1001);
        assertTrue(user.getMessagesFromUsers().isEmpty());

        // Create a message
        MessageCreator creator = new MessageCreator(MessageType.UTU);
        creator.setContents("Hello, this is a test message.");
        creator.setDate(System.currentTimeMillis());
        creator.setFromUserID(1002);
        creator.setFromUserName("senderUser");
        creator.setToUserID(1001);
        creator.setToUserName("testuser");

        Message message = new Message(creator);

        user.addToInbox(message);

        ConcurrentHashMap<Integer, List<Message>> messagesFromUsers = user.getMessagesFromUsers();
        assertTrue(messagesFromUsers.isEmpty());
        assertTrue(messagesFromUsers.containsKey(1002));

        List<Message> messagesFromSender = messagesFromUsers.get(1002);
        assertEquals(1, messagesFromSender.size());
        assertEquals(message, messagesFromSender.get(0));

        // Check that the Inbox.txt file contains the message
        String inboxFileName = tempDir.toString() + File.separator + user.getID() + "Inbox.txt";
        File inboxFile = new File(inboxFileName);
        assertTrue(inboxFile.exists());

        // Read the file and check its contents
        List<String> lines = Files.readAllLines(inboxFile.toPath());
        assertEquals(1, lines.size());
        
    }

    @Test
    public void testLoadChatrooms() throws Exception {
        User user = new User("testuser", "password", false, 1001);

        // Manually create a Chats.txt file with known data
        String chatsFileName = user.getID() + "Chats.txt";
        List<String> lines = Arrays.asList("2001", "2002", "2003");
        Files.write(Paths.get(chatsFileName), lines);

        // Clear the current chatrooms list
        user.getChatrooms().clear();

        // Call loadChatrooms()
        user.loadChatrooms();

        List<Integer> chatrooms = user.getChatrooms();
        assertEquals(3, chatrooms.size());
        assertTrue(chatrooms.contains(2001));
        assertTrue(chatrooms.contains(2002));
        assertTrue(chatrooms.contains(2003));
    }
    
}
