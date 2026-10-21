package edu.pkmon.capture.port;

/** 当前用户端口。userId 只从这里取得，任何接口都不接受请求体传入 userId。 */
public interface CurrentUserProvider {

    String currentUserId();
}
