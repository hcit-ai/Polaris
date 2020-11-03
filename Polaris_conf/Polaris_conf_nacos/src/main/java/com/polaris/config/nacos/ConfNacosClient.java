package com.polaris.config.nacos;

import java.util.Properties;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.polaris.core.Constant;
import com.polaris.core.config.ConfClient;
import com.polaris.core.config.ConfHandlerListener;
import com.polaris.core.util.StringUtil;

public class ConfNacosClient { 
	private static final Logger logger = LoggerFactory.getLogger(ConfNacosClient.class);
	private volatile static ConfNacosClient INSTANCE;
	private volatile ConfigService configService;

	public static ConfNacosClient getInstance(){
		if (INSTANCE == null) {
			synchronized(ConfNacosClient.class) {
				if (INSTANCE == null) {
					INSTANCE = new ConfNacosClient();
				}
			}
		}
		return INSTANCE;
	}
	private ConfNacosClient() {
		//配置文件
    	if (StringUtil.isEmpty(ConfClient.getConfigRegistryAddress())) {
    		return;
    	}
    	iniConfNacos();
	}
	
	private void iniConfNacos() {
		Properties properties = new Properties();
		properties.put(PropertyKeyConst.SERVER_ADDR, ConfClient.getConfigRegistryAddress());
		if (StringUtil.isNotEmpty(ConfClient.getNameSpace())) {
			properties.put(PropertyKeyConst.NAMESPACE, ConfClient.getNameSpace());
		}
		if (StringUtil.isNotEmpty(ConfClient.get(Constant.CONFIG_REGISTRY_USERNAME))) {
		    properties.put(PropertyKeyConst.USERNAME, ConfClient.get(Constant.CONFIG_REGISTRY_USERNAME));
		}
        if (StringUtil.isNotEmpty(ConfClient.get(Constant.CONFIG_REGISTRY_PASSWORD))) {
            properties.put(PropertyKeyConst.PASSWORD, ConfClient.get(Constant.CONFIG_REGISTRY_PASSWORD));
        }
		try {
			configService = NacosFactory.createConfigService(properties);
			logger.info("nacos init success");
		} catch (NacosException e) {
			logger.error(e.getMessage());
			throw new IllegalArgumentException(Constant.CONFIG_REGISTRY_ADDRESS_NAME + ":"+ConfClient.getConfigRegistryAddress()+" is not correct ");
		}
	}
	
	
	// 获取文件内容
	public String getConfig(String group,String fileName) {
		//配置文件
    	if (StringUtil.isEmpty(ConfClient.getConfigRegistryAddress())) {
    		return null;
    	}
    	if (configService == null) {
    		synchronized(this) {
    			if (configService == null) {
    				iniConfNacos();
    			}
    		}
    	}
 		
		try {
			String propertyContent = configService.getConfig(fileName, group, 5000);
			return propertyContent;

		} catch (NacosException e) {
			logger.error(e.getMessage());
		}
		return null;
	}
	
	// 监听需要关注的内容
	public void addListener(String group, String fileName, ConfHandlerListener listener) {
		//配置文件
    	if (StringUtil.isEmpty(ConfClient.getConfigRegistryAddress())) {
    		return;
    	}
    	if (configService == null) {
    		synchronized(this) {
    			if (configService == null) {
    				iniConfNacos();
    			}
    		}
    	}
    	
		try {
			configService.addListener(fileName, group, new Listener() {
				@Override
				public void receiveConfigInfo(String configInfo) {
					if (listener != null) {
						listener.receive(configInfo);
					} 
				}

				@Override
				public Executor getExecutor() {
					return null;
				}
			});
		} catch (NacosException e) {
			logger.error(e.getMessage());
		}
	}

}