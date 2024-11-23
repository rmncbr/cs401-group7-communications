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
import java.util.concurrent.ConcurrentHashMap;

import shared.*;

public class ChatroomManager {
	
	private ConcurrentHashMap<Integer, Chatroom> chatrooms = new ConcurrentHashMap<Integer, Chatroom>();
	private ArrayList<Integer> chatroomIDs = new ArrayList<Integer>();
	
	
	
	private String chatroomFile = "ChatroomFile";
	
	
	public ChatroomManager()
	{
		try //this will populate the valid user accounts
		{
			File myFile = new File(chatroomFile);
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
				
				//if there are more or less than 1 token, then it is invalid
				if (token.size() != 1)
				{
					line.close(); //do nothing and skip this iteration
            		continue;
				}
				
				/*
				// create and add new chatrooms to chatmanager data
				Chatroom make = new Chatroom(token.get(0)); // uses chatroom id for constructor
				chatroomIDs.add(Integer.valueOf(token.get(0)));
				chatrooms.put(Integer.valueOf(token.get(0)), make);
				*/
				
				line.close();
			}
			reader.close();
		}
		catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
	public void handleClient(Socket socket, Message message, Server server)
	{
		//idk yet
	}
	
	public void sendMessageToChatroom(Socket fromSocket, Message message, ConcurrentHashMap<Integer, Socket> clients)
	{
		try
		{
			//establish a one way connection
			OutputStream outputStream = fromSocket.getOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(outputStream);
			
			//message variable
			Message Send;
			MessageCreator create;
			create = new MessageCreator(MessageType.UTC);
			create.setContents("Error"); 
			Send = new Message(create);// have message ready to return a deny
			
			
			String input = message.getContents();
			
			Integer id = message.getToChatroom(); // get chatroom id
			
			if (input == null || id == null) //check if input is good
			{
				//don't send a message
				out.writeObject(Send); //send the deny message
				return;
			}
			
			
			Chatroom receive = chatrooms.get(id);
			if(receive == null)
			{
				out.writeObject(Send); //send the deny message
				return;
			}
			
			
			receive.addMessage(message); //give message to chatroom so they can store it
			
			/*
			//grab members in chatroom
			List<Integer> members = receive.getMembers(); //gets list of members
			
			// send message to all the members in the chatroom that are active
			for (int i = 0; i<members.size(); i++)
			{
				if (clients.contains(members.get(i)))
				{
					Socket toSend = clients.get(members.get(i));
					
					OutputStream outputStream2 = toSend.getOutputStream();
					ObjectOutputStream outReceiver = new ObjectOutputStream(outputStream2);
					outReceiver.writeObject(message);
				}
			}
			*/
			
			receive.addMessage(message); 
			
			create.setContents("Success");
			Send = new Message(create);// create an accept message
		    out.writeObject(Send); //send message
			
			
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		
		
		
		
	}
	
	public Chatroom getChatroom(Integer chatroomID)
	{
		Chatroom find;
		find = chatrooms.get(chatroomID);
		//if id is not found, 'find' will be labeled as null
		return find;
	}
	
	public void addMessageToChatroom(Message message)
	{
		//not sure how this differs from send message to chatroom
	}
	
	//inorder to get userToInvite, have the server get that value with the user manager function
	public void addUsertoChatroom(Socket socket, Message message)
	{
		try
		{
			//establish a one way connection
			OutputStream outputStream = socket.getOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(outputStream);
			
			//message variable
			Message Send;
			MessageCreator create;
			create = new MessageCreator(MessageType.IUC);
			create.setContents("Error"); 
			Send = new Message(create);// have message ready to return a deny
			
			
			
			Integer id = message.getToChatroom(); // get chatroom id
			
			if (id == null) //check if input is good
			{
				//don't send a message
				out.writeObject(Send); //send the deny message
				return;
			}
			
			
			Chatroom receive = chatrooms.get(id); //check if it exists
			if(receive == null)
			{
				out.writeObject(Send); //send the deny message
				return;
			}
			
			
			receive.addMember(message.getToUserID()); //give user ID to chatroom so they can store it
			
			
			create.setContents("Success");
			Send = new Message(create);// create an accept message
		    out.writeObject(Send); //send message
			
			
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void createChatroom(Socket socket, Message message)
	{
		try
		{
			//establish a one way connection
			OutputStream outputStream = socket.getOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(outputStream);
			
			//message variable
			Message Send;
			MessageCreator create;
			create = new MessageCreator(MessageType.CC);
			create.setContents("Error"); 
			Send = new Message(create);// have message ready to return a deny
			
			
			
			Integer id = message.getToChatroom(); // get chatroom id
			
			if (id == null) //check if input is good
			{
				//don't send a message
				out.writeObject(Send); //send the deny message
				return;
			}
			
			
			
			/*
			//make a new chatroom and add it to list 
			Chatroom make = new Chatroom(message.getFromUserID());
			Integer ChatId = make.getChatroomID();
			chatroomIDs.add(ChatId);
			chatrooms.put(ChatId, make);
			*/
			
			
			
			create.setContents("Success");
			//create.setToChatroom(make.getChatroomID); //add the chatroom ID in the return message
			Send = new Message(create);// create an accept message
		    out.writeObject(Send); //send message
			
			
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void deleteChatroom(Socket socket, Message message)
	{
		
	}
	
	public void removeUserfromChatroom(Socket clientSocket, Message message, Socket removeSocket)
	{
		
	}
	
	
	
}
