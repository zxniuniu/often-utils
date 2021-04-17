package com.png.download;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.haic.often.ReadWriteUtils;

public class ChildRout {

	public static void exitTask() {
		long start = System.currentTimeMillis(); // 获取开始时间
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			long end = System.currentTimeMillis(); // 获取结束时间
			System.out.println("程序运行时间：" + (end - start) / 1000 + "s 本次共计下载 " + App.imageCount + " 张图片");
		}));
	}

	public static List<String> GetFileInfo(String filePath) {
		File file = new File(filePath);
		return file.isFile() ? ReadWriteUtils.orgin(filePath).list() : new ArrayList<>();
	}

	public static void WriteFileInfo(String str, String filePath) {
		ReadWriteUtils.orgin(filePath).text(str);
	}

	public static void outInfo() {
		//
		//
		//
		//
		//
		//
		String info = """
				*********************************************************************************
				*                                喜欢挑三拣四的图片爬虫                           \t*
				*                                   作者：haicdust                             \t*
				*                                禁止传播，仅供学习使用                           \t*
				*                            支持网页 Yande Sankaku Pixiv                      \t*
				*                              最后更新：2021/3/11 17:17                        \t*
				*********************************************************************************""";
		System.out.println(info);
	}

}
