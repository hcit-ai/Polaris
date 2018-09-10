package com.polaris.comm.util;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

import java.io.*;
import java.util.Enumeration;

/**
 * @标题: UnZipUtil.java
 * @包名: com.polaris.utils
 * @描述:
 * @作者: yanghao
 * @时间: Dec 7, 2015 2:15:25 PM
 * @版权: (c) 2015, 卫宁软件科技有限公司
 */
public class UnZipUtil {

    private static LogUtil logger = LogUtil.getInstance(UnZipUtil.class);

    private UnZipUtil() {
    }

    /**
     * @param zipFile zip包的文件名
     * @param descDir 要解压到的文件夹名
     * @throws IOException
     * @作者: yanghao
     * @时间: Dec 7, 2015 3:20:42 PM
     * @描述: 解压文件主方法
     * @备注:
     */
    @SuppressWarnings("rawtypes")
    public static void unZipFiles(File zipFile, String descDir)
            throws IOException {

        File pathFile = new File(descDir);
        if (!pathFile.exists()) {
            pathFile.mkdirs();
        }
        ZipFile zip = new ZipFile(zipFile);
        for (Enumeration entries = zip.getEntries(); entries.hasMoreElements(); ) {
            InputStream in = null;
            OutputStream out = null;
            try {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                String zipEntryName = entry.getName();
                in = zip.getInputStream(entry);
                String outPath = (descDir + zipEntryName).replaceAll("\\*", "/");
                // 判断路径是否存在,不存在则创建文件路径
                File file = new File(outPath.substring(0, outPath.lastIndexOf('/')));
                if (!file.exists()) {
                    file.mkdirs();
                }
                // 判断文件全路径是否为文件夹,如果是上面已经上传,不需要解压
                if (new File(outPath).isDirectory()) {
                    continue;
                }

                out = new FileOutputStream(outPath);
                byte[] buf1 = new byte[1024];
                int len;
                while ((len = in.read(buf1)) > 0) {
                    out.write(buf1, 0, len);
                }
            } finally {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            }

        }
    }

    /**
     * @param zipFileName zip文件的文件地址
     * @param unFileName  解压zip内容存放的文件地址
     * @return
     * @throws Exception
     * @author lt
     * @备注:
     */
    @SuppressWarnings("rawtypes")
    public static void unZip(String zipFileName, String unFileName) {
        try {
            File f = new File(zipFileName);
            ZipFile zipFile = new ZipFile(zipFileName);
            if ((!f.exists()) && (f.length() <= 0)) {
                throw new FileNotFoundException("要解压的文件不存在!");
            }

			/* 解压ZIP文件指定文件夹路径 */
            String strPath;
			
			/* ZIP文件内的文件名 */
            String gbkPath;
			
			/* ZIP文件内每个文件路径 */
            String strTemp;

            File tempFile = new File(unFileName);
            if (!tempFile.exists()) {
                tempFile.mkdir();
            }
			
			/* 获取绝对路径 */
            strPath = tempFile.getAbsolutePath();
			
			/* 获得zip文件内每一个文件 */
            Enumeration e = zipFile.getEntries();
			
			/* 遍历zip文件每一个文件 */
            while (e.hasMoreElements()) {
                ZipEntry zipEnt = (ZipEntry) e.nextElement();
                gbkPath = zipEnt.getName();
                if (zipEnt.isDirectory()) {
                    strTemp = strPath + File.separator + gbkPath;
                    File dir = new File(strTemp);
                    dir.mkdirs();
                    continue;
                } else {

                    FileOutputStream fos = null;
                    InputStream is = null;
                    BufferedOutputStream bos = null;
                    try {
						/* 读写文件 */
                        is = zipFile.getInputStream(zipEnt);
                        BufferedInputStream bis = new BufferedInputStream(is);
                        gbkPath = zipEnt.getName();
                        strTemp = strPath + File.separator + gbkPath;
		                
		                /* 建目录 */
                        String strsubdir = gbkPath;
                        for (int i = 0; i < strsubdir.length(); i++) {
                            if ("/".equalsIgnoreCase(strsubdir.substring(i, i + 1))) {
                                String temp = strPath + File.separator + strsubdir.substring(0, i);
                                File subdir = new File(temp);
                                if (!subdir.exists()) {
                                    subdir.mkdir();
                                }
                            }
                        }

                        fos = new FileOutputStream(strTemp);
                        bos = new BufferedOutputStream(fos);
                        int c;
                        while ((c = bis.read()) != -1) {
                            bos.write((byte) c);
                        }
                    } finally {
                        if (bos != null) {
                            bos.close();
                        }
                        if (fos != null) {
                            fos.close();
                        }
                        if (is != null) {
                            is.close();
                        }
                    }

                }
            }
        } catch (FileNotFoundException e) {
            logger.error("unZip.FileNotFoundException", e);
        } catch (IOException e) {
            logger.error("unZip.IOException", e);
        } catch (Exception e) {
            logger.error("unZip.Exception", e);
        }
    }
}

/* Copyright (C) 2016, 上海金仕达卫宁软件科技有限公司 Project, All Rights Reserved. */