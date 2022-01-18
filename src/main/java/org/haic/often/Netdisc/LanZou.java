package org.haic.often.Netdisc;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.haic.often.Judge;
import org.haic.often.Network.HtmlUnitUtils;
import org.haic.often.Network.JsoupUtils;
import org.haic.often.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Connection;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author haicdust
 * @version 1.0
 * @since 2022/1/18 23:59
 */
public class LanZou {

	/**
	 * 获取页面文件信息集合
	 *
	 * @param lanzurl 蓝奏URL
	 * @param passwd  访问密码
	 * @return 文件信息集合
	 */
	@NotNull @Contract(pure = true) public static Map<String, String> lanzouPageInfos(String lanzurl, String passwd) {
		String javascript = null;
		while (Judge.isNull(javascript)) {
			Elements elements = JsoupUtils.connect(lanzurl).get().select("script[type='text/javascript']");
			javascript = elements.isEmpty() ? null : String.valueOf(elements.get(1));
		}
		String infos = javascript.substring(154, javascript.indexOf("隐藏") - 60).replaceAll("'*", "");
		// 获取post参数
		Map<String, String> params = new HashMap<>();
		for (String data : StringUtils.extractRegex(infos.replaceAll("\t*\n*", ""), "data[\\s\\S]*pwd").substring(9).split(",")) {
			String[] entry = data.split(":");
			params.put(entry[0], entry[1]);
		}
		// 获取修正后的参数
		String pgs = StringUtils.extractRegex(infos, "pgs =.*;").replaceAll(" ", "");
		pgs = pgs.substring(pgs.indexOf("=") + 1, pgs.length() - 1);
		String t = StringUtils.extractRegex(infos, params.get("t") + " =.*;").replaceAll(" ", "");
		t = t.substring(t.indexOf("=") + 1, t.length() - 1);
		String k = StringUtils.extractRegex(infos, params.get("k") + " =.*;").replaceAll(" ", "");
		k = k.substring(k.indexOf("=") + 1, k.length() - 1);
		// 修正post参数
		params.put("pg", pgs);
		params.put("t", t);
		params.put("k", k);
		params.put("pwd", passwd);
		// 处理json数据
		String lanzouUrl = "https://wws.lanzoux.com/";
		JSONArray jsonArray = null;
		while (Judge.isNull(jsonArray)) {
			try {
				jsonArray = JSONObject.parseObject(JsoupUtils.connect(lanzouUrl + "filemoreajax.php").data(params).execute(Connection.Method.POST).body())
						.getJSONArray("text");
			} catch (Exception e) {
				// e.printStackTrace();
			}
		}
		Map<String, String> result = new HashMap<>();
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject info = jsonArray.getJSONObject(i);
			result.put(info.getString("name_all"), lanzouUrl + info.getString("id"));
		}
		return result;
	}

	/**
	 * 获取蓝奏云URL直链
	 *
	 * @param lanzouUrl 蓝奏云文件链接
	 * @return 蓝奏云URL直链
	 */
	@Contract(pure = true) public static String lanzouStraight(final @NotNull String lanzouUrl) {
		return JsoupUtils.connect(Objects.requireNonNull(HtmlUnitUtils.connect(
						"https://wws.lanzoux.com" + Objects.requireNonNull(JsoupUtils.connect(lanzouUrl).get().selectFirst("iframe[class='ifr2']")).attr("src"))
				.waitJSTime(1000).get().selectFirst("div[id='go'] a")).attr("href")).followRedirects(false).execute().header("Location");
	}

	/**
	 * 获取页面文件信息集合
	 *
	 * @param lanzurl 蓝奏URL
	 * @return 文件信息集合
	 */
	@NotNull @Contract(pure = true) public static Map<String, String> lanzouPageInfos(String lanzurl) {
		Map<String, String> result = new HashMap<>();
		HtmlUnitUtils.connect(lanzurl).waitJSTime(1000).get().select("div[id='name']")
				.forEach(name -> result.put(name.text(), "https://wws.lanzoux.com" + name.select("a").attr("href")));
		return result;
	}
}