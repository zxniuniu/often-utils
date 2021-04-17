package com.png.download.Yande;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.haic.often.FilesUtils;
import org.haic.often.Judge;
import org.haic.often.URIUtils;
import org.haic.often.Multithread.MultiThreadUtils;
import org.haic.often.Network.JsoupUtils;
import org.haic.often.Network.NetworkFileUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.png.download.App;
import com.png.download.ChildRout;

/**
 * 爬取Yande.re的每日热门图片
 *
 * @author haicdust
 * @since 2021/3/11 17:13
 * @version 2.5
 */
public class Yande_PopularDailyImages {

	private static final int MAX_THREADS = App.MAX_THREADS; // 多线程下载
	private static final int MILLISECONDS_SLEEP = App.MILLISECONDS_SLEEP; // 程序等待

	private static final String start_date = App.yande_start_date; // 开始日期

	private static final boolean record_date = App.yande_record_date; // 记录已完成的日期
	private static final boolean bypass_record_date = App.yande_bypass_record_date; // 跳过记录的日期
	private static final boolean unbypass_within_aweek = App.yande_unbypass_within_aweek; // 不跳过一星期内的日期
	private static final boolean record_usedid = App.yande_record_usedid; // 记录已下载的图片ID
	private static final boolean bypass_usedid = App.yande_bypass_usedid; // 跳过已记录的图片ID
	private static final boolean bypass_blacklabels = App.yande_bypass_blacklabels; // 跳过标签黑名单

	private static final String yande_url = App.yande_url;
	private static final String image_folderPath = FilesUtils.GetAbsolutePath(App.yande_image_folderPath); // 图片文件夹
	private static final String already_usedid_filePath = App.yande_already_usedid_filePath; // 记录ID文件
	private static final String record_date_filePath = App.yande_record_date_filePath; // 日期文件
	private static final String blacklabels_filePath = App.yande_blacklabels_filePath; // 黑名单文件

	private static final String proxyHost = App.proxyHost;
	private static final int proxyPort = App.proxyPort;

	private static final int API_MAX_THREADS = App.yande_api_maxthreads; // 访问API最大线程
	private static final int limit = App.yande_api_limit; // API单页获取数量限制

	private static final Map<String, String> cookies = App.yande_cookies;

	private static List<String> usedid_lists = new CopyOnWriteArrayList<>();
	private static List<String> blacklabel_lists = new ArrayList<>();

	public static void ImagesDownload() {
		List<String> record_date_lists = ChildRout.GetFileInfo(record_date_filePath);
		usedid_lists = ChildRout.GetFileInfo(already_usedid_filePath);
		blacklabel_lists = ChildRout.GetFileInfo(blacklabels_filePath);
		blacklabel_lists.replaceAll(LabelBlack -> LabelBlack.replaceAll(" ", "_"));
		DateTimeFormatter date_format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate system_date = LocalDate.now();
		LocalDate current_date = LocalDate.parse(start_date);
		LocalDate within_aweek_date = null;
		if (unbypass_within_aweek) {
			within_aweek_date = system_date.minusDays(7);
		}
		while (!current_date.isAfter(system_date)) {
			String current_date_str = current_date.format(date_format);
			if (bypass_record_date && record_date_lists.contains(current_date_str)) {
				if (!unbypass_within_aweek || current_date.isBefore(Objects.requireNonNull(within_aweek_date))) {
					current_date = current_date.plusDays(1);
					continue;
				}
			}
			int day = current_date.getDayOfMonth();
			int month = current_date.getMonthValue();
			int year = current_date.getYear();
			System.out.println("正在下载每日热门图片,当前日期 : " + current_date_str + " 存储路径: " + image_folderPath);
			String heatday_url = yande_url + "post/popular_by_day.xml?day=" + day + "&month=" + month + "&year=" + year;
			executeProgram(heatday_url);
			if (record_date && !record_date_lists.contains(current_date_str)) {
				ChildRout.WriteFileInfo(current_date_str, record_date_filePath);
				record_date_lists.add(current_date_str);
			}
			current_date = current_date.plusDays(1);
		}
		System.out.println("Yande.re 下载 每日热门图片 已完成 存储路径: " + image_folderPath);
	}

	private static void executeProgram(String heatdayUrl) {
		Map<String, String> imageInfos = GetHeatdayImagesInfo(heatdayUrl);
		ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREADS); // 限制多线程
		for (Map.Entry<String, String> imageInfo : imageInfos.entrySet()) { // 下载
			executorService.submit(new Thread(() -> {// 执行多线程程
				download(imageInfo.getKey(), imageInfo.getValue());
			}));
		}
		MultiThreadUtils.WaitForEnd(executorService); // 等待线程结束
	}

	public static Map<String, String> GetHeatdayImagesInfo(String heatday_url) {
		Map<String, String> imagesInfo = new ConcurrentHashMap<>();
		Document document = JsoupUtils.connect(heatday_url).timeout(10000).proxy(proxyHost, proxyPort).cookies(cookies).retry(10, MILLISECONDS_SLEEP).errorExit(true).GetDocument();
		Elements posts = document.select("post");
		ExecutorService executorService = Executors.newFixedThreadPool(API_MAX_THREADS); // 限制多线程
		for (Element post : posts) { // 执行多线程程序
			executorService.submit(new Thread(() -> { // 程序
				imagesInfo.putAll(GetPostInfo(post));
			}));
		}
		MultiThreadUtils.WaitForEnd(executorService); // 等待线程结束
		return new HashMap<>(imagesInfo);
	}

	private static Map<String, String> GetPostInfo(Element post) {
		Map<String, String> imagesInfo = new HashMap<>();
		String imageid = post.attr("id");
		String[] image_labels = post.attr("tags").split(" ");
		for (String image_label : image_labels) {
			if (bypass_blacklabels && blacklabel_lists.contains(image_label)) {
				return imagesInfo;
			}
		}
		String parent_imageid = post.attr("parent_id");
		boolean has_children = Boolean.parseBoolean(post.attr("has_children"));
		if (!parent_imageid.equals("")) {
			Map<String, String> parent_imageInfos = GetParentImagesInfo(parent_imageid);
			if (!parent_imageInfos.isEmpty()) {
				imagesInfo.putAll(parent_imageInfos);
			}
			return imagesInfo;
		} else if (has_children) {
			Map<String, String> parent_imageInfos = GetParentImagesInfo(imageid);
			if (!parent_imageInfos.isEmpty()) {
				imagesInfo.putAll(parent_imageInfos);
			}
			return imagesInfo;
		}
		if (bypass_usedid && usedid_lists.contains(imageid)) {
			return imagesInfo;
		}
		String imagefile_url = post.attr("file_url");
		if (!Judge.isEmpty(imagefile_url)) {
			imagesInfo.put(imageid, imagefile_url);
		}
		return imagesInfo;
	}

	private static Map<String, String> GetParentImagesInfo(String parent_imageid) {
		return GetParentImagesInfo(parent_imageid, new ArrayList<>());
	}

	private static Map<String, String> GetParentImagesInfo(String parent_imageid, List<String> children_imageid_lists) {
		Map<String, String> imagesInfo = new HashMap<>();
		String parentid_url = yande_url + "post.xml?tags=parent%3A" + parent_imageid + "&limit=" + limit;
		Document document = JsoupUtils.connect(parentid_url).timeout(10000).proxy(proxyHost, proxyPort).cookies(cookies).retry(10, MILLISECONDS_SLEEP).errorExit(true).GetDocument();
		Elements posts = document.select("post");
		forPosts: for (Element post : posts) {
			String imageid = post.attr("id");
			if (imageid.equals(parent_imageid) && !children_imageid_lists.contains(parent_imageid)) {
				String new_parent_imageid = post.attr("parent_id");
				if (!new_parent_imageid.equals("")) {
					children_imageid_lists.add(parent_imageid);
					return GetParentImagesInfo(new_parent_imageid, children_imageid_lists);
				}
			}
			String[] image_labels = post.attr("tags").split(" ");
			for (String image_label : image_labels) {
				if (bypass_blacklabels && blacklabel_lists.contains(image_label)) {
					continue forPosts;
				}
			}
			if (bypass_usedid && usedid_lists.contains(imageid)) {
				continue;
			}
			String imagefile_url = post.attr("file_url");
			if (!Judge.isEmpty(imagefile_url)) {
				imagesInfo.put(imageid, imagefile_url);
			}
		}
		return imagesInfo;
	}

	private static void download(String imageid, String imageUrl) {
		String imageidUrl = yande_url + "post/show/" + imageid;
		System.out.println("正在下载 ID: " + imageid + " URL: " + imageidUrl);
		usedid_lists.add(imageid);
		int statusCode = NetworkFileUtils.connect(imageUrl).proxy(proxyHost, proxyPort).retry(true, MILLISECONDS_SLEEP).download(image_folderPath);
		App.imageCount.addAndGet(1);
		if (URIUtils.statusIsOK(statusCode) && record_usedid) {
			ChildRout.WriteFileInfo(imageid, already_usedid_filePath);
		}
	}
}
