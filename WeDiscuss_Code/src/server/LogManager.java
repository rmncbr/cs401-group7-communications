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

	public void handleClient(Socket socket, Message message) {
		try (socket) {
			OutputStream outputStream = socket.getOutputStream();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
			objectOutputStream.writeObject(answerLogRequest(message)); // The response here could be in JSON format
			objectOutputStream.flush();
		} catch (Exception e) {
			System.out.println("Error:" + socket);
		}
	}// handleClient

	public List<Message> getUserMessages(int userID) {
		return userMessageLogs.getOrDefault(userID, new ArrayList<>());
	}// getUserMessages

	public List<Message> getChatroomMessages(int chatroomID) {
		return chatroomMessageLogs.getOrDefault(chatroomID, new ArrayList<>());
	}// getChatroomMessages

	public List<Message> answerLogRequest(Message message) {
		return (message.getMessageType() == MessageType.GUL ? getUserMessages(message.getFromUserID())
				: getChatroomMessages(message.getToChatroom()));
	}// answerLogRequest

	public void storeMessage(Message message) {
		// Check if the message is addressed to a specific user
		if (message.getToUserID() >= 0) {
			// Retrieve the list of messages for this user, or initialize an empty list if
			// none exists
			List<Message> userMessages = userMessageLogs.getOrDefault(message.getToUserID(), new ArrayList<>());

			// Add the message to the list
			userMessages.add(message);

			// Put the updated list back into the map
			userMessageLogs.put(message.getToUserID(), userMessages);
		} // if

		// Check if the message is addressed to a specific chatroom
		if (message.getToChatroom() >= 0) {
			// Retrieve the list of messages for this chatroom, or initialize an empty list
			// if none exists
			List<Message> chatroomMessages = chatroomMessageLogs.getOrDefault(message.getToChatroom(),
					new ArrayList<>());

			// Add the message to the list
			chatroomMessages.add(message);

			// Put the updated list back into the map
			chatroomMessageLogs.put(message.getToChatroom(), chatroomMessages);
		} // if

	}// storeMessage

	public List<Message> getLogs(String filePath) {
	    // List to hold the loaded messages
	    List<Message> loadedMessages = new ArrayList<>();
	    
	    // Open the log file
	    File loadLog = new File(filePath);
	    
	    try (Scanner scanner = new Scanner(loadLog)) {
	        
	        // Loop through the file and process each line
	        while (scanner.hasNextLine()) {
	            String data = scanner.nextLine();
	            String[] values = data.split(",");
	            
	            // Ensure the data is in the correct format
	            if (values.length < 4) continue;  // Basic validation

	            // Extract the required information from the file
	            int id = Integer.parseInt(values[0].trim());  // Message ID
	            String content = values[1].trim();            // Message content
	            String timestamp = values[2].trim();          // Timestamp 
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



	// Message class toString()
	/*
	 * @Override public String toString() { return this.getMessageID() + "," +
	 * this.getContents() + "," + this.getDateSent() + "," + this.getMessageType();
	 * }
	 */
	
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
