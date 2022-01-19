package org.haic.often.Netdisc;

import org.haic.often.Network.JsoupUtils;
import org.haic.often.StringUtils;
import org.haic.often.Tuple.FourTuple;
import org.haic.often.Tuple.ThreeTuple;
import org.haic.often.Tuple.TupleUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.*;

/**
 * 天翼云API
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/1/18 22:43
 */
public class TianYi {
	private static final String dataApi = "https://cloud.189.cn/api/open/share/getShareInfoByCode.action";
	private static final String listApi = "https://cloud.189.cn/api/open/share/listShareDir.action";
	private static final String downApi = "https://cloud.189.cn/api/open/file/getFileDownloadUrl.action";

	/**
	 * 在需要提取码但自己没有时,可直接获得文件直链<br/>
	 * 如果有多个文件,返回第一个文件直链
	 *
	 * @param url     天翼URL
	 * @param cookies cookies
	 * @return 文件自链
	 */
	@NotNull @Contract(pure = true) public static String getFileUrlOfNotCode(@NotNull final String url, @NotNull final Map<String, String> cookies) {
		FourTuple<String, String, String, String> urlInfo = getUrlInfo(url);
		return JsoupUtils.connect(downApi + "?dt=1&fileId=" + urlInfo.first + "&shareId=" + urlInfo.second).cookies(cookies).retry(true).get().text();
	}

	/**
	 * 获得所有文件的直链(无密码)
	 *
	 * @param url     天翼URL
	 * @param cookies cookies
	 * @return Map - 文件名 ,文件自链
	 */
	@NotNull @Contract(pure = true) public static Map<String, String> getFilesUrl(@NotNull final String url, @NotNull final Map<String, String> cookies) {
		return getFilesUrl(url, "", cookies);
	}

	/**
	 * 获得所有文件的直链
	 *
	 * @param url        天翼URL
	 * @param accessCode 提取码
	 * @param cookies    cookies
	 * @return Map - 文件名 ,文件自链
	 */
	@NotNull @Contract(pure = true) public static Map<String, String> getFilesUrl(@NotNull final String url, String accessCode,
			@NotNull final Map<String, String> cookies) {
		Map<String, String> fileUrls = new HashMap<>();
		for (ThreeTuple<String, String, String> fileInfo : getFilesInfo(url, accessCode)) {
			String fileUrl = JsoupUtils.connect(downApi + "?dt=1&fileId=" + fileInfo.second + "&shareId=" + fileInfo.third).cookies(cookies).retry(true).get()
					.text();
			fileUrls.put(fileInfo.first, fileUrl);
		}
		return fileUrls;
	}

	/**
	 * @param url        天翼URL
	 * @param accessCode 提取码
	 * @return List - fileName, fileId, shareId
	 */
	@NotNull @Contract(pure = true) private static List<ThreeTuple<String, String, String>> getFilesInfo(@NotNull final String url,
			@NotNull final String accessCode) {
		FourTuple<String, String, String, String> urlInfo = getUrlInfo(url);
		Map<String, String> listData = new HashMap<>();
		listData.put("fileId", urlInfo.first);
		listData.put("shareId", urlInfo.second);
		listData.put("isFolder", urlInfo.third);
		listData.put("shareMode", urlInfo.fourth);
		listData.put("accessCode", accessCode);

		Document docDown = JsoupUtils.connect(listApi).data(listData).get();

		List<ThreeTuple<String, String, String>> fileInfos = new ArrayList<>();
		for (Element element : docDown.select("file")) {
			String fileName = Objects.requireNonNull(element.selectFirst("name")).text();
			String id = Objects.requireNonNull(element.selectFirst("id")).text();
			fileInfos.add(TupleUtil.tuple(fileName, id, urlInfo.second));
		}
		return fileInfos;
	}

	/**
	 * 获取URL的ID等信息
	 *
	 * @param url 天翼URL
	 * @return fileId, shareId, isFolder, shareMode
	 */
	@Contract(pure = true) private static FourTuple<String, String, String, String> getUrlInfo(@NotNull final String url) {
		String code = url.contains("code") ?
				StringUtils.extractRegex(url, "code=.*").substring(5) :
				StringUtils.extractRegex(url, "code=.*").substring(url.lastIndexOf("/"), url.length());
		Document docData = JsoupUtils.connect(dataApi + "?shareCode=" + code).retry(true).get();
		String fileId = Objects.requireNonNull(docData.selectFirst("fileId")).text();
		String shareId = Objects.requireNonNull(docData.selectFirst("shareId")).text();
		String isFolder = Objects.requireNonNull(docData.selectFirst("isFolder")).text();
		String shareMode = Objects.requireNonNull(docData.selectFirst("shareMode")).text();
		return TupleUtil.tuple(fileId, shareId, isFolder, shareMode);
	}

}