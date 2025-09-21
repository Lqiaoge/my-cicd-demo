package com.windcore.response;

import lombok.Data;

/**
 * @author qiaoge
 * @version 1.0
 * @description 统一结果返回
 * @date 2023/4/30 11:59
 */

@Data
public class WcResponse<T> {

    private int code;
    private T data;
    private String msg;

    public WcResponse() {
    }

    public WcResponse(int code, T data, String msg) {
        this.code = code;
        this.data = data;
        this.msg = msg;
    }

    public static <T> WcResponse<T> fail(String msg) {
        WcResponse<T> response = new WcResponse<T>();
        response.setCode(WcErrorCode.FAIL.getCode());
        response.setMsg(msg);
        return response;
    }

    public static <T> WcResponse<T> fail(int code, String msg){
        WcResponse<T> response = new WcResponse<T>();
        response.setCode(code);
        response.setMsg(msg);
        return response;
    }
    public static <T> WcResponse<T> fail(T data, String msg) {
        WcResponse<T> response = new WcResponse<T>();
        response.setCode(WcErrorCode.FAIL.getCode());
        response.setData(data);
        response.setMsg(msg);
        return response;
    }

    public static <T> WcResponse<T> success(T data) {
        WcResponse<T> response = new WcResponse<T>();
        response.setData(data);
        response.setCode(200);
        response.setMsg("操作成功");
        return response;
    }
    public static <T> WcResponse<T> success(T data, String msg) {
        WcResponse<T> response = new WcResponse<T>();
        response.setData(data);
        response.setMsg(msg);
        response.setCode(200);
        return response;
    }

    public static <T> WcResponse<T> success() {
        return new WcResponse<T>(200, null, "操作成功");
    }

}
