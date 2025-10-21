package com.windcore.exception;

import java.io.Serial;

/**
 * 文件上传异常
 */
public class FileUploadException extends RuntimeException{
    @Serial
    private static final long serialVersionUID = 1L;

    public FileUploadException(String message){
        super(message);
    }

    public FileUploadException(String message, Throwable cause){
        super(message, cause);
    }
}
