package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AdminUI extends ClientUI {

    // New components specific to AdminUI
    private JMenu adminMenu;
    private JMenuItem addUserItem, deleteUserItem, getMessageLogsItem;

    public AdminUI(JFrame mainFrame) {
        super();  // Call the constructor of ClientUI (ClientUI's initialization)
        appInitialize(mainFrame);  // Initialize the admin UI components after calling ClientUI initialization
    }

    @Override
    public void appInitialize(JFrame mainFrame) {
        // Initialize the client UI first by calling super
        super.appInitialize(mainFrame);  // This will initialize the base ClientUI components

        // Now add admin-specific tools, for example, a new menu for the admin tools
        // Add admin-specific menu items
        JMenuBar menuBar = mainFrame.getJMenuBar();
        adminMenu = new JMenu("Admin Tools");

        addUserItem = new JMenuItem("Add User");
        addUserItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Call the method to handle adding a user
                doAddUser();
            }
        });

        deleteUserItem = new JMenuItem("Delete User");
        deleteUserItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Call the method to handle deleting a user
                doDeleteUser();
            }
        });

        getMessageLogsItem = new JMenuItem("Get Message Logs");
        getMessageLogsItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Call the method to handle fetching message logs
                doGetMessageLogs();
            }
        });

        // Add these items to the admin menu
        adminMenu.add(addUserItem);
        adminMenu.add(deleteUserItem);
        adminMenu.add(getMessageLogsItem);

        // Add the admin menu to the existing menu bar
        menuBar.add(adminMenu);
        mainFrame.setJMenuBar(menuBar);

        // Update the UI (show the admin menu)
        mainFrame.revalidate();
        mainFrame.repaint();
    }

    // Admin-specific methods for actions
    private void doAddUser() {
        System.out.println("Add User functionality called");
        // Add the logic for adding a user
    }

    private void doDeleteUser() {
        System.out.println("Delete User functionality called");
        // Add the logic for deleting a user
    }

    private void doGetMessageLogs() {
        System.out.println("Get Message Logs functionality called");
        // Add the logic for fetching message logs
    }
}
