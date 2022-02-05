package org.haic.often;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Java 8 Stream 流工具类
 *
 * @author haicdust
 * @version 1.0
 * @since 2021/3/13 12:01
 */
public class IOUtils extends org.apache.commons.io.IOUtils {

	private static final int defaultCharBufferSize = 8192;
	private static final Charset defaultCharset = StandardCharsets.UTF_8;

	/**
	 * 两个数组去除重复项
	 *
	 * @param frist  第一个动态数组
	 * @param second 第二个动态数组
	 * @param <T>    泛型
	 * @return 返回未重复的数组
	 */
	@NotNull @Contract(pure = true) public static <T> List<T> listDeduplication(List<T> frist, List<T> second) {
		return listDeduplication(frist.parallelStream(), second.parallelStream());
	}

	/**
	 * 两个数组去除重复项
	 *
	 * @param frist  第一个动态数组
	 * @param second 第二个动态数组 流
	 * @param <T>    泛型
	 * @return 返回未重复的数组
	 */
	@NotNull @Contract(pure = true) public static <T> List<T> listDeduplication(List<T> frist, Stream<T> second) {
		return listDeduplication(frist.parallelStream(), second);
	}

	/**
	 * 两个数组去除重复项
	 *
	 * @param frist  第一个动态数组 流
	 * @param second 第二个动态数组
	 * @param <T>    泛型
	 * @return 返回未重复的数组
	 */
	@NotNull @Contract(pure = true) public static <T> List<T> listDeduplication(Stream<T> frist, List<T> second) {
		return listDeduplication(frist, second.parallelStream());
	}

	/**
	 * 两个数组去除重复项
	 *
	 * @param frist  第一个动态数组 流
	 * @param second 第二个动态数组 流
	 * @param <T>    泛型
	 * @return 返回未重复的数组
	 */
	@NotNull @Contract(pure = true) public static <T> List<T> listDeduplication(Stream<T> frist, Stream<T> second) {
		return frist.filter(one -> second.noneMatch(two -> Objects.equals(one, two))).collect(Collectors.toList());
	}

	/**
	 * 流排序，隐式处理
	 *
	 * @param list 动态数组
	 * @param <E>  泛型
	 * @return 无排序的数组
	 */
	@NotNull @Contract(pure = true) public static <E> List<E> sort(@NotNull final List<E> list) {
		return list.parallelStream().sorted().collect(Collectors.toList());
	}

	/**
	 * 流排序，隐式处理
	 *
	 * @param list       动态数组
	 * @param <E>        泛型
	 * @param comparator Comparator排序参数
	 * @return 无排序的数组
	 */
	@NotNull @Contract(pure = true) public static <E> List<E> sort(@NotNull final List<E> list, @NotNull final Comparator<E> comparator) {
		return list.parallelStream().sorted(comparator).collect(Collectors.toList());
	}

	/**
	 * 去重无排序,流排序，隐式处理
	 *
	 * @param list 动态数组
	 * @param <E>  泛型
	 * @return 无排序的数组
	 */
	@NotNull @Contract(pure = true) public static <E> List<E> streamSet(@NotNull final List<E> list) {
		return list.parallelStream().distinct().collect(Collectors.toList());
	}

	/**
	 * int类型数组转换Integer类型动态数组
	 *
	 * @param nums int类型静态数组
	 * @return Integer类型动态数组
	 */
	@NotNull @Contract(pure = true) public static List<Integer> intToInteger(final int[] nums) {
		return Arrays.stream(nums).parallel().boxed().collect(Collectors.toList());
	}

	/**
	 * 流转字符串
	 *
	 * @param inputStream 流
	 * @return 字符串
	 */
	@NotNull @Contract(pure = true) public static String streamToString(@NotNull final InputStreamReader inputStream) {
		return streamToString(inputStream, defaultCharBufferSize);
	}

	/**
	 * 流转字符串
	 *
	 * @param inputStream 输入流
	 * @param buffersize  缓存大小
	 * @return 字符串
	 */
	@NotNull @Contract(pure = true) public static String streamToString(@NotNull final InputStreamReader inputStream, int buffersize) {
		StringBuilder result = new StringBuilder();
		try (BufferedReader bufferedReader = new BufferedReader(inputStream, buffersize)) {
			String line;
			while (!Judge.isNull(line = bufferedReader.readLine())) {
				result.append(line).append(StringUtils.LINE_SEPARATOR);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return String.valueOf(result);
	}

	/**
	 * 流转字符串
	 *
	 * @param inputStream 流
	 * @return 字符串
	 */
	@NotNull @Contract(pure = true) public static String streamToString(@NotNull final InputStream inputStream) {
		return streamToString(inputStream, defaultCharset, defaultCharBufferSize);
	}

	/**
	 * 流转字符串
	 *
	 * @param inputStream 流
	 * @param charsetName 字符集编码格式
	 * @return 字符串
	 */
	@NotNull @Contract(pure = true) public static String streamToString(@NotNull final InputStream inputStream, @NotNull final String charsetName) {
		return streamToString(inputStream, Charset.forName(charsetName), defaultCharBufferSize);
	}

	/**
	 * 流转字符串
	 *
	 * @param inputStream 流
	 * @param charset     字符集编码
	 * @return 字符串
	 */
	@NotNull @Contract(pure = true) public static String streamToString(@NotNull final InputStream inputStream, @NotNull final Charset charset) {
		return streamToString(inputStream, charset, defaultCharBufferSize);
	}

	/**
	 * 流转字符串
	 *
	 * @param inputStream 流
	 * @param buffersize  缓冲区大小
	 * @return 字符串
	 */
	@NotNull @Contract(pure = true) public static String streamToString(@NotNull final InputStream inputStream, final int buffersize) {
		return streamToString(inputStream, defaultCharset, buffersize);
	}

	/**
	 * 流转字符串
	 *
	 * @param inputStream 流
	 * @param charsetName 字符集编码
	 * @param buffersize  缓冲区大小
	 * @return 字符串
	 */
	@NotNull @Contract(pure = true) public static String streamToString(@NotNull final InputStream inputStream, @NotNull final String charsetName,
			final int buffersize) {
		return streamToString(inputStream, Charset.forName(charsetName), buffersize);
	}

	/**
	 * 流转字符串
	 *
	 * @param inputStream 流
	 * @param charset     字符集编码
	 * @param buffersize  缓冲区大小
	 * @return 字符串
	 */
	@NotNull @Contract(pure = true) public static String streamToString(@NotNull final InputStream inputStream, @NotNull final Charset charset,
			final int buffersize) {
		String result = null;
		try (ByteArrayOutputStream outputStream = inputStreamToByteArrayOutputStream(inputStream, buffersize)) {
			result = outputStream.toString(charset);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 流转动态字符串数组,按行分割
	 *
	 * @param inputStream 流
	 * @return 字符串数组
	 */
	@NotNull @Contract(pure = true) public static List<String> streamToStringAsLine(@NotNull final InputStreamReader inputStream) {
		return streamToStringAsLine(inputStream, defaultCharBufferSize);
	}

	/**
	 * 流转字符串
	 *
	 * @param inputStream 流
	 * @param buffersize  缓冲区大小
	 * @return 字符串
	 */
	@NotNull @Contract(pure = true) public static List<String> streamToStringAsLine(@NotNull final InputStreamReader inputStream, final int buffersize) {
		List<String> result = new ArrayList<>();
		try (BufferedReader bufferedReader = new BufferedReader(inputStream, buffersize)) {
			String line;
			while (!Judge.isNull(line = bufferedReader.readLine())) {
				result.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 流转动态字符串数组,按行分割
	 *
	 * @param inputStream 流
	 * @return 字符串数组
	 */
	@NotNull @Contract(pure = true) public static List<String> streamToStringAsLine(@NotNull final InputStream inputStream) {
		return streamToStringAsLine(inputStream, defaultCharset, defaultCharBufferSize);
	}

	/**
	 * 流转字符串
	 *
	 * @param inputStream 流
	 * @param buffersize  缓冲区大小
	 * @return 字符串
	 */
	@NotNull @Contract(pure = true) public static List<String> streamToStringAsLine(@NotNull final InputStream inputStream, final int buffersize) {
		return streamToStringAsLine(inputStream, defaultCharset, buffersize);
	}

	/**
	 * 流转字符串
	 *
	 * @param inputStream 流
	 * @param charsetName 字符集编码名称
	 * @return 字符串
	 */
	@NotNull @Contract(pure = true) public static List<String> streamToStringAsLine(@NotNull final InputStream inputStream, @NotNull final String charsetName) {
		return streamToStringAsLine(inputStream, Charset.forName(charsetName), defaultCharBufferSize);
	}

	/**
	 * 流转字符串
	 *
	 * @param inputStream 流
	 * @param charset     字符集编码
	 * @return 字符串
	 */
	@NotNull @Contract(pure = true) public static List<String> streamToStringAsLine(@NotNull final InputStream inputStream, @NotNull final Charset charset) {
		return streamToStringAsLine(inputStream, charset, defaultCharBufferSize);
	}

	/**
	 * 流转字符串
	 *
	 * @param inputStream 流
	 * @param charsetName 字符集编码名称
	 * @param buffersize  缓冲区大小
	 * @return 字符串
	 */
	@NotNull @Contract(pure = true) public static List<String> streamToStringAsLine(@NotNull final InputStream inputStream, @NotNull final String charsetName,
			final int buffersize) {
		return streamToStringAsLine(inputStream, Charset.forName(charsetName), buffersize);
	}

	/**
	 * 流转字符串
	 *
	 * @param inputStream 流
	 * @param charset     字符集编码
	 * @param buffersize  缓冲区大小
	 * @return 字符串
	 */
	@NotNull @Contract(pure = true) public static List<String> streamToStringAsLine(@NotNull final InputStream inputStream, @NotNull final Charset charset,
			final int buffersize) {
		return streamToStringAsLine(new InputStreamReader(inputStream, charset), buffersize);
	}

	/**
	 * 流转Bytes
	 *
	 * @param inputStream 流
	 * @return bytes
	 */
	@Contract(pure = true) public static byte[] streamToByteArray(@NotNull final InputStream inputStream) {
		return streamToByteArray(inputStream, defaultCharBufferSize);
	}

	/**
	 * 流转Bytes
	 *
	 * @param inputStream 流
	 * @param buffersize  缓冲区大小
	 * @return bytes
	 */
	@Contract(pure = true) public static byte[] streamToByteArray(@NotNull final InputStream inputStream, final int buffersize) {
		byte[] result = null;
		try (ByteArrayOutputStream outputStream = inputStreamToByteArrayOutputStream(inputStream, buffersize)) {
			result = outputStream.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * inputStream转ByteArrayOutputStream
	 *
	 * @param inputStream 流
	 * @return ByteArrayOutputStream
	 */
	@NotNull @Contract(pure = true) public static ByteArrayOutputStream inputStreamToByteArrayOutputStream(@NotNull final InputStream inputStream) {
		return inputStreamToByteArrayOutputStream(inputStream, defaultCharBufferSize);
	}

	/**
	 * inputStream转ByteArrayOutputStream
	 *
	 * @param inputStream 流
	 * @param buffersize  * 缓冲区大小
	 * @return ByteArrayOutputStream
	 */
	@NotNull @Contract(pure = true) public static ByteArrayOutputStream inputStreamToByteArrayOutputStream(@NotNull final InputStream inputStream,
			final int buffersize) {
		return inputStreamToByteArrayOutputStream(new BufferedInputStream(inputStream), buffersize);
	}

	/**
	 * 缓冲流转ByteArrayOutputStream
	 *
	 * @param inputStream 流
	 * @return ByteArrayOutputStream
	 */
	@NotNull @Contract(pure = true) public static ByteArrayOutputStream inputStreamToByteArrayOutputStream(@NotNull final BufferedInputStream inputStream) {
		return inputStreamToByteArrayOutputStream(inputStream, defaultCharBufferSize);
	}

	/**
	 * 缓冲流转ByteArrayOutputStream
	 *
	 * @param inputStream 流
	 * @param buffersize  缓冲区大小
	 * @return ByteArrayOutputStream
	 */
	@NotNull @Contract(pure = true) public static ByteArrayOutputStream inputStreamToByteArrayOutputStream(@NotNull final BufferedInputStream inputStream,
			final int buffersize) {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[buffersize];
		int length;
		try {
			while (!Judge.isMinusOne(length = inputStream.read(buffer))) {
				result.write(buffer, 0, length);
			}
			result.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * inputStream转InputStreamReader
	 *
	 * @param inputStream 流
	 * @return InputStreamReader
	 */
	@NotNull @Contract(pure = true) public static InputStreamReader inputStreamToInputStreamReader(@NotNull final InputStream inputStream) {
		return new InputStreamReader(inputStream, defaultCharset);
	}

	/**
	 * inputStream转InputStreamReader
	 *
	 * @param inputStream 流
	 * @param charsetName 字符集编码格式
	 * @return InputStreamReader
	 */
	@NotNull @Contract(pure = true) public static InputStreamReader inputStreamToInputStreamReader(@NotNull final InputStream inputStream,
			@NotNull final String charsetName) {
		return new InputStreamReader(inputStream, Charset.forName(charsetName));
	}

	/**
	 * inputStream转InputStreamReader
	 *
	 * @param inputStream 流
	 * @param charset     字符集编码格式
	 * @return InputStreamReader
	 */
	@NotNull @Contract(pure = true) public static InputStreamReader inputStreamToInputStreamReader(@NotNull final InputStream inputStream,
			@NotNull final Charset charset) {
		return new InputStreamReader(inputStream, charset);
	}

	/**
	 * 字符串转流
	 *
	 * @param str 字符串
	 * @return 流
	 */
	@NotNull @Contract(pure = true) public static InputStream streamByString(@NotNull final String str) {
		return new ByteArrayInputStream(str.getBytes());
	}

}