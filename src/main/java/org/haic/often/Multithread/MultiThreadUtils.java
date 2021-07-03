package org.haic.often.Multithread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * 多线程 工具类
 *
 * @author haicdust
 * @version 1.0
 * @since 2020/2/18 17:26
 */
public class MultiThreadUtils {

	/**
	 * 关闭线程池,并等待结束
	 *
	 * @param executor
	 *            线程池对象
	 */
	@Contract(pure = true)
	public static void WaitForEnd(final @NotNull ExecutorService executor) {
		executor.shutdown(); // 关闭线程
		try { // 等待线程结束
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 线程等待 MILLISECONDS_SLEEP (毫秒)
	 *
	 * @param time
	 *            线程等待时间
	 */
	@Contract(pure = true)
	public static void WaitForThread(final int time) {
		try { // 程序等待
			TimeUnit.MILLISECONDS.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取Future容器的多线程返回值
	 *
	 * @param future
	 *            future对象
	 * @param <E>
	 *            泛型
	 * @return 返回值
	 */
	@Contract(pure = true)
	public static <E> E GetFuture(final @NotNull Future<E> future) {
		E result = null;
		try {
			result = future.get(); // 获得返回值
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 输出程序运行时间，请在程序开始处加入函数
	 */
	@Contract(pure = true)
	public static void RunTime() {
		long start = System.currentTimeMillis(); // 获取开始时间
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			long end = System.currentTimeMillis(); // 获取结束时间
			System.out.println("程序运行时间：" + (end - start) + "ms");
		}));
	}

}
