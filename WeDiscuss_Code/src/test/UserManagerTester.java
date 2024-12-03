package test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class UserManagerTest {
    private UserManager userManager;
    private List<File> testFiles;

    @BeforeEach
    void setUp() {
        userManager = new UserManager();
        testFiles = new ArrayList<>();
    }

    @AfterEach
    void cleanup() {
        // Clean up any test files created during initialization
        cleanupUserFiles();

        // Clean up the main user file
        File userFile = new File("UserFile.txt");
        if (userFile.exists()) {
            userFile.delete();
        }
    }

    private void cleanupUserFiles() {
        // Clean up any user-specific files created during tests
        for (Integer userId : userManager.getAllUserIDs()) {
            // Delete inbox file
            File inboxFile = new File(userId + "Inbox.txt");
            if (inboxFile.exists()) {
                inboxFile.delete();
            }

            // Delete chats file
            File chatsFile = new File(userId + "Chats.txt");
            if (chatsFile.exists()) {
                chatsFile.delete();
            }
        }
    }

    @Test
    void testGetUsername() {
        // Testing existing username lookup
        String username = userManager.getUsername(1);
        assertNotNull(username, "Username should be returned for valid ID");

        // Testing non-existent username
        assertNull(userManager.getUsername(999), "Should return null for non-existent ID");
    }

    @Test
    void testGetUserID() {
        // Get first user's ID (should exist after initialization)
        int id = userManager.getUserID(userManager.getUsername(1));
        assertTrue(id > 0, "Should return positive ID for existing user");

        // Testing non-existent user
        assertEquals(-1, userManager.getUserID("nonexistentUser"),
            "Should return -1 for non-existent username");
    }

    @Test
    void testGetAllUserIDs() {
        List<Integer> userIDs = userManager.getAllUserIDs();
        assertTrue(userIDs.contains(1));
        assertTrue(userIDs.contains(2));
        assertEquals(2, userIDs.size());
        // Verify the list is not null and contains elements
        assertNotNull(userManager.getAllUserIDs(), "User ID list should not be null");
        assertFalse(userManager.getAllUserIDs().isEmpty(), "User ID list should not be empty");
    }

    @Test
    void testGetUser() {
        // Get first user (should exist after initialization)
        String firstUsername = userManager.getUsername(1);
        User user = userManager.getUser(1);

        assertNotNull(user, "User should not be null");
        assertEquals(firstUsername, user.getUsername(), "Username should match");
        assertEquals(1, user.getID(), "User ID should match");

        // Test non-existent user
        assertNull(userManager.getUser(999), "Should return null for non-existent user ID");
    }

    @Test
    void testAddChatroomToUser() {
        // Get an existing user
        User user = userManager.getUser(1);
        assertNotNull(user, "Test user should exist");

        // Initial chatroom count
        int initialChatroomCount = user.getChatrooms().size();

        // Add a new chatroom
        int newChatroomId = 100;
        userManager.addChatroomToUser(1, newChatroomId);

        // Verify chatroom was added
        assertEquals(initialChatroomCount + 1, user.getChatrooms().size(),
            "Chatroom count should increase by 1");
        assertTrue(user.getChatrooms().contains(newChatroomId),
            "Added chatroom should be in user's chatroom list");

        // Test adding same chatroom again (shouldn't duplicate)
        userManager.addChatroomToUser(1, newChatroomId);
        assertEquals(initialChatroomCount + 1, user.getChatrooms().size(),
            "Adding same chatroom again should not increase count");
    }

    @Test
    void testSaveUsers(@TempDir Path tempDir) {
        // Initial state - modified should be false
        assertFalse(userManager.modified, "Modified flag should be false initially");

        // Make a modification (add a chatroom)
        userManager.addChatroomToUser(1, 100);
        assertTrue(userManager.modified, "Modified flag should be true after modification");

        // Save users
        userManager.saveUsers();
        assertFalse(userManager.modified, "Modified flag should be false after saving");

        // Verify file was created
        assertTrue(tempDir.resolve("UserFile.txt").toFile().exists(),
            "User file should be created after saving");
    }

    @Test
    void testFileCleanup() {
        // Create a test file
        File testFile = new File("testFile.txt");
        try {
            testFile.createNewFile();
            testFiles.add(testFile);
        } catch (IOException e) {
            fail("Failed to create test file");
        }

        // Verify file exists
        assertTrue(testFile.exists(), "Test file should exist before cleanup");

        // Run cleanup
        cleanup();

        // Verify file was deleted
        assertFalse(testFile.exists(), "Test file should be deleted after cleanup");
    }
}
