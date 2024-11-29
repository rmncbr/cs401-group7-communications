package client;

import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class AdminUI extends ClientUI {

	private JMenu adminMenu;
	private JMenuItem addUserItem, deleteUserItem, getMessageLogsItem, getChatLogsItem;

	private boolean isAdminMenuAdded = false; // Flag to check if the Admin menu is already added

	public AdminUI(JFrame mainFrame) {
		super(); // Call the constructor of ClientUI (ClientUI's initialization)
		appInitialize(mainFrame); // Initialize the admin UI components after calling ClientUI initialization
	}

	@Override
	public void appInitialize(JFrame mainFrame) {
		// Initialize the client UI first by calling super
		super.appInitialize(mainFrame); // This will initialize the base ClientUI components

		// Only add the Admin Tools menu once
		if (!isAdminMenuAdded) {
			JMenuBar menuBar = mainFrame.getJMenuBar();
			adminMenu = new JMenu("Admin Tools");

			addUserItem = new JMenuItem("Add User");
			addUserItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					doAddUser();
				}
			});

			deleteUserItem = new JMenuItem("Delete User");
			deleteUserItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					doDeleteUser();
				}
			});

			getMessageLogsItem = new JMenuItem("Get Message Logs");
			getMessageLogsItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					doGetMessageLogs();
				}
			});
			
			getChatLogsItem = new JMenuItem("Get Chat Logs");
			getChatLogsItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					doGetChatLogs();
				}
			});

			adminMenu.add(addUserItem);
			adminMenu.add(deleteUserItem);
			adminMenu.add(getMessageLogsItem);
			adminMenu.add(getChatLogsItem);

			menuBar.add(adminMenu);
			mainFrame.setJMenuBar(menuBar);

			isAdminMenuAdded = true;

			mainFrame.revalidate();
			mainFrame.repaint();
		}
	}

	/* admin functions */
	private boolean doGetMessageLogs() {
		JTextField usernameField = new JTextField(15);

		ActionListener actionListener = e -> {
			String username = usernameField.getText().trim(); 

			if (username.isEmpty()) {
				JOptionPane.showMessageDialog(mainFrame, "Please enter a username.", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			try {
				this.client.getMessageLogs(username); 
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(mainFrame, "Error fetching message logs: " + ex.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		};

		showInputDialog("Get Message Logs", "Username:", usernameField, actionListener);

		return true; 
						
	}

	private void doGetChatLogs() { 
		JTextField chatroomIDField = new JTextField(15);

		ActionListener actionListener = e -> {
			String chatroom = chatroomIDField.getText().trim(); 

			if (chatroom.isEmpty()) {
				JOptionPane.showMessageDialog(mainFrame, "Please enter a chatroom ID.", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			try {
	            String chatroomIDText = chatroomIDField.getText();
	            int chatroomID = Integer.parseInt(chatroomIDText); 

				this.client.getChatLogs(chatroomID);
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(mainFrame, "Error fetching message logs: " + ex.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		};

		showInputDialog("Get Message Logs", "Chatroom ID:", chatroomIDField, actionListener);
		
	}// doGetChatLogs()

	private boolean doAddUser() {
	    JTextField usernameField = new JTextField(15);  
	    JPasswordField passwordField = new JPasswordField(15);  

	    ActionListener actionListener = e -> {
	        String username = usernameField.getText().trim();  
	        String password = new String(passwordField.getPassword()).trim();  
	  
	        if (username.isEmpty() || password.isEmpty()) {
	            JOptionPane.showMessageDialog(mainFrame, "Please fill in both fields.", "Error", JOptionPane.ERROR_MESSAGE);
	            return;
	        }

	        try {
	            this.client.addUser(username, password);
	            JOptionPane.showMessageDialog(mainFrame, "User added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
	        } catch (IOException ex) {
	            JOptionPane.showMessageDialog(mainFrame, "Error adding user: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
	        }
	    };

	    showDoubleInputDialog("Add User", "Username:", usernameField, "Password:", passwordField, actionListener);
	    
	    return true;  
	}// doAddUser()


	private void doDeleteUser() {
	    JTextField usernameField = new JTextField(15);  
	    JPasswordField passwordField = new JPasswordField(15);  

	    ActionListener actionListener = e -> {
	        String username = usernameField.getText().trim();  
	        String password = new String(passwordField.getPassword()).trim();  

	        if (username.isEmpty() || password.isEmpty()) {
	            JOptionPane.showMessageDialog(mainFrame, "Please fill in both fields.", "Error", JOptionPane.ERROR_MESSAGE);
	            return;
	        }

	        try {
	            this.client.deleteUser(username, password);
	            
	            JOptionPane.showMessageDialog(mainFrame, "User deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
	        } catch (IOException ex) {
	            JOptionPane.showMessageDialog(mainFrame, "Error deleting user: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
	        }
	    };

	    showDoubleInputDialog("Delete User", "Username:", usernameField, "Password:", passwordField, actionListener);
	}// doDeleteUser()

}

