package com.png.download;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.haic.often.FilesUtils;
import org.haic.often.StringUtils;
import org.haic.often.URIUtils;
import org.haic.often.Multithread.MultiThreadUtils;
import org.haic.often.Network.NetworkFileUtils;

/**
 * @author haicdust
 * @version 1.0
 * @since 2021/4/18 11:08
 */
public class Test {

	private static final String proxyHost = App.proxyHost;
	private static final int proxyPort = App.proxyPort;
	private static final String already_usedid_filePath = App.yande_already_usedid_filePath; // 记录ID文件
	private static final String image_folderPath = App.yande_image_folderPath;

	public static void main(String[] args) {

		ExecutorService executorService = Executors.newFixedThreadPool(4); // 限制多线程
		for (File down : FilesUtils.iterateSuffixFiles(image_folderPath, "down")) {
			executorService.submit(new Thread(() -> { // 执行多线程程
				String imageid = StringUtils.extractRegex(down.getName(), "\\d+");
				System.out.println("download: " + App.yande_url + "post/show/" + imageid);
				int statusCode = NetworkFileUtils.down(down).proxy(proxyHost, proxyPort).retry(2).download(image_folderPath);
				if (URIUtils.statusIsOK(statusCode)) {
					ChildRout.WriteFileInfo(imageid, already_usedid_filePath);
				} else {
					System.out.println(statusCode);
				}
			}));
			MultiThreadUtils.WaitForThread(32);
		}
		MultiThreadUtils.WaitForEnd(executorService); // 等待线程结束
	}
}
