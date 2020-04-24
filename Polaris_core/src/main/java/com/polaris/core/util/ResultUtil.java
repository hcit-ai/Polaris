package com.polaris.core.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.polaris.core.Constant;
import com.polaris.core.pojo.Result;

/**
 * @author 
 * @ClassName: ResultUtil
 * @Description: 接口返回的结果工具类
 * @date 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ResultUtil {

	private static final String MSG_SUCCESS = "success";

    private static final String MSG_FAIL = "fail";

    public static Result success() {
        return ResultUtil.success(MSG_SUCCESS);
    }


    public static Result success(Object val) {
        return ResultUtil.success(MSG_SUCCESS, val);
    }

    public static Result success(String msg, Object val) {
        return new Result(Constant.RESULT_SUCCESS, msg, val);
    }

    public static Result fail() {
        return ResultUtil.fail(MSG_FAIL);
    }

    public static Result fail(String msg) {
        return new Result(Constant.RESULT_FAIL, msg);
    }

    public static Result fail(String msg, String detailMsg, Object val) {
        return new Result(Constant.RESULT_FAIL, msg, detailMsg, val);
    }

	public static Result fail(String msg, Object val) {
        return new Result(Constant.RESULT_FAIL, msg, val);
    }
	
	public static Result get(String json) {
		if (StringUtil.isEmpty(json)) {
			return null;
		}
		try {
			return JSON.parseObject(json, new TypeReference<Result>() {});
    	} catch (Exception ex) {
    		return null;
    	}
	}
	
	public static Result create(String code,  String message) {
    	Result result = new Result();
    	result.setCode(code);
    	result.setMessage(message);
    	return result;
    }

}
