package com.polaris.core.config.value;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.polaris.core.config.ConfClient;
import com.polaris.core.config.PlaceholderHelper;


@Component
public class AutoUpdateConfigChangeListener implements BeanFactoryAware{
  private static final Logger logger = LoggerFactory.getLogger(AutoUpdateConfigChangeListener.class);

  private final boolean typeConverterHasConvertIfNecessaryWithFieldParameter;
  private ConfigurableBeanFactory beanFactory = null;
  private TypeConverter typeConverter = null;

  public AutoUpdateConfigChangeListener(){
    this.typeConverterHasConvertIfNecessaryWithFieldParameter = testTypeConverterHasConvertIfNecessaryWithFieldParameter();
  }

  public void onChange(Map<String, String> cache) {
	
	 //是否关闭@Value的自动更新
	String isAutoUpdate = ConfClient.get("value.auto.update", "true");
	if (!Boolean.valueOf(isAutoUpdate)) {
		return;
	}
	
    if (cache == null || CollectionUtils.isEmpty(cache.keySet())) {
      return;
    }
    for (String key : cache.keySet()) {
      // 1. check whether the changed key is relevant
      Collection<SpringValue> targetValues = SpringValueRegistry.get(beanFactory, key);
      if (targetValues == null || targetValues.isEmpty()) {
        continue;
      }

      // 2. update the value
      for (SpringValue val : targetValues) {
        updateSpringValue(val);
      }
    }
  }

  private void updateSpringValue(SpringValue springValue) {
    try {
      Object value = resolvePropertyValue(springValue);
      springValue.update(value);

      logger.info("Auto update polaris changed value successfully, new value: {}, {}", value,
          springValue);
    } catch (Throwable ex) {
      logger.error("Auto update polaris changed value failed, {}", springValue.toString(), ex);
    }
  }

  /**
   * Logic transplanted from DefaultListableBeanFactory
   * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#doResolveDependency(org.springframework.beans.factory.config.DependencyDescriptor, java.lang.String, java.util.Set, org.springframework.beans.TypeConverter)
   */
  private Object resolvePropertyValue(SpringValue springValue) {
    // value will never be null, as @Value and @ApolloJsonValue will not allow that
    Object value = PlaceholderHelper
        .resolvePropertyValue(beanFactory, springValue.getBeanName(), springValue.getPlaceholder());

    if (springValue.isJson()) {
      value = parseJsonValue((String)value, springValue.getGenericType());
    } else {
      if (springValue.isField()) {
        // org.springframework.beans.TypeConverter#convertIfNecessary(java.lang.Object, java.lang.Class, java.lang.reflect.Field) is available from Spring 3.2.0+
        if (typeConverterHasConvertIfNecessaryWithFieldParameter) {
          value = this.typeConverter
              .convertIfNecessary(value, springValue.getTargetType(), springValue.getField());
        } else {
          value = this.typeConverter.convertIfNecessary(value, springValue.getTargetType());
        }
      } else {
        value = this.typeConverter.convertIfNecessary(value, springValue.getTargetType(),
            springValue.getMethodParameter());
      }
    }

    return value;
  }

  private Object parseJsonValue(String json, Type targetType) {
    try {
    	return JSON.parseObject(json, targetType.getClass());
    } catch (Throwable ex) {
      logger.error("Parsing json '{}' to type {} failed!", json, targetType, ex);
      throw ex;
    }
  }

  private boolean testTypeConverterHasConvertIfNecessaryWithFieldParameter() {
    try {
      TypeConverter.class.getMethod("convertIfNecessary", Object.class, Class.class, Field.class);
    } catch (Throwable ex) {
      return false;
    }

    return true;
  }

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = (ConfigurableBeanFactory)beanFactory;
	    this.typeConverter = this.beanFactory.getTypeConverter();

	}
}