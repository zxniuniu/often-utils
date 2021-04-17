package org.haic.often;

import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * CMD控制台
 *
 * @author haicdust
 * @version 1.0
 * @since 2020/2/18 17:16
 */
public class RunCmd {

	/**
	 * 执行CMD命令并返回信息
	 *
	 * @param dos
	 *            cmd命令
	 * @return 命令执行后返回的信息
	 */
	@NotNull
	@Contract(pure = true)
	public static String readInfo(final @NotNull String dos) {
		String result = "";
		Process process;
		try (InputStreamReader inputStream = new InputStreamReader((process = Runtime.getRuntime().exec(dos)).getInputStream(), Charset.forName("GBK"))) {
			result = IOUtils.streamToString(inputStream);
			process.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return StringUtils.deletePefixAndSuffix(result, StringUtils.LINE_SEPARATOR);
	}

	/**
	 * 执行CMD命令
	 *
	 * @param dos
	 *            cmd命令
	 * @return 是否正常退出
	 */
	@Contract(pure = true)
	public static boolean execute(final @NotNull String dos) {
		int status;
		try {
			Process process = Runtime.getRuntime().exec(dos);
			status = process.waitFor();
		} catch (Exception e) {
			status = 1;
		}
		return Judge.isEmpty(status);
	}

	/**
	 * 如果路径有空格，会导致命令执行错误，为路径添加引号使命令可以正常执行
	 *
	 * @param path
	 *            路径
	 * @return 添加双引号的路径(常用于cmd命令)
	 */
	@NotNull
	@Contract(pure = true)
	public static String addDoubleQuotes(final @NotNull String path) {
		return StringUtils.DOUBLE_QUOTES + path + StringUtils.DOUBLE_QUOTES;
	}

	/**
	 * 如果路径有空格，会导致命令执行错误，为路径添加引号使命令可以正常执行
	 *
	 * @param path
	 *            路径
	 * @return 添加单引号的路径(常用于adb shell命令)
	 */
	@NotNull
	@Contract(pure = true)
	public static String addApostrophe(final @NotNull String path) {
		return StringUtils.APOSTROPHE + path + StringUtils.APOSTROPHE;
	}

}
