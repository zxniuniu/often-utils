package org.haic.often.Netdisc;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.haic.often.Network.JsoupUtils;
import org.jsoup.nodes.Document;

import java.util.HashMap;
import java.util.Map;

/**
 * 阿里云盘API
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/1/22 9:50
 */
public class ALiYunPan {

	private static final String aliDataApi = "https://api.aliyundrive.com/adrive/v3/share_link/get_share_by_anonymous";
	private static final String alidownApi = "https://api.aliyundrive.com/v2/file/get_share_link_download_url";
	// private static final String aliFileListApi = "https://api.aliyundrive.com/adrive/v3/file/list";
	private static final String shareTokenApi = "https://api.aliyundrive.com/v2/share_link/get_share_token";

	/**
	 * 获得分享页面所有文件直链(方法暂时废弃,阿里云盘限制,分享页面获取链接无用,需保存至个人盘内才能获取直链)
	 *
	 * @param shareUrl      sharePwd
	 * @param authorization 身份识别信息,登录后,可在开发者本地存储(Local Storage)获取token项access_token值,或者在网络请求头中查找
	 * @return Map - 文件名, 文件直链
	 */
	public static Map<String, String> getPageStraights(String shareUrl, String authorization) {
		return getPageStraights(shareUrl, "", authorization);
	}

	/**
	 * 获得分享页面所有文件直链(方法暂时废弃,阿里云盘限制,分享页面获取链接无用,需保存至个人盘内才能获取直链)
	 *
	 * @param shareUrl      分享链接
	 * @param sharePwd      提取码
	 * @param authorization 身份识别信息,登录后,可在开发者本地存储(Local Storage)获取token项access_token值,或者在网络请求头中查找
	 * @return Map - 文件名, 文件直链
	 */
	public static Map<String, String> getPageStraights(String shareUrl, String sharePwd, String authorization) {
		String shareId = shareUrl.substring(shareUrl.lastIndexOf("/") + 1);
		String shareToken = getShareToken(shareId, sharePwd);
		Map<String, String> filesStraight = new HashMap<>();
		for (Map.Entry<String, String> entry : getFilesInfo(shareId).entrySet()) {
			filesStraight.put(entry.getKey(), getStraight(shareId, entry.getValue(), shareToken, authorization));
		}
		return filesStraight;
	}

	public static String getStraight(String shareId, String fileid, String shareToken, String authorization) {
		JSONObject apiJson = new JSONObject();
		apiJson.put("share_id", shareId);
		apiJson.put("file_id", fileid);
		Map<String, String> headers = new HashMap<>();
		headers.put("x-share-token", shareToken);
		headers.put("authorization", authorization.startsWith("Bearer") ? authorization : "Bearer " + authorization);
		Document doc = JsoupUtils.connect(alidownApi).headers(headers).requestBody(apiJson.toString()).post();
		return JSONObject.parseObject(doc.text()).getString("download_url");

	}

	/**
	 * 获取分享页面文件的信息
	 *
	 * @param shareId 分享链接ID
	 * @return Map - 文件名,文件ID
	 */
	public static Map<String, String> getFilesInfo(String shareId) {
		JSONObject apiJson = new JSONObject();
		apiJson.put("share_id", shareId);
		Document doc = JsoupUtils.connect(aliDataApi).requestBody(apiJson.toString()).post();
		JSONArray fileInfoArray = JSONArray.parseArray(JSONObject.parseObject(doc.text()).getString("file_infos"));
		Map<String, String> filesInfo = new HashMap<>();
		for (int i = 0; i < fileInfoArray.size(); i++) {
			JSONObject fileInfo = fileInfoArray.getJSONObject(i);
			filesInfo.put(fileInfo.getString("file_name"), fileInfo.getString("file_id"));
		}
		return filesInfo;
	}

	/**
	 * 获得ShareToken
	 *
	 * @param shareId 分享链接ID
	 * @return ShareToken
	 */
	public static String getShareToken(String shareId) {
		return getShareToken(shareId, "");

	}

	/**
	 * 获得ShareToken
	 *
	 * @param shareId  分享链接ID
	 * @param sharePwd 提取码
	 * @return ShareToken
	 */
	public static String getShareToken(String shareId, String sharePwd) {
		JSONObject apiJson = new JSONObject();
		apiJson.put("share_id", shareId);
		apiJson.put("share_pwd", sharePwd);
		Document doc = JsoupUtils.connect(shareTokenApi).requestBody(apiJson.toString()).post();
		return JSONObject.parseObject(doc.text()).getString("share_token");
	}

}