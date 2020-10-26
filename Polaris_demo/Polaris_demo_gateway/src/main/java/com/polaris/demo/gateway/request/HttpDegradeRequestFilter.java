package com.polaris.demo.gateway.request;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.polaris.container.gateway.pojo.HttpFile;
import com.polaris.container.gateway.pojo.HttpFilterMessage;
import com.polaris.container.gateway.request.HttpRequestFilter;
import com.polaris.container.gateway.util.FileReaderUtil;
import com.polaris.core.Constant;
import com.polaris.core.pojo.KeyValuePair;
import com.polaris.core.util.PropertyUtil;
import com.polaris.core.util.ResultUtil;
import com.polaris.core.util.StringUtil;

import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;

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
 * 降级，针对URL
 */
public class HttpDegradeRequestFilter extends HttpRequestFilter {
	private static Logger logger = LoggerFactory.getLogger(HttpDegradeRequestFilter.class);
	private static Set<String> degradeUrlSet = new HashSet<>();
	private static String degradeMessageCode = Constant.RESULT_FAIL;
	private static String degradeMessage = Constant.MESSAGE_GLOBAL_ERROR;

	@Override
	public void onChange(HttpFile file) {
    	Set<String> tempDegradeUrlSet = new HashSet<>();
    	String tempDegradeMessageCode = null;
    	String tempDegradeMessage = null;
    	for (String conf : FileReaderUtil.getDataSet(file.getData())) {
    		KeyValuePair kv = PropertyUtil.getKVPair(conf);
			if (kv != null && StringUtil.isNotEmpty(kv.getValue())) {
				// degrade.url
    			if (kv.getKey().equals("degrade.url")) {
					tempDegradeUrlSet.add(kv.getValue());
    			}
    			// degrade.message
    			if (kv.getKey().equals("degrade.message.code")) {
					tempDegradeMessageCode = kv.getValue();
    			}
    			// degrade.message
    			if (kv.getKey().equals("degrade.message")) {
					tempDegradeMessage = kv.getValue();
    			}
			}
    	}
    	if (tempDegradeMessageCode != null) {
        	degradeMessageCode = tempDegradeMessageCode;
    	}
    	if (tempDegradeMessage != null) {
        	degradeMessage = tempDegradeMessage;
    	}
    	degradeUrlSet = tempDegradeUrlSet;
    }
    
	@Override
    public HttpFilterMessage doFilter(HttpRequest originalRequest,HttpObject httpObject) {
        if (httpObject instanceof HttpRequest) {
            logger.debug("filter:{}", this.getClass().getName());
            
            //获取request
            HttpRequest httpRequest = (HttpRequest)httpObject;

            //降级URL
            String url = HttpCCRequestFilter.getUrl(httpRequest);
            if (degradeUrlSet.size() > 0 && degradeUrlSet.contains(url)) {
                HttpFilterMessage httpMessage = new HttpFilterMessage();
            	httpMessage.setResult(ResultUtil.create(degradeMessageCode,degradeMessage).toJSONString());
            	return httpMessage;
            }
        }
        return null;
    }

}


