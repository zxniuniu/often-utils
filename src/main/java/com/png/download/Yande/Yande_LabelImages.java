package com.png.download.Yande;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.haic.often.FilesUtils;
import org.haic.often.URIUtils;
import org.haic.often.Multithread.MultiThreadUtils;
import org.haic.often.Multithread.ParameterizedThread;
import org.haic.often.Network.JsoupUtils;
import org.haic.often.Network.NetworkFileUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.png.download.App;
import com.png.download.ChildRout;

/**
 * 以标签的方式爬取Yande.re的图片
 *
 * @author haicdust
 * @since 2021/2/14 13:14
 * @version 2.13
 */
public class Yande_LabelImages {

	private static final int MAX_THREADS = App.MAX_THREADS; // 多线程
	private static final int MILLISECONDS_SLEEP = App.MILLISECONDS_SLEEP; // 程序等待

	private static final String yande_url = App.yande_url;
	private static final String image_folderPath = FilesUtils.GetAbsolutePath(App.yande_image_folderPath);

	private static final boolean record_usedid = App.yande_record_usedid; // 记录已下载的图片ID
	private static final boolean bypass_usedid = App.yande_bypass_usedid; // 跳过已记录的图片ID
	private static final boolean bypass_blacklabels = App.yande_bypass_blacklabels; // 标签黑名单
	private static final String whitelabels_filePath = App.yande_whitelabels_filePath; // 白名单文件
	private static final String blacklabels_filePath = App.yande_blacklabels_filePath; // 黑名单文件
	private static final String already_usedid_filePath = App.yande_already_usedid_filePath; // 记录ID文件

	private static final String proxyHost = App.proxyHost;
	private static final int proxyPort = App.proxyPort;

	private static final int API_MAX_THREADS = App.yande_api_maxthreads; // 访问API最大线程
	private static final int limit = App.yande_api_limit; // API单页获取数量限制

	private static final Map<String, String> cookies = App.yande_cookies;

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
		System.out.println("下载 Yande 标签图片 已完成 存储路径: " + image_folderPath);
	}

	private static void executeProgram(String whitelabel) {
		System.out.println("正在下载 Yande 标签白名单图片,当前标签: " + whitelabel + " 存储路径: " + image_folderPath);
		Map<String, String> imagesInfo = GetLabelImagesInfo(whitelabel);
		ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREADS); // 限制多线程
		for (Map.Entry<String, String> imageInfo : imagesInfo.entrySet()) { // 下载
			// 执行多线程程
			executorService.submit(new Thread(() -> { // 程序
				download(imageInfo.getKey(), imageInfo.getValue());
			}));
		}
		MultiThreadUtils.WaitForEnd(executorService); // 等待线程结束
	}

	public static Map<String, String> GetLabelImagesInfo(String whitelabel) {
		String whitelabel_url = yande_url + "post.xml?tags=" + whitelabel + "&limit=" + limit;
		Document document = JsoupUtils.connect(whitelabel_url).timeout(10000).proxy(proxyHost, proxyPort).cookies(cookies).retry(10, MILLISECONDS_SLEEP).errorExit(true).GetDocument();
		int post_count = Integer.parseInt(document.selectFirst("posts").attr("count"));
		Map<String, String> imagesInfo = new ConcurrentHashMap<>(GetPostsInfo(document));
		Elements posts = document.select("post");
		if (posts.size() < limit && posts.size() < post_count) {
			System.out.println("无法获取标签全部信息,请缩小limit值");
			System.exit(0);
		}
		if (post_count > limit) {
			int url_maxpage = (int) Math.ceil((double) post_count / (double) limit);
			ExecutorService executorService = Executors.newFixedThreadPool(API_MAX_THREADS); // 限制多线程
			for (int i = 2; i <= url_maxpage; i++) {
				executorService.submit(new ParameterizedThread<>(i, (index) -> { // 执行多线程程
					imagesInfo.putAll(GetPostsInfo(JsoupUtils.connect(yande_url + "post.xml?tags=" + whitelabel + "&page=" + index + "&limit=" + limit).timeout(10000).proxy(proxyHost, proxyPort)
							.cookies(cookies).retry(10, MILLISECONDS_SLEEP).errorExit(true).GetDocument()));
				}));
			}
			MultiThreadUtils.WaitForEnd(executorService); // 等待线程结束
		}
		return new HashMap<>(imagesInfo);

	}

	private static Map<String, String> GetPostsInfo(Document document) {
		Map<String, String> imagesInfo = new HashMap<>();
		Elements posts = document.select("post");
		for_posts: for (Element post : posts) {
			String imageid = post.attr("id");
			if (bypass_usedid && usedid_lists.contains(imageid)) {
				continue;
			}
			String[] image_labels = post.attr("tags").split(" ");
			for (String image_label : image_labels) {
				if (bypass_blacklabels && blacklabel_lists.contains(image_label)) {
					continue for_posts;
				}
			}
			String imagefile_url = post.attr("file_url");
			imagesInfo.put(imageid, imagefile_url);
		}
		return imagesInfo;
	}

	private static void download(String imageid, String imageUrl) {
		String imageidUrl = yande_url + "post/show/" + imageid;
		System.out.println("正在下载 ID: " + imageid + " URL: " + imageidUrl);
		usedid_lists.add(imageid);
		int statusCode = NetworkFileUtils.connect(imageUrl).proxy(proxyHost, proxyPort).retry(true, MILLISECONDS_SLEEP).download(image_folderPath);
		if (URIUtils.statusIsOK(statusCode) && record_usedid) {
			ChildRout.WriteFileInfo(imageid, already_usedid_filePath);
		}
	}
}
