package com.polaris.container.gateway;

import java.net.InetSocketAddress;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.polaris.container.config.ConfigurationHelper;
import com.polaris.container.gateway.proxy.ActivityTrackerAdapter;
import com.polaris.container.gateway.proxy.FlowContext;
import com.polaris.container.gateway.proxy.HttpFilters;
import com.polaris.container.gateway.proxy.HttpFiltersSourceAdapter;
import com.polaris.container.gateway.proxy.HttpProxyServerBootstrap;
import com.polaris.container.gateway.proxy.extras.SelfSignedSslEngineSourceExt;
import com.polaris.container.gateway.proxy.impl.DefaultHttpProxyServer;
import com.polaris.container.gateway.proxy.impl.ThreadPoolConfiguration;
import com.polaris.container.listener.ServerListenerHelper;
import com.polaris.container.util.NetUtils;
import com.polaris.core.config.ConfClient;
import com.polaris.core.util.SpringUtil;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;

public class HttpFilterServer {
	
	private static Logger logger = LoggerFactory.getLogger(HttpFilterServer.class);
	
	/**
     * 服务器
     */
    private HttpProxyServerBootstrap httpProxyServerBootstrap = null;
    
	/**
     * 私有构造方法
     */
    private HttpFilterServer() {
    }
    
    /**
     * 获取单实例公共静态方法
     *
     * @return 单实例
     */
    public static HttpFilterServer getInstance() {
        return Singletone.INSTANCE;
    }

    /**
     * 静态内部类实现单例
     */
    private static class Singletone {
        /**
         * 单实例
         */
        private static final HttpFilterServer INSTANCE = new HttpFilterServer();
    }
    
    /**
     * 启动服务器
     *
     * @throws Exception
     */
    public void start() {

    	//创建context
    	SpringUtil.refresh(ConfigurationHelper.getConfiguration());
        ThreadPoolConfiguration threadPoolConfiguration = new ThreadPoolConfiguration();
        threadPoolConfiguration.withAcceptorThreads(HttpFilterConstant.AcceptorThreads);
        threadPoolConfiguration.withClientToProxyWorkerThreads(HttpFilterConstant.ClientToProxyWorkerThreads);
        threadPoolConfiguration.withProxyToServerWorkerThreads(HttpFilterConstant.ProxyToServerWorkerThreads);

        InetSocketAddress inetSocketAddress = new InetSocketAddress(Integer.parseInt(ConfClient.get("server.port")));
        httpProxyServerBootstrap = DefaultHttpProxyServer.bootstrap()
                .withAddress(inetSocketAddress);
        httpProxyServerBootstrap.withServerResolver(HttpFilterResolverFactory.get());
        boolean proxy_tls = HttpFilterConstant.ON.equals(ConfClient.get("server.tls"));
        if (proxy_tls) {
            logger.info("开启TLS支持");
            httpProxyServerBootstrap
                    //不验证client端证书
                    .withAuthenticateSslClients(false)
                    .withSslEngineSource(new SelfSignedSslEngineSourceExt());
        } 
        //milliseconds - 40seconds
        int timeout = Integer.parseInt(ConfClient.get("connect.timeout","40000"));
        httpProxyServerBootstrap.withConnectTimeout(timeout);        
        httpProxyServerBootstrap.withAllowRequestToOriginServer(true)
                .withProxyAlias(ConfClient.get("server.tls.alias"))
                .withThreadPoolConfiguration(threadPoolConfiguration)
                //X-Real-IP,XFF设置
                .plusActivityTracker(new ActivityTrackerAdapter() {
                    @Override
                    public void requestReceivedFromClient(FlowContext flowContext,
                                                          HttpRequest httpRequest) {

                    	//如何设置真实IP
                        List<String> headerValues = HttpFilterConstant.getHeaderValues(httpRequest, HttpFilterConstant.X_Real_IP);
                        List<String> headerValues2 = HttpFilterConstant.getHeaderValues(httpRequest, HttpFilterConstant.X_Forwarded_For);
                        if (headerValues.size() == 0) {
                        	if (headerValues2 != null && headerValues2.size() > 0) {
                        		httpRequest.headers().add(HttpFilterConstant.X_Real_IP, headerValues2.get(0));
                        	} else {
                        		String remoteAddress = flowContext.getClientAddress().getAddress().getHostAddress();
                                httpRequest.headers().add(HttpFilterConstant.X_Real_IP, remoteAddress);
                        	}
                        }

                        //设置XFF
                        StringBuilder xff = new StringBuilder();
                        if (headerValues2.size() > 0 && headerValues2.get(0) != null) {
                            //逗号面一定要带一个空格
                            xff.append(headerValues2.get(0)).append(", ");
                        }
                        xff.append(NetUtils.getLocalHost());
                        httpRequest.headers().set(HttpFilterConstant.X_Forwarded_For, xff.toString());
                    }
                })
                .withFiltersSource(new HttpFiltersSourceAdapter() {
                    @Override
                    public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
                        return new HttpFilterAdapterImpl(originalRequest, ctx);
                    }
                }).start();
        ServerListenerHelper.started();
        logger.info("Gateway started on port(s) " + inetSocketAddress.getPort() + " with context path '/'");
        
        // add shutdown hook to stop server
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                	ServerListenerHelper.stopped();
                	logger.info("Gateway stopped on port(s) " + inetSocketAddress.getPort() + " with context path '/'");
                } catch (Exception e) {
                    logger.error("failed to stop gateway.", e);
                }
            }
        });
    	
    }
}