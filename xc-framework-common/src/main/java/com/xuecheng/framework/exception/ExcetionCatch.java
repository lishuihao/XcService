package com.xuecheng.framework.exception;

import com.google.common.collect.ImmutableMap;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 统一异常捕获类
 */
@ControllerAdvice// 控制器增强
@ResponseBody
public class ExcetionCatch {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExcetionCatch.class);

    //定义一个map,配置异常类型所对应的异常代码
    private static ImmutableMap<Class<? extends Throwable>,ResultCode> EXCEPTIONS;
    //定义map的build对象，去构建ImmutableMap
    protected static ImmutableMap.Builder<Class<? extends Throwable>,ResultCode> builder = ImmutableMap.builder();

    //遇到CustomException，就捕获此类异常
    @ExceptionHandler(CustomException.class)
    public ResponseResult customeException(CustomException c){
        //记录日志
        LOGGER.error("catch Exception:{}",c.getMessage());
        ResultCode resultCode = c.getResultCode();
        return new ResponseResult(resultCode);
    }


    //遇到Exception(不可预知异常)，就捕获此类异常
    @ExceptionHandler(Exception.class)
    public ResponseResult exception(Exception e){
        //记录日志
        LOGGER.error("catch Exception:{}",e.getMessage());
        if(EXCEPTIONS == null){
            EXCEPTIONS=builder.build(); //ExceptionS构建成功，且无法更改
        }
        //从EXCEPTIONS中找异常类型所对应的错误代码,如果找到了就将错误代码相应给页面
        ResultCode resultCode = EXCEPTIONS.get(Exception.class);
        if(resultCode != null){
            return new ResponseResult(resultCode);
        }else{
            //如果找不到，给用户响应99999异常
            return new ResponseResult(CommonCode.SERVER_ERROR);
        }


    }

    static {
        //异常类型对用的错误代码
        builder.put(HttpMessageNotReadableException.class,CommonCode.InVALID_PARAM);
    }

}
