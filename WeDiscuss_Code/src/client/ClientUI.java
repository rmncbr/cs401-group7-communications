package client;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import server.User;
import shared.*;

import java.io.File;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
//import java.util.HashMap;
//import java.util.Map;
import java.nio.file.Files;
import java.io.IOException;

public class ClientUI extends JFrame {
	private User user;
	private boolean isLoggedIn = false;
	private ArrayList<Message> userMessages = new ArrayList<Message>();

	JFrame mainFrame = new JFrame("WeDiscuss");

	// menu bar items
	JMenuBar menuBar = new JMenuBar();

	// login fields
	private JDialog loginDialog;
	private JTextField usernameField;
	private JPasswordField passwordField;
	JTextArea loginTextArea = new JTextArea();

	// swing components to display message and chatroom lists
	private DefaultListModel<Message> privateMessagesModel;
	private DefaultListModel<Message> chatroomMessagesModel;
	private JList<Message> privateMessagesList;
	private JList<Message> chatroomMessagesList;

	// User and chatroom maps caches
	private ConcurrentHashMap<Integer, Chatroom> chatrooms = new ConcurrentHashMap<Integer, Chatroom>();
	private ConcurrentHashMap<Integer, String> userMap = new ConcurrentHashMap<Integer, String>();

	// messaging display area
	private JPanel infoPanel;
	private JTextArea msgInfoArea;

	private Client client;

	public ClientUI() {
		client = new Client(this); // Init Client w/ this GUI

		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setSize(1100, 700);
		mainFrame.setVisible(false);
	}// ClientUI()

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			ClientUI clientUI = new ClientUI();
			clientUI.showLoginDialog();
		});
	}// main()

	public void initUpdate(Message message) {
		// MessageType.LOGIN
	}// initUpdate()

	public void update(Message message) {
		privateMessagesModel.clear();
		chatroomMessagesModel.clear();
	}// update()

	// do functions
	private boolean doSendLoginRequest(String userName, String password) {
		try {
			this.client.sendLoginRequest(userName, password);
			return true;
		} catch (IllegalStateException | IOException e) {
			return false;
		}
	}// doSendLoginRequest()

	private boolean doSendLogoutRequest() {
		try {
			this.client.sendLogoutRequest();
			return true;
		} catch (IOException e) {
			return false;
		}
	}// doSendLogoutRequest()

	private void doUTU() {

	}// doUTU()

	private void doSendMessage(MessageCreator mc, Message message) {
	}// doSendMessage()

	private void doReconnect() {

	}// doReconnect()

	private void showLoginDialog() {
		loginDialog = new JDialog(mainFrame, "WeDiscuss Login", true); // Modal dialog
		loginDialog.setSize(300, 150);
		loginDialog.setLocationRelativeTo(mainFrame);

		// Create the main panel with BorderLayout
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		// Create the input fields
		JPanel inputPanel = new JPanel();
		inputPanel.setLayout(new FlowLayout());
		JLabel usernameLabel = new JLabel("Username:");
		usernameField = new JTextField(15);
		JLabel passwordLabel = new JLabel("Password:");
		passwordField = new JPasswordField(15);
		JButton loginButton = new JButton("Login");
		loginButton.addActionListener(new LoginButtonListener());

		// Add components to the input panel
		inputPanel.add(usernameLabel);
		inputPanel.add(usernameField);
		inputPanel.add(passwordLabel);
		inputPanel.add(passwordField);
		inputPanel.add(new JLabel()); // Empty label to align the button
		inputPanel.add(loginButton);

		// Add the inputPanel to the center of the main panel
		panel.add(inputPanel, BorderLayout.CENTER);

		// Initialize the loginTextArea to show login errors
		loginTextArea = new JTextArea(1, 20);
		loginTextArea.setEditable(false);
		loginTextArea.setBackground(new Color(238, 238, 238));

		// Add the loginTextArea to the bottom of the main panel
		panel.add(new JScrollPane(loginTextArea), BorderLayout.SOUTH);

		// Add the main panel to the dialog
		loginDialog.add(panel);

		// Window listener for application close
		loginDialog.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				System.exit(0);
			}
		});

		// Show the login dialog
		loginDialog.setVisible(true);
	}

	private class LoginButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String username = usernameField.getText();
			String password = new String(passwordField.getPassword());

			// validate credentials
//					if (doSendLoginRequest(username, password)) {
//						isLoggedIn = true;
//						loginDialog.setVisible(false);
//						// Proceed to the main application
//						showMainApplication(user);
			// testing
			if (username.equals("admin") && password.equals("123")) {
				// Admin login
				isLoggedIn = true;
				AdminUI adminUI = new AdminUI(mainFrame);
				loginDialog.setVisible(false);

				adminUI.appInitialize(mainFrame); // Initialize admin UI components
				mainFrame.setVisible(true);
			} else if (username.equals("user") && password.equals("123")) {
				// Regular user login
				isLoggedIn = true;
				loginDialog.setVisible(false);

				appInitialize(mainFrame); // Initialize client UI components
				mainFrame.setVisible(true);
			} else {
				loginTextArea.setText("Invalid credentials. Please try again.");

			}

		}

	}// LoginButtonListener()

	private void showMainApplication(User user) {
		// Initialize main UI components
//	    if (user.adminStatus == true) {
		if (true) {
			// If user is an admin, create AdminUI
			AdminUI adminUI = new AdminUI(mainFrame);
			adminUI.appInitialize(mainFrame);
			mainFrame.setVisible(true);
		} else {
			// If user is a regular user, create ClientUI
			appInitialize(mainFrame);
			mainFrame.setVisible(true);
		}

		// Make sure the main application frame is visible
		mainFrame.setVisible(true);
	}// showMainApplication()

	public void appInitialize(JFrame mainFrame) {
		if (mainFrame == null) {
			mainFrame = new JFrame();
		}

		// Create the menu bar if it hasn't been created yet
		if (menuBar.getMenuCount() == 0) {
			JMenu fileMenu = new JMenu("𓈒∘☁︎");

			JMenuItem createChatroomItem = new JMenuItem("Create Chatroom");
			createChatroomItem.addActionListener(e -> {
				// doCreateChatroom();
			});

			JMenuItem joinChatroomItem = new JMenuItem("Join Chatroom");
			joinChatroomItem.addActionListener(e -> {
				// doJoinChatroom();
			});

			JMenuItem inviteUserToChatroomItem = new JMenuItem("Invite User to Chatroom");
			inviteUserToChatroomItem.addActionListener(e -> {
				// doInviteUserToChatroom();
			});

			JMenuItem leaveChatroomItem = new JMenuItem("Leave Chatroom");
			leaveChatroomItem.addActionListener(e -> {
				// doLeaveChatroom();
			});

			JMenuItem logoutItem = new JMenuItem("Logout");
			logoutItem.addActionListener(e -> {
				// Perform logout actions here
			});

			// Add the items to the file menu
			fileMenu.add(createChatroomItem);
			fileMenu.add(joinChatroomItem);
			fileMenu.add(inviteUserToChatroomItem);
			fileMenu.add(leaveChatroomItem);
			fileMenu.addSeparator();
			fileMenu.add(logoutItem);

			// Add the file menu to the menu bar
			menuBar.add(fileMenu);

			// Set the menu bar to the main frame
			mainFrame.setJMenuBar(menuBar);
		} // if

		// Initialize the models
		privateMessagesModel = new DefaultListModel<>();
		chatroomMessagesModel = new DefaultListModel<>();

		// Initialize the JLists
		privateMessagesList = new JList<>(privateMessagesModel);
		chatroomMessagesList = new JList<>(chatroomMessagesModel);
		privateMessagesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		chatroomMessagesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// Create the JTabbedPane
		JTabbedPane tabbedPane = new JTabbedPane();

		// Create the panel for private messages
		JPanel privateMessagesPanel = new JPanel(new BorderLayout());
		privateMessagesPanel.add(new JScrollPane(privateMessagesList), BorderLayout.CENTER);

		// Create the panel for chatroom messages
		JPanel chatroomMessagesPanel = new JPanel(new BorderLayout());
		chatroomMessagesPanel.add(new JScrollPane(chatroomMessagesList), BorderLayout.CENTER);

		// Add the panels to the JTabbedPane
		tabbedPane.addTab("Private Messages", privateMessagesPanel);
		tabbedPane.addTab("Chatrooms", chatroomMessagesPanel);

		// Create a middle panel for displaying current chat section
		infoPanel = new JPanel(new BorderLayout());
		infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// Initialize the JTextArea for message display
		msgInfoArea = new JTextArea();
		msgInfoArea.setEditable(false);
		msgInfoArea.setLineWrap(true); // Allow the text to wrap
		msgInfoArea.setWrapStyleWord(true); // Wrap whole words, not just characters
		JScrollPane msgInfoScrollPane = new JScrollPane(msgInfoArea); // Wrap the chat area in a scroll pane
		msgInfoScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS); // Always show scroll bar
		msgInfoScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); // Disable horizontal
																								// scrolling

		// Add the message info area to the info panel (this is the right side panel)
		infoPanel.add(msgInfoScrollPane, BorderLayout.CENTER);

		// Create the panel that will hold both the input text area and send button
		JPanel inputPanel = new JPanel(new BorderLayout());
		inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10));

		// Initialize the new JTextArea for message input
		JTextArea inputTextArea = new JTextArea();
		inputTextArea.setEditable(true);
		inputTextArea.setLineWrap(true); // Allow text to wrap as the user types
		inputTextArea.setWrapStyleWord(true); // Wrap whole words
		inputTextArea.setRows(3); // Set a default size for the input area (adjust as needed)
		JScrollPane inputScrollPane = new JScrollPane(inputTextArea);

		// Add the input area to the left side of the inputPanel
		inputPanel.add(inputScrollPane, BorderLayout.CENTER);

		// Create the Send button
		JButton sendButton = new JButton(">");
		// Add the Send button to the right side of the inputPanel
		inputPanel.add(sendButton, BorderLayout.EAST);

		// Add the input panel (with the text area and button) to the bottom of the
		// infoPanel
		infoPanel.add(inputPanel, BorderLayout.SOUTH);

		// Add a selection listener to the JList to update the chat display area
		privateMessagesList.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				Message selectedMessage = privateMessagesList.getSelectedValue();
				if (selectedMessage != null) {
					msgInfoArea.setText(selectedMessage.getContents());
				} else {
					msgInfoArea.setText("");
				}
			}
		});

		chatroomMessagesList.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				Message selectedMessage = chatroomMessagesList.getSelectedValue();
				if (selectedMessage != null) {
					msgInfoArea.setText(selectedMessage.getContents());
				} else {
					msgInfoArea.setText("");
				}
			}
		});

		// Create a split pane to separate the message list and chat area
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabbedPane, infoPanel);
		splitPane.setDividerLocation(300);
		splitPane.setResizeWeight(0.2); // This means the left panel (message list) will take 20% of the space

		// Add the split pane to the main frame and set properties
		mainFrame.add(splitPane, BorderLayout.CENTER);
		mainFrame.setLocationRelativeTo(null); // Center the frame on screen
		mainFrame.setVisible(true);

	}// appInitialize()

}
