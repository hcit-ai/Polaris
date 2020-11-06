package com.polaris.config.apollo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctrip.framework.apollo.ConfigFile;
import com.ctrip.framework.apollo.ConfigFileChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.model.ConfigFileChangeEvent;
import com.polaris.core.config.ConfHandlerListener;

public class ConfApolloClient { 
	
	private static final Logger logger = LoggerFactory.getLogger(ConfApolloClient.class);
	private volatile static ConfApolloClient INSTANCE = new ConfApolloClient();
	public static ConfApolloClient getInstance(){
		return INSTANCE;
	}
	private ConfApolloClient() {
		//must load META-INF/app.properties
	}
	
	// 获取文件内容
	public String getConfig(String group,String fileName) {
		String fileFormart = "properties";
		if (fileName != null && fileName.lastIndexOf(".") > 0) {
			fileFormart = fileName.substring(fileName.lastIndexOf(".") + 1);
			fileName = fileName.substring(fileName.lastIndexOf("."));
		}
		ConfigFile config = ConfigService.getConfigFile(fileName, ConfigFileFormat.fromString(fileFormart));
		if (config == null) {
			logger.error("Apollo ConfigFile load error,ConfigFile is null");
			return null;
		}
		return config.getContent();
	}
	
	// 监听需要关注的内容
	public void addListener(String group,String fileName, ConfHandlerListener listener) {
		String fileFormart = null;
		if (fileName != null && fileName.lastIndexOf(".") > 0) {
			fileFormart = fileName.substring(fileName.lastIndexOf(".") + 1);
		}
		ConfigFile config = ConfigService.getConfigFile(fileName, ConfigFileFormat.fromString(fileFormart));
		if (config == null) {
			logger.error("Apollo ConfigFile load error,ConfigFile is null");
		}
		config.addChangeListener(new ConfigFileChangeListener() {
			@Override
			public void onChange(ConfigFileChangeEvent changeEvent) {
				listener.receive(changeEvent.getNewValue());
			}
		});
	}
}