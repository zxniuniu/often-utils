package com.png.download.Sankaku;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.haic.often.FilesUtils;
import org.haic.often.Judge;
import org.haic.often.Multithread.MultiThreadUtils;
import org.haic.often.Multithread.ParameterizedThread;
import org.haic.often.Network.JsoupUtils;
import org.haic.often.Network.NetworkFileUtils;
import org.haic.often.Tuple.ThreeTuple;
import org.haic.often.Tuple.TupleUtil;
import org.jsoup.nodes.Document;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.png.download.App;
import com.png.download.ChildRout;

public class Sankaku_LabelImages {

	private static final String sankaku_url = App.sankaku_url;
	private static final String sankaku_api_url = "https://capi-v2.sankakucomplex.com/posts/keyset";

	private static final String image_folderPath = FilesUtils.GetAbsolutePath(App.sankaku_image_folderPath);
	private static final String whitelabels_filePath = App.sankaku_whitelabels_filePath;
	private static final String blacklabels_filePath = App.sankaku_blacklabels_filePath;
	private static final String already_usedid_filePath = App.sankaku_already_usedid_filePath;

	private static final boolean record_usedid = App.sankaku_record_usedid; // 记录已下载的图片ID
	private static final boolean bypass_usedid = App.sankaku_bypass_usedid; // 跳过已记录的图片ID
	private static final boolean bypass_blacklabels = App.sankaku_bypass_blacklabels;

	private static final String proxyHost = App.proxyHost; // 代理
	private static final int proxyPort = App.proxyPort;

	private static final int MAX_THREADS = App.MAX_THREADS; // 多线程
	private static final int MILLISECONDS_SLEEP = App.MILLISECONDS_SLEEP; // 重试间隔时间
	private static final int limit = App.sankaku_api_limit; // API单页获取数量限制

	private static final Map<String, String> cookies = App.sankaku_cookies;

	private static List<String> blacklabel_lists = new ArrayList<>();
	private static List<String> usedid_lists = new CopyOnWriteArrayList<>();

	public static void ImagesDownload() {
		List<String> whitelabel_lists = ChildRout.GetFileInfo(whitelabels_filePath);
		whitelabel_lists.replaceAll(LabelWhite -> LabelWhite.replaceAll(" ", "_"));
		if (whitelabel_lists.isEmpty()) {
			System.out.println("File: " + whitelabel_lists + " is null");
			return;
		}
		blacklabel_lists = ChildRout.GetFileInfo(blacklabels_filePath);
		blacklabel_lists.replaceAll(LabelBlack -> LabelBlack.replaceAll(" ", "_"));
		for (String whitelabel : whitelabel_lists) {
			if (blacklabel_lists.contains(whitelabel)) {
				System.out.println("标签冲突,白名单和黑名单存在相同值: " + whitelabel);
				blacklabel_lists.remove(whitelabel);
			}
		}
		usedid_lists = ChildRout.GetFileInfo(already_usedid_filePath);
		for (String whitelabel : whitelabel_lists) {
			executeProgram(whitelabel);
		}
		System.out.println("下载 Sankaku 标签图片 已完成 存储路径: " + image_folderPath);
	}

	private static void executeProgram(String whitelabel) {
		System.out.println("正在下载 Sankaku 标签白名单图片,当前标签: " + whitelabel + " 存储路径: " + image_folderPath);
		Set<ThreeTuple<String, String, String>> imagesInfo = GetLabelInfo(whitelabel);
		ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREADS);
		for (ThreeTuple<String, String, String> imageInfo : imagesInfo) {
			executorService.submit(new ParameterizedThread<>(imageInfo, (info) -> { // 执行多线程程
				download(info.first, info.second, info.third);
			}));
		}
		MultiThreadUtils.WaitForEnd(executorService); // 等待线程结束
	}

	private static String GetImageUrl(String imageid) {
		Document labelurl_doc = JsoupUtils.connect(sankaku_url + "cn/post/show/" + imageid).timeout(12000).proxy(proxyHost, proxyPort).cookies(cookies).retry(10, MILLISECONDS_SLEEP).errorExit(true)
				.GetDocument();
		return "https:" + labelurl_doc.selectFirst("a[id='image-link']").attr("href");
	}

	private static Set<ThreeTuple<String, String, String>> GetLabelInfo(String whitelabel) {
		return GetLabelInfo(whitelabel, sankaku_api_url + "?tags=" + whitelabel + "&limit=" + limit);
	}

	private static Set<ThreeTuple<String, String, String>> GetLabelInfo(String whitelabel, String label_api_url) {
		Set<ThreeTuple<String, String, String>> imagesInfo = new HashSet<>();
		Document document = JsoupUtils.connect(label_api_url).timeout(12000).proxy(proxyHost, proxyPort).cookies(cookies).retry(10, MILLISECONDS_SLEEP).errorExit(true).GetDocument();
		JSONObject jsonObject = JSONObject.parseObject(document.text());
		String next = JSONObject.parseObject(jsonObject.getString("meta")).getString("next");
		if (next != null) {
			String next_label_api_url = sankaku_api_url + "?tags=" + whitelabel + "&limit=" + limit + "&next=" + next;
			imagesInfo.addAll(GetLabelInfo(whitelabel, next_label_api_url));
		}
		JSONArray jsonArray = jsonObject.getJSONArray("data");// 获取数组
		for_jsonArray: for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject data_jsonObject = jsonArray.getJSONObject(i);
			String imageid = data_jsonObject.getString("id");
			if (bypass_usedid && usedid_lists.contains(imageid)) {
				continue;
			}
			JSONArray tags_jsonArray = data_jsonObject.getJSONArray("tags");// 获取数组
			StringBuilder filename = new StringBuilder("Sankaku " + imageid);
			for (int j = 0; j < tags_jsonArray.size(); j++) {
				JSONObject tags_jsonObject = tags_jsonArray.getJSONObject(j);
				String label = tags_jsonObject.getString("name");
				if (bypass_blacklabels && blacklabel_lists.contains(label)) {
					continue for_jsonArray;
				}
				int type = Integer.parseInt(tags_jsonObject.getString("type"));
				if (type < 6) {
					if (filename.length() + label.length() < 200) {
						filename.append(" ").append(label);
					} else {
						break;
					}
				}
			}
			String file_type = data_jsonObject.getString("file_type");
			file_type = "." + file_type.substring(file_type.lastIndexOf("/") + 1);
			if (file_type.equals("jpeg")) {
				file_type = "jpg";
			}
			String suffix = "." + file_type;
			filename.append(" ").append(suffix);
			String imagefile_url = data_jsonObject.getString("file_url");
			imagesInfo.add(TupleUtil.Tuple(imageid, imagefile_url, filename.toString()));
		}
		return imagesInfo;
	}

	private static void download(String imageid, String imageUrl, String filename) {
		String imageidUrl = sankaku_url + "cn/post/show/" + imageid;
		System.out.println("正在下载 ID: " + imageid + " URL: " + imageidUrl);
		usedid_lists.add(imageid);
		imageUrl = Judge.isEmpty(imageUrl) ? GetImageUrl(imageid) : imageUrl;
		NetworkFileUtils.connect(imageUrl).proxy(proxyHost, proxyPort).filename(filename).retry(true, MILLISECONDS_SLEEP).download(image_folderPath);
		App.imageCount.addAndGet(1);
		if (record_usedid && !usedid_lists.contains(imageid)) {
			ChildRout.WriteFileInfo(imageid, already_usedid_filePath);
		}
	}

}
