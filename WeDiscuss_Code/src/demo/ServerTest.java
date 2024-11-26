package demo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

import shared.*;

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
		private InetAddress clientIP;
		private Integer clientPort;
		
		clientHandle(Socket socket, ConcurrentHashMap<Socket, String> clientSockets) throws IOException{
			this.socket = socket;
			this.clientIP = socket.getInetAddress();
			this.clientPort = socket.getPort();
			
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
					if(socket.isClosed()) break;
					Message message = (Message) in.readObject();
					
					MessageType type = message.getMessageType();
					System.out.println("Received Message: ");
					printMessage(message, socket);
					
					switch(type) {
						case LOGIN:
							handleLogin(message);
							break;
						case UTU:
							handleText(message);
							break;
						case LOGOUT:
							if(handleLogout(message)) {
								return;
							}
							continue;
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
				closeResources();
			}
			
		}
		
		private void handleLogin(Message message) {
			
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
					closeResources();
					System.out.println("Login Failed, Disconnecting");
				}
			} catch (IOException e) {
				System.err.println("Error during login processing!");
				closeResources();
			}
			
			
			
		}
		
		private void handleText(Message message) {
			MessageCreator messageCreator = new MessageCreator(MessageType.UTU);
			String text = message.getContents().toLowerCase();
			messageCreator.setContents(text);
			
			try {
				sendMessage(messageCreator.createMessage());
			} catch (IOException e) {
				System.err.println("Error during sending message!");
			}
		}
		
		private Boolean handleLogout(Message message) {
			MessageCreator messageCreator = new MessageCreator(MessageType.LOGOUT);
			messageCreator.setContents("SUCCESS");
			try {
				sendMessage(messageCreator.createMessage());
				System.out.println("Ending connection with: ");
				System.out.println(clientIP);
				socket.close();
				return true;
			} catch (IOException e) {
				System.err.println("Error during login processing!");
				messageCreator.setContents("FAIL");
				try {
					sendMessage(messageCreator.createMessage());
				} catch (IOException e1) {
					System.err.println("Error during logout processing!");
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
			finally {
				closeResources();
			}
			
			return false;	
		}
		
		private void sendMessage(Message message) throws IOException {
			// Send a message to client
			System.out.println("Sending...");
			printMessage(message, null);
			out.writeObject(message);
			out.flush();
		}
		
		private void printMessage(Message msg, Socket socket) {
			System.out.println("From : " + clientIP);
			System.out.println("Type :" + msg.getMessageType());
			System.out.println("Contents : " + msg.getContents());
			System.out.println("--------------------------------\n");
		}
		
		private void closeResources() {
			try {
				if(in != null) in.close();
				if(out != null) out.close();
				if(socket != null) socket.close();
			}
			catch(IOException e) {
				System.err.println("Error closing resources!");
				e.printStackTrace();
			}
		}
		
	}
}
