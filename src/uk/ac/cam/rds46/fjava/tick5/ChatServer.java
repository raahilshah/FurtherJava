package uk.ac.cam.rds46.fjava.tick5;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;

import uk.ac.cam.cl.fjava.messages.Message;

public class ChatServer {
	public static void main(String args[]) {
		
		//Testing
		// args = new String[] {"1234", "db3"};
		
		 try{
			 ServerSocket ss = new ServerSocket(Integer.parseInt(args[0]));
			 MultiQueue<Message> mq = new MultiQueue<Message>();
			 Database db = new Database(args[1]);
			 while(true){
				 Socket soc = ss.accept();
				 ClientHandler ch = new ClientHandler(soc, mq, db);
			 }
		 } catch (IOException e) {
			 System.out.println("Cannot use port number " + args[0]);
		 } catch (NumberFormatException | ArrayIndexOutOfBoundsException e){
			 System.out.println("Usage: java ChatServer <port> <database-path>");
		 } catch (SQLException sqle){
			 System.out.println("Cannot create database at " + args[1]);
		 }
	 }
}