package client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

import server.User;
import shared.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class ClientUI extends JFrame {
	protected Client client;
	private boolean isLoggedIn = false;
	private volatile boolean operationCheck = false;
	private User user;

	// User and chatroom maps caches - populated on initUpdate() call
	private ConcurrentMap<Integer, Chatroom> chatrooms = new ConcurrentHashMap<Integer, Chatroom>();
	private ConcurrentMap<Integer, String> userMap = new ConcurrentHashMap<Integer, String>();

	private ArrayList<String> userLog;
	private ArrayList<String> chatroomLog;

	private ConcurrentLinkedQueue<Message> clientMessages = new ConcurrentLinkedQueue<Message>();

	private CountDownLatch serverResponse = new CountDownLatch(1);

	private Thread listenThread;

	// GUI components
	JFrame mainFrame = new JFrame("WeDiscuss");
	// menu bar items
	JMenuBar menuBar = new JMenuBar();
	private JMenu adminMenu;
	private JMenu userMenu;
	private JMenu peopleMenu;

	// login
	JTextArea loginTextArea = new JTextArea();
	// swing components to display message and chatroom lists
	private DefaultListModel<Message> privateMessagesModel = new DefaultListModel<>();
	private DefaultListModel<Message> chatroomMessagesModel = new DefaultListModel<>();
	private JList<Message> privateMessagesList = new JList<>(privateMessagesModel);
	private JList<Message> chatroomMessagesList = new JList<>(chatroomMessagesModel);
	
	//logs
    DefaultListModel<String> userLogModel;
    DefaultListModel<String> chatroomLogModel;
	
	DefaultListModel<String> usersListModel;
	DefaultListModel<String> chatroomsListModel;
	
	
	private int activeTabIndex = 0; // 0: "Private Messages", 1: "Chatrooms"
	// messaging display area
	private JPanel msgAreaPanel;
	JScrollPane privateMsgInfoScrollPane;
	JScrollPane chatroomMsgInfoScrollPane;
	// Map to hold message areas for each private message and chatroom (by ID)
	private Map<Integer, JTextArea> privateMessageAreas = new HashMap<>();
	private Map<Integer, JTextArea> chatroomMessageAreas = new HashMap<>();

	public ClientUI() {
		client = new Client(this); // Init Client w/ this GUI
		
		usersListModel = new DefaultListModel<>();
		chatroomsListModel = new DefaultListModel<>();
		privateMessagesModel = new DefaultListModel<>();
		chatroomMessagesModel = new DefaultListModel<>();
		
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setSize(575, 400);
		mainFrame.setVisible(false);
	}// ClientUI()

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			ClientUI clientUI = new ClientUI();
			clientUI.showInitDialog(); // -> clientUI.showLoginDialog() -> clientUI.startMessageProcessingThread();
		});
	}// main()

	public void initUpdate(Message message) {
		System.out.println("Received Client Message: " + message.getMessageType());
		System.out.println(message.getContents());

		if (message.getContents().equals("Success")) {
			// Init everything
			user = message.getUser();
			if (!(message.getChatroomMap() == null)) {
				chatrooms = message.getChatroomMap();
			}
			userMap = message.getUserMap();
			// List<Message> messages = user.getMessagesFromUsers();

			ConcurrentHashMap<Integer, List<Message>> table = user.getMessagesFromUsers();

			userMap.put(user.getID(), user.getUsername());
			operationCheck = true;

			// Thread-safe update to Swing components
			SwingUtilities.invokeLater(() -> {
				// Update private messages list
				privateMessagesModel.clear();
				for (Integer userID : userMap.keySet()) {
					if (user.getID() == userID)
						continue; // skip self user cell

					createPrivateMessageArea(userID);

					List<Message> messages = table.get(userID);
					if (!(messages == null)) {
						for (Message m : messages) {
							createPrivateMessageArea(userID); // Create if not exists
							JTextArea privateArea = privateMessageAreas.get(userID);
							if (privateArea != null) {
								privateArea.append(m.getFromUserName() + ": " + m.getContents() + "\n");
							}
							// appendMessageToCorrectArea(messages.get(i));
						}
					}

					MessageCreator pm = new MessageCreator(MessageType.LOGIN);
					pm.setFromUserID(userID);
					pm.setFromUserName(userMap.get(userID));
					Message privateMessage = new Message(pm);
					privateMessagesModel.addElement(privateMessage);
				}

				// Update chatroom messages list
				chatroomMessagesModel.clear();
				for (Integer chatroomID : chatrooms.keySet()) {
					createChatroomMessageArea(chatroomID);
					List<Message> messages = chatrooms.get(chatroomID).getMessages();
					if (!(messages == null)) {
						for (Message m : messages) {
							createChatroomMessageArea(chatroomID);
							JTextArea chatroomArea = chatroomMessageAreas.get(chatroomID);
							if (chatroomArea != null) {
								chatroomArea.append(m.getFromUserName() + ": " + m.getContents() + "\n");
							}
						}
					}

					MessageCreator cr = new MessageCreator(MessageType.LOGIN);
					cr.setToChatroom(chatroomID);
					cr.setChatroom(chatrooms.get(chatroomID));
					Message chatMessage = new Message(cr);
					chatroomMessagesModel.addElement(chatMessage);
				}

				privateMessagesList.setModel(privateMessagesModel);
				chatroomMessagesList.setModel(chatroomMessagesModel);
				privateMessagesList.setCellRenderer(new MessageListCellRenderer());
				chatroomMessagesList.setCellRenderer(new MessageListCellRenderer());
			});
		}

		serverResponse.countDown();
	}
	
	private void loadChatrooms() {
		SwingUtilities.invokeLater(() -> {
			chatroomMessagesModel.clear();
			for (Integer chatroomID : chatrooms.keySet()) {
				createChatroomMessageArea(chatroomID);

				MessageCreator cr = new MessageCreator(MessageType.LOGIN);
				cr.setToChatroom(chatroomID);
				cr.setChatroom(chatrooms.get(chatroomID));
				Message chatMessage = new Message(cr);
				chatroomMessagesModel.addElement(chatMessage);
			}

			// Set the models for both private and chatroom messages
			
			chatroomMessagesList.setModel(chatroomMessagesModel);
			
			chatroomMessagesList.setCellRenderer(new MessageListCellRenderer());
		});
	}

	private void startMessageProcessingThread() {
		// Start a single message processing thread in the background
		Thread messageProcessingThread = new Thread(() -> processMessages());
		messageProcessingThread.start();
	}

	private void processMessages() {
		while (true) {
			if (Thread.interrupted()) {
				break;
			}

			Message message = clientMessages.poll();
			if (message == null)
				continue;

			MessageType type = message.getMessageType();
			System.out.println("Received Client Message: " + type);

			SwingUtilities.invokeLater(() -> {
				switch (type) {
				case LOGOUT:
					processLogout(message);
					break;
				case ADDUSER:
					processAddUser(message);
					break;
				case DELUSER:
					processDelUser(message);
					break;
				case CPWD:
					processChangePassword(message);
					break;
				case GUL:
					processGetUserLogs(message);
					break;
				case GCL:
					processGetChatroomLogs(message);
					break;
				case CC:
					processCreateChatroom(message);
					break;
				case IUC:
	                 processInviteUserToChatroom(message);
					break;
				case JC:
					processJoinChatroom(message);
					break;
				case LC:
					processLeaveChatroom(message);
					break;
				case UTU:
					processUserMessage(message); // For private messages
					break;
				case UTC:
					processChatroomMessage(message); // For chatroom messages
					break;
				case UPDATEUM:
                    processUserMapUpdate(message);
					break;
				case UPDATECM:
					// Handle chatroom map updates
					break;
				default:
					break;
				}
			});

			try {
				Thread.sleep(100); // Adjust the sleep time as necessary
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	/* Process-Functions */
	public void addToMessageQueue(Message message) {
		clientMessages.add(message);
	}

	private void processLogout(Message message) {
		SwingUtilities.invokeLater(() -> {
			JOptionPane.showMessageDialog(null, "You have successfully logged out.", "Logout Successful",
					JOptionPane.INFORMATION_MESSAGE);
			System.exit(0);
		});
	}

	private void processUserMessage(Message message) {
		// This function handles private messages (UTU)
		
			appendMessageToCorrectArea(message);
		
	}

	private void processChatroomMessage(Message message) {
		// This function handles chatroom messages (UTC)
		
			appendMessageToCorrectArea(message);
		
	}

	private void processCreateChatroom(Message message) {
		SwingUtilities.invokeLater(() -> {
			if (message.getContents().equals("Success")) {
				// Add chatroom to the map
				chatrooms.put(message.getToChatroomID(), message.getChatroom());

				// Add chatroom to the chatroomsListModel
				chatroomsListModel.addElement("Chatroom " + message.getToChatroomID());
			} else {
				System.out.println("Error creating chatroom.");
			}
			 loadChatrooms();
			System.out.println("List of Chatrooms Updated!");
		});
	}
	
	private void processLeaveChatroom(Message message) {
		SwingUtilities.invokeLater(() -> {
			if (message.getContents().equals("Remove")) {
				// Remove chatroom to the map
				chatrooms.remove(message.getToChatroomID());

				// Remove chatroom to the chatroomsListModel
				chatroomsListModel.removeElement(message.getToChatroomID());
			} else {
				System.out.println("Error Removing chatroom.");
			}
			 loadChatrooms();
			System.out.println("List of Chatrooms Updated!");
		});
	}
	
	private void processInviteUserToChatroom(Message message) {
		SwingUtilities.invokeLater(() -> {
			if (message.getContents().equals("Add")) {
				// Add chatroom to the map
				chatrooms.put(message.getChatroom().getChatroomID(), message.getChatroom());

				// Add chatroom to the chatroomsListModel
				chatroomsListModel.addElement("Chatroom " + message.getToChatroomID());
			} else {
				System.out.println("Error adding chatroom.");
			}
			 loadChatrooms();
			System.out.println("List of Chatrooms Updated!");
		});
	}
	
	private void processJoinChatroom(Message message) {
		SwingUtilities.invokeLater(() -> {
			if (message.getContents().equals("Success")) {
				// Add chatroom to the map
				chatrooms.put(message.getChatroom().getChatroomID(), message.getChatroom());

				// Add chatroom to the chatroomsListModel
				chatroomsListModel.addElement("Chatroom " + message.getToChatroomID());
			} else {
				System.out.println("Error joining chatroom.");
			}
			 loadChatrooms();
			System.out.println("List of Chatrooms Updated!");
		});
		
	}

	/* admin processing */
	
	protected void processUserMapUpdate(Message message) {
		SwingUtilities.invokeLater(() -> {
			
			if(message.getContents().equals("Add")) {
				userMap.put(message.getFromUserID(), message.getFromUserName());
				createPrivateMessageArea(message.getFromUserID()); // Create if not exists
				JTextArea privateArea = privateMessageAreas.get(message.getFromUserID());;
				MessageCreator pm = new MessageCreator(MessageType.LOGIN);
				pm.setFromUserID(message.getFromUserID());
				pm.setFromUserName(userMap.get(message.getFromUserID()));
				Message privateMessage = new Message(pm);
				privateMessagesModel.addElement(privateMessage);
				privateMessagesList.setModel(privateMessagesModel);
				privateMessagesList.setCellRenderer(new MessageListCellRenderer());
			}
			else if(message.getContents().equals("Remove")) {
				
				userMap.remove(message.getFromUserID(), message.getFromUserName());


				MessageCreator pm = new MessageCreator(MessageType.LOGIN);
				pm.setFromUserID(message.getFromUserID());
				pm.setFromUserName(userMap.get(message.getFromUserID()));
				Message privateMessage = new Message(pm);
				privateMessagesModel.removeElement(privateMessage);
				
			}
		});
	}
	
	protected void processAddUser(Message message) {

	}

	protected void processDelUser(Message message) {

	}

	protected void processChangePassword(Message message) {

	}
	
	protected void processGetUserLogs(Message message) {
		if (true) {
            SwingUtilities.invokeLater(() -> {
            // display dialog of user logs

            JDialog userLogDialog = new JDialog(mainFrame, "Users", true);
            userLogModel = new DefaultListModel<>();
            
            
            String userLog = message.getContents();
            String[] split = userLog.split("\\|");
            
            
            userLogModel.clear();
            for (String log : split) {
                userLogModel.addElement(log); // Add to the model
                System.out.println("Added A Log!");
            }
            
            
            
            JList<String> userLogList = new JList<>(userLogModel);
            userLogList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            JScrollPane usersScrollPane = new JScrollPane(userLogList);

            userLogDialog.setLayout(new BorderLayout());
            userLogDialog.add(usersScrollPane, BorderLayout.CENTER);

            userLogDialog.setSize(200, 150);
            userLogDialog.setLocationRelativeTo(mainFrame);
            userLogDialog.setVisible(true);
//
            userLogDialog.revalidate();
            userLogDialog.repaint();
            });
        } else {
            System.out.println("Error obtaining user log.");
        }
	}

	protected void processGetChatroomLogs(Message message) {
		if (true) {
            SwingUtilities.invokeLater(() -> {
            // display dialog of user logs

            JDialog userLogDialog = new JDialog(mainFrame, "Chatroom" , true);
            userLogModel = new DefaultListModel<>();
            
            
            String userLog = message.getContents();
            String[] split = userLog.split("\\|");
            
            
            userLogModel.clear();
            for (String log : split) {
                userLogModel.addElement(log); // Add to the model
                System.out.println("Added A Log!");
            }
            
            
            
            JList<String> userLogList = new JList<>(userLogModel);
            userLogList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            JScrollPane usersScrollPane = new JScrollPane(userLogList);

            userLogDialog.setLayout(new BorderLayout());
            userLogDialog.add(usersScrollPane, BorderLayout.CENTER);

            userLogDialog.setSize(200, 150);
            userLogDialog.setLocationRelativeTo(mainFrame);
            userLogDialog.setVisible(true);
//
            userLogDialog.revalidate();
            userLogDialog.repaint();
            });
        } else {
            System.out.println("Error obtaining user log.");
        }
	}

	/* Do-Functions */
	/* user functions */
	private boolean doSendLoginRequest(String userName, String password) {
		try {
			this.client.sendLoginRequest(userName, password);
			operationCheck = false;
			serverResponse = new CountDownLatch(1);
			serverResponse.await();
			if (operationCheck) {
				return true;
			}
			System.out.println("Invalid Credentials!");
		} catch (IOException e) {
			System.err.println("Login Request Error!");
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			System.err.println("Login process interrupted.");
		}
		return false;
	}// doSendLoginRequest()

	private boolean doSendLogoutRequest() {
		try {
			this.client.sendLogoutRequest();
			serverResponse.await();

			if (operationCheck) {
				// Interrupt the listenThread
				if (listenThread != null && listenThread.isAlive()) {
					listenThread.interrupt();
				}
				return true;
			}
		} catch (IOException e) {
			System.err.println("Logout Request Error!");
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			System.err.println("Logout process interrupted.");
		}

		operationCheck = false;
		return false;
	}

	private void doSendPasswordChangeRequest() {
		JPasswordField newPwField = new JPasswordField(15);

		ActionListener actionListener = e -> {
			try {
				String newPw = new String(newPwField.getPassword());
				this.client.sendPasswordChangeRequest(this.user.getUsername(), newPw);
			} catch (IOException err) {
				System.out.println(err);
			}
		};

		showInputDialog("Change Password", "New Password:", newPwField, actionListener);
	}

	/* chatroom functions */
	private void doCreateChatroom() {
		JDialog createChatDialog = new JDialog(mainFrame, "Create Chatroom", true);
		createChatDialog.setSize(300, 100);
		createChatDialog.setLocationRelativeTo(mainFrame);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		JTextField createChatTextArea = new JTextField();
		try {
			this.client.createChatroom();
		} catch (IOException err) {
			System.out.println(err);
			createChatTextArea.setText("Error creating new Chatroom.");
		}
		createChatTextArea.setText("New Chatroom created!");
		createChatDialog.add(panel);

	}

	private void doInviteUserToChatroom() {
		JTextField usernameField = new JTextField(15);
		JTextField userIDField = new JTextField(15);
		JTextField chatroomIDField = new JTextField(15);

		ActionListener actionListener = e -> {
			String username = usernameField.getText().trim();
			String userIDText = userIDField.getText().trim();
			String chatroomIDText = chatroomIDField.getText().trim();

			if (username.isEmpty() || userIDText.isEmpty() || chatroomIDText.isEmpty()) {
				JOptionPane.showMessageDialog(null, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
				return; // Early exit if validation fails
			}

			try {
				int userID = Integer.parseInt(userIDText);
				int chatroomID = Integer.parseInt(chatroomIDText);

				// Call the appropriate method to invite the user to the chatroom
				this.client.inviteUserToChatroom(username, userID, chatroomID);
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(null, "Please enter valid User ID and Chatroom ID (numeric).", "Error",
						JOptionPane.ERROR_MESSAGE);
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(null, "Error inviting user to chatroom: " + ex.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		};

		showTripleInputDialog("Invite User To Chatroom", "Username:", usernameField, "User ID:", userIDField,
				"Chatroom ID:", chatroomIDField, actionListener);
	}

	private void doJoinChatroom() {
		JTextField chatroomIDField = new JTextField(15);

		ActionListener actionListener = e -> {
			try {
				String chatroomIDText = chatroomIDField.getText();
				int joinChatroomID = Integer.parseInt(chatroomIDText);

				this.client.joinChatroom(joinChatroomID);

			} catch (NumberFormatException ex) {
				System.out.println("Invalid chatroom ID. Please enter a valid number.");
			} catch (IOException err) {
				System.out.println("Error joining the chatroom: " + err.getMessage());
			}
		};

		showInputDialog("Join Chatroom", "Chatroom ID:", chatroomIDField, actionListener);
	}

	private void doLeaveChatroom() {
		JTextField chatroomIDField = new JTextField(15);

		ActionListener actionListener = e -> {
			try {
				String chatroomIDText = chatroomIDField.getText();
				int leaveChatroomID = Integer.parseInt(chatroomIDText);

				this.client.leaveChatroom(leaveChatroomID);
				
			} catch (NumberFormatException ex) {
				System.out.println("Invalid chatroom ID. Please enter a valid number.");
			} catch (IOException err) {
				System.out.println("Error leaving the chatroom: " + err.getMessage());
			}
		};

		showInputDialog("Leave Chatroom", "Chatroom ID:", chatroomIDField, actionListener);
	}

	/* messaging functions */
	private boolean doSendMessageToUser(String message, String toUsername, int toUserID) { // []
		try {
			this.client.sendMessageToUser(message, toUsername, toUserID);
			System.out.println("Sent message to user!");
			return true;
		} catch (IOException e) {
			System.out.println(e);
			System.out.println("Msg not sent!");
			return false;
		}
	}// doSendMessageToUser()

	private boolean doSendMessageToChatroom(String message, int chatroomID) {
		try {
			this.client.sendMessageToChatroom(message, chatroomID);
			System.out.println("Sent message to chatroom!");
			return true;
		} catch (IOException e) {
			System.out.println(e);
			System.out.println("Msg not sent!");
			return false;
		}

	}// doSendMessageToChatroom()

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
				JOptionPane.showMessageDialog(mainFrame, "Please fill in both fields.", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			try {
				this.client.addUser(username, password);
				JOptionPane.showMessageDialog(mainFrame, "User added successfully.", "Success",
						JOptionPane.INFORMATION_MESSAGE);
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(mainFrame, "Error adding user: " + ex.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
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
				JOptionPane.showMessageDialog(mainFrame, "Please fill in both fields.", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			try {
				this.client.deleteUser(username, password);
				if (operationCheck) {
					JOptionPane.showMessageDialog(mainFrame, "User deleted successfully.", "Success",
							JOptionPane.INFORMATION_MESSAGE);
				}
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(mainFrame, "Error deleting user: " + ex.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
			// operationCheck = false;
		};
		showDoubleInputDialog("Delete User", "Username:", usernameField, "Password:", passwordField, actionListener);
	}// doDeleteUser()

	/* UI */
	public void appInitialize(JFrame mainFrame) {
		if (mainFrame == null) {
			mainFrame = new JFrame();
		}

		// Menu setup
		if (menuBar.getMenuCount() == 0) {
			userMenu = new JMenu("𓈒∘☁︎");
			peopleMenu = new JMenu("People");
			// Add items to the menu bar
			addUserMenuItems();
			menuBar.add(userMenu);
			menuBar.add(peopleMenu);
			if (user != null && user.getAdminStatus() == true) {
				addAdminMenu(mainFrame);
			}
			mainFrame.setJMenuBar(menuBar);
		}

		// Initialize message models and lists
		privateMessagesModel = new DefaultListModel<>();
		chatroomMessagesModel = new DefaultListModel<>();
		privateMessagesList = new JList<>(privateMessagesModel);
		chatroomMessagesList = new JList<>(chatroomMessagesModel);
		privateMessagesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		chatroomMessagesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// Set up tabbed pane for Private Messages and Chatrooms
		JTabbedPane tabbedPane = new JTabbedPane();
		JPanel privateMessagesPanel = new JPanel(new BorderLayout());
		privateMessagesPanel.add(new JScrollPane(privateMessagesList), BorderLayout.CENTER);
		JPanel chatroomMessagesPanel = new JPanel(new BorderLayout());
		chatroomMessagesPanel.add(new JScrollPane(chatroomMessagesList), BorderLayout.CENTER);
		tabbedPane.addTab("Private Messages", privateMessagesPanel);
		tabbedPane.addTab("Chatrooms", chatroomMessagesPanel);

		// Panel to hold all message areas
		msgAreaPanel = new JPanel();
		msgAreaPanel.setLayout(new BoxLayout(msgAreaPanel, BoxLayout.Y_AXIS)); // Stack areas vertically

		// Panel for the input text area
		JPanel inputPanel = createInputPanel();
		mainFrame.add(inputPanel, BorderLayout.SOUTH);

		// Set up the split pane layout
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabbedPane, msgAreaPanel);
		splitPane.setDividerLocation(250);
		splitPane.setResizeWeight(0.2);

		mainFrame.add(splitPane, BorderLayout.CENTER);
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setVisible(true);

		// Start message processing thread
		startMessageProcessingThread();

		// Add listeners for both lists
		privateMessagesList.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting() && privateMessagesList.getSelectedValue() != null) {
				Message selectedMessage = privateMessagesList.getSelectedValue();
				int userID = selectedMessage.getFromUserID();

				createPrivateMessageArea(userID);
				JTextArea pmArea = privateMessageAreas.get(userID);

				if (pmArea != null) {
					msgAreaPanel.removeAll();
					msgAreaPanel.add(new JScrollPane(pmArea));
					pmArea.setVisible(true);
					msgAreaPanel.revalidate();
					msgAreaPanel.repaint();
				}
			}
		});

		chatroomMessagesList.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting() && chatroomMessagesList.getSelectedValue() != null) {
				Message selectedChatroomMessage = chatroomMessagesList.getSelectedValue();
				int chatroomID = selectedChatroomMessage.getToChatroomID();

				createChatroomMessageArea(chatroomID);
				JTextArea chatroomArea = chatroomMessageAreas.get(chatroomID);

				if (chatroomArea != null) {
					msgAreaPanel.removeAll();
					msgAreaPanel.add(new JScrollPane(chatroomArea));
					chatroomArea.setVisible(true);
					msgAreaPanel.revalidate();
					msgAreaPanel.repaint();
				}
			}
		});

		// Add ChangeListener to the tabbed pane to toggle between Private and Chatroom
		// messages
		tabbedPane.addChangeListener(e -> {
			activeTabIndex = tabbedPane.getSelectedIndex();

			msgAreaPanel.removeAll();

			if (activeTabIndex == 0) { // Private Messages Tab
				Message selectedUserMessage = privateMessagesList.getSelectedValue();
				if (selectedUserMessage != null) {
					int userID = selectedUserMessage.getFromUserID();

					JTextArea privateArea = privateMessageAreas.get(userID);
					if (privateArea != null) {
						msgAreaPanel.add(new JScrollPane(privateArea));
						privateArea.setVisible(true);
					}
				}
			} else if (activeTabIndex == 1) { // Chatrooms Tab
				Message selectedChatroomMessage = chatroomMessagesList.getSelectedValue();
				if (selectedChatroomMessage != null) {
					int chatroomID = selectedChatroomMessage.getToChatroomID();
					JTextArea chatroomArea = chatroomMessageAreas.get(chatroomID);
					if (chatroomArea != null) {
						msgAreaPanel.add(new JScrollPane(chatroomArea));
						chatroomArea.setVisible(true);
					}
				}
			}

			msgAreaPanel.revalidate();
			msgAreaPanel.repaint();
		});
	}// appInitialize()

	private void showInitDialog() {
		JTextField IPField = new JTextField(20);
		JTextField portField = new JTextField(20);

		ActionListener actionListener = e -> {
			String ip = IPField.getText().trim();
			String portText = portField.getText();
			int port = Integer.parseInt(portText);

			try {
				client.connectToServer(ip, port);
			} catch (IOException err) {
				System.out.println("Error connecting to server!");
				err.printStackTrace();
			}
			showLoginDialog();
		};

		showDoubleInputDialog("WeDiscuss Init", "IP:", IPField, "Port:", portField, actionListener);

	}// showInitDialog()

	private void showLoginDialog() {
		JTextField usernameField = new JTextField(15);
		JPasswordField passwordField = new JPasswordField(15);

		ActionListener actionListener = e -> {
			String username = usernameField.getText().trim();
			String password = new String(passwordField.getPassword()).trim();
			if (doSendLoginRequest(username, password)) {
				showMainApplication(user);
			} else {
				System.out.println("Login error");
			}
		};

		showDoubleInputDialog("WeDiscuss Login", "Username:", usernameField, "Password:", passwordField,
				actionListener);
	}// showLoginDialog()

	private void showMainApplication(User user) {
		appInitialize(mainFrame);
		mainFrame.setVisible(true);
	}// showMainApplication()

	private void displayPopulation(int type) {
		if (type == 0) {
			// Display Users
			JDialog usersListDialog = new JDialog(mainFrame, "Users", true);
			DefaultListModel<String> usersListModel = new DefaultListModel<>();

			usersListModel.clear();
			for (Integer userID : userMap.keySet()) {
				String user = "User ID: " + userID + " | Username: " + userMap.get(userID);
				usersListModel.addElement(user); // Add to the model
			}

			JList<String> usersList = new JList<>(usersListModel);
			usersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			JScrollPane usersScrollPane = new JScrollPane(usersList);

			usersListDialog.setLayout(new BorderLayout());
			usersListDialog.add(usersScrollPane, BorderLayout.CENTER);

			usersListDialog.setSize(200, 150);
			usersListDialog.setLocationRelativeTo(mainFrame);
			usersListDialog.setVisible(true);

			usersListDialog.revalidate();
			usersListDialog.repaint();

		} else if (type == 1) {
			// Display Chatrooms
			JDialog chatroomsListDialog = new JDialog(mainFrame, "Chatrooms", true);
			DefaultListModel<String> chatroomsListModel = new DefaultListModel<>();

			chatroomsListModel.clear();
			for (Integer chatroomID : chatrooms.keySet()) {
				chatroomsListModel.addElement("Chatroom " + chatroomID);
			}

			JList<String> chatroomsList = new JList<>(chatroomsListModel);
			chatroomsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			JScrollPane chatroomsScrollPane = new JScrollPane(chatroomsList);

			chatroomsListDialog.setLayout(new BorderLayout());
			chatroomsListDialog.add(chatroomsScrollPane, BorderLayout.CENTER);

			chatroomsListDialog.setSize(200, 150);
			chatroomsListDialog.setLocationRelativeTo(mainFrame);
			chatroomsListDialog.setVisible(true);

			chatroomsListDialog.revalidate();
			chatroomsListDialog.repaint();
		}
	}// displayPopulation()

	protected void showInputDialog(String dialogTitle, String labelText, JComponent inputField,
			ActionListener actionListener) {
		JDialog dialog = new JDialog(mainFrame, dialogTitle, true); // Modal dialog
		dialog.setSize(300, 100);
		dialog.setLocationRelativeTo(mainFrame);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		JPanel inputPanel = new JPanel();
		inputPanel.setLayout(new FlowLayout());

		JLabel label = new JLabel(labelText);
		inputPanel.add(label);

		inputPanel.add(inputField);

		JButton submitButton = new JButton("Submit");
		inputPanel.add(new JLabel());
		inputPanel.add(submitButton);

		panel.add(inputPanel, BorderLayout.CENTER);
		dialog.add(panel);

		// submit listener disposes dialog
		ActionListener submitListener = e -> {
			actionListener.actionPerformed(e);
			dialog.dispose();
		};

		submitButton.addActionListener(submitListener);

		dialog.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				dialog.dispose();
			}
		});

		dialog.setVisible(true);
	}// showInputDialog()

	protected void showDoubleInputDialog(String title, String label1, JComponent input1, String label2,
			JComponent input2, ActionListener actionListener) {
		JDialog dialog = new JDialog(mainFrame, title, true); // Modal dialog
		dialog.setSize(300, 150);
		dialog.setLocationRelativeTo(mainFrame);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		JPanel inputPanel = new JPanel();
		inputPanel.setLayout(new FlowLayout());

		JLabel firstLabel = new JLabel(label1);
		inputPanel.add(firstLabel);
		inputPanel.add(input1);

		JLabel secondLabel = new JLabel(label2);
		inputPanel.add(secondLabel);
		inputPanel.add(input2);

		JButton submitButton = new JButton("Submit");
		inputPanel.add(submitButton);

		panel.add(inputPanel, BorderLayout.CENTER);
		dialog.add(panel);

		ActionListener submitListener = e -> {
			actionListener.actionPerformed(e);
			dialog.dispose();
		};

		submitButton.addActionListener(submitListener);

		dialog.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				dialog.dispose();
			}
		});

		dialog.setVisible(true);
	}// showDoubleInputDialog()

	protected void showTripleInputDialog(String title, String label1, JComponent input1, String label2,
			JComponent input2, String label3, JComponent input3, ActionListener actionListener) {
		JDialog dialog = new JDialog(mainFrame, title, true); // Modal dialog
		dialog.setSize(300, 200);
		dialog.setLocationRelativeTo(mainFrame);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		JPanel inputPanel = new JPanel();
		inputPanel.setLayout(new FlowLayout());

		JLabel firstLabel = new JLabel(label1);
		inputPanel.add(firstLabel);
		inputPanel.add(input1);

		JLabel secondLabel = new JLabel(label2);
		inputPanel.add(secondLabel);
		inputPanel.add(input2);

		JLabel thirdLabel = new JLabel(label3);
		inputPanel.add(thirdLabel);
		inputPanel.add(input3);
		
		JButton submitButton = new JButton("Submit");
		inputPanel.add(submitButton);

		panel.add(inputPanel, BorderLayout.CENTER);
		dialog.add(panel);

		ActionListener submitListener = e -> {
			actionListener.actionPerformed(e);
			dialog.dispose();
		};

		submitButton.addActionListener(submitListener);

		dialog.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				dialog.dispose();
			}
		});

		dialog.setVisible(true);
	} // showTripleInputDialog

	private void addAdminMenu(JFrame mainFrame) {
		adminMenu = new JMenu("Admin Tools");

		JMenuItem addUserItem = new JMenuItem("Add User");
		addUserItem.addActionListener(e -> doAddUser());
		adminMenu.add(addUserItem);

		JMenuItem deleteUserItem = new JMenuItem("Delete User");
		deleteUserItem.addActionListener(e -> doDeleteUser());
		adminMenu.add(deleteUserItem);

		JMenuItem getMessageLogsItem = new JMenuItem("Get Message Logs");
		getMessageLogsItem.addActionListener(e -> doGetMessageLogs());
		adminMenu.add(getMessageLogsItem);

		JMenuItem getChatLogsItem = new JMenuItem("Get Chat Logs");
		getChatLogsItem.addActionListener(e -> doGetChatLogs());
		adminMenu.add(getChatLogsItem);

		menuBar.add(adminMenu);
	}// addAdminMenu()

	// Utility to create panel for text input
	private JPanel createInputPanel() {
		JPanel inputPanel = new JPanel(new BorderLayout());
		inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JTextArea inputTextArea = new JTextArea();
		inputTextArea.setEditable(true);
		inputTextArea.setLineWrap(true);
		inputTextArea.setWrapStyleWord(true);
		inputTextArea.setRows(3);
		JScrollPane inputScrollPane = new JScrollPane(inputTextArea);

		inputPanel.add(inputScrollPane, BorderLayout.CENTER);

		// Send button
		JButton sendButton = new JButton(">");
		sendButton.addActionListener(e -> {
			sendMessageBox(inputTextArea);
		});

		// KeyListener for sending messages when the Enter key is pressed
		inputTextArea.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					// Prevent new line when Enter key is pressed
					e.consume();
					sendMessageBox(inputTextArea);
				}
			}
		});

		inputPanel.add(sendButton, BorderLayout.EAST);

		return inputPanel;
	}// createInputPanel()

	private void sendMessageBox(JTextArea inputTextArea) {
		String messageText = inputTextArea.getText().trim();

		if (!messageText.isEmpty()) {
			if (activeTabIndex == 0) {
				// Send to Private Messages
				Message selectedUserMessage = privateMessagesList.getSelectedValue();
				if (selectedUserMessage != null) {
					String toUsername = selectedUserMessage.getFromUserName();
					int toUserID = selectedUserMessage.getFromUserID();
					System.out.println("sending to user: " + toUsername);

					doSendMessageToUser(messageText, toUsername, toUserID);

					JTextArea pmArea = privateMessageAreas.get(toUserID);
					if (pmArea != null) {
						msgAreaPanel.removeAll();
						msgAreaPanel.add(new JScrollPane(pmArea));
						pmArea.setVisible(true);
					}

					pmArea.append(user.getUsername() + ": " + messageText + "\n");

					msgAreaPanel.revalidate();
					msgAreaPanel.repaint();
				}
			} else if (activeTabIndex == 1) {
				// Send to Chatrooms
				Message selectedChatroomMessage = chatroomMessagesList.getSelectedValue();
				int chatroomID = selectedChatroomMessage.getToChatroomID();
				createChatroomMessageArea(chatroomID);

				if (selectedChatroomMessage != null) {
					System.out.println("sending to chatroom: " + chatroomID);

					doSendMessageToChatroom(messageText, chatroomID);

					JTextArea chatroomArea = chatroomMessageAreas.get(chatroomID);
					if (chatroomArea != null) {
						msgAreaPanel.removeAll();
						msgAreaPanel.add(new JScrollPane(chatroomArea));
						chatroomArea.setVisible(true);
					}

					msgAreaPanel.revalidate();
					msgAreaPanel.repaint();
				}
			}

			inputTextArea.setText("");
		}
	}// sendMessageBox()

	// Utility to add menu items to the menu bar
	private void addUserMenuItems() {
		JMenuItem createChatroomItem = new JMenuItem("Create Chatroom");
		createChatroomItem.addActionListener(e -> doCreateChatroom());

		JMenuItem joinChatroomItem = new JMenuItem("Join Chatroom");
		joinChatroomItem.addActionListener(e -> doJoinChatroom());

		JMenuItem inviteUserToChatroomItem = new JMenuItem("Invite User to Chatroom");
		inviteUserToChatroomItem.addActionListener(e -> doInviteUserToChatroom());

		JMenuItem leaveChatroomItem = new JMenuItem("Leave Chatroom");
		leaveChatroomItem.addActionListener(e -> doLeaveChatroom());

		JMenuItem displayUsersItem = new JMenuItem("Display Users");
		displayUsersItem.addActionListener(e -> displayPopulation(0));

		JMenuItem displayChatroomsItem = new JMenuItem("Display Chatrooms");
		displayChatroomsItem.addActionListener(e -> displayPopulation(1));

		JMenuItem changePasswordItem = new JMenuItem("Change Password");
		changePasswordItem.addActionListener(e -> doSendPasswordChangeRequest());

		JMenuItem logoutItem = new JMenuItem("Logout");
		logoutItem.addActionListener(e -> doSendLogoutRequest());

		userMenu.add(createChatroomItem);
		userMenu.add(joinChatroomItem);
		userMenu.add(inviteUserToChatroomItem);
		userMenu.add(leaveChatroomItem);
		userMenu.addSeparator();
		userMenu.add(changePasswordItem);
		userMenu.add(logoutItem);

		peopleMenu.add(displayUsersItem);
		peopleMenu.add(displayChatroomsItem);
	}// addUserMenuItems()

	// Utility to create a JTextArea for messages
	private JTextArea createMsgInfoArea() {
		JTextArea msgInfoArea = new JTextArea();
		msgInfoArea.setEditable(false);
		msgInfoArea.setLineWrap(true);
		msgInfoArea.setWrapStyleWord(true);
		return msgInfoArea;
	}// createMsgInfoArea()

	private void createPrivateMessageArea(int userID) {
		if (!privateMessageAreas.containsKey(userID)) {
			JTextArea messageArea = createMsgInfoArea();
			privateMessageAreas.put(userID, messageArea);
			privateMsgInfoScrollPane = new JScrollPane(messageArea);
			msgAreaPanel.add(privateMsgInfoScrollPane, Integer.toString(userID));
		}
	}// createPrivateMessageArea()

	private void createChatroomMessageArea(int chatroomID) {
		if (!chatroomMessageAreas.containsKey(chatroomID)) {
			JTextArea chatroomArea = createMsgInfoArea();
			chatroomMessageAreas.put(chatroomID, chatroomArea);
			chatroomMsgInfoScrollPane = new JScrollPane(chatroomArea);
			msgAreaPanel.add(chatroomMsgInfoScrollPane, Integer.toString(chatroomID));
		}
	}// createChatroomMessageArea()

	private void appendMessageToCorrectArea(Message message) {
		if (message.getToChatroomID() != -1) {
			int chatroomID = message.getToChatroomID();
			createChatroomMessageArea(chatroomID); // Create if not exists
			JTextArea chatroomArea = chatroomMessageAreas.get(chatroomID);
			if (chatroomArea != null) {
				chatroomArea.append(message.getFromUserName() + ": " + message.getContents() + "\n");
			}
		} else {
			int userID = message.getFromUserID();
			createPrivateMessageArea(userID); // Create if not exists
			JTextArea privateArea = privateMessageAreas.get(userID);
			if (privateArea != null) {
				privateArea.append(message.getFromUserName() + ": " + message.getContents() + "\n");
			}
		}
	}// appendMessageToCorrectArea()

	public class MessageListCellRenderer extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {

			// Call the superclass method to get the default rendering behavior
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			if (value instanceof Message) {
				Message message = (Message) value;

				String displayText = "Message from: " + message.getFromUserName();

				// If the message is from a chatroom, display the chatroom ID or name
				if (message.getChatroom() != null) {
					displayText = "Chatroom " + message.getToChatroomID();
				}

				setText(displayText);

				if (isSelected) {
					setBackground(Color.LIGHT_GRAY); // Light gray for selection
				} else {
					setBackground(Color.WHITE); // White for unselected
				}

				setForeground(Color.BLACK);

				setFont(new Font("Arial", Font.PLAIN, 14));
				setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
			}

			return this;
		}
	}
}
