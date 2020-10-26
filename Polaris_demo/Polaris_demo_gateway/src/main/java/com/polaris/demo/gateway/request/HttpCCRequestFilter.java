package com.polaris.demo.gateway.request;

import java.io.File;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;
import com.google.common.util.concurrent.RateLimiter;
import com.polaris.container.gateway.HttpConstant;
import com.polaris.container.gateway.pojo.HttpFile;
import com.polaris.container.gateway.pojo.HttpFilterMessage;
import com.polaris.container.gateway.request.HttpRequestFilter;
import com.polaris.container.gateway.util.FileReaderUtil;
import com.polaris.core.Constant;
import com.polaris.core.pojo.KeyValuePair;
import com.polaris.core.pojo.Result;
import com.polaris.core.thread.ThreadPoolBuilder;
import com.polaris.core.util.PropertyUtil;
import com.polaris.core.util.ResultUtil;
import com.polaris.core.util.StringUtil;
import com.polaris.extension.cache.Cache;
import com.polaris.extension.cache.CacheFactory;

import cn.hutool.core.io.FileUtil;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author:Tom.Yu
 *
 * Description:
 * cc拦截
 */
/**
 * @author:Tom.Yu
 *
 * Description:
 * cc拦截
 */
public class HttpCCRequestFilter extends HttpRequestFilter {
	private static Logger logger = LoggerFactory.getLogger(HttpCCRequestFilter.class);
	
    //控制总的流量
	public static volatile RateLimiter totalRateLimiter;
	public static volatile int int_all_rate = 0;
	public static volatile int int_all_timeout=3;//最大等待3秒返回
	
	//无需验证的IP
	public static volatile Set<String> ccSkipIp = new HashSet<>();
	
	//无需验证的URL
	public static volatile Set<String> ccSkipUrl = new HashSet<>();
	
	//ip维度，每秒钟的访问数量
	public static volatile LoadingCache<String, AtomicInteger> secIploadingCache;
	public static volatile LoadingCache<String, AtomicInteger> minIploadingCache;
	public static volatile int[] int_ip_rate = {10,60};
	
	//被禁止的IP是否要持久化磁盘 
	public static volatile boolean isBlackIp = false;
	public static volatile Cache blackIpCache = CacheFactory.getCache("cc.black.ip");//被禁止的ip
	public static volatile Cache statisticsIpCache = CacheFactory.getCache("cc.statistics.ip");//统计用的缓存
	public static volatile Integer blockSeconds = 60;
	public static volatile boolean ipPersistent = false;
	public static volatile String ipSavePath = "";
	private ThreadPoolExecutor threadPool = null;
	private DateTimeFormatter dataFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private DateTimeFormatter dataFormat2=DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
	
	static {
		
		//IP维度，每秒钟的访问数量
		secIploadingCache = CacheBuilder.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(1, TimeUnit.SECONDS)
                .build(new CacheLoader<String, AtomicInteger>() {
                    @Override
                    public AtomicInteger load(String key) throws Exception {
                    	return new AtomicInteger(0);
                    }
                });
		
		//IP维度，每秒钟的访问数量
		minIploadingCache = CacheBuilder.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build(new CacheLoader<String, AtomicInteger>() {
                    @Override
                    public AtomicInteger load(String key) throws Exception {
                    	return new AtomicInteger(0);
                    }
                });
    }
	
	@Override
	public void onChange(HttpFile file) {
    	int blockSecondsTemp = 60;
    	boolean isBlackIpTemp = false;
    	int[] IP_RATE = {10,60};
    	int ALL_RATE = 300;
    	int int_all_timeout_temp = 3;
    	boolean ipPersistentTemp = false;
    	String ipSavePathTemp = null;
    	
    	Set<String> tempCcSkipIp = new HashSet<>();
    	Set<String> tempCcSkipUrl = new HashSet<>();
    	for (String conf : FileReaderUtil.getDataSet(file.getData())) {
    		KeyValuePair kv = PropertyUtil.getKVPair(conf);
			if (kv != null) {
	    		// skip.ip
				if (kv.getKey().equals("cc.skip.ip")) {
					try {
						String[] ips = kv.getValue().split(",");
						for (String ip : ips) {
							tempCcSkipIp.add(ip);
						}
					} catch (Exception ex) {
					}
				}
				// skip.ip
				if (kv.getKey().equals("cc.skip.url")) {
					try {
						String[] urls = kv.getValue().split(",");
						for (String url : urls) {
							tempCcSkipUrl.add(url);
						}
					} catch (Exception ex) {
					}
				}
				// ip.rate
				if (kv.getKey().equals("cc.ip.rate")) {
					try {
						String[] rates = kv.getValue().split(",");
						if (rates.length == 1) {
							IP_RATE = new int[]{Integer.parseInt(rates[0]),60};
						} else {
							IP_RATE = new int[]{Integer.parseInt(rates[0]),Integer.parseInt(rates[1])};
						}
					} catch (Exception ex) {
					}
				}
				// all.rate
				if (kv.getKey().equals("cc.all.rate")) {
					try {
						ALL_RATE = Integer.parseInt(kv.getValue());
					} catch (Exception ex) {
					}
				}
				// all.rate
				if (kv.getKey().equals("cc.all.timeout")) {
					try {
						int_all_timeout_temp = Integer.parseInt(kv.getValue());
					} catch (Exception ex) {
					}
				}
				// 被禁止IP的时间-seconds
				if (kv.getKey().equals("cc.ip.block")) {
					try {
						isBlackIpTemp = Boolean.parseBoolean(kv.getValue());
					} catch (Exception ex) {
					}
				}
				if (kv.getKey().equals("cc.ip.block.seconds")) {
					try {
	    				blockSecondsTemp = Integer.parseInt(kv.getValue());
					} catch (Exception ex) {
					}
				}
				
				// 被禁止IP的是否持久化
				if (kv.getKey().equals("cc.ip.persistent")) {
					try {
						ipPersistentTemp = Boolean.parseBoolean(kv.getValue());
					} catch (Exception ex) {
					}
				}
				
				// 持久化地址
				if (kv.getKey().equals("cc.ip.persistent.path")) {
					ipSavePathTemp = kv.getValue();
				}
			}
    	}
    	
    	//无需验证的IP
    	ccSkipIp = tempCcSkipIp;
    	
    	//无需验证的URL
    	ccSkipUrl = tempCcSkipUrl;
    	
    	//总访问量
    	if (int_all_rate != ALL_RATE) {
    		int_all_rate = ALL_RATE;
    		totalRateLimiter = RateLimiter.create(int_all_rate);//控制总访问量
    	}
    	int_all_timeout = int_all_timeout_temp;
		
		//IP地址维度的
		int_ip_rate = IP_RATE;//单个IP的访问访问量
		
		//被限制IP的访问时间
		isBlackIp = isBlackIpTemp;
        blockSeconds = blockSecondsTemp;
        ipPersistent = ipPersistentTemp;
        ipSavePath = ipSavePathTemp;
    }
	
	@Override
	public void doStart() {
        threadPool = ThreadPoolBuilder.newBuilder()
                                      .poolName("HttpCCRequestFilter Thread Pool")
                                      .coreThreads(1)
                                      .maximumThreads(1)
                                      .keepAliveSeconds(10l)
                                      .workQueue(new LinkedBlockingDeque<Runnable>(10000))
                                      .build();
    }
	@Override
	public void doStop() {
		ThreadPoolBuilder.destroy(threadPool);
	}
    
	@Override
    public HttpFilterMessage doFilter(HttpRequest originalRequest,HttpObject httpObject) {
        if (httpObject instanceof HttpRequest) {
            logger.debug("filter:{}", this.getClass().getName());
            String realIp = HttpConstant.getRealIp((HttpRequest) httpObject);
            
            //控制总流量，超标直接返回
            HttpRequest httpRequest = (HttpRequest)httpObject;
            String url = getUrl(httpRequest);
            
            //view black ip list
            HttpFilterMessage httpMessage = viewBlackLog(url);
            if (httpMessage != null) {
            	return httpMessage;
            }
            
            //判断是否是无需验证的IP
            if (ccSkipIp.contains(realIp)) {
            	return null;
            }
            
            //无需验证的url
            if (ccSkipUrl.contains(url)) {
            	return null;
            }
            
        	//是否黑名单
        	if (isBlackIp && blackIpCache.get(realIp) != null){
        		String message = realIp + " access has exceeded ";
        		httpMessage = new HttpFilterMessage();
        		httpMessage.setResult(ResultUtil.create(Constant.RESULT_FAIL,message).toJSONString());
                hackLog(logger, realIp, "cc", message);
        		return httpMessage;
        	}

        	//cc攻击
            if (ccHack(url, realIp)) {
            	String message = httpRequest.uri() + " " + realIp + " access  has exceeded ";
            	httpMessage = new HttpFilterMessage();
            	httpMessage.setResult(ResultUtil.create(Constant.RESULT_FAIL,message).toJSONString());
                hackLog(logger, realIp, "cc", message);
            	return httpMessage;
            }
            
            //统计
            saveStatisticsLog(url, realIp);
        }
        return null;
    }
	
	private HttpFilterMessage viewBlackLog(String url) {
        if (url.equals("/gateway/cc/ip")) {
        	@SuppressWarnings("rawtypes")
			Result<List> dto = new Result<>();
        	dto.setCode(Constant.RESULT_SUCCESS);
    		List<String> dataList = new ArrayList<>();
        	try {
            	for (Object key : blackIpCache.getKeys()) {
            		Object obj = blackIpCache.get(key);
            		if (obj != null) {
            			dataList.add(key + " " +obj.toString());
            		}
            	}
        	} catch (Exception ex) {}
        	dto.setData(dataList);
        	HttpFilterMessage httpMessage = new HttpFilterMessage();
        	httpMessage.setStatus(HttpResponseStatus.OK);
        	httpMessage.setResult(dto.toJSONString());
        	return httpMessage;
        }
        return null;
	}
	
	public boolean doSentinel(String url, String realIp) {
    	Entry entry = null;
    	try {
            SphU.entry(url, EntryType.IN, 1, realIp);
            return false;
        } catch (BlockException e) {
        	
        	//热点参数目前只有IP维度限流，限流的场合直接加入黑名单
        	if (e instanceof ParamFlowException) {
        		saveBlackLog(realIp,"sentinel block "+((ParamFlowException)e).getLimitParam());//拒绝
        	}
        	return true;
        } finally {
        	if (entry != null) {
                entry.exit(1,realIp);
            }
        }
    }
    
    public static String getUrl(HttpRequest httpRequest) {
        String uri = httpRequest.uri();
        int index = uri.indexOf("?");
        if (index > 0) {
            return uri.substring(0, index);
        } 
        return uri;
    }
    
    public boolean ccHack(String url, String realIp) {
       	
    	//IP每秒访问
		try {
			AtomicInteger secRateLimiter = (AtomicInteger) secIploadingCache.get(realIp);
	        int count = secRateLimiter.incrementAndGet();
	        if (count > int_ip_rate[0]) {
	        	saveBlackLog(realIp, count + " visits per second");//拒绝
	    		return true;//拒绝
	        } 
		} catch (ExecutionException e) {
			logger.error(e.toString());
        	return true;
		}
		
		//IP每分访问
		try {
	        AtomicInteger minRateLimiter = (AtomicInteger) minIploadingCache.get(realIp);
	        int count = minRateLimiter.incrementAndGet();
	        if (count > int_ip_rate[1]) {
	        	saveBlackLog(realIp, count + " visits per minute");//拒绝
	    		return true;//拒绝
	        } 
		} catch (ExecutionException e) {
			logger.error(e.toString());
        	return true;
		}
		
        //总量控制
		try {
	        if (!totalRateLimiter.tryAcquire(1, int_all_timeout, TimeUnit.SECONDS)) {
	            return true;
	        }
		} catch (Exception ex) {
			logger.error(ex.toString());
        	return true;
		}
		
		//sentinel控制
		if (doSentinel(url, realIp)) {
			return true;
		}

        return false;
    }
    
    public void saveStatisticsLog(String url, String realIp) {
    	if (ipPersistent && StringUtil.isNotEmpty(ipSavePath)) {
    		try {
    			threadPool.execute(new Runnable() {
    				@Override
    				public void run() {
    					LocalDateTime time = LocalDateTime.now();
    		    		String fileNamePrefix = dataFormat.format(time);
    					String path = ipSavePath + File.separator + fileNamePrefix+"_statistics.txt";
    					try {
							Files.asCharSink(FileUtil.touch(path), Charset.defaultCharset(), FileWriteMode.APPEND).write(dataFormat2.format(time) + " " + realIp + " " +url);
							Files.asCharSink(FileUtil.touch(path), Charset.defaultCharset(), FileWriteMode.APPEND).write(FileUtil.getLineSeparator());
    					} catch (Exception e) {
							logger.error(e.getMessage());
						} 
    				}
    			});
    		} catch (Exception ex) {
    			logger.error("Error:",ex);
    		}
		}
    }
    
    public void saveBlackLog(String realIp, String reason) {
    	if (isBlackIp) {
    		logger.info("ip:{} is blocked ,caused by:{}",realIp, reason);
    		blackIpCache.put(realIp, reason ,blockSeconds);//拒绝
    		
    		//持久化
    		if (ipPersistent && StringUtil.isNotEmpty(ipSavePath)) {
    			try {
    				threadPool.execute(new Runnable() {
    					@Override
    					public void run() {
    						LocalDateTime time = LocalDateTime.now();
    		    			String fileNamePrefix = dataFormat.format(time);
    						String path = ipSavePath + File.separator + fileNamePrefix+"_black.txt";
    						try {
								Files.asCharSink(FileUtil.touch(path), Charset.defaultCharset(), FileWriteMode.APPEND).write(dataFormat2.format(time) + " " + realIp + " " +reason);
								Files.asCharSink(FileUtil.touch(path), Charset.defaultCharset(), FileWriteMode.APPEND).write(FileUtil.getLineSeparator());
							} catch (Exception e) {
								logger.error(e.getMessage());
							} 
    					}
        			});
    			}catch (Exception ex) {
        			logger.error("Error:",ex);
        		}
    			
    		}
    	}
    }
}


