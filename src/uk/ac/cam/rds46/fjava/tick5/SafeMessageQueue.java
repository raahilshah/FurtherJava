package uk.ac.cam.rds46.fjava.tick5;

public class SafeMessageQueue<T> implements MessageQueue<T> {
	private static class Link<L> {
		L val;
		Link<L> next;
		Link(L val) { this.val = val; this.next = null; }
	}
	private Link<T> first = null;
	private Link<T> last = null;

	public synchronized void put(T val) {
		Link<T> bot = new Link<T>(val);
		if (first == null) first = bot;
		else last.next = bot;
		last = bot;
		this.notify();
	}

	public synchronized T take() {
		while(first == null) // use a loop to block thread until data is available
			try {this.wait();} catch(InterruptedException ie) {}
		T val = first.val;
		first = first.next;
		if (first == null) last = first;
		return val;
	}
}