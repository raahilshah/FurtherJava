package uk.ac.cam.rds46.fjava.tick4;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;

import uk.ac.cam.cl.fjava.messages.ChangeNickMessage;
import uk.ac.cam.cl.fjava.messages.ChatMessage;
import uk.ac.cam.cl.fjava.messages.Message;
import uk.ac.cam.cl.fjava.messages.RelayMessage;
import uk.ac.cam.cl.fjava.messages.StatusMessage;

public class ClientHandler {
	private Socket socket;
	private MultiQueue<Message> multiQueue;
	private String nickname;
	private MessageQueue<Message> clientMessages;

	public ClientHandler(Socket s, MultiQueue<Message> q) {
		socket = s;
		multiQueue = q;
		
		clientMessages = new SafeMessageQueue<Message>();
		multiQueue.register(clientMessages);
		
		nickname = "Anonymous" + (new Random()).nextInt(100000); 
		StatusMessage connectionMsg = new StatusMessage(nickname + " connected from " 
			+ socket.getInetAddress().getCanonicalHostName() + ".");
		multiQueue.put(connectionMsg);
		
		Thread incomingHandlerThrd = new Thread() {
			@Override
			public void run() {
				try {
					ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
					while (true) {
						try {
							Object msg = ois.readObject();
							if (msg instanceof ChangeNickMessage) {
								String newNick = ((ChangeNickMessage) msg).name;
								nickname = newNick;
								StatusMessage newNickStatus = new StatusMessage(nickname + " is now known as " + newNick + ".");
								multiQueue.put(newNickStatus);
							} else if (msg instanceof ChatMessage) {
								RelayMessage relay = new RelayMessage(nickname, (ChatMessage) msg);
								multiQueue.put(relay);
							}
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						}
					}
				} catch (IOException e) {
					StatusMessage dcStatus = new StatusMessage(nickname + " has disconnected.");
					multiQueue.put(dcStatus);
					multiQueue.deregister(clientMessages);
					return;
				}
			}

		};
		incomingHandlerThrd.setDaemon(true);
		incomingHandlerThrd.start();
		
		Thread ourgoingHandlerThrd = new Thread() {
			@Override
			public void run() {
				try {
					ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
					while (true) 
						oos.writeObject(clientMessages.take());
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		};
		ourgoingHandlerThrd.setDaemon(true);
		ourgoingHandlerThrd.start();
	}

}