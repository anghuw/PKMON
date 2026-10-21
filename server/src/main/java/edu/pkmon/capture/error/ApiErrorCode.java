package edu.pkmon.capture.error;

import org.springframework.http.HttpStatus;

/**
 * 业务错误码。HTTP 状态与业务 code 同时有意义：客户端两者都要检查。
 * 40301 / 40903 / 40101 / 503xx 为本模块新增候选码，已在 design/interfaces.json 标注待与调用方确认。
 */
public enum ApiErrorCode {

    OK(0, HttpStatus.OK, "ok"),
    UNAUTHENTICATED(40101, HttpStatus.UNAUTHORIZED, "未提供可用的身份"),
    INVALID_PARAM(40001, HttpStatus.BAD_REQUEST, "参数错误"),
    DEV_ONLY(40301, HttpStatus.FORBIDDEN, "开发遭遇仅在 dev/test 环境开放"),
    NOT_FOUND(40401, HttpStatus.NOT_FOUND, "资源不存在或不属于当前用户"),
    CONFLICT_REQUEST(40902, HttpStatus.CONFLICT, "同一 requestId 提交了不同内容"),
    ALREADY_CAPTURED(40903, HttpStatus.CONFLICT, "该遭遇已收录，不能重复挑战"),
    EXPIRED(41001, HttpStatus.GONE, "挑战或遭遇已过期"),
    AUTH_NOT_READY(50301, HttpStatus.SERVICE_UNAVAILABLE, "认证上下文尚未接入"),
    QUESTION_UNAVAILABLE(50302, HttpStatus.SERVICE_UNAVAILABLE, "题库暂不可用");

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;

    ApiErrorCode(int code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public int code() {
        return code;
    }

    public HttpStatus httpStatus() {
        return httpStatus;
    }

    public String message() {
        return message;
    }
}
