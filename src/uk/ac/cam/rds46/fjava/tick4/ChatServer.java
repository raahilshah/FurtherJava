package uk.ac.cam.rds46.fjava.tick4;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import uk.ac.cam.cl.fjava.messages.Message;

public class ChatServer {
	public static void main(String args[]) {
	
		// Testing
		// args = new String[] {"1234"};
		
		 try{
			 ServerSocket ss = new ServerSocket(Integer.parseInt(args[0]));
			 MultiQueue<Message> mq = new MultiQueue<Message>();
			 while(true){
				 Socket soc = ss.accept();
				 ClientHandler ch = new ClientHandler(soc, mq);
			 }
		 }
		 catch (IOException e) {
			 System.out.println("Cannot use port number " + args[0]);
		 }
		 catch (NumberFormatException | ArrayIndexOutOfBoundsException e){
			 System.out.println("Usage: java ChatServer <port>");
		 }
	 }
}