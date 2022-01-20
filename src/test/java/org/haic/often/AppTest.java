package org.haic.often;

import java.util.Iterator;
import java.util.Map;

/**
 * Unit test for simple App.
 */
public class AppTest {

	private static final String proxyHost = "127.0.0.1";
	private static final int proxyPort = 7890;
	private static final String aliDataApi = "https://api.aliyundrive.com/adrive/v3/share_link/get_share_by_anonymous";

	public static void main(String[] args) {
		//  String url = "https://files.yande.re/image/48126bd4124671beefdebe39b9b7bbf8/yande.re%20906157%20bikini%20breasts%20garter%20maid%20mignon%20nipples%20no_bra%20nopan%20pointy_ears%20see_through%20swimsuits%20tail%20thighhighs%20wet%20wet_clothes.png";
		//  url = "https://down.qq.com/qqweb/PCQQ/PCQQ_EXE/QQ9.5.3.28008.exe";
		//  int statusCode = NetworkFileUtils.connect(url)//
		// .proxy(proxyHost, proxyPort)//
		//  .interval(50).multithread(10).retry(true).download("F:\\Downloads");
		//for (int i = 0; i < 100; i++) {
		//System.out.println(UserAgentUtils.randomPE());
		Map<String, String> cookies2 = LocalCookies.home().getCookiesForDomain("aliyundrive.com");
		Iterator<Map.Entry<String, String>> entries2 = cookies2.entrySet().iterator();
		while (entries2.hasNext()) {
			Map.Entry<String, String> entry = entries2.next();

			System.out.println(entry.getKey() + " = " + entry.getValue());
		}

		System.exit(0);

	}
}