package org.haic.often.Multithread;

/**
 * ParameterizedThread defines a thread with a generic parameter
 *
 * @author haicdust
 * @param <T>
 *            泛型
 */
public class ParameterizedThread<T> implements Runnable {

	private final T T;
	private final ParameterizedThreadStart<T> parameterStart;

	/**
	 * Constructor
	 *
	 * @param T
	 *            泛型
	 */
	public ParameterizedThread(final T T, final ParameterizedThreadStart<T> parameterStart) {
		this.T = T;
		this.parameterStart = parameterStart;
	}

	/**
	 * getContext returns the context of current thread.
	 *
	 * @return 泛型
	 */
	public T GetContext() {
		return T;
	}

	/**
	 * run method to be called in that separately executing thread.
	 */
	@Override
	public void run() {
		parameterStart.run(T);
	}
}
