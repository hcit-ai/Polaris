package com.polaris.naming;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;

import com.polaris.core.config.ConfClient;
import com.polaris.core.naming.NamingHandler;
import com.polaris.core.naming.NamingHandlerOrder;
import com.polaris.core.pojo.Server;
import com.polaris.core.util.StringUtil;
import com.polaris.core.util.WeightedRoundRobinScheduling;

@Order(NamingHandlerOrder.ZK)
public class ZkServer implements NamingHandler {
	private static final Logger logger = LoggerFactory.getLogger(ZkServer.class);
	private CuratorFramework curator;
	private Map<String, ZkCache> zkCacheMap = new ConcurrentHashMap<>();
	public ZkServer() {
	}
	
	private CuratorFramework getCurator() {
		if (curator == null) {
			synchronized(this) {
				if (curator == null) {
			    	int sessionTimeoutMs = Integer.parseInt(ConfClient.get("name.registry.zk.sessionTimeoutMs", "20000"));
			    	int retryCount = Integer.parseInt(ConfClient.get("name.registry.zk.retryCount", "5"));
			    	int sleepMsBetweenRetries = Integer.parseInt(ConfClient.get("name.registry.zk.sleepMsBetweenRetries", "1000"));
			    	int connectionTimeoutMs = Integer.parseInt(ConfClient.get("name.registry.zk.connectionTimeoutMs", "12000"));
			    	curator = CuratorFrameworkFactory.newClient(ConfClient.getNamingRegistryAddress(), 
							sessionTimeoutMs, connectionTimeoutMs, new RetryNTimes(retryCount, sleepMsBetweenRetries));
					curator.start();
				}
			}
		}
		return curator;
	}

	@Override
	public Server getServer(String serviceName) {
	    if (StringUtil.isEmpty(serviceName)) {
            return null;
        }
		//get curator
		CuratorFramework curator = getCurator();
		
		//获取Group=xxx@@yyyyy
        String[] groupAndServiceName = serviceName.split(Constant.SERVICE_INFO_SPLITER);
        String group = null;
        if (groupAndServiceName.length >= 2) {
            group = groupAndServiceName[0];
            serviceName = groupAndServiceName[1];
        } else {
            group = Constant.DEFAULT_GROUP;
        }
		String childNodePathCache = getPath(serviceName,group);

        //childData：设置缓存节点的数据状态
		ZkCache zkCache = getZkCache(curator, childNodePathCache);
		if (zkCache == null || zkCache.getWeight() == null) {
			return null;
		}
		return zkCache.getWeight().getServer();
	}

	@Override
	public List<Server> getServerList(String serviceName) {
	    if (StringUtil.isEmpty(serviceName)) {
            return null;
        }
		//get curator
		CuratorFramework curator = getCurator();
		
		//获取Group=xxx@@yyyyy
        String[] groupAndServiceName = serviceName.split(Constant.SERVICE_INFO_SPLITER);
        String group = null;
        if (groupAndServiceName.length >= 2) {
            group = groupAndServiceName[0];
            serviceName = groupAndServiceName[1];
        } else {
            group = Constant.DEFAULT_GROUP;
        }
		String childNodePathCache = getPath(serviceName,group);
		
		//childData：设置缓存节点的数据状态
		ZkCache zkCache = getZkCache(curator, childNodePathCache);
		if (zkCache == null) {
			return null;
		}
				
		//获取所有子节点
        List<ChildData> childDataList = zkCache.getCache().getCurrentData();
        List<Server> childList = new ArrayList<>();
        for(ChildData cd : childDataList){
        	String data = new String(cd.getData());
        	String[] datas = data.split(":");
    		childList.add(Server.of(datas[0], Integer.parseInt(datas[1]),Integer.parseInt(datas[2])));
        }
        return childList;
	}

	@Override
	public boolean register(Server server) {
		String ip = server.getIp();
		int port = server.getPort();
		//get curator
		CuratorFramework curator = getCurator();
		
		//register-data
		String regContent = ip + ":" + port+ ":" + ConfClient.get(Constant.PROJECT_WEIGHT, Constant.PROJECT_WEIGHT_DEFAULT);
		String zkRegPathPrefix = getPath(ConfClient.getAppName(),ConfClient.getAppGroup()) + Constant.SLASH + "server-provider-";
		
		//re-connect
		ZkConnectionStateListener stateListener = new ZkConnectionStateListener(zkRegPathPrefix, regContent);
		curator.getConnectionStateListenable().addListener(stateListener);
		
		//create node
		try {
			curator.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
			.forPath(zkRegPathPrefix, regContent.getBytes(Constant.UTF_CODE));
			return  true;
		} catch (Exception ex) {
			logger.error("ERROR:",ex);
			return false;
		}
	}

	@Override
	public boolean deregister(Server server) {
		for (ZkCache zkCache : zkCacheMap.values()) {
			try {
				zkCache.getCache().close();
			} catch (IOException ex) {
				logger.error("ERROR:",ex);
			}
		}
		getCurator().close();
		return true;
	}
	
	private String getPath(String key, String group) {
		StringBuilder groupSb = new StringBuilder();
		
		//rootPath
		groupSb.append(ConfClient.get("naming.zk.root.path",Constant.NAMING_ROOT_PATH));
		groupSb.append(Constant.SLASH);

		//namespace
		String nameSpace = ConfClient.getNameSpace();
		if (StringUtil.isNotEmpty(nameSpace)) {
			groupSb.append(nameSpace);
			groupSb.append(Constant.SLASH);
		}
		
		//group
        groupSb.append(group);
        groupSb.append(Constant.SLASH);
		
		//key
		groupSb.append(key);
		
		//返回
		return groupSb.toString();
	}
	
	@SuppressWarnings("deprecation")
	private ZkCache getZkCache(CuratorFramework client, String childNodePathCache) {
		
		ZkCache zkCache = zkCacheMap.get(childNodePathCache);
		try {
			if (zkCache == null) {
				synchronized(childNodePathCache.intern()) {
					zkCache = zkCacheMap.get(childNodePathCache);
					if (zkCache == null) {
						
						//create
						PathChildrenCache childrenCache = new PathChildrenCache(client,childNodePathCache,true);
						zkCache = new ZkCache(childrenCache);

				        /*
				        * StartMode：初始化方式
				        * POST_INITIALIZED_EVENT：异步初始化。初始化后会触发事件
				        * NORMAL：异步初始化
				        * BUILD_INITIAL_CACHE：同步初始化
				        * */
				        childrenCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);

				        //获取所有子节点
						List<Server> serverList = new ArrayList<>();
						List<ChildData> childDataList = childrenCache.getCurrentData();
				        for(ChildData cd : childDataList){
				        	String serverInfo = new String(cd.getData());
							String[] si = serverInfo.split(":");
				            if (si.length == 2) {
					                Server server = new Server(si[0], Integer.valueOf(si[1]), 1);
				                serverList.add(server);
				            } else if (si.length == 3) {
					                Server server = new Server(si[0], Integer.valueOf(si[1]), Integer.valueOf(si[2]));
				                serverList.add(server);
				            }
				        }
						WeightedRoundRobinScheduling wrrs = new WeightedRoundRobinScheduling(serverList);
						zkCache.setWeight(wrrs);
						PathChildrenCacheListener listener = new PathChildrenCacheListener() {
				            public void childEvent(CuratorFramework ient, PathChildrenCacheEvent event) throws Exception {
				               if(event.getType().equals(PathChildrenCacheEvent.Type.CHILD_ADDED)){
				            	   String serverInfo = new String(event.getData().getData());
				            	   String[] si = serverInfo.split(":");
				            	   wrrs.add(new Server(si[0], Integer.valueOf(si[1]), Integer.valueOf(si[2])));
				               }else if(event.getType().equals(PathChildrenCacheEvent.Type.CHILD_REMOVED)){
				            	   String serverInfo = new String(event.getData().getData());
				            	   String[] si = serverInfo.split(":");
				            	   wrrs.remove(wrrs.getServer(si[0], Integer.valueOf(si[1])));
				               }
				            }
				        };
				        childrenCache.getListenable().addListener(listener);
						zkCacheMap.put(childNodePathCache, zkCache);
					}
				}
			}
		} catch (Exception ex) {
			logger.error("ERROR:",ex);
			return null;
		}
		return zkCache;
	}
	
	protected static class ZkCache {
		private PathChildrenCache cache;
		private WeightedRoundRobinScheduling weight;
		public ZkCache(PathChildrenCache cache) {
			this.cache = cache;
		}
		public PathChildrenCache getCache() {
			return cache;
		}
		public void setCache(PathChildrenCache cache) {
			this.cache = cache;
		}
		public WeightedRoundRobinScheduling getWeight() {
			return weight;
		}
		public void setWeight(WeightedRoundRobinScheduling weight) {
			this.weight = weight;
		}
	}
}
