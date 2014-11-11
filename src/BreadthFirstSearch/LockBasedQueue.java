package BreadthFirstSearch;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

public class LockBasedQueue<T> {
	ReentrantLock enqueLock = new ReentrantLock();
	ReentrantLock dequeLock = new ReentrantLock();
	private Queue<T> queue;

	public LockBasedQueue() {
		queue = new LinkedList<T>();
	}

	public void enque(T t) {
		enqueLock.lock();
		try {
			queue.add(t);
		} finally {
			enqueLock.unlock();
		}
	}

	public void deque() {
		dequeLock.lock();
		try {
			queue.remove();
		} finally {
			dequeLock.unlock();
		}
	}

}
