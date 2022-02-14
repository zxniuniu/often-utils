package org.haic.often.Network;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.haic.often.Judge;
import org.jetbrains.annotations.Contract;

/**
 * UserAgent工具类
 *
 * @author haicdust
 * @version 1.0
 * @since 2020/3/7 20:18
 */
public class UserAgent {

	protected static final String mozilla = "Mozilla/5.0 (";

	/**
	 * 获取 Chrome Browser UserAgent
	 *
	 * @return Chrome Browser UserAgent
	 */
	@Contract(pure = true) public static String randomChrome() {
		return random(Browser.CHROME);
	}

	/**
	 * 获取 Random UserAgent
	 *
	 * @return Random UserAgent
	 */
	@Contract(pure = true) public static String random() {
		return random(Browser.getEnum(RandomUtils.nextInt(0, Browser.values().length)));
	}

	/**
	 * 获取 Corresponding Browser UserAgent
	 *
	 * @return UserAgent
	 */
	@Contract(pure = true) public static String random(Browser browser) {
		String userAgent;
		switch (browser) {
		case FIREFOX -> userAgent = mozilla + header() + firefoxTail(); // firefox
		case CHROME -> // chrome
				userAgent = mozilla + header() + chromeTail();
		case IE -> { // ie
			if (Judge.isEmpty(RandomUtils.nextInt(0, 2))) {
				userAgent = mozilla + "compatible; MSIE " + RandomUtils.nextInt(4, 20) + ".0; ";
				userAgent += headerWin() + "; Trident/" + RandomUtils.nextInt(4, 10) + ".0)";
			} else {
				userAgent = mozilla + headerWin() + "; Trident/" + RandomUtils.nextInt(4, 10) + ".0) like Gecko";
			}
		}
		case MAXTHON -> // Maxthon
				userAgent = mozilla + "compatible; MSIE " + RandomUtils.nextInt(4, 20) + ".0; " + headerWin() + "; Maxthon 2.0)";
		case TT -> // TT
				userAgent = mozilla + "compatible; MSIE " + RandomUtils.nextInt(4, 20) + ".0; " + headerWin() + "; TencentTraveler  4.0)";
		case Green -> // The World,Green Browser
				userAgent = mozilla + "compatible; MSIE " + RandomUtils.nextInt(4, 20) + ".0; " + headerWin() + ")";
		case World, Avant, THREESIXZERO -> { // The World,Avant,360
			String[] browserList = { "The World", "360SE", "Avant Browser" };
			userAgent =
					mozilla + "compatible; MSIE " + RandomUtils.nextInt(4, 20) + ".0; " + headerWin() + browserList[RandomUtils.nextInt(0, browserList.length)]
							+ ")";
		}
		case SUGO -> // sogu
				userAgent = mozilla + "compatible; MSIE " + RandomUtils.nextInt(4, 20) + ".0; " + headerWin() + "; Trident/" + RandomUtils.nextInt(4, 10)
						+ "; SE 2.X MetaSr 1.0; SE 2.X MetaSr 1.0; .NET CLR 2.0.50727; SE 2.X MetaSr 1.0)";
		default -> userAgent = mozilla + header() + chromeTail(); // default chrome
		}
		return userAgent;
	}

	/**
	 * 获取 手机端 Chrome UserAgent
	 *
	 * @return 手机端 Chrome UserAgent
	 */
	@Contract(pure = true) public static String randomChromeAsPE() {
		return randomAsPE(PEBrowser.CHROME);
	}

	/**
	 * 获取 随机手机端UserAgent
	 *
	 * @return 随机手机端UserAgent
	 */
	@Contract(pure = true) public static String randomAsPE(PEBrowser browser) {
		String userAgent;
		switch (PEBrowser.getEnum(RandomUtils.nextInt(0, PEBrowser.values().length))) {
		case FIREFOX -> { // firefox
			String version = RandomUtils.nextInt(0, 100) + ".0";
			String[] equipment = { "Tablet", "Mobile" };
			userAgent = mozilla + androidVersion();
			userAgent += "; " + equipment[RandomUtils.nextInt(0, equipment.length)];
			userAgent += "; rv:" + version + ") Gecko/" + version + " Firefox/" + version;
		}
		case CHROME ->  // chrome
				userAgent = mozilla + chromeInfo() + chromeTail();
		case QQ -> userAgent = "MQQBrowser/26 (" + chromeInfo() + "; CyanogenMod-7" + chromeTail(); // qq
		case OPERA -> { // Opera
			String[] systems = { "Linux", "iPhone", "iPad", "iPod" };
			String system = systems[RandomUtils.nextInt(0, systems.length)];
			userAgent = "Opera/9.80 (" + system + "; U;";
			if (system.equals("iPhone") || system.equals("iPad") || system.equals("iPod")) {
				userAgent +=
						"CPU iPhone OS " + RandomUtils.nextInt(1, 20) + "_" + RandomUtils.nextInt(1, 10) + "_" + RandomUtils.nextInt(1, 10) + " like Mac OS X";
			} else {
				userAgent += androidVersion();
			}
			userAgent += language() + ") Presto/" + RandomUtils.nextInt(1, 10) + "." + RandomUtils.nextInt(0, 10) + "." + RandomUtils.nextInt(10, 500)
					+ " Version/11.10";
		}
		case TOUCHPAD -> // Touchpad
				userAgent = mozilla + hpInfo() + ") AppleWebKit/" + RandomUtils.nextInt(500, 800) + "." + RandomUtils.nextInt(10, 100)
						+ " (KHTML, like Gecko) Version/5.0." + RandomUtils.nextInt(2, 5) + " Safari/537.36" + " wOSBrowser/" + RandomUtils.nextInt(100, 800)
						+ "." + RandomUtils.nextInt(0, 100) + " TouchPad/1.0";
		case SYMBIANOS -> { // SymbianOS
			userAgent = mozilla + "SymbianOS/9.4; Series60/5.0 NokiaN97-1/20.0.019; Profile/MIDP-2.1 Configuration/CLDC-1.1";
			userAgent += ") AppleWebKit/" + RandomUtils.nextInt(500, 800) + "." + RandomUtils.nextInt(10, 100);
			userAgent +=
					" (KHTML, like Gecko) BrowserNG/" + RandomUtils.nextInt(2, 10) + "." + RandomUtils.nextInt(0, 10) + "." + RandomUtils.nextInt(10000, 50000);
		}
		case UC -> { // UC
			String[] info = { "NOKIA5700/ UCWEB7.0.2.37/28/999", "Openwave/ UCWEB7.0.2.37/28/999",
					"Mozilla/4.0 (compatible; MSIE 6.0; ) Opera/UCWEB7.0.2.37/28/999" };
			userAgent = info[RandomUtils.nextInt(0, info.length)];
		}
		default -> userAgent = mozilla + chromeInfo() + chromeTail(); // default chrome
		}
		return userAgent;
	}

	@Contract(pure = true) protected static String chromeInfo() {
		String info;
		String[] system = { "iPhone", "iPad", "iPod", "Linux", "BlackBerry" };
		int index = RandomUtils.nextInt(0, 20);
		switch (index) {
		case 0, 1, 2 -> info =
				system[index] + "; U;CPU iPhone OS " + RandomUtils.nextInt(1, 20) + "_" + RandomUtils.nextInt(1, 10) + "_" + RandomUtils.nextInt(1, 10)
						+ " like Mac OS X";
		case 3, 4 -> info =
				system[index] + "; U;" + androidVersion() + "; " + RandomStringUtils.random(3, true, false) + "-" + RandomStringUtils.random(4, true, true)
						+ " Build/" + RandomStringUtils.randomAlphabetic(6);
		default -> info =
				"Linux; U;" + androidVersion() + "; " + RandomStringUtils.random(3, true, false) + "-" + RandomStringUtils.random(4, true, true) + " Build/"
						+ RandomStringUtils.randomAlphabetic(6);
		}
		info += language();
		return info;
	}

	@Contract(pure = true) protected static String hpInfo() {
		String[] brand = { "SM-G850F Build/LRX22G", "GT-I9300 Build/JRO03C", "ZTE BLADE A610 Build/MRA58K" };
		return "hp-tablet" + "; U;" + androidVersion() + "; " + brand[RandomUtils.nextInt(0, brand.length)] + language();
	}

	@Contract(pure = true) protected static String language() {
		String info = "";
		if (Judge.isEmpty(RandomUtils.nextInt(0, 2))) {
			String[] language = { "en-us", "zh-cn", "en-GB" };
			info += "; " + language[RandomUtils.nextInt(0, language.length)];
		}
		return info;
	}

	@Contract(pure = true) protected static String androidVersion() {
		String version = "Android " + RandomUtils.nextInt(4, 13) + "." + RandomUtils.nextInt(0, 5);
		if (!version.endsWith("0")) {
			version += "." + RandomUtils.nextInt(0, 5);
		}
		return version;
	}

	@Contract(pure = true) protected static String header() {
		String header = "";
		switch (RandomUtils.nextInt(0, 2)) {
		case 0 -> header = headerWin();
		case 1 -> header = headerLinux();
		case 2 -> header = headerMac();
		}
		return header;
	}

	@Contract(pure = true) protected static String headerWin() {
		String header = winVersion();
		switch (RandomUtils.nextInt(0, 3)) {
		case 1 -> header += "; WOW64";
		case 2 -> header += "; Win64; x64";
		}
		return header;
	}

	@Contract(pure = true) protected static String winVersion() {
		String[] system = { "Windows NT 10.0", "windows NT 6.2", "Windows NT 6.1", "Windows NT 6.0", "Windows NT 5.2", "Windows NT 5.1", "Windows NT 5.0",
				"Windows ME", "Windows 98" };
		return system[RandomUtils.nextInt(0, system.length)];
	}

	@Contract(pure = true) protected static String headerLinux() {
		String[] hardware = { "Linux x86_64", "Linux i686", "Linux ppc64", "Linux ppc" };
		String header = "X11; ";
		if (Judge.isEmpty(RandomUtils.nextInt(0, 2))) {
			String[] system = { "Ubuntu", "Manjaro Linux", "Pop!_OS", "Deepin", "Debian GNU/Linux", "elementary OS" };
			header += system[RandomUtils.nextInt(0, system.length)] + "; ";
		}
		header += hardware[RandomUtils.nextInt(0, hardware.length)];
		if (Judge.isEmpty(RandomUtils.nextInt(0, 2))) {
			header += " on x86_64";
		}
		return header;
	}

	@Contract(pure = true) protected static String headerMac() {
		return "Macintosh; Intel Mac OS X " + RandomUtils.nextInt(4, 20) + "_" + RandomUtils.nextInt(0, 10) + "_" + RandomUtils.nextInt(0, 10);
	}

	@Contract(pure = true) protected static String firefoxTail() {
		String firefox = "";
		String version = RandomUtils.nextInt(0, 100) + ".0";
		if (Judge.isEmpty(RandomUtils.nextInt(0, 2))) {
			firefox += "; rv:" + version;
		}
		firefox += ") Gecko/20100101 Firefox/" + version;
		return firefox;
	}

	@Contract(pure = true) protected static String chromeTail() {
		String chrome = ") AppleWebKit/" + RandomUtils.nextInt(500, 800) + "." + RandomUtils.nextInt(10, 100);
		chrome += " (KHTML, like Gecko) Version/5.0." + RandomUtils.nextInt(2, 5);
		chrome += " Chrome/" + RandomUtils.nextInt(10, 100) + "." + "0" + "." + RandomUtils.nextInt(1000, 10000) + "." + RandomUtils.nextInt(10, 100)
				+ " Safari/537.36";
		return chrome;
	}

	/**
	 * 移动端浏览器名称
	 */
	public enum PEBrowser {
		/**
		 * firefox Browser
		 */
		FIREFOX(0),
		/**
		 * chrome Browser
		 */
		CHROME(1),
		/**
		 * qq Browser
		 */
		QQ(2),
		/**
		 * opera Browser
		 */
		OPERA(3),
		/**
		 * touchpad Browser
		 */
		TOUCHPAD(4),
		/**
		 * symbianos Browser
		 */
		SYMBIANOS(5),
		/**
		 * uc Browser
		 */
		UC(6);

		private final int value;

		PEBrowser(final int value) {
			this.value = value;
		}

		/**
		 * 通过值匹配获得枚举方法
		 *
		 * @param index 值
		 * @return enum Browser
		 */
		@Contract(pure = true) public static PEBrowser getEnum(int index) {
			for (PEBrowser sexEnum : PEBrowser.values()) {
				if (sexEnum.value == index) {
					return sexEnum;
				}
			}
			return CHROME;
		}

		/**
		 * 获得 枚举方法的值
		 *
		 * @return value
		 */
		@Contract(pure = true) public final int getValue() {
			return value;
		}
	}

	/**
	 * 浏览器名称
	 */
	public enum Browser {
		/**
		 * firefox Browser
		 */
		FIREFOX(0),
		/**
		 * chrome Browser
		 */
		CHROME(1),
		/**
		 * IE Browser
		 */
		IE(2),
		/**
		 * maxthon Browser
		 */
		MAXTHON(3),
		/**
		 * TT  Browser
		 */
		TT(4),
		/**
		 * Green Browser
		 */
		Green(5),
		/**
		 * The World Browser
		 */
		World(6),
		/**
		 * Avant Browser
		 */
		Avant(6),
		/**
		 * 360 Browser
		 */
		THREESIXZERO(6),
		/**
		 * sogu Browser
		 */
		SUGO(7);

		private final int value;

		Browser(final int value) {
			this.value = value;
		}

		/**
		 * 通过值匹配获得枚举方法
		 *
		 * @param index 值
		 * @return enum Browser
		 */
		@Contract(pure = true) public static Browser getEnum(int index) {
			for (Browser sexEnum : Browser.values()) {
				if (sexEnum.value == index) {
					return sexEnum;
				}
			}
			return CHROME;
		}

		/**
		 * 获得 枚举方法的值
		 *
		 * @return value
		 */
		@Contract(pure = true) public final int getValue() {
			return value;
		}
	}

}