package org.haic.often;

/**
 * Unit test for simple App.
 */
public class AppTest {

	private static final String proxyHost = "127.0.0.1";
	private static final int proxyPort = 7890;

	public static void main(String[] args) {
		String url = "https://down.qq.com/qqweb/PCQQ/PCQQ_EXE/QQ9.5.3.28008.exe";
		//NetworkFileUtils.connect(url).multithread(10).method(NetworkFileUtils.Method.MULTITHREAD).interval(10).retry(true).download("F:\\Downloads");
		String dos = "java -version";
		String[] cmd = { "cmd", "/c", "java", "-version" };
		String str = RunCmd.dos(cmd).readInfo();
		System.out.println(str);
		System.out.println(URIUtils.pingIp("192.168.2.1"));
	}

}
