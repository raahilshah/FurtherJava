package uk.ac.cam.rds46.fjava.tick1;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class StringReceive {
	public static void main(String[] args) {
		try {
			if (args.length < 2) throw new IllegalArgumentException();
			String host = args[0];
			int port = Integer.parseInt(args[1]);
			Socket socket = new Socket(host, port);
			InputStream is = socket.getInputStream();
			byte[] buffer = new byte[1024];
			
			while (true) {
				int bytesRead = is.read(buffer);
				String read = new String(buffer, 0, bytesRead);
				System.out.println(read);
			}
			
			
		} catch (IllegalArgumentException iae) {
			System.err.println("This application requires two arguments: <machine> <port>");
		} catch (IOException e) {
			System.err.println("Cannot connect to " + args[0]  + " on port " + args[1]);
		}
	}
}
