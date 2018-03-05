package worksheet;

public final class Semaphore {

	private int value;
	private final Object LOCK = new Object();

	public Semaphore(int initial) {
		if (initial < 0)
			throw new IllegalArgumentException();
		value = initial;
	}

	public int available() {
		return value;
	}

	public void acquire() {
		synchronized (LOCK) {
			while (value == 0) {
				try {
					LOCK.wait();
				} catch (InterruptedException e) {
					// NOP
				}
			}

			value--; // decrement value
		}
	}

	public void release() {
		synchronized (LOCK) {
			value++; // increment value
			LOCK.notify(); // notify a waiting thread
		}
	}
}
