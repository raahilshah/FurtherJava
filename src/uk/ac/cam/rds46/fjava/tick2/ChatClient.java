package uk.ac.cam.rds46.fjava.tick2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import uk.ac.cam.cl.fjava.messages.ChangeNickMessage;
import uk.ac.cam.cl.fjava.messages.ChatMessage;
import uk.ac.cam.cl.fjava.messages.DynamicObjectInputStream;
import uk.ac.cam.cl.fjava.messages.Execute;
import uk.ac.cam.cl.fjava.messages.Message;
import uk.ac.cam.cl.fjava.messages.NewMessageType;
import uk.ac.cam.cl.fjava.messages.RelayMessage;
import uk.ac.cam.cl.fjava.messages.StatusMessage;



@FurtherJavaPreamble(
		author = "Raahil Shah",
		date = "12 November 2014",
		crsid = "rds46",
		summary = "Chat Client",
		ticker = FurtherJavaPreamble.Ticker.A
	)
public class ChatClient {
	
	public static void main(String[] args) throws IOException {

		String server = null;
		int port = 0;

		// Testing.
//		args = new String[] {"java-1b.cl.cam.ac.uk", "15004" };

		try {
			if (args.length < 2) throw new IllegalArgumentException();
			server = args[0];
			port = Integer.parseInt(args[1]);
			
		} catch (IllegalArgumentException iae) {
			System.err.println("This application requires two arguments: <machine> <port>");
			return;
		}
		
		try {
			final Socket s = new Socket(server, port);
			System.out.println(formatTime(new Date()) + " [Client] Connected to " + server + " on port " + port + ".");
			
			
			Thread output = new Thread() {
				@Override
				public void run() {
					try {
						DynamicObjectInputStream dois = new DynamicObjectInputStream(s.getInputStream());
						
						while(true) {
							Message m = (Message) dois.readObject();
							String text = formatTime(m.getCreationTime()) + " ";
							
							if (m instanceof RelayMessage) {
								RelayMessage rm =  (RelayMessage) m;
								text += "[" + rm.getFrom() + "] " + rm.getMessage();
								System.out.println(text);
							} else if (m instanceof StatusMessage) {
								StatusMessage sm = (StatusMessage) m;
								text += "[Server] " + sm.getMessage();
								System.out.println(text);
							} else if (m instanceof NewMessageType) {
								NewMessageType nmt = (NewMessageType) m;
								dois.addClass(nmt.getName(), nmt.getClassData());
								text += "[Client] New class " + nmt.getName() + " loaded.";
								System.out.println(text);
							} else  {
								Field[] fields = m.getClass().getDeclaredFields();
								text += "[Client] " + m.getClass().getSimpleName() + ": ";
								
								for (int i = 0; i < fields.length; i++) {
									fields[i].setAccessible(true);
									text += fields[i].getName() + "(" + fields[i].get(m) + ")";
									if (i != fields.length - 1) text += ", ";
								}

								System.out.println(text);
								
								Method[] methods = m.getClass().getMethods();
								for (Method meth : methods) {
									if (meth.getGenericParameterTypes().length == 0 && 
											meth.isAnnotationPresent(Execute.class)) {
										meth.invoke(m, new Object[0]);
									}
								}
							}
						}
					} catch (IOException ioe) {
						System.err.println("IOException while reading.");
						ioe.printStackTrace();
					} catch (ClassNotFoundException e) {
						System.err.println("Object deserialisation error.");
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						System.err.println("Error with reflection class.");
						e.printStackTrace();
					} catch (IllegalArgumentException | InvocationTargetException e) {
						System.err.println("Error invokng methods of loaded class.");
						e.printStackTrace();
					}
				}
			};
			output.setDaemon(true);
			output.start();
	
			BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
			OutputStream os = s.getOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(os);
			
			while(true) {
				String msg = r.readLine().trim();
				if (msg.startsWith("\\nick")) {
					oos.writeObject(new ChangeNickMessage(msg.split(" ", 2)[1]));
				} else if (msg.startsWith("\\quit")) {
					System.out.println(formatTime(new Date()) + " [Client] Connection terminated.");
					System.exit(0);
				} else if (msg.startsWith("\\")) {
					System.out.println(formatTime(new Date()) + " [Client] Unknown command \"" + msg.substring(1) + "\"");
				} else {
					oos.writeObject(new ChatMessage(msg));
				}
			}
		} catch (IOException ioe) {
			System.err.println("Cannot connect to " + server + " on port " + port);
			ioe.printStackTrace();
		}
	}	
	
	private static String formatTime(Date d) {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		return sdf.format(d);
	}
}