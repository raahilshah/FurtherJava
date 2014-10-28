package uk.ac.cam.rds46.fjava.tick1star;

import javax.swing.JFrame;

public class ImageChatClient extends JFrame {
	
	public ImageChatClient(String server, int port) {
		super("");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.println("This application requires two arguments: <machine> <port>");
			return;
		}
		String server = args[0];
		int port = Integer.parseInt(args[1]);
		ImageChatClient client = new ImageChatClient(server, port);
		client.setVisible(true);
	}
}
 