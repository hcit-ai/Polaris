package com.polaris.container.jetty.listener;

import javax.servlet.ServletContext;

import org.eclipse.jetty.util.component.AbstractLifeCycle.AbstractLifeCycleListener;
import org.eclipse.jetty.util.component.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.polaris.http.initializer.WSEndpointExporter;
import com.polaris.http.listener.ServerListener;

/**
 * Class Name : ServerHandler
 * Description : 服务器Handler
 * Creator : yufenghua
 * Modifier : yufenghua
 *
 */

public class ServerHandlerListerner extends AbstractLifeCycleListener{
	
	private static final Logger logger = LoggerFactory.getLogger(ServerHandlerListerner.class);
	/**
	 * 服务器监听器集合
	 */
	private static ServerHandlerListerner instance = null;
	private ServerListener listener;
	private ServletContext servletContext;

	private ServerHandlerListerner(ServerListener listener,ServletContext servletContext) {
		this.listener = listener;
		this.servletContext = servletContext;
	}

	/**
	 * 获取单实例公共静态方法
	 * @return 单实例
	 */
	public static ServerHandlerListerner getInstance(ServerListener listener,ServletContext servletContext) {
		if (instance == null) {
			synchronized(ServerHandlerListerner.class) {
				if (instance == null) {
					instance = new ServerHandlerListerner(listener,servletContext);
				}
			}
		}
		return instance;
	}
	
	/**
	 * 监听server的状态
	 * 启动中
	 */
	public void lifeCycleStarting(LifeCycle event) {
		listener.starting(servletContext);
    	logger.info("JettyServer启动中！");
	}
	
	/**
	 * 监听server的状态
	 * 启动结束
	 */
    public void lifeCycleStarted(LifeCycle event) {
    	
    	//外部监听
    	listener.started(servletContext);
    	
    	//加载websocket
    	WSEndpointExporter wsEndpointExporter = new WSEndpointExporter();
    	wsEndpointExporter.initServerContainer(servletContext);
    	
    	//日志
    	logger.info("JettyServer启动成功！");
    }
    
	/**
	 * 监听server的状态
	 * 异常
	 */
    public void lifeCycleFailure(LifeCycle event,Throwable cause) {
    	listener.failure(servletContext);
    	logger.info("JettyServer启动失败！");
    }
    
	/**
	 * 监听server的状态
	 * 结束中
	 */
   public void lifeCycleStopping(LifeCycle event) {
	   listener.stopping(servletContext);
	   logger.info("JettyServer已经中！");
   }
   
	/**
	 * 监听server的状态
	 * 结束
	 */
    public void lifeCycleStopped(LifeCycle event) {
    	listener.stopped(servletContext);
    	logger.info("JettyServer已经停止！");
    }
    
}
