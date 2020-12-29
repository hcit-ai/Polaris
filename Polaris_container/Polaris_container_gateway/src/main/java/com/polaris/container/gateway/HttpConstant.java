package com.polaris.container.gateway;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.polaris.container.gateway.pojo.HttpProtocolForConnection;

import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpRequest;

/**
 * @author:Tom.Yu
 *
 * Description:
 *
 */
public class HttpConstant {
    enum X_Frame_Options {
        DENY,//表示该页面不允许在 frame 中展示,即便是在相同域名的页面中嵌套也不允许.
        SAMEORIGIN//表示该页面可以在相同域名页面的 frame 中展示.
    }
    public static int AcceptorThreads = HttpProtocolForConnection.getAcceptorThreads();
    public static int ClientToProxyWorkerThreads = HttpProtocolForConnection.getClientToProxyWorkerThreads();
    public static int ProxyToServerWorkerThreads = HttpProtocolForConnection.getProxyToServerWorkerThreads();
    public static X_Frame_Options X_Frame_Option = X_Frame_Options.SAMEORIGIN;
    public static final String X_Forwarded_For = "X-Forwarded-For";
    public static final String X_Real_IP = "X-Real-IP";
    public static final String OFF = "off";
    public static final String ON = "on";
    
    public static final String HOST = "Host";
    public static final String DEFAULT="default";
    public static final String CLAIMS = "claims";
    
    public static final String COLON = ":";
    public static final String SLASH = "/";
    
    public static final String REVERSE_PROXY = "reverseProxy";//反向代理，采用context来区分
    public static final String CONNECT_PROXY = "connectProxy";//connect连接方式
    public static final String DNS_PROXY = "dnsProxy";//DNS代理

    public static String getRealIp(HttpRequest httpRequest) {
	    List<String> headerValues = getHeaderValues(httpRequest, X_Real_IP);
	    if (headerValues != null && headerValues.size() > 0) {
		    return headerValues.get(0);
	    }
    	try {
			return getIpAddress(httpRequest);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null;
    }

    /**
     * RFC7230/RFC7231/RFC7232/RFC7233/RFC7234
     * Each header field consists of a case-insensitive field name followed
     * by a colon (":"), optional leading whitespace, the field value, and
     * optional trailing whitespace.
     *
     * @param httpMessage
     * @param headerName
     * @return headerValue
     */
    public static List<String> getHeaderValues(HttpMessage httpMessage, String headerName) {
        List<String> list = Lists.newArrayList();
        for (Map.Entry<String, String> header : httpMessage.headers().entries()) {
            if (header.getKey().toLowerCase().equals(headerName.toLowerCase())) {
                list.add(header.getValue());
            }
        }
        return list;
    }
    
    /**
     * 获取请求主机IP地址,如果通过代理进来，则透过防火墙获取真实IP地址;
     *
     * @param request
     * @return
     * @throws IOException
     */
    public static String getIpAddress(HttpRequest request) throws IOException {
        // 获取请求主机IP地址,如果通过代理进来，则透过防火墙获取真实IP地址

        String ip = request.headers().get("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.headers().get("Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.headers().get("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.headers().get("HTTP_CLIENT_IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.headers().get("HTTP_X_FORWARDED_FOR");
            }
            if (ip == null || ip.length() == 0 || "127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
                try {
                    ip = InetAddress.getLocalHost().getHostAddress();
                } catch (UnknownHostException unknownhostexception) {
                }
            }
        } else if (ip.length() > 15) {
            String[] ips = ip.split(",");
            for (int index = 0; index < ips.length; index++) {
                String strIp = (String) ips[index];
                if (!("unknown".equalsIgnoreCase(strIp))) {
                    ip = strIp;
                    break;
                }
            }
        }
        return ip;
    }
}
