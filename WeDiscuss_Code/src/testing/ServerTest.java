package testing;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

import shared.*;

/* Testing server, you can connect to it. CAN HANDLE:
 * - LOGIN messages
 * 
 */

public class ServerTest {
	private static ConcurrentHashMap<Socket, String> clientSockets = new ConcurrentHashMap<Socket, String>();
	private static String userName = "123";
	private static String password = "456";
	
	
	public static void main(String[] args) throws IOException, ClassNotFoundException{
		int serverPort = 8899;
		ServerSocket serverSocket = new ServerSocket(serverPort);
		String serverIP = InetAddress.getLocalHost().getHostAddress().trim();
		System.out.println("IPV4 Adress : " + serverIP + " | Port : " + serverPort);
		
		// In real implementation, probably need to use a thread pool
		while(true) {
			// Listen for connections
			Socket listenSocket = serverSocket.accept();
			clientHandle ch = new clientHandle(listenSocket, clientSockets);
			new Thread(ch).start();
		}
		
		
	}
	
	private static class clientHandle implements Runnable{
		private Socket socket;
		private ConcurrentHashMap<Socket, String> clientSockets = null;
		private ObjectOutputStream out;
		private ObjectInputStream in;
		
		clientHandle(Socket socket, ConcurrentHashMap<Socket, String> clientSockets) throws IOException{
			this.socket = socket;
			this.clientSockets = clientSockets;
			this.out = new ObjectOutputStream(socket.getOutputStream()); // For Write
			this.in = new ObjectInputStream(socket.getInputStream()); // For Read
			System.out.println("New Thread! From: " + socket.getLocalSocketAddress());
		}

		@Override
		public void run() {
			// Read Message
			try {
				while(true) {
					Message message = (Message) in.readObject();
					
					MessageType type = message.getMessageType();
					System.out.println("Received Message: ");
					printMessage(message, socket);
					
					switch(type) {
						case LOGIN:
							handleLogIn(message);
							break;
						case UTU:
							handleText(message);
							break;
						case LOGOUT:
							handleLogOut(message);
							return;
						default:
							MessageCreator messageCreator = new MessageCreator(MessageType.LOGIN);
							sendMessage(messageCreator.createMessage());
					}
				}
			}
			catch(IOException | ClassNotFoundException e){
				System.err.println("Error in client handle run method: " + e.getMessage());
			}
			finally {
				try {
					in.close();
					out.close();
					socket.close();
				}
				catch(IOException e) {
					System.err.println("Error closing resources: " + e.getMessage());
				}
			}
			
		}
		
		private void handleLogIn(Message message) {
			
			String contents = message.getContents();
			
			String[] creds = contents.split("\\|"); 
			
			try {
				MessageCreator messageCreator = new MessageCreator(MessageType.LOGIN);
				if(creds[0].equals(userName) && creds[1].equals(password)) {
					messageCreator.setContents("SUCCESS");
					sendMessage(messageCreator.createMessage());
				}
				else {
					messageCreator.setContents("FAIL");
					sendMessage(messageCreator.createMessage());
					socket.close();
					System.out.println("Login Failed, Disconnecting");
				}
			} catch (IOException e) {
				System.err.println("Error during login processing!");
				try {
					socket.close();
				}
				catch(IOException e1) {
					System.err.println("Error closing socket after fail!");
				}
			}
			
		}
		
		private void handleText(Message message) {
		
			
		}
		
		private void handleLogOut(Message message) {
			
		}
		
		private void sendMessage(Message message) throws IOException {
			// Send a message to client
			System.out.println("Sending...");
			printMessage(message, null);
			out.writeObject(message);
			out.flush();
		}
		
		private void printMessage(Message msg, Socket socket) {
			//System.out.println("From : " + socket.getLocalSocketAddress());
			System.out.println("Type :" + msg.getMessageType());
			System.out.println("Contents : " + msg.getContents());
			System.out.println("--------------------------------\n");
		}
		
	}
}
