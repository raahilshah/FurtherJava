package uk.ac.cam.rds46.fjava.tick1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class StringChat {
	public static void main(String[] args) throws IOException {

		String server = null;
		int port = 0;
		
		try {
			if (args.length < 2) throw new IllegalArgumentException();
			server = args[0];
			port = Integer.parseInt(args[1]);
			
		} catch (IllegalArgumentException iae) {
			System.err.println("This application requires two arguments: <machine> <port>");
			return;
		}

		// "s" is declared final here because the Thread instance (output)
		// can remain in memory after the main method returns. 
		// The scope of "s" is the main method and so if main returns
		// "s" goes out of scope, but the copy in "output" still can be accessed
		// which would make the inner and outer copy of "s" out of sync. 
		
		try {
			final Socket s = new Socket(server, port);
			Thread output = new Thread() {
				@Override
				public void run() {
					try {
						InputStream is = s.getInputStream();
						byte[] buffer = new byte[1024];
						
						while (true) {
							int bytesRead = is.read(buffer);
							String read = new String(buffer, 0, bytesRead);
							System.out.println(read);
						}
					} catch (IOException ioe) {
						System.out.println("IOException while reading.");
						ioe.printStackTrace();
					}
				}
			};
			output.setDaemon(true);
			output.start();
	
			BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
			OutputStream os = s.getOutputStream();
			while(true) {
				String msg = r.readLine();
				byte[] byteMsg = msg.getBytes();
				os.write(byteMsg);
			}
		} catch (IOException ioe) {
			System.out.println("Cannot connect to " + server + " on port " + port);
		}
		
	}
}