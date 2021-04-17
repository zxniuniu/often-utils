package com.png.download.Pixiv;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
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

public class Pixiv_PopularDailyImages {

	private static final int MAX_THREADS = App.MAX_THREADS; // 多线程
	private static final int MILLISECONDS_SLEEP = App.MILLISECONDS_SLEEP; // 程序等待
	private static final int api_maxthreads = App.pixiv_api_maxthreads; // API线程

	private static final String start_date = App.pixiv_start_date;

	private static final String proxyHost = App.proxyHost;
	private static final int proxyPort = App.proxyPort;

	private static final boolean bypass_blacklabels = App.pixiv_bypass_blacklabels; // 跳过黑名单标签
	private static final boolean record_usedid = App.pixiv_record_usedid; // 记录已下载的图片ID
	private static final boolean bypass_usedid = App.pixiv_bypass_usedid; // 跳过已记录的图片ID
	private static final boolean record_date = App.pixiv_record_date; // 记录已完成的日期
	private static final boolean bypass_record_date = App.pixiv_bypass_record_date; // 跳过记录的日期
	private static final boolean unbypass_within_aweek = App.pixiv_unbypass_within_aweek; // 不跳过一星期内的日期

	private static final String pixiv_url = App.pixiv_url;
	private static final String image_folderPath = FilesUtils.GetAbsolutePath(App.pixiv_image_folderPath);
	private static final String blacklabels_filePath = App.pixiv_blacklabels_filePath;
	private static final String already_usedid_filePath = App.pixiv_already_usedid_filePath;
	private static final String record_date_filePath = App.pixiv_record_date_filePath;

	public static final Map<String, String> cookies = App.pixiv_cookies;

	private static List<String> blacklabel_lists = new ArrayList<>();
	private static List<String> usedid_lists = new CopyOnWriteArrayList<>();

	public static void ImagesDownload() {
		List<String> record_date_lists = ChildRout.GetFileInfo(record_date_filePath);
		usedid_lists = ChildRout.GetFileInfo(already_usedid_filePath);
		blacklabel_lists = ChildRout.GetFileInfo(blacklabels_filePath);
		blacklabel_lists.replaceAll(LabelBlack -> LabelBlack.replaceAll(" ", "_"));
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate system_date = LocalDate.now();
		LocalDate current_date = LocalDate.parse(start_date);
		LocalDate within_aweek_date = null;
		if (unbypass_within_aweek) {
			within_aweek_date = system_date.minusDays(7);
		}
		while (current_date.isBefore(system_date)) {
			String current_date_str = current_date.format(format);
			if (bypass_record_date && record_date_lists.contains(current_date_str)) {
				if (!unbypass_within_aweek || current_date.isBefore(Objects.requireNonNull(within_aweek_date))) {
					current_date = current_date.plusDays(1);
					continue;
				}
			}
			System.out.println("正在下载 Pixiv 每日热门图片,当前日期 : " + current_date_str + " 存储路径: " + image_folderPath);
			execute_program(current_date_str.replaceAll("-", ""));
			if (record_date && !record_date_lists.contains(current_date_str)) {
				ChildRout.WriteFileInfo(current_date_str, record_date_filePath);
				record_date_lists.add(current_date_str);
			}
			current_date = current_date.plusDays(1);
		}
		System.out.println("下载 Pixiv 每日热门图片 已完成 存储路径: " + image_folderPath);
	}

	private static void execute_program(String current_date) {
		Set<ThreeTuple<String, List<String>, String>> imagesInfo = get_heatday_imagesInfo(current_date);
		ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREADS); // 限制多线程
		for (ThreeTuple<String, List<String>, String> imageInfo : imagesInfo) {
			executorService.submit(new ParameterizedThread<>(imageInfo, (info) -> { // 执行多线程程
				download_imagefile(info.first, info.second, info.third);
			}));
		}
		MultiThreadUtils.WaitForEnd(executorService);
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

	private static Set<ThreeTuple<String, List<String>, String>> get_heatday_imagesInfo(String current_date) {
		Set<ThreeTuple<String, List<String>, String>> imagesInfo = new CopyOnWriteArraySet<>();
		List<String> heatday_url_lists = new ArrayList<>();
		for (int i = 1; i <= 2; i++) {
			String heatday_url = pixiv_url + "ranking.php?mode=daily_r18&format=json&date=" + current_date + "&p=" + i;
			heatday_url_lists.add(heatday_url);
		}
		for (int i = 1; i <= 10; i++) {
			String heatday_url = pixiv_url + "ranking.php?mode=daily&format=json&date=" + current_date + "&p=" + i;
			heatday_url_lists.add(heatday_url);
		}
		ExecutorService executorService = Executors.newFixedThreadPool(api_maxthreads); // 限制多线程
		for (String heatday_url : heatday_url_lists) { // 执行多线程程
			executorService.submit(new Thread(() -> { // 程序
				imagesInfo.addAll(get_url_imagesInfo(heatday_url)); // 获取文件链接,无后缀
			}));
		}
		MultiThreadUtils.WaitForEnd(executorService);
		return new HashSet<>(imagesInfo);
	}

	private static Set<ThreeTuple<String, List<String>, String>> get_url_imagesInfo(String heatday_url) {
		return get_doc_postinfos(JsoupUtils.connect(heatday_url).timeout(10000).cookies(cookies).proxy(proxyHost, proxyPort).retry(10, MILLISECONDS_SLEEP).errorExit(true).GetDocument());
	}

	private static Set<ThreeTuple<String, List<String>, String>> get_doc_postinfos(Document document) {
		Set<ThreeTuple<String, List<String>, String>> imagesInfo = new HashSet<>();
		JSONObject jsonObject = JSONObject.parseObject(document.text());
		JSONArray jsonArray = jsonObject.getJSONArray("contents");// 获取数组
		for_jsonArray: for (int i = 0; i < jsonArray.size(); i++) { // 提取出family中的所有
			JSONObject image_jsonObject = jsonArray.getJSONObject(i);
			String illust_type = image_jsonObject.getString("illust_type");
			if (!illust_type.equals("0")) { // illust_type: 1-漫画 2-动图
				continue;
			}
			List<String> fileurl_lists = new ArrayList<>();
			String imageid = String.valueOf(image_jsonObject.get("illust_id"));
			if (bypass_usedid && usedid_lists.contains(imageid)) {
				continue;
			}
			String[] imagelabels = String.valueOf(image_jsonObject.get("tags")).replaceAll("[\\[\"\\]]", "").split(",");
			StringBuilder filename = new StringBuilder("Pixiv " + imageid);
			for (String imagelabel : imagelabels) {
				if (filename.length() + imagelabel.length() < 200) {
					filename.append(" ").append(imagelabel);
				}
				if (bypass_blacklabels && blacklabel_lists.contains(imagelabel)) {
					continue for_jsonArray;
				}
			}
			String preview_imagefile_url = String.valueOf(image_jsonObject.get("url"));
			Pattern date_pattern = Pattern.compile("\\d+/\\d+/\\d+/\\d+/\\d+/\\d+/");
			Matcher date_matcher = date_pattern.matcher(preview_imagefile_url);
			String image_date = null;
			if (date_matcher.find()) {
				image_date = date_matcher.group();
			}
			int page_count = Integer.parseInt(String.valueOf(image_jsonObject.get("illust_page_count")));
			String imagefile_headurl = "https://i.pximg.net/img-original/img/";
			for (int count = 0; count < page_count; count++) {
				String url_filename = imageid + "_p" + count;
				String imagefile_url = imagefile_headurl + image_date + url_filename;
				fileurl_lists.add(imagefile_url);
			}
			imagesInfo.add(TupleUtil.Tuple(imageid, fileurl_lists, filename.toString()));
		}
		return imagesInfo;
	}
}
