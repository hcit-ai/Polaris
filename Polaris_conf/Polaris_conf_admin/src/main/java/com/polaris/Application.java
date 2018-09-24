package com.polaris;

import java.io.File;

import org.apache.log4j.PropertyConfigurator;

import com.polaris.comm.config.ConfClient;
import com.polaris.http.supports.MainSupport;


/**
 * 入口启动类
 *
 */
public class Application
{

    public static void main( String[] args ) throws Exception
    {
    	//应用名称
    	ConfClient.setAppName("Polaris_conf_admin");
    	
    	//载入日志
    	PropertyConfigurator.configure(
    			Application.class.getClassLoader().getResourceAsStream(
    					"config" + File.separator + "log4j.properties"));
    	
    	//启动
    	MainSupport.startWebServer(args);
    }
    

}
