package com.png.download.Yande;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.haic.often.Network.JsoupUtils;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;

import com.png.download.App;
import com.png.download.ChildRout;

public class Yande_login {

	private static final String yande_url = App.yande_url;
	private static final String user_name = App.yande_user_name;
	private static final String user_password = App.yande_user_password;
	private static final String cookies_filePath = App.yande_cookies_filePath; // cookies文件

	private static final String proxyHost = App.proxyHost;
	private static final int proxyPort = App.proxyPort;

	private static final int MILLISECONDS_SLEEP = App.MILLISECONDS_SLEEP; // 重试间隔时间

	private static final boolean employ_cookies = App.yande_employ_cookies; // 使用cookies

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
		Map<String, String> params = new HashMap<>();
		params.put("user[name]", user_name);
		params.put("user[password]", user_password);
		String loginUrl = yande_url + "user/authenticate";
		Response response = JsoupUtils.connect(loginUrl).timeout(10000).data(params).proxy(proxyHost, proxyPort).retry(4, MILLISECONDS_SLEEP).errorExit(true).GetResponse(Method.POST);
		return response.cookies();
	}
}
