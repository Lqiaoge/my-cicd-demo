package com.windcore.exception;


import com.windcore.response.WcErrorCode;
import com.windcore.response.WcResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.IOException;
import java.util.stream.Collectors;

/**
 * @author 乔哥
 * @version v1.0
 * @Date: 2025-01-08 21:53
 * @Desctiption: 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class ExceptionHandlerAdvice {


    /**
     * 参数错误
     *
     * @param ex
     * @return
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public WcResponse bindException(BindException ex) {
        log.error("参数错误!", ex);
        final String message = ex.getBindingResult().getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.joining("\n"));
        return WcResponse.fail(WcErrorCode.PARAMS_ERROR.getCode(), message);
    }

    /**
     * 参数错误
     * */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public WcResponse missingServletRequestParameterException(MissingServletRequestParameterException ex) {
        log.error("参数错误!", ex);
        return WcResponse.fail(WcErrorCode.PARAMS_ERROR.getCode(), ex.getMessage());
    }

    /**
     * 数据格式有误
     *
     * @param ex
     * @return
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public WcResponse httpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.error("数据格式有误!", ex);
        return WcResponse.fail(WcErrorCode.PARAMS_ERROR.getCode(), WcErrorCode.PARAMS_ERROR.getMsg());
    }


    /**
     * 文件大小超出限制
     *
     * @param ex
     * @return
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public WcResponse maxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        log.error("文件大小超出限制!", ex);
        return WcResponse.fail(WcErrorCode.FILE_SIZE_ERROR.getCode(), WcErrorCode.FILE_SIZE_ERROR.getMsg());
    }

    /**
     * IO异常
     *
     * @param ex
     * @return
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(IOException.class)
    public WcResponse iOException(IOException ex) {
        log.error("IO异常!", ex);
        return WcResponse.fail(WcErrorCode.IO_ERROR.getCode(), WcErrorCode.IO_ERROR.getMsg());
    }

    /**
     * 自定义逻辑异常
     *
     * @param ex
     * @return
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(LogicException.class)
    public WcResponse logicException(LogicException ex) {
        log.error("逻辑异常!", ex);
        return WcResponse.fail(WcErrorCode.PARAMS_ERROR.getCode(), ex.getMessage());
    }



    /**
     * 禁止访问异常
     *
     * @param ex
     * @return
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(ForbiddenException.class)
    public WcResponse forbiddenException(ForbiddenException ex) {
        log.info("校验登录信息失败!", ex);
        return WcResponse.fail(WcErrorCode.NO_AUTH.getCode(), "您没有权限！");
    }

    /**
     * 默认异常
     *
     * @param ex
     * @return
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public WcResponse exception(Exception ex) {
        log.error("未知错误!", ex);
        return WcResponse.fail(WcErrorCode.FAIL.getCode(), WcErrorCode.FAIL.getMsg());
    }

    /**
     * 参数校验异常
     *
     * @param ex
     * @return
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public WcResponse methodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error("参数校验异常!", ex);
        final String message = ex.getBindingResult().getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).distinct().collect(Collectors.joining("<br/>"));
        return WcResponse.fail(WcErrorCode.PARAMS_ERROR.getCode(), message);
    }

    /**
     * 参数校验异常
     *
     * @param ex
     * @return
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public WcResponse violationException(ConstraintViolationException ex) {
        log.error("参数校验异常!", ex);
        final String message = ex.getConstraintViolations().stream().map(constraintViolation -> constraintViolation.getMessage()).distinct().collect(Collectors.joining("<br/>"));
        return WcResponse.fail(WcErrorCode.PARAMS_ERROR.getCode(), message);
    }
}
