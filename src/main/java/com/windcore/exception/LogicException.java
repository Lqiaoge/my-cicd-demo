package com.windcore.exception;


/**
 * @author 乔哥
 * @version v1.0
 * @Date: 2025-01-08 22:02
 * @Desctiption: 逻辑错误
 */
public class LogicException extends RuntimeException{
    public LogicException(String message){
        super(message);
    }
}
