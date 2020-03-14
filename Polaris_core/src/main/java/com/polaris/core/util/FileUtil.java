package com.polaris.core.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;

import org.springframework.util.ClassUtils;

import com.polaris.core.Constant;

public abstract class FileUtil {
	
	public static final String DOT = ".";
	
	/** 
     * classpath下的路径 
     * @param fileDir  文件路径 
     * @return 
     */  
    public static String getFullPath(String fileDir) throws IOException {  
    	URL url = ClassUtils.getDefaultClassLoader().getResource("");
    	String path = java.net.URLDecoder.decode(url.getPath(),Charset.defaultCharset().name());
    	
		//file:/C:/projects/bin/xxxxx/yyyy.jar!/BOOT-INF/classes!/
		if (path.startsWith("file:")) {
			path = path.substring(5);
		}
		path = path.split("!")[0];
		if (path.endsWith(".jar")) {
			path = new File(path).getParent();
		}
        if (StringUtil.isEmpty(fileDir)) {
        	return path;
        }
        return path + File.separator + fileDir;
    }

    public static InputStream getStream(String fileName) throws IOException {
		
		//先判断目录下的文件夹
    	InputStream inputStream = getStreamFromPath(fileName);
		
		//是否包含classpath
		if (inputStream == null) {
			return getStreamFromClassPath(fileName);
		}
		
		return inputStream;
	}
    
    public static InputStream getStreamFromPath(String fileName) throws IOException {
		
		//先判断目录下的文件夹
		String path = getFullPath("");
		File file = new File(path + File.separator + Constant.CONFIG + File.separator + fileName);
		if (file.exists()) {
			return new FileInputStream(file);
		}
		
		//根目录下
		file = new File(path + File.separator + fileName);
		if (file.exists()) {
			return new FileInputStream(file);
		}
		
		return null;
	}
    
    public static InputStream getStreamFromClassPath(String fileName) throws IOException {
    	
    	//classpath:config/filename
		InputStream inputStream = FileUtil.class.getClassLoader().getResourceAsStream(Constant.CONFIG + File.separator + fileName);
		if (inputStream != null) {
			return inputStream;
		}
		
		//classpath:filename
		return FileUtil.class.getClassLoader().getResourceAsStream(fileName);
    }
    
    public static String read(InputStream in) throws IOException {
    	if (in == null) {
    		return null;
    	}
    	try {
    		InputStreamReader reader = new InputStreamReader(in, Charset.defaultCharset());
			BufferedReader bf= new BufferedReader(reader);
			StringBuffer buffer = new StringBuffer();
			String line = bf.readLine();
	        while (line != null) {
	        	buffer.append(line);
	            line = bf.readLine();
	        	buffer.append(Constant.LINE_SEP);
	        }
	        String content = buffer.toString();
	        if (StringUtil.isNotEmpty(content)) {
	        	return content;
	        }
    	} finally {
    		if (in != null) {
    			in.close();
    		}
    	}
    	return null;
    }
	
	public static File getFileNotInJar(String fileName) {
		try {
			String path = getFullPath("");
			File file = new File(path + File.separator + Constant.CONFIG + File.separator + fileName);
			if (file.exists()) {
				return file;
			}
			file = new File(path + File.separator + fileName);
			if (file.exists()) {
				return file;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public static String getSuffix(String fileName) {
		return fileName.toLowerCase().substring(fileName.lastIndexOf(DOT) + 1);
	}
	
}