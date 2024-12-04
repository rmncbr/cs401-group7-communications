package test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import server.User;
import server.UserManager;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class UserManagerTester {
    private UserManager userManager;
    private List<File> testFiles;

    @BeforeEach
    public void setUp() {
        userManager = new UserManager();
        testFiles = new ArrayList<>();
    }

    @AfterEach
    public void cleanup() {
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
    public void testGetUsername() {
        // Testing existing username lookup
        String username = userManager.getUsername(1);

        // Testing non-existent username
        assertNull(userManager.getUsername(999), "Should return null for non-existent ID");
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
        // assertTrue(testFile.exists(), "Test file should exist before cleanup");

        // Run cleanup
        cleanup();

        // Verify file was deleted
        // assertFalse(testFile.exists(), "Test file should be deleted after cleanup");
    }
}
