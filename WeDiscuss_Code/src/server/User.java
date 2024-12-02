package server;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.io.*;



import shared.Message;
import shared.MessageCreator;
import shared.MessageType;

public class User implements Serializable{
	//static counter to generate unique IDs
	
	private static final long serialVersionUID = 1L;
	
	private static int IDCounter = 0;
	
	private String username;
	private String password;
	private int ID;
	private boolean status; //False = offline, True = online
	private boolean adminStatus; //False = non-admin, True = admin
	private List<Message> messageInbox = Collections.synchronizedList(new ArrayList<Message>());
	private List<Integer> involvedChatrooms = Collections.synchronizedList(new ArrayList<Integer>());
	
	
	private ConcurrentHashMap<Integer, List<Message>> messagesFromUsers = new ConcurrentHashMap<Integer, List<Message>>();
	
	

	
	//Constructor when loading
	public User(String username, String password, boolean adminStatus, int userID) {
		this.username = username;
		this.password = password;
		this.adminStatus = adminStatus;
		this.ID = userID;
		
		//just for keeping it
		if(userID >= IDCounter)
		{
			IDCounter = userID;
		}
		
		System.out.println(username+ " "+ password + " " + adminStatus + " " + userID);
		
		this.status = false; //Initially offline
		loadMessageInbox();
		loadChatrooms();
	}
	
	//Constructor when making new account
		public User(String username, String password, boolean adminStatus) {
			this.username = username;
			this.password = password;
			this.adminStatus = adminStatus;
			//increment ID Counter
			IDCounter += 6;
			this.ID = IDCounter+6;
			
			
			this.status = false; //Initially offline
			
			String messageFile = Integer.toString(ID) + "Inbox.txt";
			try {
	            File file = new File(messageFile);

	            if (file.createNewFile()) {
	                
	            } else {
	                System.out.println("File already exists. USER ERROR");
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
			
			String chatsFile = Integer.toString(ID) + "Chats.txt";
			
			try {
	            File file = new File(chatsFile);

	            if (file.createNewFile()) {
	                
	            } else {
	                System.out.println("File already exists. USER ERROR");
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
			
		}
		
		
	
		
		
	//Load messages from inbox file
	public void loadMessageInbox() {
		
		String messageFiles = Integer.toString(ID) + "Inbox.txt";
		try {
            File file = new File(messageFiles);

            if (file.createNewFile()) {
                
            } else {
            	
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		try 
		{
			String messageFile = Integer.toString(ID) + "Inbox.txt";
			
			File myFile = new File(messageFile);
			Scanner reader = new Scanner(myFile);

			//first populate the messages of the inbox
			while (reader.hasNextLine())
			{
				//getline and set delimiters
				Scanner line = new Scanner(reader.nextLine()).useDelimiter("|"); // \\s+ means whitespace
				
				ArrayList<String> token = new ArrayList<String>();
				line.tokens();
				
				//grab all the tokens
				while(line.hasNext())
				{
					token.add(line.next());
				}
				
				//if there are more or less than 7 tokens, then it is invalid
				if (token.size() != 7)
				{
					line.close(); //do nothing and skip this iteration
            		continue;
				}
				
				
				
				//add all message to the arraylist
				Message add;
				MessageCreator create;
				create = new MessageCreator(MessageType.UTU);
				
				create.setContents(token.get(0)); //add the message
				create.setDate(Long.parseLong(token.get(1))); // add the date
				create.setToUserName(token.get(2)); //add the toUsername
				create.setToUserID(Integer.parseInt(token.get(3))); //add the toUserid
				
				create.setFromUserName(token.get(4)); //add from username
				create.setFromUserID(Integer.parseInt(token.get(5))); //add from user id
				
				add = new Message(create);
				
				messageInbox.add(add);
				
				if (!messagesFromUsers.containsKey(Integer.parseInt(token.get(5))))
				{
					messagesFromUsers.put(Integer.parseInt(token.get(5)), new ArrayList<Message>());
				}
				
				messagesFromUsers.get(Integer.parseInt(token.get(5))).add(add);
				
				
				line.close();
			}
			reader.close();
			
		}
		catch (IOException e) {
        	e.printStackTrace();
        }
	}
		

	//Load chatrooms IDs from the chatrooms file
	public void loadChatrooms() {
		
		String chatsFiles = Integer.toString(ID) + "Chats.txt";
		
		try {
            File file = new File(chatsFiles);

            if (file.createNewFile()) {
                
            } else {
                
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		try 
		{
			
			String chatsFile = Integer.toString(ID) + "Chats.txt";
			
			File File = new File(chatsFile);
			Scanner readers = new Scanner(File);

			//first populate the chatroom ids
			while (readers.hasNextLine())
			{
				//getline and set delimiters
				Scanner line = new Scanner(readers.nextLine()).useDelimiter("\\s+"); // \\s+ means whitespace
				
				ArrayList<String> token = new ArrayList<String>();
				line.tokens();
				
				//grab all the tokens
				while(line.hasNext())
				{
					token.add(line.next());
				}
				
				//if there are more or less than 1 tokens, then it is invalid
				if (token.size() != 1)
				{
					line.close(); //do nothing and skip this iteration
            		continue;
				}
				
				//add chatroom ids to array
				involvedChatrooms.add(Integer.valueOf(token.get(0)));
				
				line.close();
			}
			readers.close();
			
			
		}
		catch (IOException e) {
        	e.printStackTrace();
        }
		
	}
	
	
		
		
	//save chatroom ID to chatrooms file
	public void saveChatrooms() {
		
		String chatsFile = Integer.toString(ID) + "Chats.txt";
		
		try
		{
			FileWriter myFile = new FileWriter(chatsFile); //open file to save on
			for (int i=0; i<involvedChatrooms.size(); i++)
			{
				int userID = involvedChatrooms.get(i);
				
				//write information to file
				myFile.write(Integer.toString(userID));
				myFile.write("\n");
			}
			myFile.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}	
		
	//display messages in inbox with console formatting for testing without GUI use
	//thought this could be useful 
	public void displayMessageInboxToConsole() {
		for (Message message : messageInbox) {
			System.out.println("From: " + message.getFromUserName());
			System.out.println("Date: " + message.getDateSent());
			System.out.println("Type: " + message.getMessageType());
			System.out.println("Content: " + message.getContents());
			System.out.println("--------------------");
		}
	}
	
	
	
	
	//Get user's username
	//return username
	public String getUsername() {
		return username;
	}
	
	//Get the user's password
	//return password
	public String getPassword() {
		return password;
	}
	
	//Get the user's ID
	//return ID
	public int getID() {
		return ID;
	}
	
	//Get the user's online status
	//return true if online, false if offline
	public boolean getStatus() {
		return status;
	}
	
	//Get the user's admin status
	//return true if admin, false if non-admin
	public boolean getAdminStatus() {
		return adminStatus;
	}
	
	//Get list of chatrooms the user is involved in
	//return list of chatroom IDs
	public List<Integer> getChatrooms() {
		return involvedChatrooms;
	}
	
	//Add a chatroom to the user's involved chatrooms list
	//Update: added functionality to save inovolved chatroomID to chatroom file
	public void addChatroom(int chatroomID) {
		if (!involvedChatrooms.contains(chatroomID)) {
			involvedChatrooms.add(chatroomID);
			saveChatrooms();
		}
	}
	
	//Add a message to the user's inbox
	//Update: added ability to save message to file
	public void addToInbox(Message message) {
		this.messageInbox.add(message);
		
		//append the message to the messagesFile
		String messageFile = Integer.toString(ID) + "Inbox.txt";
		try
		{
			FileWriter myFile = new FileWriter(messageFile, true); //open file in append mode
			
			String line = message.storeInboxMessage(); //get storable message
				
			//write information at the end of the file
			myFile.write(line);
			myFile.write("\n");
			myFile.close();
			
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	//Display all messages in the user's inbox
	public void displayMessageInbox() {
		for (Message message : messageInbox) {
			System.out.println(message.toString());
		}
	}
	
	//Set a new password for the user
	public void setPassword(String password) {
		this.password = password;
	}
		
	//Set the user's online status
	public void setStatus(boolean status) {
		this.status = status;
	}
	
	
	
	
}
