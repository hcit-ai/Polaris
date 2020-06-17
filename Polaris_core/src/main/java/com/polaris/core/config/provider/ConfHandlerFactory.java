package com.polaris.core.config.provider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.polaris.core.config.Config.Type;

public class ConfHandlerFactory {
	
	private static Map<Type , ConfHandlerProxy> confHandlerProviderMap = new ConcurrentHashMap<>();
	public static ConfHandlerProxy getOrCreate(Type type) {
	    ConfHandlerProxy confHandlerProxy = confHandlerProviderMap.get(type);
		if (confHandlerProxy == null) {
			synchronized(type.name().intern()) {
			    if (type == Type.SYS) {
	                confHandlerProxy = new ConfHandlerSystem(type,ConfHandlerComposite.INSTANCE);
			    } else {
	                confHandlerProxy = new ConfHandlerProxy(type,ConfHandlerComposite.INSTANCE);
			    }
				confHandlerProviderMap.put(type, confHandlerProxy);
			}
		}
		return confHandlerProxy;
		
	}
}
