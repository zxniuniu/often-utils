package com.png.download.Pixiv;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpStatus;
import org.haic.often.FilesUtils;
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

public class Pixiv_LabelImages {

	private static final int MAX_THREADS = App.MAX_THREADS; // 多线程
	private static final int MILLISECONDS_SLEEP = App.MILLISECONDS_SLEEP; // 程序等待
	private static final int api_maxthreads = App.pixiv_api_maxthreads; // API线程

	private static final String proxyHost = App.proxyHost;
	private static final int proxyPort = App.proxyPort;

	private static final boolean bypass_blacklabels = App.pixiv_bypass_blacklabels; // 跳过黑名单标签
	private static final boolean record_usedid = App.pixiv_record_usedid; // 记录已下载的图片ID
	private static final boolean bypass_usedid = App.pixiv_bypass_usedid; // 跳过已记录的图片ID

	private static final String pixiv_url = App.pixiv_url;
	private static final String image_folderPath = FilesUtils.GetAbsolutePath(App.pixiv_image_folderPath);
	private static final String whitelabels_filePath = App.pixiv_whitelabels_filePath;
	private static final String blacklabels_filePath = App.pixiv_blacklabels_filePath;
	private static final String already_usedid_filePath = App.pixiv_already_usedid_filePath;

	public static final Map<String, String> cookies = App.pixiv_cookies;

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
			execute_program(whitelabel);
		}
		System.out.println("下载 Pixiv 标签图片 已完成 存储路径: " + image_folderPath);
	}

	private static void execute_program(String whitelabel) {
		System.out.println("正在下载 Pixiv 标签白名单图片,当前标签: " + whitelabel + " 存储路径: " + image_folderPath);
		Set<ThreeTuple<String, List<String>, String>> imagesInfo = get_label_infos(whitelabel);
		ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREADS);
		for (ThreeTuple<String, List<String>, String> imageInfo : imagesInfo) {
			executorService.submit(new ParameterizedThread<>(imageInfo, (info) -> { // 执行多线程程
				download_imagefile(info.first, info.second, info.third);
			}));
		}
		MultiThreadUtils.WaitForEnd(executorService); // 等待线程结束
	}

	private static Set<ThreeTuple<String, List<String>, String>> get_label_infos(String whitelabel) {
		String label_api_url = pixiv_url + "ajax/search/artworks/" + whitelabel + "?mode=all&s_mode=s_tag_full&type=illust";
		Document document = JsoupUtils.connect(label_api_url).timeout(10000).proxy(proxyHost, proxyPort).cookies(cookies).retry(10, MILLISECONDS_SLEEP).errorExit(true).GetDocument();
		Set<ThreeTuple<String, List<String>, String>> imagesInfo = get_doc_postinfos(document);
		int total = get_label_total(document); // 获取图片数量
		ExecutorService executorService = Executors.newFixedThreadPool(api_maxthreads); // 限制多线程
		for (int i = 2; i <= (int) Math.ceil((double) total / (double) 60); i++) {
			executorService.submit(new ParameterizedThread<>(i, (index) -> { // 执行多线程程
				imagesInfo.addAll(get_labelurl_infos(pixiv_url + "ajax/search/artworks/" + whitelabel + "?mode=all&s_mode=s_tag_full&type=illust&p=" + index));
			}));
		}
		MultiThreadUtils.WaitForEnd(executorService); // 等待线程结束
		return imagesInfo;
	}

	private static Set<ThreeTuple<String, List<String>, String>> get_labelurl_infos(String label_api_url) {
		return get_doc_postinfos(JsoupUtils.connect(label_api_url).timeout(10000).proxy(proxyHost, proxyPort).cookies(cookies).retry(10, MILLISECONDS_SLEEP).errorExit(true).GetDocument());
	}

	private static int get_label_total(Document document) {
		JSONObject jsonObject = JSONObject.parseObject(document.text());
		JSONObject body_jsonObject = JSONObject.parseObject(jsonObject.getString("body"));
		JSONObject illustManga_jsonObject = JSONObject.parseObject(body_jsonObject.getString("illustManga"));
		return Integer.parseInt(illustManga_jsonObject.getString("total"));
	}

	private static Set<ThreeTuple<String, List<String>, String>> get_doc_postinfos(Document document) {
		Set<ThreeTuple<String, List<String>, String>> imagesInfo = new HashSet<>();
		JSONObject jsonObject = JSONObject.parseObject(document.text());
		JSONObject body_jsonObject = JSONObject.parseObject(jsonObject.getString("body"));
		JSONObject illustManga_jsonObject = JSONObject.parseObject(body_jsonObject.getString("illustManga"));
		JSONArray data_jsonArray = illustManga_jsonObject.getJSONArray("data");// 获取数组
		for_data_jsonArray: for (int i = 0; i < data_jsonArray.size(); i++) {
			JSONObject data_jsonObject = data_jsonArray.getJSONObject(i);
			String illust_type = data_jsonObject.getString("illustType");
			if (!illust_type.equals("0")) { // illust_type: 1-漫画 2-动图
				continue;
			}
			String imageid = String.valueOf(data_jsonObject.get("id"));
			if (bypass_usedid && usedid_lists.contains(imageid)) {
				continue;
			}
			String[] imagelabels = String.valueOf(data_jsonObject.get("tags")).replaceAll("[\\[\"\\]]", "").split(",");
			StringBuilder filename = new StringBuilder("Pixiv " + imageid);
			for (String imagelabel : imagelabels) {
				if (filename.length() + imagelabel.length() < 200) {
					filename.append(" ").append(imagelabel);
				}
				if (bypass_blacklabels && blacklabel_lists.contains(imagelabel)) {
					continue for_data_jsonArray;
				}
			}
			String preview_imagefile_url = String.valueOf(data_jsonObject.get("url"));
			Pattern date_pattern = Pattern.compile("\\d+/\\d+/\\d+/\\d+/\\d+/\\d+/");
			Matcher date_matcher = date_pattern.matcher(preview_imagefile_url);
			String image_date = null;
			if (date_matcher.find()) {
				image_date = date_matcher.group();
			}
			int page_count = Integer.parseInt(String.valueOf(data_jsonObject.get("pageCount")));
			String imagefile_headurl = "https://i.pximg.net/img-original/img/";
			List<String> fileurl_lists = new ArrayList<>(); // 文件URL,无后缀
			for (int count = 0; count < page_count; count++) {
				String url_filename = imageid + "_p" + count;
				String imagefile_url = imagefile_headurl + image_date + url_filename;
				fileurl_lists.add(imagefile_url);
			}
			imagesInfo.add(TupleUtil.Tuple(imageid, fileurl_lists, filename.toString()));
		}
		return imagesInfo;
	}

	private static void download_imagefile(String imageid, List<String> fileurl_lists, String filename) {
		String imageid_url = pixiv_url + "artworks/" + imageid;
		usedid_lists.add(imageid);
		StringBuilder filenameBuilder = new StringBuilder(filename);
		for_fileurl_lists: for (String imagefile_url : fileurl_lists) {
			String suffix = ".jpg";
			if (fileurl_lists.size() > 1) {
				int index = fileurl_lists.indexOf(imagefile_url) + 1;
				if (index > 1) {
					filenameBuilder.append(" ").append(index);
				}
				System.out.println("正在下载 ID: " + imageid + " URL: " + imageid_url + " " + index + "/" + fileurl_lists.size());
			} else {
				System.out.println("正在下载 ID: " + imageid + " URL: " + imageid_url);
			}
			String new_filename = filenameBuilder + suffix;
			String new_imagefile_url = imagefile_url + suffix;
			while (true) {
				int statusCode = NetworkFileUtils.connect(new_imagefile_url).proxy(proxyHost, proxyPort).referrer(imageid_url).filename(new_filename).download(image_folderPath);
				if (statusCode == HttpStatus.SC_NOT_FOUND && suffix.equals(".jpg")) {
					suffix = ".png";
					new_imagefile_url = imagefile_url + suffix;
				} else if (statusCode == HttpStatus.SC_NOT_FOUND) {
					break for_fileurl_lists;
				} else if (statusCode == HttpStatus.SC_OK) {
					continue for_fileurl_lists;
				}
				MultiThreadUtils.WaitForThread(MILLISECONDS_SLEEP);
			}
		}
		if (record_usedid) {
			ChildRout.WriteFileInfo(imageid, already_usedid_filePath);
		}
	}
}
