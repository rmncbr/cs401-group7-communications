package server;

import shared.Message;
import shared.MessageCreator;
import shared.MessageType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.HashMap;

public class LogManager {
	static ConcurrentHashMap<Integer, List<Message>> userMessageLogs;
	static ConcurrentHashMap<Integer, List<Message>> chatroomMessageLogs;
	private static ConcurrentLinkedQueue<Message> messageQueue = new ConcurrentLinkedQueue<Message>();

	LogManager(List<Integer> allUserIDs, List<Integer> allChatroomIDs) {
		userMessageLogs = new ConcurrentHashMap<Integer, List<Message>>();
		chatroomMessageLogs = new ConcurrentHashMap<Integer, List<Message>>();
		loadUserMessages(allUserIDs);
		loadChatroomMessages(allChatroomIDs);
		
		Thread logReader = new Thread(() -> startLogReader());
		logReader.start();
	}
	
	public void loadUserMessages(List<Integer> allUserIDs) {
		for(Integer ID : allUserIDs) {
			try 
	        {
	            String messageFile = Integer.toString(ID) + "Inbox.txt";

	            File myFile = new File(messageFile);
	            Scanner reader = new Scanner(myFile);

	            //first populate the messages of the inbox
	            while (reader.hasNextLine())
	            {
	                //getline and set delimiters
	                Scanner line = new Scanner(reader.nextLine()).useDelimiter("|"); // \s+ means whitespace

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

	                userMessageLogs.get(ID).add(add);

	                line.close();
	            }
	            reader.close();

	        }
	        catch (IOException e) {
	            e.printStackTrace();
		        System.out.println("Error loading file: " + e.getMessage());
		        System.out.println("File " + ID + " does not exist.");
	        }
		}
	}
	
	public void loadChatroomMessages(List<Integer> allChatroomIDs) {
		for(Integer ID : allChatroomIDs) {
			try 
	        {
	            String messageFile = Integer.toString(ID) + "Messages.txt";

	            File myFile = new File(messageFile);
	            Scanner reader = new Scanner(myFile);

	            //first populate the messages of the inbox
	            while (reader.hasNextLine())
	            {
	                //getline and set delimiters
	                Scanner line = new Scanner(reader.nextLine()).useDelimiter("|"); // \s+ means whitespace

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

	                chatroomMessageLogs.get(ID).add(add);

	                line.close();
	            }
	            reader.close();

	        }
	        catch (IOException e) {
	            e.printStackTrace();
		        System.out.println("Error loading file: " + e.getMessage());
		        System.out.println("File " + ID + " does not exist.");
	        }
		}
	}
	
	public void getUserMessages(ObjectOutputStream output, Message message) {
        int userID = message.getFromUserID(); 
        List<Message> messages = userMessageLogs.getOrDefault(userID, new ArrayList<>());
        
        try {
            output.writeObject(messages);  
        } catch (IOException e) {
            e.printStackTrace();
        }
	}// getUserMessages

	public void getChatroomMessages(ObjectOutputStream output, Message message) {
        int chatroomID = message.getToChatroomID(); 
        List<Message> messages = chatroomMessageLogs.getOrDefault(chatroomID, new ArrayList<>());
        
        try {
            output.writeObject(messages); 
        } catch (IOException e) {
            e.printStackTrace();
        }
	}// getChatroomMessages
	
	
	/******/

	public void answerLogRequest(Message message) {
        MessageType type = message.getMessageType();
        if (type == MessageType.GUL) {  // Get User Logs
            getUserMessages(null, message);
        } else if (type == MessageType.GCL) {  // Get Chatroom Logs
            getChatroomMessages(null, message);
        }
	}// answerLogRequest

	public void storeMessage(Message message) {
		// Check if the message is addressed to a specific user
		if (message.getToUserID() >= 0) {
			List<Message> userMessages = userMessageLogs.getOrDefault(message.getToUserID(), new ArrayList<>());
			userMessages.add(message);

			userMessageLogs.put(message.getToUserID(), userMessages);
			saveLogs(message);
		} // if

		
		if (message.getToChatroomID() >= 0) {
			List<Message> chatroomMessages = chatroomMessageLogs.getOrDefault(message.getToChatroomID(), new ArrayList<>());
			chatroomMessages.add(message);

			chatroomMessageLogs.put(message.getToChatroomID(), chatroomMessages);
			saveLogs(message);
		} // if

	}// storeMessage

	public List<Message> getLogs(String filePath) {
	    List<Message> loadedMessages = new ArrayList<>();
	    File loadLog = new File(filePath);
	    
	    try (Scanner scanner = new Scanner(loadLog)) {
	        while (scanner.hasNextLine()) {
	            String data = scanner.nextLine();
	            String[] values = data.split("|");
	            
	            if (values.length < 4) continue; 

	            // Extract the required information from the file
	            int id = Integer.parseInt(values[0].trim());  
	            String content = values[1].trim();            
	            String timestamp = values[2].trim();          
	            MessageType type = MessageType.valueOf(values[3].trim().toUpperCase());  // Message type
	            
	            // Create a new MessageCreator object
	            MessageCreator messageCreator = new MessageCreator(type);
	            
	            // Depending on the message type, configure the message creator
	            if (type == MessageType.GUL) { // User Log request
	                int fromUserID = Integer.parseInt(values[0].trim());  // Assuming the user ID is stored here
	                messageCreator.setFromUserID(fromUserID);
	            } else if (type == MessageType.GCL) { // Chatroom Log request
	                int toChatroom = Integer.parseInt(values[4].trim());  // Assuming the chatroom ID is in position 4
	                messageCreator.setToChatroom(toChatroom);
	            }

	            // Set the fields in MessageCreator 
	            messageCreator.setContents(content);
	            
	            // Create the Message object using the MessageCreator
	            Message message = messageCreator.createMessage();
	            loadedMessages.add(message);
	        }
	        
	        System.out.println(filePath + " loaded successfully.");
	        
	    } catch (Exception e) {
	        System.out.println("Error loading file: " + e.getMessage());
	        System.out.println("File " + filePath + " does not exist.");
	    }
	    
	    return loadedMessages;
	}//getLogs

	
	public static void saveLogs(Message message) {
		if(message.getMessageType().equals(MessageType.UTU)) {
			String logFilePath = message.getFromUserName() + String.valueOf(message.getFromUserID()) + "log.txt";
			try(FileWriter writer = new FileWriter(logFilePath, true)) { // True = will not overwrite
				writer.write(String.valueOf(message.getFromUserID()) + "|" + message.getContents() + "|" + message.getDateSent().toString()  + "|" + message.getMessageType().toString());
			}
			catch(IOException e) {
				System.err.println("Error saving log of: " + logFilePath);
			}
		}
		else if(message.getMessageType().equals(MessageType.UTC)) {
			String logFilePath = String.valueOf(message.getToChatroomID()) + "log.txt";
			try(FileWriter writer = new FileWriter(logFilePath, true)) {
				writer.write(String.valueOf(message.getFromUserID()) + "|" + message.getContents() + "|" + message.getDateSent().toString() + "|" + message.getMessageType().toString() + "|" + String.valueOf(message.getToChatroomID()));
			}
			catch(IOException e) {
				System.err.println("Error saving log of: " + logFilePath);
			}
		}
	}
	
	public void addToLogQueue(Message message) {
		messageQueue.add(message);
	}
	
	public Boolean isLogQueueEmpty() {
		if(messageQueue.isEmpty()) return true;
		
		return false;
	}
	
	private void startLogReader() {
		
		while(true) {
			Message message = messageQueue.poll();
			if(message == null) continue;
			
			MessageType type = message.getMessageType();
			
			switch(type) {
				case UTU:
					storeMessage(message);
					break;
				case UTC:
					storeMessage(message);
					break;
				default:
					break;
			}
			
		}
	}

}
