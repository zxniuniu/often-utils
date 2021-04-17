package org.haic.often;

/**
 * Unit test for simple App.
 */
public class AppTest {

	private static final String proxyHost = "127.0.0.1";
	private static final int proxyPort = 7890;

	public static void main(String[] args) {
		String url = "https://www.lanzous.com/ikqOtns6jlc";
		// Connection.Response response = JsoupUtils.connect(URIUtils.lanzouStraight(url)).GetResponse();
		String str = URIUtils.lanzouStraight(url);
		// String filename = url.substring(url.lastIndexOf("/") + 1);
		String filename = str.contains("?") ? str.substring(str.lastIndexOf("/") + 1, str.indexOf("?")) : str.substring(str.lastIndexOf("/") + 1);
		System.out.println(str);
		System.out.println(filename);
	}
}
