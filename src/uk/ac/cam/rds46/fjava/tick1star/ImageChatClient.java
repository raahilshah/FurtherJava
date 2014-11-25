package uk.ac.cam.rds46.fjava.tick1star;

import java.awt.Canvas;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

public class ImageChatClient extends JFrame {
	
	private static int height = 480, width = 640;
	
	public ImageChatClient(String server, int port) throws IOException {
		super("");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(width, height);
		JButton upload = new JButton("Upload");
		final Canvas canvas = new Canvas();

		add(upload);
		add(canvas);
		
		final Socket s = new Socket(server, port);
		Thread output = new Thread() {
			@Override
			public void run() {
				try {
					InputStream is = s.getInputStream(); 
					byte[] buffer = new byte[1024];
					ByteArrayOutputStream bo = new ByteArrayOutputStream();
					
					while (true) {
						int bytesRead = is.read(buffer);
						int eoi = -1;
						for (int i = 0; i < buffer.length - 1; i++)
							if (buffer[i] == -1 && buffer[i + 1] == -39) eoi = i;
						if (eoi != -1) bo.write(buffer);
						else {
							bo.write(buffer);
							BufferedImage image = ImageIO.read(new ByteArrayInputStream(bo.toByteArray()));
							Graphics2D g2 = image.createGraphics();
							canvas.printAll(g2);  
							g2.dispose();  

						}
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
		
		upload.addActionListener(new ActionListener() {
		      public void actionPerformed(ActionEvent ae) {
		        JFileChooser fileChooser = new JFileChooser();
		        int returnValue = fileChooser.showOpenDialog(null);
		        if (returnValue == JFileChooser.APPROVE_OPTION) {
		          File selectedFile = fileChooser.getSelectedFile();
		          try {
					FileInputStream fis = new FileInputStream(selectedFile);
					OutputStream os = s.getOutputStream();
					byte[] buffer = new byte[1024];
					while (fis.available() > 0) {
						fis.read(buffer);
						os.write(buffer);
					}
					
		          } catch (FileNotFoundException e) {
		        	  System.out.println("File not found.");
		          } catch (IOException e) {
					System.out.println("IO Exception.");
		          }
		        }
		      }
		    });
		
	}
	
	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.println("This application requires two arguments: <machine> <port>");
			return;
		}
		String server = args[0];
		int port = Integer.parseInt(args[1]);
		try {
			ImageChatClient client = new ImageChatClient(server, port);
			client.setVisible(true);
		} catch (IOException e) {
			System.out.println("IO Exception occured.");
			e.printStackTrace();
		}
	}
}
 