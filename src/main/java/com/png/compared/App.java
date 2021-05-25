package com.png.compared;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.haic.often.Judge;
import org.haic.often.ReadWriteUtils;
import org.haic.often.Multithread.MultiThreadUtils;

/**
 * 对比并处理相似图片,删除相同图片,相似度88以上图片移动至数据文件夹
 *
 * @author haicdust
 * @since : 2021/2/24 5:25
 */
public class App {
	public static final String imageFolderPath = "E:/Pictures/壁纸";
	public static final String dataImageFolderPath = "data/Pictures/临时";
	public static final String dataFolderPath = "data/ImageCpd";
	public static final String imageInfoPath = "data/MiXinData/ImageInfo";

	private static final int MAX_THREADS = 32; // 多线程建立图片指纹库

	private static final AtomicInteger comcount = new AtomicInteger(0);

	public static void main(String[] args) {

		Map<String, FingerPrint> imagesInfo = new HashMap<>();
		Map<String, FingerPrint> dataImagesInfo = new HashMap<>();

		// ==================== 读取指纹库 ====================
		List<String> imageInfos = ReadWriteUtils.orgin(imageInfoPath).list();
		for (int i = 0; i < imageInfos.size(); i = i + 2) {
			String imageName = imageInfos.get(i);
			if (new File(imageFolderPath, imageName).isFile()) {
				imagesInfo.put(imageName, new FingerPrint(imageInfos.get(i + 1)));
			} else if (new File(dataImageFolderPath, imageName).isFile()) {
				dataImagesInfo.put(imageName, new FingerPrint(imageInfos.get(i + 1)));
			}
		}

		// ==================== 建立壁纸文件夹指纹库 ====================
		File[] images = new File(imageFolderPath).listFiles();
		ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREADS); // 限制多线程
		for (File image : Objects.requireNonNull(images)) {
			executorService.submit(new Thread(() -> { // 执行多线程程
				if (!imagesInfo.containsKey(image.getName()) && image.isFile()) {
					comcount.addAndGet(1);
					int schedule = (int) Math.ceil((double) comcount.get() * 100 / (double) images.length);
					System.out.print("\r正在建立壁纸文件夹指纹库,当前进度: " + schedule + "%");
					FingerPrint fingerprint = FingerPrint.GetFingerPrint(image);
					if (!Judge.isNull(fingerprint)) {
						String fingerprintstr = fingerprint.toString().replaceAll("\n", " ");
						fingerprintstr = fingerprintstr.substring(0, fingerprintstr.length() - 1);
						ReadWriteUtils.orgin(imageInfoPath).text(image.getName() + (char) 10 + fingerprintstr);
						imagesInfo.put(image.getName(), fingerprint);
					}
				}
			}));
		}
		MultiThreadUtils.WaitForEnd(executorService);

		// ==================== 建立数据文件夹指纹库 ====================
		File DataImageFolder = new File(dataImageFolderPath);
		File[] dataImages = DataImageFolder.listFiles();
		comcount.set(0);
		executorService = Executors.newFixedThreadPool(MAX_THREADS); // 限制多线程
		for (File dataImage : Objects.requireNonNull(dataImages)) {
			executorService.submit(new Thread(() -> { // 执行多线程程
				if (!dataImagesInfo.containsKey(dataImage.getName())) {
					if (dataImage.isFile()) {
						comcount.addAndGet(1);
						int schedule = (int) Math.ceil((double) comcount.get() * 100 / (double) dataImages.length);
						System.out.print("\r正在建立数据文件夹指纹库,当前进度: " + schedule + "%");
						FingerPrint fingerprint = FingerPrint.GetFingerPrint(dataImage.getPath());
						if (!Judge.isNull(fingerprint)) {
							String fingerprintstr = fingerprint.toString().replaceAll("\n", " ");
							fingerprintstr = fingerprintstr.substring(0, fingerprintstr.length() - 1);
							ReadWriteUtils.orgin(imageInfoPath).text(dataImage.getName() + (char) 10 + fingerprintstr);
							dataImagesInfo.put(dataImage.getName(), fingerprint);
						}
					}
				} else if (imagesInfo.containsKey(dataImage.getName())) {
					dataImage.delete();
					System.out.println("Delete: " + dataImage.getName());
				}
			}));
		}
		MultiThreadUtils.WaitForEnd(executorService);

		// ==================== 开始处理图片 ====================
		imagesInfo.putAll(dataImagesInfo);
		App3.delblankfolder(); // 删除数据文件夹的空文件夹
		int folderCount = 1, count = 0;
		for_data_imageinfo: for (Map.Entry<String, FingerPrint> dataImageInfo : dataImagesInfo.entrySet()) {
			count++;
			int schedule = (int) Math.ceil((double) count * 100 / (double) dataImages.length);
			System.out.println("\r正在对比图片,当前进度: " + count + "/" + dataImagesInfo.size() + " ------ " + schedule + "%");
			File dataImage = new File(dataImageFolderPath, dataImageInfo.getKey());
			if (!dataImage.isFile()) {
				continue;
			}
			FingerPrint fingerprint = dataImageInfo.getValue();
			boolean hassimilarity = false;
			for (Map.Entry<String, FingerPrint> imageInfo : imagesInfo.entrySet()) {
				File image = new File(imageFolderPath, imageInfo.getKey());
				if (!image.isFile() || image == dataImage) {
					continue;
				}
				double Similarity = fingerprint.compare(imageInfo.getValue()) * 100;
				if (dataImage.getName().matches("yande.re .*") && !image.getName().matches("yande.re .*") && Similarity == 100) {
					image.delete();
					System.out.println("Delete: " + image.getPath());
					File New_Image = new File(imageFolderPath + dataImage.getName());
					image.renameTo(New_Image);
				} else if (image.getName().matches("yande.re .*") && !dataImage.getName().matches("yande.re .*") && Similarity == 100) {
					dataImage.delete();
					System.out.println("Delete: " + dataImage.getPath());
					continue for_data_imageinfo;
				} else if (Similarity > 88) {
					if (dataImage.isFile()) {
						if (!hassimilarity) {
							while (true) {
								File folder = new File(dataFolderPath, String.valueOf(folderCount));
								if (folder.exists()) {
									folderCount++;
								} else {
									folder.mkdirs();
									break;
								}
							}
						}
						image.renameTo(new File(dataFolderPath + "/" + folderCount, image.getName()));
					}
					hassimilarity = true;
				}
			}
			if (hassimilarity) {
				dataImage.renameTo(new File(dataFolderPath + "/" + folderCount, dataImage.getName()));
				folderCount++;
			} else {
				File rename = new File(imageFolderPath, dataImage.getName());
				if (dataImage.isFile() && !rename.exists()) {
					dataImage.renameTo(rename);
				}
			}
		}
		System.out.println("程序完成");
	}

}
