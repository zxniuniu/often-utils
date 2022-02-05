package org.haic.often;

import org.haic.often.Network.HttpsUtils;

/**
 * Unit test for simple App.
 */
public class AppTest {

	public static final String proxyHost = "127.0.0.1";
	public static final int proxyPort = 7890;

	public static void main(String[] args) {
		// String url = "https://files.yande.re/image/48126bd4124671beefdebe39b9b7bbf8/yande.re%20906157%20bikini%20breasts%20garter%20maid%20mignon%20nipples%20no_bra%20nopan%20pointy_ears%20see_through%20swimsuits%20tail%20thighhighs%20wet%20wet_clothes.png";
		// url = "https://down.qq.com/qqweb/PCQQ/PCQQ_EXE/QQ9.5.3.28008.exe";
		//  int statusCode = NetworkFileUtils.connect(url)//
		// .proxy(proxyHost, proxyPort)//
		//  .interval(50).multithread(10).retry(true).download("F:\\Downloads");
		//for (int i = 0; i < 100; i++) {
		//System.out.println(UserAgentUtils.randomPE());
		//Connection.Response res = JsoupUtils.connect("https://www.baidu.com").socks(proxyHost, proxyPort).execute();
		//System.out.println(res.headers());
		//System.out.println(LocalCookies.home().getCookiesForDomain("yande.re"));
		System.out.println(HttpsUtils.connect("https://www.lanzoui.com/b0ejszleh").get());

	}
}