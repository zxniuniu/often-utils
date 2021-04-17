package com.png.download;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.haic.often.FilesUtils;

import com.png.download.Pixiv.Pixiv_login;
import com.png.download.Sankaku.Sankaku_login;
import com.png.download.Yande.Yande_LabelImages;
import com.png.download.Yande.Yande_PopularDailyImages;
import com.png.download.Yande.Yande_login;

public class App {
	// **************************** MiXin *********************************
	public static final String image_folderPath = "data\\Pictures"; // 图片保存路径

	public static final String proxyHost = "127.0.0.1"; // 代理主机
	public static final int proxyPort = 7890; // 代理端口

	public static final int MAX_THREADS = 4; // 多线程下载,建议4
	public static final int MILLISECONDS_SLEEP = 2000; // 重试间隔时间(毫秒)

	// public static final boolean AdultContent = true; // 允许成人内容

	public static AtomicInteger imageCount = new AtomicInteger(0);

	// **************************** Pixiv *********************************
	public static final String pixiv_url = "https://www.pixiv.net/";
	public static final String pixiv_image_folderPath = image_folderPath + "/Pixiv";

	public static final String pixiv_start_date = "2020-08-01"; // 开始日期

	public static final boolean pixiv_employ_cookies = true;// 使用cookie
	public static final boolean pixiv_bypass_blacklabels = true; // 跳过黑名单标签
	public static final boolean pixiv_record_usedid = true; // 记录已下载的图片ID
	public static final boolean pixiv_bypass_usedid = true; // 跳过已记录的图片ID
	public static final boolean pixiv_record_date = true; // 记录已完成的日期
	public static final boolean pixiv_bypass_record_date = true; // 跳过记录的日期
	public static final boolean pixiv_unbypass_within_aweek = true; // 不跳过一星期内的日期

	public static final String pixiv_cookies_filePath = "data/PixivData/Pixiv_Cookies";
	public static final String pixiv_whitelabels_filePath = "data/PixivData/Pixiv_WhiteLabels";
	public static final String pixiv_blacklabels_filePath = "data/PixivData/Pixiv_BlackLabels";
	public static final String pixiv_record_date_filePath = "data/PixivData/Pixiv_RecordDate"; // 日期文件
	public static final String pixiv_already_usedid_filePath = "data/PixivData/Pixiv_AlreadyUsedId";

	public static final int pixiv_api_maxthreads = 40; // 访问API最大线程
	public static final Map<String, String> pixiv_cookies = Pixiv_login.GetCookies(); // cookies

	// **************************** Sankaku *********************************
	public static final String sankaku_url = "https://chan.sankakucomplex.com/";
	public static final String sankaku_image_folderPath = image_folderPath + "/Sankaku";

	public static final String sankaku_user_name = "hnuuhc@hotmail.com"; // 账号
	public static final String sankaku_user_password = "huang3699"; // 密码

	public static final boolean sankaku_employ_cookies = true;// 使用cookie
	public static final boolean sankaku_bypass_blacklabels = true; // 跳过黑名单标签
	public static final boolean sankaku_record_usedid = true; // 记录已下载的图片ID
	public static final boolean sankaku_bypass_usedid = true; // 跳过已记录的图片ID

	public static final String sankaku_cookies_filePath = "data/SankakuData/Sankaku_Cookies";
	public static final String sankaku_whitelabels_filePath = "data/SankakuData/Sankaku_WhiteLabels";
	public static final String sankaku_blacklabels_filePath = "data/SankakuData/Sankaku_BlackLabels";
	public static final String sankaku_already_usedid_filePath = "data/SankakuData/Sankaku_AlreadyUsedId"; // 记录ID文件

	// public static int sankaku_api_maxthreads = 40; // 访问API最大线程
	public static final int sankaku_api_limit = 100; // API单页获取数量限制

	public static final Map<String, String> sankaku_cookies = Sankaku_login.GetCookies(); // cookies

	// **************************** Yande.re *********************************
	public static final String yande_url = "https://yande.re/";
	public static final String yande_image_folderPath = image_folderPath + "/Yande";

	public static final String yande_user_name = "璀璨の光芒"; // 账号
	public static final String yande_user_password = "huang3699"; // 密码

	public static final String yande_start_date = "2018-01-01"; // 开始日期

	public static final boolean yande_employ_cookies = true;// 使用cookie
	public static final boolean yande_record_date = true; // 记录已完成的日期
	public static final boolean yande_bypass_record_date = true; // 跳过记录的日期
	public static final boolean yande_unbypass_within_aweek = true; // 不跳过一星期内的日期
	public static final boolean yande_record_usedid = true; // 记录已下载的图片ID
	public static final boolean yande_bypass_usedid = true; // 跳过已记录的图片ID
	public static final boolean yande_bypass_blacklabels = true; // 跳过黑名单标签

	public static final String yande_whitelabels_filePath = "data/YandeData/Yande_WhiteLabels"; // 白名单文件
	public static final String yande_blacklabels_filePath = "data/YandeData/Yande_BlackLabels"; // 黑名单文件
	public static final String yande_record_date_filePath = "data/YandeData/Yande_RecordDate"; // 日期文件
	public static final String yande_already_usedid_filePath = "data/YandeData/Yande_AlreadyUsedId"; // 记录ID文件
	public static final String yande_cookies_filePath = "data/YandeData/Yande_Cookies"; // cookies文件

	public static final int yande_api_maxthreads = 40; // 访问API最大线程
	public static final int yande_api_limit = 100; // API单页获取数量限制

	public static final Map<String, String> yande_cookies = Yande_login.GetCookies(); // cookies

	public static void main(String[] args) {
		ChildRout.outInfo();
		ChildRout.exitTask();
		Yande_PopularDailyImages.ImagesDownload(); // Yande 每日热门
		Yande_LabelImages.ImagesDownload(); // Yande 标签
		// Sankaku_LabelImages.ImagesDownload(); // Sankaku 标签
		// Pixiv_PopularDailyImages.ImagesDownload(); // Pixiv 每日热门
		// Pixiv_LabelImages.ImagesDownload(); // Pixiv 标签
		// Pixiv_Suggestion.ImagesDownload(); // Pixiv 推荐
		// Pixiv_Optimal.ImagesDownload(); // Pixiv 最佳
		FilesUtils.openDesktop(App.image_folderPath);
	}
}
