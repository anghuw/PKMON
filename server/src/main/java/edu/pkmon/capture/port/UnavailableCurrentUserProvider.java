package edu.pkmon.capture.port;

import edu.pkmon.capture.error.ApiErrorCode;
import edu.pkmon.capture.error.ApiException;

/**
 * 认证未接入时的身份来源：一律拒绝，不做任何猜测。
 * 用于 pkmon.capture.dev-user-header=false 的配置，避免开发期替身流入正式环境。
 */
public final class UnavailableCurrentUserProvider implements CurrentUserProvider {

    @Override
    public String currentUserId() {
        throw new ApiException(ApiErrorCode.AUTH_NOT_READY);
    }
}
