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

		RandomAccessFile fileA = new RandomAccessFile(f1, "rw");
	   
		int lo = 0;
		int hi = (int) fileA.length() / 4;
		mergeSort(f1, f2, lo, hi);
		fileA.close();
	}
	
	public static void mergeSort(String f1, String f2, int lo, int hi) throws IOException {

		if(hi - lo > 100000) {
			int mid = (lo + hi)/2;
			mergeSort(f1, f2, lo, mid);
			mergeSort(f1, f2, mid, hi);
			merge(f1, f2, lo, mid, hi);
		} else {
			smallSort(f1, f2, lo, hi);
		}

	}
	
	public static void smallSort(String f1, String f2, int lo, int hi) throws IOException {

		RandomAccessFile raf = new RandomAccessFile(f1, "rw");
		DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(raf.getFD())));
		dis.skipBytes(lo * 4);
        
		
		byte[] bytes = new byte[(hi - lo) * 4];
		int[] ints = new int[(hi - lo)];

		dis.read(bytes, 0, bytes.length);	
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		for(int i = 0; i < ints.length; i++) ints[i] = bb.getInt();

		Arrays.sort(ints);

		raf.close();
		dis.close();


		raf = new RandomAccessFile(f1, "rw");
		raf.seek(lo * 4);
		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(raf.getFD())));


		for(int i = 0; i < hi - lo; i++) dos.writeInt(ints[i]);
		dos.flush();
		
		raf.close();
		dos.close();

	}
	
	public static void merge(String f1, String f2, int lo, int mid, int hi) throws IOException {

		RandomAccessFile rafA1 = new RandomAccessFile(f1, "rw");
		DataInputStream disA1 = new DataInputStream(new BufferedInputStream(new FileInputStream(rafA1.getFD())));
		disA1.skipBytes(lo * 4);

		RandomAccessFile rafA2 = new RandomAccessFile(f1, "rw");
		DataInputStream disA2 = new DataInputStream(new BufferedInputStream(new FileInputStream(rafA2.getFD())));
		disA2.skipBytes(mid * 4);

		RandomAccessFile rafB = new RandomAccessFile(f2, "rw");
		rafB.seek(lo * 4);
		DataOutputStream dosB = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(rafB.getFD())));


		int j = lo, k = mid, firstInt = 0, secondInt = 0;
		boolean seekFirst = true, seekSecond = true;

		while (j < mid || k < hi) {
			if (j >= mid) {
				if(seekSecond) secondInt = disA2.readInt();
				dosB.writeInt(secondInt); k++;
				seekSecond = true;
			} else if (k >= hi) {
				if(seekFirst) firstInt = disA1.readInt();
				dosB.writeInt(firstInt); j++;
				seekFirst = true;
			} else {
				if(seekFirst) firstInt = disA1.readInt();
				if(seekSecond) secondInt = disA2.readInt();
				if(firstInt <= secondInt) {
					dosB.writeInt(firstInt); j++;
					seekFirst = true;
					seekSecond = false;
				} else {
					dosB.writeInt(secondInt); k++;
					seekFirst = false;
					seekSecond = true;
				}
			}
		}

		dosB.flush();

		rafB.close();
		dosB.close();
		rafA1.close();
		disA1.close();
		rafA2.close();
		disA2.close();


		fileCopy(f1, f2, lo, hi);
	}

	public static void fileCopy(String to, String from, int lo, int hi) throws IOException {

		RandomAccessFile rafB = new RandomAccessFile(from, "rw");
		DataInputStream disB = new DataInputStream(new BufferedInputStream(new FileInputStream(rafB.getFD())));
		disB.skipBytes(lo * 4);

		RandomAccessFile rafA = new RandomAccessFile(to, "rw");
		rafA.seek(lo * 4);
		DataOutputStream dosA = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(rafA.getFD())));


		for(int i = lo; i < hi; i++) {
			dosA.writeInt(disB.readInt());
		}

		dosA.flush();

		rafA.close();
		dosA.close();
		rafB.close();
		disB.close();
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
		System.out.println("The checksum is: " + checkSum(f1));
	}
}