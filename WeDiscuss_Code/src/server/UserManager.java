package server;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.concurrent.*;

import shared.*;

public class UserManager {
	// all maps 
	protected ConcurrentHashMap<String, String> validUsers = new ConcurrentHashMap<String, String>(); // map of all valid username/pw combos

	private ConcurrentHashMap<String, String> adminStatus = new ConcurrentHashMap<String, String>(); // map of admin status
	private ConcurrentHashMap<Integer, String> userIDToUsername = new ConcurrentHashMap<Integer, String>(); // id to username
	private ConcurrentHashMap<String, Integer> usernameToUserID = new ConcurrentHashMap<String, Integer>(); // username to id
	private ConcurrentHashMap<String, User> allUsers = new ConcurrentHashMap<String, User>(); // map of all existing users to usernames
	
	private List<String> activeUsers = Collections.synchronizedList(new ArrayList<String>()); // list of active users
	private List<String> allUsernames = Collections.synchronizedList(new ArrayList<String>()); // list of all usernames
	
	private List<Integer> allUserIDs = Collections.synchronizedList(new ArrayList<Integer>()); // list of all userIDs
	
	//filename
	private String userFile = "UserFile.txt";
	
	private boolean modified = false;
	
	
	public UserManager() 
	{
		
		try //this will populate the valid user accounts
		{
			File myFile = new File(userFile);
			Scanner reader = new Scanner(myFile);
		
			while (reader.hasNextLine())
			{
				//getline and set delimiters
				Scanner line = new Scanner(reader.nextLine()).useDelimiter("\\s+"); // \\s+ means whitespace
				
				ArrayList<String> token = new ArrayList<String>();
				line.tokens();
				
				//grab all the tokens
				while(line.hasNext())
				{
					token.add(line.next());
				}
				
				//if there are more or less than 4 tokens, then it is invalid
				if (token.size() != 4)
				{
					line.close(); //do nothing and skip this iteration
            		continue;
				}
				
				//get userid to store
				Integer id = Integer.valueOf(token.get(3));
				
				allUsernames.add(token.get(0));
				validUsers.put(token.get(0), token.get(1)); //add username and password to map
				adminStatus.put(token.get(0), token.get(2)); //add admin status to username
				userIDToUsername.put(id,  token.get(0));
				usernameToUserID.put(token.get(0), id);
				
				//create user and add it to list of all users
				User makeuser = new User(token.get(0), token.get(1), Boolean.valueOf(token.get(2)), id);
				allUsers.put(token.get(0), makeuser);
				allUserIDs.add(id);
				
				line.close();
			}
			reader.close();
		}
		catch (IOException e) {
        	e.printStackTrace();
        }
	}
	
	
	public List<Integer> getAllUserIDs(){
		return allUserIDs;
	}
	
	public String getUsername(int id)
	{
		String find;
		find = userIDToUsername.get(id);
		//if id is not found, 'find' will be labeled as null
		return find;
		
	}
	
	public User getUser(int userID)
	{
		String grab = getUsername(userID);
		User find = allUsers.get(grab);
		
		return find;
		
	}
	
	public int getUserID(String username)
	{
		Integer find;
		find = usernameToUserID.get(username);
		//if username is not found, 'find' will be labeled as null
		if (find == null)
		{
			return -1;
		}
		return find;
		
	}
	
	public int authUser(ObjectOutputStream out, Message message, ChatroomManager chatroomManager) {
	    try {
	        // message variable
	        Message Send;
	        MessageCreator create = new MessageCreator(MessageType.LOGIN);
	        create.setContents("Error");
	        Send = new Message(create);  

	        String input = message.getContents();
	        System.out.println("Received login request: " + input); 

	        // Check for bad input
	        if (input == null) {
	            create.setContents("Error: Invalid input.");
	            out.writeObject(Send);
	            return -1;
	        }

	        String[] split = input.split("\\s+");
	        if (split.length != 2) {
	            create.setContents("Error: Invalid number of arguments.");
	            out.writeObject(Send);
	            return -1;
	        }

	        // Check if username exists and is not logged in already
	        if (!validUsers.containsKey(split[0])) {
	            create.setContents("Error: User does not exist.");
	            out.writeObject(Send);
	            return -1;
	        }

	        if (activeUsers.contains(split[0])) {
	            create.setContents("Error: User already logged in.");
	            out.writeObject(Send);
	            return -1;
	        }

	        String storedPassword = validUsers.get(split[0]).trim();
	        String clientPassword = split[1].trim();

	        if (storedPassword.equals(clientPassword)) {	            
	            create.setContents("Success");
	            create.setUser(allUsers.get(split[0]));
	            
	            create.setUserMap(userIDToUsername);
	            
	            create.setChatroomMap(chatroomManager.getUserChatrooms(getUserID(split[0])));
	            
	            Send = new Message(create);
	            out.writeObject(Send);  
	            activeUsers.add(split[0]);  
	            return getUserID(split[0]); 
	        }

	        create.setContents("Error: Incorrect password.");
	        out.writeObject(Send);
	        return -1;

	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    return -1;
	}

	
	public int addUser(ObjectOutputStream out, Message message)
	{
		try {

			//message variable
			Message Send;
			MessageCreator create;
			create = new MessageCreator(MessageType.ADDUSER);
			create.setContents("Error"); 
			Send = new Message(create);// have message ready to return a deny
			
			
			String password = message.getContents();
			
			//check for bad input
			if (password == null)
			{
				out.writeObject(Send); //send the deny message
				return -1;
			}  
		    
		    //check if already exists
		    if (validUsers.containsKey(message.getToUserName()))
		    {
		    	out.writeObject(Send);
		    	return -1;
		    }

			//create user and add it to list of all users
			User makeuser = new User(message.getToUserName(), password, false);
		    //then add credentials to valid user map
		    validUsers.put(makeuser.getUsername(), makeuser.getPassword()); //add username and password to map
		    
			allUsernames.add(makeuser.getUsername()); // add to all Username map
			allUsers.put(makeuser.getUsername(), makeuser);
			allUserIDs.add(makeuser.getID());
			
			// adminStatus.put(token.get(0), token.get(2)); //add admin status to username
			
			userIDToUsername.put(makeuser.getID(),  makeuser.getUsername());
			usernameToUserID.put(makeuser.getUsername(),  makeuser.getID());
			
		    
		    create.setContents("Success");
			Send = new Message(create);// create an accept message
		    out.writeObject(Send); //send message
		    modified = true;
		    
		    return makeuser.getID();
		    
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return -1;
	}
	
	public void changeUserPassword(ObjectOutputStream out, Message message)
	{
		try {
			
			//message variable
			Message Send;
			MessageCreator create;
			create = new MessageCreator(MessageType.CPWD);
			create.setContents("Error"); 
			Send = new Message(create);// have message ready to return a deny
			
			
			String input = message.getContents();
			
			//check for bad input
			if (input == null)
			{
				out.writeObject(Send); //send the deny message
				return;
			}
			
		    //split the given string by using space as delimiter
		    String[] split = input.split("\\s+");
		    //check for bad input (username password)
		    if (split.length != 2)
		    {
				out.writeObject(Send); //send the deny message
				return;
		    }
		    
		    
		    //check if already exists inorder to change
		    if (validUsers.containsKey(split[0]))
		    {
		    	validUsers.put(split[0], split[1]);
		    	User toChange = allUsers.get(split[0]);
		    	toChange.setPassword(split[1]);
		    	
		    	create.setContents("Success");
				Send = new Message(create);// create an accept message
		    	out.writeObject(Send);//send success message
		    	modified = true;
		    	return;
		    }
		    
		    out.writeObject(Send); //send deny if not an existing account
		    
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void sendMessage(ObjectOutputStream out, Message message, ConcurrentHashMap<Integer, ObjectOutputStream> listOfClients)
	{
		try {
			
			//message variable
			Message Send;
			MessageCreator create;
			create = new MessageCreator(MessageType.UTU);
			create.setContents("Error"); 
			Send = new Message(create);// have message ready to return a deny
			
			String input = message.getContents();
			
			if (input == null)
			{
				//don't send a message
				out.writeObject(Send); //send the deny message
				return;
			}
			
			
			Integer id = getUserID(message.getToUserName()); // get user id of receiver
			
			if(id != null) // if its valid
			{
				if(activeUsers.contains(message.getToUserName())) // only send out message if user is online
				{
					//get output stream for receiver
					int receiver = message.getToUserID();
					ObjectOutputStream outReceiver = listOfClients.get(receiver);
					
					//just make sure the output stream exists
					if(outReceiver != null)
					{
						outReceiver.writeObject(message);
					}
					
				}
				
				//then add the message to the receiving User's inbox file
				User receivingUser = allUsers.get(message.getToUserName());
				receivingUser.addToInbox(message);
				
				
				//additionally, add the message to the sending user's inbox file
				User sendingUser = allUsers.get(message.getFromUserName());
				sendingUser.addToInbox(message);
				
				create.setContents("Success");
				Send = new Message(create);// create an accept message 
		    	out.writeObject(Send);//send success message to sender
		    	return;
				
			}
			
			out.writeObject(Send); //send the deny message
			return;
			
			
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public User deleteUser(ObjectOutputStream out, Message message)
	{
		try {
			//message variable
			Message Send;
			MessageCreator create;
			create = new MessageCreator(MessageType.DELUSER);
			create.setContents("Error"); 
			Send = new Message(create);// have message ready to return a deny
			
			
			String password = message.getContents();
			
			//check for bad input
			if (password == null)
			{
				out.writeObject(Send); //send the deny message
				return null;
			}
			
		    //split the given string by using space as delimiter
		    String removeName = message.getToUserName();
		    //check for bad input (username)
		    if (removeName == null)
		    {
				out.writeObject(Send); //send the deny message
				return null;
		    }
		    
		    //check if already exists inorder to remove
		    if (validUsers.containsKey(removeName))
		    {
		    	User delUser = allUsers.get(removeName);
		    	//get extra details of account being deleted
		    	int removeID = getUserID(removeName);
		    	
		    	//remove from all local data
		    	userIDToUsername.remove(removeID);
		    	usernameToUserID.remove(removeName);
		    	activeUsers.remove(removeName);
		    	allUsernames.remove(removeName);	    	
		    	allUsers.remove(removeName);
		    	validUsers.remove(removeName);
		    	
		    	/*
		    	//return a success message
		    	create.setContents("Success");
				Send = new Message(create);// create an accept message
		    	out.writeObject(Send);//send success message
		    	*/
		    	modified = true;
		    	
		    	//return the removed user id
		    	return delUser;
		    }
		    
		    out.writeObject(Send);
		    return null;//send deny if not an existing account
		    
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return null;
		
	}
	
	public void saveUsers()
	{
		
		if (modified == false) // check if saving is needed
		{
			return;
		}
		try
		{
			FileWriter myFile = new FileWriter(userFile); //open file to save on
			for (int i=0; i<allUsernames.size(); i++)
			{
				String username = allUsernames.get(i); //get username
				String pw = validUsers.get(username); // get password
				String admin = adminStatus.get(username); //get admin status
				Integer id = getUserID(username); // get user id
				
				//writ information to file
				myFile.write(username + " " + pw + " " + admin + " " + Integer.toString(id));
				myFile.write("\r\n");
			}
			modified = false;
			myFile.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
	}
	
	public Boolean logout(ObjectOutputStream out, Message message)
	{
		
		try
		{
			//message variable
			Message Send;
			MessageCreator create;
			create = new MessageCreator(MessageType.LOGOUT); //make logout message
			create.setContents("Error"); 
			Send = new Message(create);// have message ready to return a deny
			
			//get username
			String username = message.getContents();
			
			if(username == null)
			{
				out.writeObject(Send);
				return false;
			}
			
			
			//check if actually logged on
			if(activeUsers.contains(username))
			{
				activeUsers.remove(username); // remove from list of active users
				create.setContents("Success");
				Send = new Message(create);// create a success message
				out.writeObject(Send); //send the message
				return true;
			}
			out.writeObject(Send);
			return false;
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return false;
		
	}
	
	public void logout(Message message) {
		String username = message.getFromUserName();
		activeUsers.remove(username);
	}
	
	public void addChatroomToUser(int userID, int chatroomID)
	{
		String grab = getUsername(userID);
		User find = allUsers.get(grab); //find the correct user
		
		
		find.addChatroom(chatroomID); //add the chatroom ID to the user
		
	}
	
	
}