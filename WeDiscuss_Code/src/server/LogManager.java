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
import java.util.HashMap;

public class LogManager {
	static Map<Integer, List<Message>> userMessageLogs;
	static Map<Integer, List<Message>> chatroomMessageLogs;
	static String logFile;

	LogManager() {
		userMessageLogs = new HashMap<>();
		chatroomMessageLogs = new HashMap<>();
		logFile = "";
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
		} // if

		
		if (message.getToChatroomID() >= 0) {
			List<Message> chatroomMessages = chatroomMessageLogs.getOrDefault(message.getToChatroomID(), new ArrayList<>());
			chatroomMessages.add(message);

			chatroomMessageLogs.put(message.getToChatroomID(), chatroomMessages);
		} // if

	}// storeMessage

	public List<Message> getLogs(String filePath) {
	    List<Message> loadedMessages = new ArrayList<>();
	    File loadLog = new File(filePath);
	    
	    try (Scanner scanner = new Scanner(loadLog)) {
	        while (scanner.hasNextLine()) {
	            String data = scanner.nextLine();
	            String[] values = data.split(",");
	            
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

	
	public static String saveLogs() {
		StringBuilder result = new StringBuilder(); // to accumulate errors

		if (userMessageLogs.isEmpty() && chatroomMessageLogs.isEmpty()) {
			result.append("Error: Log list is empty.\n");
			return result.toString();
		}

		try (FileWriter writer = new FileWriter(logFile)) {
			// Write user message logs
			for (Map.Entry<Integer, List<Message>> entry : userMessageLogs.entrySet()) {
				for (Message message : entry.getValue()) {
					writer.write(message.toString() + "\n");
				}
			}

			// Write chatroom message logs
			for (Map.Entry<Integer, List<Message>> entry : chatroomMessageLogs.entrySet()) {
				for (Message message : entry.getValue()) {
					writer.write(message.toString() + "\n");
				}
			}

			result.append("Logs saved successfully.");
		} catch (IOException e) {
			result.append("Error while saving the collection: " + e.getMessage() + "\n");
		}

		return result.toString();
	}

}
