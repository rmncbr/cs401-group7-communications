package shared;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class Chatroom implements Serializable {
	private static int IDCounter =0;
	private int id;
	private List<Integer> members = Collections.synchronizedList(new ArrayList<Integer>());
	private List<Message> messages = Collections.synchronizedList(new ArrayList<Message>());
	
	//constructor when making a new chatroom
	public Chatroom()
	{
		IDCounter += 6;
		id = IDCounter;
		
		
		String messageFile = Integer.toString(id) + "Messages.txt";
		try {
            File file = new File(messageFile);

            if (file.createNewFile()) {
                
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		String membersFile = Integer.toString(id) + "Members.txt";
		
		try {
            File file = new File(membersFile);

            if (file.createNewFile()) {
                
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	//constructor when loading existing chatroom
	public Chatroom(int chatroomID) {
		this.id = chatroomID;
		
		if(chatroomID >= IDCounter)
		{
			IDCounter = chatroomID;
		}
		
		
		try //this will populate messages and members of a chatroom
		{
			String messageFile = Integer.toString(id) + "Messages.txt";
			
			File myFile = new File(messageFile);
			Scanner reader = new Scanner(myFile);

			//first populate the messages of the chatroom
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
				if (token.size() != 6)
				{
					line.close(); //do nothing and skip this iteration
            		continue;
				}
				
				//add all message to the arraylist
				Message add;
				MessageCreator create;
				create = new MessageCreator(MessageType.UTC);
				
				create.setContents(token.get(0)); //add the message
				create.setDate(Long.parseLong(token.get(1))); // add the date
				create.setToChatroom(Integer.parseInt(token.get(2))); //add chatroom id
				create.setFromUserName(token.get(3)); //add from username
				create.setFromUserID(Integer.parseInt(token.get(4))); //add from user id
				
				add = new Message(create);
				
				messages.add(add);
				
				line.close();
			}
			reader.close();
			
			
			String memberFile = Integer.toString(id) + "Members.txt";
			
			File File = new File(memberFile);
			Scanner readers = new Scanner(File);

			//first populate the messages of the chatroom
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
				
				//add all members to the arraylist
				members.add(Integer.valueOf(token.get(0)));
				
				line.close();
			}
			readers.close();
			
			
		}
		catch (IOException e) {
        	e.printStackTrace();
        }
	}
	
	public Chatroom(int chatroomID, int creatorID) {
		this.id = chatroomID;
		this.members.add(creatorID);
	}
	
	public int getChatroomID() {
		return this.id;
	}
	
	public List<Message> getMessages() {
		return this.messages;
	}
	
	public void addMessage(Message message) {
		this.messages.add(message);
		
		//append the message to the messagesFile
		String messageFile = Integer.toString(id) + "Messages.txt";
		try
		{
			FileWriter myFile = new FileWriter(messageFile, true); //open file in append mode
			String line = message.storeChatroomMessage();
				
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
	
	public void displayMessages() {
		for (Message message : this.messages) {
			System.out.println(message.toString());
		}
	}
	
	public Boolean findMember(int userID) {
		if (this.members.contains(userID)) {
            return true;        } 
		else {
            return false;
        }
    }
	
	public void addMember(int userID) {
		if (!this.members.contains(userID)) {
			this.members.add(userID);
			saveMembers();
		}
		else {
			System.out.println("User already exists in chatroom.");
		}
	}
	
	public void removeMember(int userID) {
		if (this.members.contains(userID)) {
			this.members.remove(userID);
			saveMembers();
		} else {
			System.out.println("User does not exist in chatroom.");
		}
		
	}
	
	public void saveMembers()
	{
		String memberFile = Integer.toString(id) + "Members.txt";
		try
		{
			FileWriter myFile = new FileWriter(memberFile); //open file to save on
			for (int i=0; i<members.size(); i++)
			{
				int userID = members.get(i);
				
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
	
	public void saveMessages()
	{
		String messageFile = Integer.toString(id) + "Messages.txt";
		try
		{
			FileWriter myFile = new FileWriter(messageFile); //open file to save on
			for (int i=0; i<messages.size(); i++)
			{
				String line = messages.get(i).storeChatroomMessage();
				
				//write information to file
				myFile.write(line);
				myFile.write("\n");
			}
			myFile.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}



