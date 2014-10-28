package uk.ac.cam.rds46.fjava.tick0;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class ExternalSort {

	public static void sort(String f1, String f2) throws FileNotFoundException, IOException {
		RandomAccessFile f = new RandomAccessFile(f1, "rw");
	   
		int a = 0;
		int b = (int) f.length() / 4;
		mergeSort(f1, f2, a, b);
		f.close();
        
	}
	
	public static void mergeSort(String f, String g, int a, int b) throws IOException {
		if(b - a > 100000) {
			int mid = (a + b)/2;
			mergeSort(f, g, a, mid);
			mergeSort(f, g, mid, b);
			merge(f, g, a, mid, b);
		} else {
			smallSort(f, g, a, b);
		}

	}
	
	public static void smallSort(String f, String g, int a, int b) throws IOException {

		RandomAccessFile f1 = new RandomAccessFile(f, "rw");
		DataInputStream df1 = new DataInputStream(new BufferedInputStream(new FileInputStream(f1.getFD())));
		df1.skipBytes(a*4);
        
		
		byte[] numbers = new byte[(b - a) * 4];
		df1.read(numbers, 0, numbers.length);	
		ByteBuffer byteBuffer = ByteBuffer.wrap(numbers);
		int[] intNos = new int[(b - a)];
		for(int i = 0; i < intNos.length; i++)
			intNos[i] = byteBuffer.getInt();

		Arrays.sort(intNos);

		f1.close();
		df1.close();

   


		f1 = new RandomAccessFile(f, "rw");
		f1.seek(a*4);
		DataOutputStream dg1 = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f1.getFD())));


		for(int i = 0; i < b-a; i++) {
			dg1.writeInt(intNos[i]);
		}

		dg1.flush();
		f1.close();
		df1.close();

	
		dg1.close();

	}
	
	public static void merge(String f, String g, int a, int mid, int b) throws IOException {
		RandomAccessFile f1 = new RandomAccessFile(f, "rw");
		DataInputStream df1 = new DataInputStream(new BufferedInputStream(new FileInputStream(f1.getFD())));
		df1.skipBytes(a * 4);

		RandomAccessFile f2 = new RandomAccessFile(f, "rw");
		DataInputStream df2 = new DataInputStream(new BufferedInputStream(new FileInputStream(f2.getFD())));
		df2.skipBytes((mid) * 4);

		RandomAccessFile g1 = new RandomAccessFile(g, "rw");
		g1.seek(a * 4);
		DataOutputStream dg1 = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(g1.getFD())));


		int x = a;
		int y = mid;
		int currentOne = 0;
		int currentTwo = 0;
		boolean readOne = true;
		boolean readTwo = true;

		for(int i = a; i < b; i++) {
			if(x < mid) {
				if(y < b) {
					if(readOne) {
						currentOne = df1.readInt();
					} 
					if(readTwo) {
						currentTwo = df2.readInt();
					}

					if(currentOne <= currentTwo) {
						dg1.writeInt(currentOne);
						readOne = true;
						readTwo = false;
						x++;
					} else {
						dg1.writeInt(currentTwo);
						readOne = false;
						readTwo = true;
						y++;
					}
				} else {
					if(readOne) {
						currentOne = df1.readInt();
						x++;
					} 

					dg1.writeInt(currentOne);
					readOne = true;
				}
			} else {
				if(readTwo) {
					currentTwo = df2.readInt();
					y++;
				}

				dg1.writeInt(currentTwo);
				readTwo = true;
			}

		}

		dg1.flush();
		f1.close();
		df1.close();

		f2.close();
		df2.close();

		g1.close();
		dg1.close();


		f1 = new RandomAccessFile(g, "rw");
		df1 = new DataInputStream(new BufferedInputStream(new FileInputStream(f1.getFD())));
		df1.skipBytes(a*4);

		g1 = new RandomAccessFile(f, "rw");
		g1.seek(a*4);
		dg1 = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(g1.getFD())));


		for(int i = a; i < b; i++) {
			dg1.writeInt(df1.readInt());
		}

		dg1.flush();
		f1.close();
		df1.close();

		g1.close();
		dg1.close();

	}

	private static String byteToHex(byte b) {
		String r = Integer.toHexString(b);
		if (r.length() == 8) {
			return r.substring(6);
		}
		return r;
	}

	public static String checkSum(String f) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			DigestInputStream ds = new DigestInputStream(
					new FileInputStream(f), md);
			byte[] b = new byte[512];
			while (ds.read(b) != -1)
				;

			String computed = "";
			for(byte v : md.digest()) 
				computed += byteToHex(v);

			return computed;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "<error computing checksum>";
	}

	public static void main(String[] args) throws Exception {
		String f1 = args[0];
		String f2 = args[1];
		sort(f1, f2);
		System.out.println("The checksum is: "+checkSum(f1));
	}
}