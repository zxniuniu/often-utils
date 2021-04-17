package com.png.compared;

import java.io.File;
import java.util.Objects;

import org.haic.often.FilesUtils;

/**
 * 删除数据文件夹的空文件夹 文件夹仅有一张图片将移回壁纸文件夹
 *
 * @author haicdust
 * @version 1.0
 * @since 2021/1/14 1:34
 */
public class App3 {

	public static String DataFolderPath = App.dataFolderPath;
	public static String ImageFolderPath = App.imageFolderPath;

	public static void main(String[] args) {
		delblankfolder();
	}

	public static void delblankfolder() {
		for (File file : Objects.requireNonNull(new File(DataFolderPath).listFiles())) {
			if (file.isDirectory()) {
				delblankfolder(file);
			}
		}
	}

	public static void delblankfolder(File folder) {
		for (File file : Objects.requireNonNull(folder.listFiles())) {
			if (file.isDirectory()) {
				delblankfolder(file);
			}
		}
		File[] files = folder.listFiles();
		if (FilesUtils.isBlankDirectory(folder)) {
			folder.delete();
		} else if (Objects.requireNonNull(files).length == 1) {
			File file = files[0];
			File newfile = new File(ImageFolderPath + "/" + file.getName());
			file.renameTo(newfile);
			folder.delete();
		}
	}
}
