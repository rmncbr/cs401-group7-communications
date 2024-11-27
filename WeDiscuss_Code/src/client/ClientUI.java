package client;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import server.User;
import shared.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ClientUI extends JFrame {
	private User user;
	private Client client;
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
	// User and chatroom maps caches - populated on initUpdate() call
	// chatroom id and user id
	private ConcurrentHashMap<Integer, Chatroom> chatrooms = new ConcurrentHashMap<Integer, Chatroom>();
	private ConcurrentHashMap<Integer, String> userMap = new ConcurrentHashMap<Integer, String>();
	// messaging display area
	private JPanel infoPanel;
	private JTextArea msgInfoArea;

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
		// On MessageType.LOGIN, load the models from the User's cache maps
		MessageType type = message.getMessageType();
		if (type.equals(MessageType.LOGIN)) {
			if (!message.getContents().equals("SUCCESS")) {
				doSendLoginRequest(user.getUsername(), user.getPassword());
			}
			// populate chatrooms and userMap
			// Load user model from userMap (for private messages)
			privateMessagesModel.clear(); // Clear any existing messages
			// Loop through the userMap to create messages for each user
			userMap.forEach((key, value) -> {

				Message privateMessage = new Message(new MessageCreator(MessageType.ADDUSER));

				// Add the message to the model, will call Message toString() by default to
				// display contents
				privateMessagesModel.addElement(privateMessage);

				// Debugging output
				System.out.println("User ID: " + key + ", Username: " + value);
			});

			// Similarly, load the chatroom messages model
			chatroomMessagesModel.clear(); // Clear any existing messages
			// Loop through the chatrooms map to create chatroom messages
			chatrooms.forEach((key, value) -> {
				// Create a message related to the chatroom
				Message chatroomMessage = new Message(new MessageCreator(MessageType.ADDUSER));

				// Add the chatroom message to the model
				chatroomMessagesModel.addElement(chatroomMessage);

				// Debugging output
				System.out.println("Chatroom ID: " + key + ", Chatroom: " + value);
			});
		}
	}

	public void update(Message message) {
		// For updating the models throughout the User's session
		privateMessagesModel.clear();
		chatroomMessagesModel.clear();

		MessageType type = message.getMessageType();
		switch (type) {
//		clientGui.update(message);
		case LOGOUT:
			// close application
			break;
		case ADDUSER:
			// confirm message

			break;
		case DELUSER:
			// confirm message

			break;
		case CPWD:
			// confirm message

			break;
		case GUL:
			// message w/ log contents

			break;
		case GCL:
			// message w/ log contents

			break;
		case CC:
			// messge w/ chatroom id

			break;
		case IUC:
			// message w/ chatroom

			break;
		case JC:
			// message w/ chatroom

			break;
		case LC:
			// confirm message

			break;
		case UTU:
			// message w/ message contents & info

			break;
		case UTC:
			// message w/ message contents add to chatroom

			break;
		default:
			break;
		}

	}// update()

	/* user functions */
	private boolean doSendLoginRequest(String userName, String password) {

		try {
			this.client.sendLoginRequest(userName, password);
			return true;
		} catch (IOException e) {
			System.out.println(e);
			return false;
		}
	}// doSendLoginRequest()

	private boolean doSendLogoutRequest() {
		try {
			this.client.sendLogoutRequest();
			return true;
		} catch (IOException e) {
			System.out.println(e);
			return false;
		}
	}// doSendLogoutRequest()

	private boolean doSendPasswordChangeRequest(String userName, String password) {
		try {
			this.client.sendPasswordChangeRequest(userName, password);
			return true;
		} catch (IOException e) {
			System.out.println(e);
			return false;
		}
	}// doSendPasswordChangeRequest()

	/* chatroom functions */

	private void doCreateChatroom() {
		try {
			this.client.createChatroom();
//			return true;
		} catch (IOException e) {
			System.out.println(e);
//			return false;
		}
	}// doCreateChatroom()

	private void doInviteUserToChatroom(String toUsername, int toUserID) {
		// requires String toUsername, int toUserID
		try {
			this.client.inviteUserToChatroom(toUsername, toUserID);
		} catch (IOException e) {
			System.out.println(e);
		}
	}// doInviteUserToChatroom()

	private void doJoinChatroom(int chatroomID) {
		try {
			this.client.joinChatroom(chatroomID);
			;
		} catch (IOException e) {
			System.out.println(e);
		}
	}// doJoinChatroom()

	private void doLeaveChatroom(int chatroomID) {
		try {
			this.client.leaveChatroom(chatroomID);
		} catch (IOException e) {
			System.out.println(e);
		}
	}// doLeaveChatroom()

	/* messaging functions */
	private boolean doSendMessageToUser(String message, String toUsername, int toUserID) {
		try {
			this.client.sendMessageToUser(message, toUsername, toUserID);
			return true;
		} catch (IOException e) {
			System.out.println(e);
			return false;
		}
	}// doSendMessageToUser()

	private boolean doSendMessageToChatroom(String message, int chatroomID) {
		try {
			this.client.sendMessageToChatroom(message, chatroomID);
			return true;
		} catch (IOException e) {
			System.out.println(e);
			return false;
		}
	}// doSendMessageToChatroom()

	private boolean doGetChatroom(int chatroomID) {
		try {
			this.client.getChatroom(chatroomID);
			return true;
		} catch (IOException e) {
			System.out.println(e);
			return false;
		}
	}// doGetChatroom()

	/* admin functions */
	private boolean doGetMessageLogs(String username) {
		try {
			this.client.getMessageLogs(username);
			return true;
		} catch (IOException e) {
			System.out.println(e);
			return false;
		}
	}// doGetMessageLogs()

	private boolean doGetChatLogs(int chatroomID) {
		try {
			this.client.getChatLogs(chatroomID);
			return true;
		} catch (IOException e) {
			System.out.println(e);
			return false;
		}
	}// doGetChatLogs()

	private boolean doAddUser(String userName, String password) {
		try {
			this.client.addUser(userName, password);
			return true;
		} catch (IOException e) {
			System.out.println(e);
			return false;
		}
	}// doAddUser()

	private void doDeleteUser(String userName, String password) {
		try {
			this.client.deleteUser(userName, password);
//			return true;
		} catch (IOException e) {
			System.out.println(e);
//			return false;
		}
	}// doDeleteUser()

	/* UI things */

	private void changePasswordDialog() {
		JDialog cpwdDialog = new JDialog(mainFrame, "Change Password", true); // Modal dialog
		cpwdDialog.setSize(300, 150);
		cpwdDialog.setLocationRelativeTo(mainFrame);

		// Create the main panel with BorderLayout
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		// Create the input fields
		JPanel inputPanel = new JPanel();
		inputPanel.setLayout(new FlowLayout());
//		JLabel oldPwLabel = new JLabel("Old Password:");
//		JPasswordField oldPwField = new JPasswordField(15);
		JLabel newPwLabel = new JLabel("New Password:");
		JPasswordField newPwField = new JPasswordField(15);

		JButton cpwdButton = new JButton("Submit");

		// Add components to the input panel
//		inputPanel.add(oldPwLabel);
//		inputPanel.add(oldPwField);

		inputPanel.add(newPwLabel);
		inputPanel.add(newPwField);
		inputPanel.add(new JLabel()); // Empty label to align the button
		inputPanel.add(cpwdButton);

		// Add the inputPanel to the center of the main panel
		panel.add(inputPanel, BorderLayout.CENTER);
		cpwdDialog.add(panel);
		cpwdDialog.setVisible(true);

		cpwdButton.addActionListener(e -> {
//			String oldPw = new String(oldPwField.getPassword());
			String newPw = new String(newPwField.getPassword());
			doSendPasswordChangeRequest(this.user.getUsername(), newPw);
			cpwdDialog.setVisible(false);
		});

	}// changePasswordDialog()

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

		loginButton.addActionListener(e -> {
			String username = usernameField.getText();
			String password = new String(passwordField.getPassword());

			// validate credentials
			if (doSendLoginRequest(username, password)) {
				isLoggedIn = true;
				loginDialog.setVisible(false);
				// Proceed to the main application
				showMainApplication(user);

			} else {
				loginTextArea.setText("Invalid credentials. Please try again.");
			}
		});
	}// showLoginDialog()

//			if (username.equals("admin") && password.equals("123")) {
//				// Admin login
//				isLoggedIn = true;
//				AdminUI adminUI = new AdminUI(mainFrame);
//				loginDialog.setVisible(false);
//
//				adminUI.appInitialize(mainFrame); // Initialize admin UI components
//				mainFrame.setVisible(true);
//			} else if (username.equals("user") && password.equals("123")) {
//				// Regular user login
//				isLoggedIn = true;
//				loginDialog.setVisible(false);
//
//				appInitialize(mainFrame); // Initialize client UI components
//				mainFrame.setVisible(true);
//			} else {
//				loginTextArea.setText("Invalid credentials. Please try again.");
//
//			}
//

	private void showMainApplication(User user) {
		// Initialize main UI components
		if (user.getAdminStatus()) {
			// If user is an admin, create AdminUI
			AdminUI adminUI = new AdminUI(mainFrame);
			adminUI.appInitialize(mainFrame);
			mainFrame.setVisible(true);
		} else {
			// If user is a regular user, create ClientUI
			appInitialize(mainFrame);
			mainFrame.setVisible(true);
		}
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
				doCreateChatroom();
			});

			JMenuItem joinChatroomItem = new JMenuItem("Join Chatroom");
			joinChatroomItem.addActionListener(e -> {
//				 doJoinChatroom();
			});

			JMenuItem inviteUserToChatroomItem = new JMenuItem("Invite User to Chatroom");
			inviteUserToChatroomItem.addActionListener(e -> {
//				 doInviteUserToChatroom();
			});

			JMenuItem leaveChatroomItem = new JMenuItem("Leave Chatroom");
			leaveChatroomItem.addActionListener(e -> {
//				 doLeaveChatroom();
			});

			JMenuItem changePasswordItem = new JMenuItem("Change Password");
			changePasswordItem.addActionListener(e -> {
				changePasswordDialog();
			});

			JMenuItem logoutItem = new JMenuItem("Logout");
			logoutItem.addActionListener(e -> {

				if (doSendLogoutRequest()) {
					// close connection and application
				} else {
					// throw error message
				}
			});

			// Add the items to the file menu
			fileMenu.add(createChatroomItem);
			fileMenu.add(joinChatroomItem);
			fileMenu.add(inviteUserToChatroomItem);
			fileMenu.add(leaveChatroomItem);
			fileMenu.addSeparator();
			fileMenu.add(changePasswordItem);
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
