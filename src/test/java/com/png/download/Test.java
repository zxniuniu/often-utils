package com.png.download;

import java.io.File;
import java.util.List;

import org.haic.often.FilesUtils;
import org.haic.often.Judge;
import org.haic.often.StringUtils;
import org.haic.often.URIUtils;
import org.haic.often.Network.JsoupUtils;
import org.haic.often.Network.NetworkFileUtils;
import org.jsoup.nodes.Document;

/**
 * @author haicdust
 * @version 1.0
 * @since 2021/4/18 11:08
 */
public class Test {

	private static final String proxyHost = App.proxyHost;
	private static final int proxyPort = App.proxyPort;
	private static final String already_usedid_filePath = App.yande_already_usedid_filePath; // 记录ID文件

	public static void main(String[] args) {
		String path = "F:\\Project\\IDEA\\PngAcc\\data\\Pictures\\yande";
		List<File> downs = FilesUtils.iterateSuffixFiles(path, "down");
		for (File down : downs) {
			String imageid = StringUtils.extractRegex(down.getName(), "\\d+");
			Document doc = JsoupUtils.connect("https://yande.re/post/show/" + imageid).proxy(proxyHost, proxyPort).retry(true).GetDocument();
			String url = doc.selectFirst("id[png]").attr("href");
			if (Judge.isNull(url)) {
				url = doc.selectFirst("id[highres]").attr("href");
			}
			int statusCode = NetworkFileUtils.connect(url).proxy(proxyHost, proxyPort).retry(true).download(path);
			if (URIUtils.statusIsOK(statusCode)) {
				ChildRout.WriteFileInfo(imageid, already_usedid_filePath);
			}
		}
	}
}
