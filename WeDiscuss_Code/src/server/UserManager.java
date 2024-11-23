package server;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

import shared.*;

public class UserManager {
	// all maps 
	private ConcurrentHashMap<String, String> validUsers = new ConcurrentHashMap<String, String>(); // map of all valid username/pw combos

	private ConcurrentHashMap<String, String> adminStatus = new ConcurrentHashMap<String, String>(); // map of admin status
	private ConcurrentHashMap<Integer, String> userIDToUsername = new ConcurrentHashMap<Integer, String>(); // id to username
	private ConcurrentHashMap<String, Integer> usernameToUserID = new ConcurrentHashMap<String, Integer>(); // username to id
	private ArrayList<String> activeUsers = new ArrayList<String>(); //
	private ArrayList<String> allUsernames = new ArrayList<String>(); // list of all usernames
	private ConcurrentHashMap<String, User> allUsers = new ConcurrentHashMap<String, User>(); // map of all existing users
	
	//filename
	private String userFile = "UserFile.txt";
	
	boolean modified = false;
	
	
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
				//User makeuser = new User(token.get(0), token.get(1), token.get(2), id);
				//allUsers.put(token.get(0), makeuser);
				
				
				line.close();
			}
			reader.close();
		}
		catch (IOException e) {
        	e.printStackTrace();
        }
	}
	
	public void handleClient(Socket socket, Message message, Server server)
	{
		//idk yet
	}
	
	public String getUsername(int id)
	{
		String find;
		find = userIDToUsername.get(id);
		//if id is not found, 'find' will be labeled as null
		return find;
		
	}
	
	public Integer getUserID(String username)
	{
		Integer find;
		find = usernameToUserID.get(username);
		//if username is not found, 'find' will be labeled as null
		return find;
		
	}
	
	public void authUser(Socket socket, Message message)
	{
		
		try {
			//establish a one way connection
			OutputStream outputStream = socket.getOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(outputStream);
			
			//message variable
			Message Send;
			MessageCreator create;
			create = new MessageCreator(MessageType.LOGIN);
			create.setContents("Error"); 
			Send = new Message(create);// have message ready to return a deny
			
			boolean ConfirmName = false;
			boolean ConfirmPW = false;
			
			String input = message.getContents();
			
			//check for bad input
			if (input == null)
			{
				out.writeObject(Send); //send the deny message
				return;
			}
			
		    //split the given string by using space as delimiter
		    String[] split = input.split("\\s+");
		    //check for bad input
		    if (split.length != 2)
		    {
				out.writeObject(Send); //send the deny message
				return;
		    }
		    
		    // check if username exits and is not signed in already
		    if (validUsers.containsKey(split[0]) && activeUsers.contains(split[0]))//short circuits 
		    {
		    	if(validUsers.get(split[0]) != null)//check if Password exits
		    	{
					create.setContents("Success");
					//create.setUser(allUsers.get(split[0])); // send back User object
					Send = new Message(create);// create an accept message
					out.writeObject(Send); //send the message
					activeUsers.add(split[0]); // add to list of active users
					return;
		    	} 
				out.writeObject(Send); //send deny if credentials dont work
				return;
		    	
		    }
		    out.writeObject(Send); //send deny if credentials dont work
		    
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	
	
	public void addUser(Socket socket, Message message)
	{
		try {
			//establish a one way connection
			OutputStream outputStream = socket.getOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(outputStream);
			
			//message variable
			Message Send;
			MessageCreator create;
			create = new MessageCreator(MessageType.ADDUSER);
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
		    //check for bad input (username, password, admin, userID)
		    if (split.length != 4)
		    {
				out.writeObject(Send); //send the deny message
				return;
		    }
		    
		    
		    //check if already exists
		    if (validUsers.containsKey(split[0]))
		    {
		    	out.writeObject(Send);
		    	return;
		    }
		    
		    //then add credentials to valid user map
		    validUsers.put(split[0], split[1]); //add username and password to map
			adminStatus.put(split[0], split[2]); //add admin status to username)
		    create.setContents("Success");
			Send = new Message(create);// create an accept message
		    out.writeObject(Send); //send message
		    modified = true;
		    
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void changeUserPassword(Socket socket, Message message)
	{
		try {
			//establish a one way connection
			OutputStream outputStream = socket.getOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(outputStream);
			
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
		    //check for bad input (username,password)
		    if (split.length != 2)
		    {
				out.writeObject(Send); //send the deny message
				return;
		    }
		    
		    
		    //check if already exists inorder to change
		    if (validUsers.containsKey(split[0]))
		    {
		    	validUsers.put(split[0], split[1]);
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
	
	public void sendMessage(Socket fromSocket, Message message, Socket toSocket)
	{
		try {
			//establish a one way connection
			OutputStream outputStream = fromSocket.getOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(outputStream);
			
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
					OutputStream outputStream2 = toSocket.getOutputStream();
					ObjectOutputStream outReceiver = new ObjectOutputStream(outputStream2);
					MessageCreator toUser = new MessageCreator(MessageType.UTU);
					toUser.setContents(input);
					toUser.setFromUserID(message.getFromUserID());
					toUser.setFromUserName(message.getFromUserName());
					toUser.setToUserID(message.getToUserID());
					toUser.setToUserName(message.getToUserName());
					Send = new Message(toUser);
					outReceiver.writeObject(Send);
				}
				
				String messageFile = message.getToUserName() + message.getToUserID()+ ".txt";
				File sendFile = new File(messageFile);
				FileWriter type = new FileWriter(sendFile, true);
				type.write(message.getContents() +  "\n" + message.getFromUserName());
				type.write("\r\n");
				type.close();
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
	
	public void deleteUser(Socket clientSocket, Message message, Socket removeSocket)
	{
		try {
			//establish a one way connection
			OutputStream outputStream = clientSocket.getOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(outputStream);
			
			//message variable
			Message Send;
			MessageCreator create;
			create = new MessageCreator(MessageType.DELUSER);
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
		    //check for bad input (username)
		    if (split.length != 1)
		    {
				out.writeObject(Send); //send the deny message
				return;
		    }
		    
		    
		    //check if already exists inorder to remove
		    if (validUsers.containsKey(split[0]))
		    {
		    	validUsers.remove(split[0]);
		    	create.setContents("Success");
				Send = new Message(create);// create an accept message
		    	out.writeObject(Send);//send success message
		    	modified = true;
		    	
		    	//not sure if i should close the socket of the person being removed
		    	
		    	return;
		    }
		    
		    out.writeObject(Send); //send deny if not an existing account
		    
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
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
	
	public void logout(String username, Socket socket)
	{
		
		try
		{
			//make one way connection
			OutputStream outputStream = socket.getOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(outputStream);
		
			//message variable
			Message Send;
			MessageCreator create;
			create = new MessageCreator(MessageType.LOGOUT); //make logout message
			
			activeUsers.remove(username); // remove from list of active users
			create.setContents("Success");
			Send = new Message(create);// create a success message
			out.writeObject(Send); //send the message
			
			
			//ATTENTION!!!
			//not sure if I have to close the user's sockets here
			
			
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
	}
	//private void checkValid(input)
	
	
}