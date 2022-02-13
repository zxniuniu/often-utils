package org.haic.often;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 终端控制台
 *
 * @author haicdust
 * @version 1.0
 * @since 2021/12/25 06:50
 */
public class RunCmd {
	private static final String OS = System.getProperty("os.name").toLowerCase();

	private List<String> command; // cmd命令
	private Charset charset; // 字符集格式
	private File directory; // 工作目录

	private RunCmd() {
		charset = OS.contains("windows") ? Charset.forName("GBK") : StandardCharsets.UTF_8;
		directory(System.getProperty("user.dir"));
	}

	/**
	 * 设置 终端命令
	 *
	 * @param dos 终端命令
	 * @return new RunCmd
	 */
	public static RunCmd dos(@NotNull String... dos) {
		return dos(Arrays.stream(dos).toList());
	}

	/**
	 * 设置 终端命令
	 *
	 * @param dos 终端命令
	 * @return new RunCmd
	 */
	public static RunCmd dos(@NotNull List<String> dos) {
		return config().command(dos);
	}

	/**
	 * 获取 new RunCmd
	 *
	 * @return new RunCmd
	 */
	private static RunCmd config() {
		return new RunCmd();
	}

	/**
	 * 设置 终端命令
	 *
	 * @param command 终端命令
	 * @return this
	 */
	private RunCmd command(@NotNull List<String> command) {
		this.command = new ArrayList<>();
		if (OS.contains("windows")) {
			this.command.add("cmd");
			this.command.add("/c");
		}
		this.command.addAll(command);
		return this;
	}

	/**
	 * 设置 字符集编码名
	 *
	 * @param CharsetName 字符集编码名
	 * @return this
	 */
	public RunCmd charset(@NotNull String CharsetName) {
		charset(Charset.forName(CharsetName));
		return this;
	}

	/**
	 * 设置 字符集编码
	 *
	 * @param charset 字符集编码
	 * @return this
	 */
	public RunCmd charset(@NotNull Charset charset) {
		this.charset = charset;
		return this;
	}

	/**
	 * 设置 工作目录
	 *
	 * @param directory 工作目录路径
	 * @return this
	 */
	public RunCmd directory(@NotNull String directory) {
		return directory(new File(directory));
	}

	/**
	 * 设置 工作目录
	 *
	 * @param directory 工作目录
	 * @return this
	 */
	public RunCmd directory(File directory) {
		this.directory = directory;
		return this;
	}

	/**
	 * 获取执行的信息
	 *
	 * @return 执行的信息
	 */
	@NotNull @Contract(pure = true) public String readInfo() {
		String result = "";
		Process process;
		try (InputStream inputStream = (process = new ProcessBuilder(command).redirectErrorStream(true).directory(directory).start()).getInputStream()) {
			result = StreamUtils.stream(inputStream).charset(charset).getString();
			process.waitFor();
			process.destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return StringUtils.deletePefixAndSuffix(result, StringUtils.LINE_SEPARATOR);
	}

	/**
	 * 执行 终端命令
	 *
	 * @return 进程的退出值, 一般情况下, 0为正常终止
	 */
	@Contract(pure = true) public int execute() {
		int status = 1;
		Process process;
		try {
			process = new ProcessBuilder(command).directory(directory).start();
			status = process.waitFor();
			process.destroy();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return status;
	}

}