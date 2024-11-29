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
	protected Client client;
	private ArrayList<Message> userMessages = new ArrayList<Message>();
	JFrame mainFrame = new JFrame("WeDiscuss");
	// menu bar items
	JMenuBar menuBar = new JMenuBar();
	// login
	JTextArea loginTextArea = new JTextArea();
	// swing components to display message and chatroom lists
	private DefaultListModel<Message> privateMessagesModel;
	private DefaultListModel<Message> chatroomMessagesModel;
	private JList<Message> privateMessagesList;
	private JList<Message> chatroomMessagesList;
	private int activeTabIndex = 0; // 0 - "Private Messages", 1 - "Chatrooms"

	// User and chatroom maps caches - populated on initUpdate() call
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

//		appInitialize(mainFrame);
	}// ClientUI()

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			ClientUI clientUI = new ClientUI();
			clientUI.showLoginDialog();
		});
	}// main()

	public void initUpdate(Message message) {
		// load the models from the User's cache maps
		MessageType type = message.getMessageType();
		if (type.equals(MessageType.LOGIN)) {
			if (!message.getContents().equals("SUCCESS")) {
				doSendLoginRequest(user.getUsername(), user.getPassword());
			}
			// populate chatrooms and userMap
			// Load user model from userMap (for private messages)
			privateMessagesModel.clear();
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

	public void update(Message message) {//[]
		// For updating the models throughout the User's session
//		privateMessagesModel.clear();
//		chatroomMessagesModel.clear();

		MessageType type = message.getMessageType();
		switch (type) {
//		Client: clientGui.update(message);
		case LOGOUT:
			doSendLogoutRequest();
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
/*
 * 	private User doSendLoginRequest(String userName, String password) {

		try {
			return this.client.sendLoginRequest(userName, password);
		} catch (IOException e) {
			System.out.println(e);
		}
	}// doSendLoginRequest()
 */
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
	    JTextField newChatroomField = new JTextField(15);

	    ActionListener actionListener = e -> {
	        try {
	            String chatroomName = newChatroomField.getText(); //
	            this.client.createChatroom();
	        } catch (IOException err) {
	            System.out.println(err);
	        }
	    };
	    // no chatroom name needed, update chatrooms list
	    showInputDialog("Create Chatroom", "New Chatroom Name:", newChatroomField, actionListener);
	}


	private void doInviteUserToChatroom() {
	    JTextField usernameField = new JTextField(15);
	    JTextField userIDField = new JTextField(15);

	    ActionListener actionListener = e -> {
	        String username = usernameField.getText().trim();
	        String userIDText = userIDField.getText().trim();

	        if (username.isEmpty() || userIDText.isEmpty()) {
	            JOptionPane.showMessageDialog(null, "Please fill in both fields.", "Error", JOptionPane.ERROR_MESSAGE);
	            return; // Early exit if validation fails
	        }

	        try {
	            int userID = Integer.parseInt(userIDText);

	            this.client.inviteUserToChatroom(username, userID);
	            
	        } catch (NumberFormatException ex) {
	            JOptionPane.showMessageDialog(null, "Please enter a valid User ID (numeric).", "Error", JOptionPane.ERROR_MESSAGE);
	        } catch (IOException ex) {
	            JOptionPane.showMessageDialog(null, "Error inviting user to chatroom: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
	        }
	    };

	    
	    showDoubleInputDialog(
	        "Invite User To Chatroom",        
	        "Username:", usernameField,       
	        "User ID:", userIDField,          
	        actionListener                    
	    );
	}


	private void doJoinChatroom() {
	    JTextField chatroomIDField = new JTextField(15);

	    ActionListener actionListener = e -> {
	        try {
	            String chatroomIDText = chatroomIDField.getText();
	            int joinChatroomID = Integer.parseInt(chatroomIDText); // Convert to int

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
	            int leaveChatroomID = Integer.parseInt(chatroomIDText); // Convert to int

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
		//
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

	
//    if (doSendLoginRequest(username, password)) {
//    loginDialog.setVisible(false); 
//    showMainApplication(user);
//} else {
//    loginTextArea.setText("Invalid credentials. Please try again.");
//}
	private void showLoginDialog() {
	    // Define the input fields for username and password
	    JTextField usernameField = new JTextField(15);
	    JPasswordField passwordField = new JPasswordField(15);
	    
	    ActionListener actionListener = e -> {
	        String username = usernameField.getText().trim();
	        String password = new String(passwordField.getPassword()).trim();


			if (username.equals("admin") && password.equals("123")) {
				// Admin login
				AdminUI adminUI = new AdminUI(mainFrame);
				adminUI.appInitialize(mainFrame); // Initialize admin UI components
				mainFrame.setVisible(true);
			} else if (username.equals("user") && password.equals("123")) {
				// Regular user login
				appInitialize(mainFrame); // Initialize client UI components
				mainFrame.setVisible(true);
			} else {
				// give error for invalid credentials in dialog text area
			}
	    };

	    showDoubleInputDialog(
	        "WeDiscuss Login",          
	        "Username:", usernameField, 
	        "Password:", passwordField, 
	        actionListener              
	    );
	}// showLoginDialog()


	private void showMainApplication(User user) {
		if (user.getAdminStatus()) {
			AdminUI adminUI = new AdminUI(mainFrame);
			adminUI.appInitialize(mainFrame);
			mainFrame.setVisible(true);
		} else {
			appInitialize(mainFrame);
			mainFrame.setVisible(true);
		}
		mainFrame.setVisible(true);
	}// showMainApplication()

	protected void showInputDialog(String dialogTitle, String labelText, JComponent inputField, ActionListener actionListener) {
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

	protected void showDoubleInputDialog(String title, String label1, JComponent input1, String label2, JComponent input2, ActionListener actionListener) {
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


	public void appInitialize(JFrame mainFrame) {
		if (mainFrame == null) {
			mainFrame = new JFrame();
		}
		
		if (menuBar.getMenuCount() == 0) {
			JMenu fileMenu = new JMenu("𓈒∘☁︎");

			JMenuItem createChatroomItem = new JMenuItem("Create Chatroom");
			createChatroomItem.addActionListener(e -> {
				doCreateChatroom();
			});

			JMenuItem joinChatroomItem = new JMenuItem("Join Chatroom");
			joinChatroomItem.addActionListener(e -> {
				doJoinChatroom();
			});

			JMenuItem inviteUserToChatroomItem = new JMenuItem("Invite User to Chatroom");
			inviteUserToChatroomItem.addActionListener(e -> {
				doInviteUserToChatroom();
			});

			JMenuItem leaveChatroomItem = new JMenuItem("Leave Chatroom");
			leaveChatroomItem.addActionListener(e -> {
				doLeaveChatroom();
			});

			JMenuItem changePasswordItem = new JMenuItem("Change Password");
			changePasswordItem.addActionListener(e -> {
				doSendPasswordChangeRequest();
			});

			JMenuItem logoutItem = new JMenuItem("Logout");
			logoutItem.addActionListener(e -> {
				doSendLogoutRequest(); 
			});

			fileMenu.add(createChatroomItem);
			fileMenu.add(joinChatroomItem);
			fileMenu.add(inviteUserToChatroomItem);
			fileMenu.add(leaveChatroomItem);
			fileMenu.addSeparator();
			fileMenu.add(changePasswordItem);
			fileMenu.add(logoutItem);

			menuBar.add(fileMenu);
			mainFrame.setJMenuBar(menuBar);
		} // if

		privateMessagesModel = new DefaultListModel<>();
		chatroomMessagesModel = new DefaultListModel<>();

		privateMessagesList = new JList<>(privateMessagesModel);
		chatroomMessagesList = new JList<>(chatroomMessagesModel);
		privateMessagesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		chatroomMessagesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JTabbedPane tabbedPane = new JTabbedPane();

		JPanel privateMessagesPanel = new JPanel(new BorderLayout());
		privateMessagesPanel.add(new JScrollPane(privateMessagesList), BorderLayout.CENTER);

		JPanel chatroomMessagesPanel = new JPanel(new BorderLayout());
		chatroomMessagesPanel.add(new JScrollPane(chatroomMessagesList), BorderLayout.CENTER);

		tabbedPane.addTab("Private Messages", privateMessagesPanel);
		tabbedPane.addTab("Chatrooms", chatroomMessagesPanel);

		infoPanel = new JPanel(new BorderLayout());
		infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		msgInfoArea = new JTextArea();
		msgInfoArea.setEditable(false);
		msgInfoArea.setLineWrap(true); 
		msgInfoArea.setWrapStyleWord(true); 
	
		JScrollPane msgInfoScrollPane = new JScrollPane(msgInfoArea); 
		msgInfoScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS); 
		msgInfoScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); 
																								
		infoPanel.add(msgInfoScrollPane, BorderLayout.CENTER);

		JPanel inputPanel = new JPanel(new BorderLayout());
		inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10));

		
		JTextArea inputTextArea = new JTextArea();
		inputTextArea.setEditable(true);
		inputTextArea.setLineWrap(true); 
		inputTextArea.setWrapStyleWord(true); 
		inputTextArea.setRows(3); 
		JScrollPane inputScrollPane = new JScrollPane(inputTextArea);

		
		inputPanel.add(inputScrollPane, BorderLayout.CENTER);

		// change listener to update activeTabIndex
		tabbedPane.addChangeListener(e -> {
			activeTabIndex = tabbedPane.getSelectedIndex(); // Update the active tab index
		});

		// send button
		JButton sendButton = new JButton(">");
		sendButton.addActionListener(e -> {
			String messageText = inputTextArea.getText().trim();

			if (!messageText.isEmpty()) {
				if (activeTabIndex == 0) {
					// Send to Private Messages
					Message selectedUserMessage = privateMessagesList.getSelectedValue();
					if (selectedUserMessage != null) {
						String toUsername = selectedUserMessage.getContents().split(":")[1].trim(); 
																									
						int toUserID = selectedUserMessage.getToUserID(); 
						doSendMessageToUser(messageText, toUsername, toUserID); 
					}
				} else if (activeTabIndex == 1) {
					// Send to Chatrooms
					Message selectedChatroomMessage = chatroomMessagesList.getSelectedValue();
					if (selectedChatroomMessage != null) {
						int chatroomID = selectedChatroomMessage.getToChatroomID();
																		
						doSendMessageToChatroom(messageText, chatroomID); 
					}
				}

				inputTextArea.setText("");
			}
		});

		inputPanel.add(sendButton, BorderLayout.EAST);
		infoPanel.add(inputPanel, BorderLayout.SOUTH);

		// selection listener to the JList to update the chat display area
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

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabbedPane, infoPanel);
		splitPane.setDividerLocation(300);
		splitPane.setResizeWeight(0.2); 

		mainFrame.add(splitPane, BorderLayout.CENTER);
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setVisible(true);

	}// appInitialize()

}
