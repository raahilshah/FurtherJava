package uk.ac.cam.rds46.fjava.tick2;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

//TODO: import required classes

class TestMessageReadWrite {
	
	static boolean writeMessage(String message, String filename) {
		TestMessage testMessage = new TestMessage();
		testMessage.setMessage(message);

		try {
			FileOutputStream fos = new FileOutputStream("message.jobj");
			ObjectOutputStream out = new ObjectOutputStream(fos);
			out.writeObject(testMessage);
			out.close();
			return true;
		} catch (FileNotFoundException e) { 
			return false;
		} catch (IOException e) {
			return false;
		}

		
	}

	static String readMessage(String location) {
		try {
			if (location.startsWith("http://")) {
				URL url = new URL(location);
				URLConnection conn = url.openConnection();
				ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());
				TestMessage dTestMessage = (TestMessage) ois.readObject();
				ois.close();
				return dTestMessage.getMessage();

			} 
			else {
				FileInputStream fis = new FileInputStream(location);
				ObjectInputStream ois = new ObjectInputStream(fis);
				TestMessage dTestMessage = (TestMessage) ois.readObject();
				ois.close();
				fis.close();
				return dTestMessage.getMessage();
			}
		} catch (MalformedURLException e) {
			return null;
		} catch (IOException e) {
			return null;
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	public static void main(String args[]) {
		String tm = readMessage("http://www.cl.cam.ac.uk/teaching/current/FJava/testmessage-rds46.jobj");
		System.out.println(tm);
	}
}