package com.polaris.container.gateway.pojo;

import java.util.HashMap;
import java.util.Map;

import com.polaris.core.pojo.KeyValuePair;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * 中途被拦截需要返回的信息
 *
 */
public class HttpFilterMessage {

	private String result;
	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}
	
	private HttpResponseStatus status = HttpResponseStatus.FORBIDDEN;
	public HttpResponseStatus getStatus() {
		return status;
	}
	public void setStatus(HttpResponseStatus status) {
		this.status = status;
	}
	
	private Map<String, Object> header = new HashMap<>();

	public Map<String, Object> getHeader() {
		return header;
	}
	public void putHeader(String key, Object value) {
		this.header.put(key, value);
	}
	public void putHeader(KeyValuePair pair) {
		putHeader(pair.getKey(),pair.getValue());
	}
	
	public static HttpFilterMessage of(String result,KeyValuePair... kvPairs) {
		return of(result,null,kvPairs);
	}
	public static HttpFilterMessage of(String result,HttpResponseStatus status,KeyValuePair... kvPairs) {
		HttpFilterMessage message = new HttpFilterMessage();
		if (status != null) {
			message.setStatus(status);
		}
		if (kvPairs != null) {
			for (KeyValuePair pair : kvPairs) {
				message.putHeader(pair.getKey(),pair.getValue());
			}
		}
		message.setResult(result);
		return message;
	}
	
	
}
