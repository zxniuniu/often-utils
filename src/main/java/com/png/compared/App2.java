package com.png.compared;

import java.io.File;

import org.haic.often.FilesUtils;

/**
 * 将数据文件夹的图片移回图片文件夹
 *
 * @author haicdust
 * @since 2021/1/14 1:11
 */
public class App2 {

	public static String imageFolderPath = App.imageFolderPath;
	public static String dataFolderPath = App.dataFolderPath;

	public static void main(String[] args) {
		for (File image : FilesUtils.iterateFiles(dataFolderPath)) {
			if (image.isFile()) {
				File imageNewFile = new File(imageFolderPath, image.getName());
				image.renameTo(imageNewFile);
			}
		}
		App3.delblankfolder();
	}

}
