package org.haic.often;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
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

	protected Charset charset = Charset.defaultCharset(); // 字符集格式
	protected File directory = new File(System.getProperty("user.dir")); // 工作目录

	protected List<String> command = new ArrayList<>(); // 命令

	protected boolean useTerminal = true; // 使用终端执行命令
	protected String terminal = Terminal.CMD.value; // 默认终端

	protected RunCmd() {
	}

	/**
	 * 设置 终端命令
	 *
	 * @param dos 终端命令
	 * @return new RunCmd
	 */
	@Contract(pure = true) public static RunCmd dos(@NotNull String... dos) {
		return dos(Arrays.stream(dos).toList());
	}

	/**
	 * 设置 终端命令
	 *
	 * @param dos 终端命令
	 * @return new RunCmd
	 */
	@Contract(pure = true) public static RunCmd dos(@NotNull List<String> dos) {
		return config().command(dos);
	}

	/**
	 * 获取 new RunCmd
	 *
	 * @return new RunCmd
	 */
	@Contract(pure = true) protected static RunCmd config() {
		return new RunCmd();
	}

	/**
	 * 设置 终端命令
	 *
	 * @param command 终端命令
	 * @return this
	 */
	@Contract(pure = true) protected RunCmd command(@NotNull List<String> command) {
		if (useTerminal) {
			this.command.add(terminal);
			this.command.add("/c");
		}
		this.command.addAll(command);
		return this;
	}

	/**
	 * 设置 是否使用终端执行命令,默认为true
	 *
	 * @param terminal boolean
	 * @return this
	 */
	@Contract(pure = true) protected RunCmd terminal(boolean terminal) {
		this.useTerminal = terminal;
		return this;
	}

	/**
	 * 设置 默认可执行终端
	 *
	 * @param terminal 终端路径
	 * @return this
	 */
	@Contract(pure = true) protected RunCmd terminal(String terminal) {
		this.terminal = terminal;
		return this;
	}

	/**
	 * 设置 默认可执行终端
	 *
	 * @param terminal 枚举Terminal类可执行终端
	 * @return this
	 */
	@Contract(pure = true) protected RunCmd terminal(Terminal terminal) {
		this.terminal = terminal.value;
		return this;
	}

	/**
	 * 设置 字符集编码名
	 *
	 * @param CharsetName 字符集编码名
	 * @return this
	 */
	@Contract(pure = true) public RunCmd charset(@NotNull String CharsetName) {
		charset(Charset.forName(CharsetName));
		return this;
	}

	/**
	 * 设置 字符集编码
	 *
	 * @param charset 字符集编码
	 * @return this
	 */
	@Contract(pure = true) public RunCmd charset(@NotNull Charset charset) {
		this.charset = charset;
		return this;
	}

	/**
	 * 设置 工作目录
	 *
	 * @param directory 工作目录路径
	 * @return this
	 */
	@Contract(pure = true) public RunCmd directory(@NotNull String directory) {
		return directory(new File(directory));
	}

	/**
	 * 设置 工作目录
	 *
	 * @param directory 工作目录
	 * @return this
	 */
	@Contract(pure = true) public RunCmd directory(File directory) {
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
		return StringUtils.deletePefixAndSuffix(result, charset.name().equals("GBK") ? StringUtils.CRLF : StringUtils.LF);
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

	/**
	 * 终端枚举类
	 */
	public enum Terminal {
		/**
		 * CMD 终端
		 */
		CMD("cmd"),
		/**
		 * powershell 终端
		 */
		POWERSHELL("powershell");

		private final String value;

		Terminal(final String value) {
			this.value = value;
		}

		/**
		 * 获得 枚举方法的值
		 *
		 * @return value
		 */
		@Contract(pure = true) public final String getValue() {
			return value;
		}
	}

}