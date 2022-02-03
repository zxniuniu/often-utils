package org.haic.often;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * CMD控制台
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
	 * 设置 cmd命令
	 *
	 * @param dos cmd命令
	 * @return new RunCmd
	 */
	public static RunCmd dos(@NotNull final String... dos) {
		return dos(Arrays.stream(dos).toList());
	}

	/**
	 * 设置 cmd命令
	 *
	 * @param dos cmd命令
	 * @return new RunCmd
	 */
	public static RunCmd dos(@NotNull final List<String> dos) {
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
	 * 设置 cmd命令
	 *
	 * @param command cmd命令
	 * @return this
	 */
	private RunCmd command(@NotNull final List<String> command) {
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
	public RunCmd charset(@NotNull final String CharsetName) {
		charset(Charset.forName(CharsetName));
		return this;
	}

	/**
	 * 设置 字符集编码
	 *
	 * @param charset 字符集编码
	 * @return this
	 */
	public RunCmd charset(@NotNull final Charset charset) {
		this.charset = charset;
		return this;
	}

	/**
	 * 设置 工作目录
	 *
	 * @param directory 工作目录路径
	 * @return this
	 */
	public RunCmd directory(@NotNull final String directory) {
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
		try (InputStreamReader inputStream = new InputStreamReader(
				(process = new ProcessBuilder(command).redirectErrorStream(true).directory(directory).start()).getInputStream(), charset)) {
			result = IOUtils.streamToString(inputStream);
			process.waitFor();
			process.destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return StringUtils.deletePefixAndSuffix(result, StringUtils.LINE_SEPARATOR);
	}

	/**
	 * 执行CMD命令
	 *
	 * @return 执行是否成功
	 */
	@Contract(pure = true) public boolean execute() {
		int status = 0;
		Process process;
		try {
			process = new ProcessBuilder(command).directory(directory).start();
			status = process.waitFor();
			process.destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Judge.isEmpty(status);
	}

}