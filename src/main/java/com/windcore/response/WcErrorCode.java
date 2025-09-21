package com.windcore.response;

/***
 * @description 错误码枚举类
 * @author 76998
 * @date 2025-01-07 22:42
 * @throws
 */
public enum WcErrorCode {

    SUCCESS(200, "操作成功"),
    FAIL(500, "未知错误"),
    PARAMS_ERROR(40000, "请求参数错误"),
    NULL_ERROR(40001, "请求数据为空"),
    NOT_LOGIN(40100, "未登录"),
    NO_AUTH(40101, "无权限"),
    AUTH_FAIL(40102,"账号或密码错误"),
    SYSTEM_ERROR(50000, "系统内部异常"),
    FILE_SIZE_ERROR(50001, "文件大小超出限制"),
    IO_ERROR(50002, "IO异常"),
    CODE_ERROR(51000, "编码错误");

    /** 错误码 */
    private int code;
    /** 错误信息 */
    private String msg;

    WcErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
