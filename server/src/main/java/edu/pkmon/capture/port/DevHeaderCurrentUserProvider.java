package edu.pkmon.capture.port;

import edu.pkmon.capture.error.ApiErrorCode;
import edu.pkmon.capture.error.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 临时身份来源：从 X-Dev-User 请求头取 userId。
 * 这是认证未接入前的替身，不是认证——接入闫昕的认证上下文后必须替换，
 * 并把 pkmon.capture.dev-user-header 置为 false 使本实现不再生效。
 */
public final class DevHeaderCurrentUserProvider implements CurrentUserProvider {

    public static final String HEADER = "X-Dev-User";

    @Override
    public String currentUserId() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes == null ? null : attributes.getRequest();
        String userId = request == null ? null : request.getHeader(HEADER);
        if (userId == null || userId.isBlank()) {
            throw new ApiException(ApiErrorCode.UNAUTHENTICATED,
                    "未提供 " + HEADER + " 请求头（开发期临时身份，接入认证后失效）");
        }
        return userId;
    }
}
