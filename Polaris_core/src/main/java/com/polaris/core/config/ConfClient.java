package com.polaris.core.config;

import java.io.File;
import java.io.IOException;

import com.polaris.core.Constant;
import com.polaris.core.util.PropertyUtils;
import com.polaris.core.util.StringUtil;

/**
*
* 项目名称：Polaris_comm
* 类名称：ConfClient
* 类描述：
* 创建人：yufenghua
* 创建时间：2018年5月9日 上午9:15:12
* 修改人：yufenghua
* 修改时间：2018年5月9日 上午9:15:12
* 修改备注：
* @version
*
*/
public class ConfClient {
	
	static {
		try {
			System.setProperty("log4j.configurationFile", PropertyUtils.getFilePath(Constant.CONFIG + File.separator + Constant.LOG4J));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	* 获取配置信息
	* @param 
	* @return 
	* @Exception 
	* @since 
	*/
	public static String get(String key) {
		return get(key, "", true);
	}
	public static String get(String key, boolean isWatch) {
		return get(key, "", isWatch);
	}
	public static String get(String key, String defaultValue) {
		return get(key, defaultValue, true);
	}
	@SuppressWarnings("static-access")
	public static String get(String key, String defaultVal, boolean isWatch) {
		
		//application.properties
		String value = ConfigHandlerProvider.getInstance().getValue(key, Constant.DEFAULT_CONFIG_NAME, isWatch);
		if (value != null) {
			return value;
		}
		
		//扩展文件
		String[] allProperties = ConfigHandlerProvider.getInstance().getExtensionProperties();
		if (allProperties != null) {
			for (String file : allProperties) {
				value = ConfigHandlerProvider.getInstance().getValue(key, file, isWatch);
				if (value != null) {
					return value;
				}
			}
		}
		
		//默认值
		if (StringUtil.isNotEmpty(defaultVal)) {
			ConfigHandlerProvider.getInstance().updateCache(key, defaultVal, Constant.DEFAULT_CONFIG_NAME);
		}
		
		//返回默认值
		return defaultVal;
	}
	

	//获取配置文件
	public static String getConfigValue(String fileName) {
		return ConfigHandlerProvider.getInstance().getConfig(fileName);
	}

	//增加文件是否修改的监听
	public static void addListener(String fileName, ConfListener listener) {
		ConfigHandlerProvider.getInstance().addListener(fileName, listener);
	}

	@SuppressWarnings("static-access")
	public static void setAppName(String inputAppName) {
		ConfigHandlerProvider.getInstance().updateCache(Constant.PROJECT_NAME, inputAppName, Constant.DEFAULT_CONFIG_NAME);
	}
	public static String getAppName() {
		String appName = ConfigHandlerProvider.getInstance().getValue(Constant.PROJECT_NAME, Constant.DEFAULT_CONFIG_NAME, false);
		return appName == null ? "" :appName;
	}
	public static String getConfigRegistryAddress() {
		String config = ConfigHandlerProvider.getInstance().getValue(Constant.CONFIG_REGISTRY_ADDRESS_NAME, Constant.DEFAULT_CONFIG_NAME, false);
		return config == null ? "" :config;
	}
	public static String getNameSpace() {
		String namespace = ConfigHandlerProvider.getInstance().getValue(Constant.PROJECR_NAMESPACE_NAME, Constant.DEFAULT_CONFIG_NAME, false);
		return namespace == null ? "" :namespace;
	}
	public static String getGroup() {
		String group = ConfigHandlerProvider.getInstance().getValue(Constant.PROJECR_GROUP_NAME, Constant.DEFAULT_CONFIG_NAME, false);
		return group == null ? "" :group;
	}

	public static String getCluster() {
		String cluster = ConfigHandlerProvider.getInstance().getValue(Constant.PROJECR_CLUSTER_NAME, Constant.DEFAULT_CONFIG_NAME, false);
		return cluster == null ? "" :cluster;
	}
	public static String getEnv() {
		String env = ConfigHandlerProvider.getInstance().getValue(Constant.PROJECT_ENV_NAME, Constant.DEFAULT_CONFIG_NAME, false);
		return env == null ? "" :env;
	}
	public static String getNamingRegistryAddress() {
		String naming = ConfigHandlerProvider.getInstance().getValue(Constant.NAMING_REGISTRY_ADDRESS_NAME, Constant.DEFAULT_CONFIG_NAME, false);
		return naming == null ? "" :naming;
	}
	public static long getUuidWorkId() {
		String workId = ConfigHandlerProvider.getInstance().getValue(Constant.UUID_WORKID, Constant.DEFAULT_CONFIG_NAME, false);
		if (StringUtil.isEmpty(workId)) {
			return 0l;
		}
		return Long.parseLong(workId.trim());
	}
	public static long getUuidDatacenterId() {
		String datacenterId = ConfigHandlerProvider.getInstance().getValue(Constant.UUID_DATACENTERID, Constant.DEFAULT_CONFIG_NAME, false);
		if (StringUtil.isEmpty(datacenterId)) {
			return 0l;
		}
		return Long.parseLong(datacenterId.trim());
	}	
}