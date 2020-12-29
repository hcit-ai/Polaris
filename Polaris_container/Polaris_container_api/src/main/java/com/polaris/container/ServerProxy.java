package com.polaris.container;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.context.ConfigurableApplicationContext;

import com.polaris.container.listener.ServerListenerProxy;
import com.polaris.core.OrderWrapper;
import com.polaris.core.component.LifeCycle;
import com.polaris.core.component.LifeCyclePublisherWithListener;
import com.polaris.core.naming.NamingClient;

public class ServerProxy extends LifeCyclePublisherWithListener implements Server {

	/**
     * constructor ServerManager for private 
     * 
     */
	public static Server INSTANCE = new ServerProxy();
	private ServerProxy() {}
	
	/**
     * JVM shutdown hook to shutdown this server. Declared as a class-level variable to allow removing the shutdown hook when the
     * server is stopped normally.
     */
    private final Thread jvmShutdownHook = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
				stop();//关闭容器
			} catch (Exception e) {
				//ignore
			}

            
        }
    }, "ServerContainer-JVM-shutdown-hook");
    
    @Override
	protected void doStart() throws Exception {
		ServerProvider.getServer().start();
    }
    
    @Override
    protected void doStop() throws Exception {
    	ServerProvider.getServer().stop();
    }
    
    @Override
    public ConfigurableApplicationContext getContext() {
    	return ServerProvider.getServer().getContext();
    }
    
	@Override
	public void starting(LifeCycle event) {
		ServerListenerProxy.INSTANCE.starting(event);
	}

	@Override
	public void started(LifeCycle event) {
		ServerListenerProxy.INSTANCE.started(event);
        NamingClient.register();
        Runtime.getRuntime().addShutdownHook(jvmShutdownHook);
	}

	@Override
	public void failure(LifeCycle event, Throwable cause) {
		ServerListenerProxy.INSTANCE.failure(event,cause);
	}

	@Override
	public void stopping(LifeCycle event) {
        NamingClient.unRegister();
		ServerListenerProxy.INSTANCE.stopping(event);
		//lifeCycleStopped method will not be called after the service is stopped
		//so stopped method is called after the service is stopping
		ServerListenerProxy.INSTANCE.stopped(event);
	}
	
	private static class ServerProvider {
		private static final ServiceLoader<Server> servers = ServiceLoader.load(Server.class);
		private static volatile AtomicBoolean initialized = new AtomicBoolean(false);
		@SuppressWarnings("rawtypes")
		private static List<OrderWrapper> serverList = new ArrayList<OrderWrapper>();
	    private static volatile Server server;
		private ServerProvider() {}
	    public static Server getServer() {
	    	if (initialized.compareAndSet(false, true)) {
	    		for (Server server : servers) {
	        		OrderWrapper.insertSorted(serverList, server);
	            }
	        	if (serverList.size() > 0) {
	        		server = (Server)serverList.get(0).getHandler();
	        	}
	        }
	    	if (server == null) {
	    		throw new RuntimeException("Polaris_container_xxx is not found, please check the pom.xml");
	    	}
	    	return server;
	    }
	}
}
