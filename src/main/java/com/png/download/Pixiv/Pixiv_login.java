package com.png.download.Pixiv;

import java.io.File;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.haic.often.Judge;
import org.haic.often.RunCmd;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import com.png.download.App;
import com.png.download.ChildRout;

public class Pixiv_login {

	private static final String pixiv_url = App.pixiv_url;

	private static final String cookies_filePath = App.pixiv_cookies_filePath;

	private static final boolean employ_cookies = App.pixiv_employ_cookies; // 使用cookies

	public static Map<String, String> GetCookies() {
		Map<String, String> cookies = new HashMap<>();
		if (employ_cookies) {
			cookies = ChildRout.GetFileInfo(cookies_filePath).parallelStream().collect(Collectors.toMap(list -> list.split("=")[0], list -> list.split("=")[1]));
			if (cookies.isEmpty()) {
				cookies = GetLoginCookies();
				ChildRout.WriteFileInfo(cookies.toString().replaceAll("[{ }]", "").replaceAll(",", "\n"), cookies_filePath);
			}
		}
		return cookies;
	}

	private static Map<String, String> GetLoginCookies() {
		// 关闭日志
		System.setProperty("webdriver.chrome.silentOutput", "true");
		java.util.logging.Logger.getLogger("org.openqa.selenium").setLevel(Level.OFF);

		// 浏览器配置
		String browserPath = "D:/Twinkstar Browser/twinkstar.exe";// Chrome浏览器路径
		String userdataPath = "C:/Users/HHC/AppData/Local/Twinkstar/User Data";
		ChromeOptions options = new ChromeOptions();
		options.setBinary(browserPath); // 指定Chrome浏览器路径
		options.addArguments("--window-size=0,0");
		options.addArguments("blink-settings=imagesEnabled=false"); // 禁止加载图片
		options.addArguments("--user-data-dir=" + userdataPath); // 用户数据文件夹
		options.addArguments("--disable-javascript"); // 禁用JavaScript
		options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation")); // 关闭被控制提示
		RunCmd.readInfo("cmd /c taskkill /f /t /im " + new File(browserPath).getName()); // 关闭所有浏览器
		WebDriver webdriver = new ChromeDriver(options); // Chrome浏览器
		Duration duration = Duration.ofSeconds(5);
		webdriver.manage().timeouts().pageLoadTimeout(duration);
		try {
			webdriver.get(pixiv_url);
		} catch (Exception e) {
			// e.printStackTrace();
		}
		Map<String, String> cookies = webdriver.manage().getCookies().parallelStream().filter(cookie -> !Judge.isEmpty(cookie.getValue())).collect(Collectors.toMap(Cookie::getName, Cookie::getValue));
		webdriver.quit(); // 关闭全部窗口
		return cookies;
	}
}
