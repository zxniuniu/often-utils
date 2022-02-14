package org.haic.often.Netdisc;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.haic.often.Network.HtmlUnitUtils;
import org.haic.often.Network.HttpsUtils;
import org.haic.often.Network.JsoupUtils;
import org.haic.often.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 蓝奏云盘API
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/1/18 23:59
 */
public class LanZouYunPan {

	public static final String domain = "https://www.lanzoui.com/";
	public static final String downApi = domain + "ajaxm.php";

	/**
	 * 获取分享页面文件信息集合
	 *
	 * @param lanzouUrl 蓝奏URL
	 * @param passwd    访问密码
	 * @return Map - 文件名, 文件ID链接
	 */
	public static Map<String, String> getPageInfos(@NotNull String lanzouUrl, @NotNull String passwd) {
		String infos = Objects.requireNonNull(JsoupUtils.connect(lanzouUrl).get().selectFirst("body script")).toString();
		infos = infos.substring(32, infos.indexOf("json") - 20).replaceAll("\t*　* *'*;*", "");

		// 获取post参数
		Map<String, String> params = new HashMap<>();
		for (String data : StringUtils.extractRegex(infos.replaceAll("\n", ""), "data[\\s\\S]*pwd").substring(6).split(",")) {
			String[] entry = data.split(":");
			params.put(entry[0], entry[1]);
		}

		// 获取修正后的参数
		String pgs = StringUtils.extractRegex(infos, "pgs=.*");
		pgs = pgs.substring(pgs.indexOf("=") + 1);
		String t = StringUtils.extractRegex(infos, params.get("t") + "=.*");
		t = t.substring(t.indexOf("=") + 1);
		String k = StringUtils.extractRegex(infos, params.get("k") + "=.*");
		k = k.substring(k.indexOf("=") + 1);

		// 修正post参数
		params.put("pg", pgs);
		params.put("t", t);
		params.put("k", k);
		params.put("pwd", passwd);

		// 处理json数据
		JSONArray jsonArray = JSONObject.parseObject(JsoupUtils.connect(domain + "filemoreajax.php").data(params).post().text()).getJSONArray("text");
		Map<String, String> result = new HashMap<>();
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject info = jsonArray.getJSONObject(i);
			result.put(info.getString("name_all"), domain + info.getString("id"));
		}
		return result;
	}

	/**
	 * 获取分享页面文件直链集合
	 *
	 * @param lanzouUrl 蓝奏URL
	 * @param passwd    访问密码
	 * @return 文件直链集合
	 */
	@NotNull @Contract(pure = true) public static Map<String, String> getPageStraights(@NotNull String lanzouUrl, @NotNull String passwd) {
		return getPageInfos(lanzouUrl, passwd).entrySet().parallelStream().collect(Collectors.toMap(Map.Entry::getKey, f -> getStraight(f.getValue())));
	}

	/**
	 * 获取分享页面文件直链集合
	 *
	 * @param lanzouUrl 蓝奏URL
	 * @return 文件直链集合
	 */
	@NotNull @Contract(pure = true) public static Map<String, String> getPageInfos(@NotNull String lanzouUrl) {
		Map<String, String> result = new HashMap<>();
		HtmlUnitUtils.connect(lanzouUrl).waitJSTime(1000).get().select("div[id='name']")
				.forEach(name -> result.put(name.text(), domain + name.select("a").attr("href")));
		return result;
	}

	/**
	 * 获取分享页面文件直链集合
	 *
	 * @param lanzouUrl 蓝奏URL
	 * @return 文件直链集合
	 */
	@NotNull @Contract(pure = true) public static Map<String, String> getPageStraights(@NotNull String lanzouUrl) {
		return getPageInfos(lanzouUrl).entrySet().parallelStream().collect(Collectors.toMap(Map.Entry::getKey, f -> getStraight(f.getValue())));
	}

	/**
	 * 获取蓝奏云URL直链
	 *
	 * @param lanzouUrl 蓝奏云文件链接
	 * @return 蓝奏云URL直链
	 */
	@Contract(pure = true) public static String getStraight(@NotNull String lanzouUrl) {
		String downUrl = domain + Objects.requireNonNull(JsoupUtils.connect(lanzouUrl).get().selectFirst("iframe[class='ifr2']")).attr("src");
		String infos = Objects.requireNonNull(JsoupUtils.connect(downUrl).get().selectFirst("body script")).toString();
		infos = infos.substring(32, infos.indexOf("json") - 17).replaceAll("\t*　* *'*;*", "");

		// 获取post参数
		Map<String, String> params = new HashMap<>();
		for (String data : StringUtils.extractRegex(infos.replaceAll("\n", ""), "data:[\\s\\S]*websignkey").substring(6).split(",")) {
			String[] entry = data.split(":");
			params.put(entry[0], entry[1]);
		}

		// 获取修正后的参数
		String signs = StringUtils.extractRegex(infos, params.get("signs") + "=.*");
		signs = signs.substring(signs.indexOf("=") + 1);
		String websign = StringUtils.extractRegex(infos, params.get("websign") + "=.*");
		websign = websign.substring(websign.indexOf("=") + 1);
		String websignkey = StringUtils.extractRegex(infos, params.get("websignkey") + "=.*");
		websignkey = websignkey.substring(websignkey.indexOf("=") + 1);

		// 修正post参数
		params.put("signs", signs);
		params.put("websign", websign);
		params.put("websignkey", websignkey);

		// 处理json数据
		JSONObject jsonbject = JSONObject.parseObject(JsoupUtils.connect(downApi).referrer(downUrl).data(params).post().text());
		String domain = jsonbject.getString("dom");
		String url = jsonbject.getString("url");

		return JsoupUtils.connect(domain + "/file/" + url).execute().url().toString();
	}

	/**
	 * 获取蓝奏云URL直链
	 *
	 * @param lanzouUrl 蓝奏云文件链接
	 * @param password  提取码
	 * @return 蓝奏云URL直链
	 */
	@Contract(pure = true) public static String getStraight(@NotNull String lanzouUrl, String password) {
		return JSONObject.parseObject(
				HttpsUtils.connect(downApi).params(StringUtils.extractRegex(JsoupUtils.connect(lanzouUrl).get().toString(), "action=.*&p=") + password).post()
						.text()).getString("url");

	}

}