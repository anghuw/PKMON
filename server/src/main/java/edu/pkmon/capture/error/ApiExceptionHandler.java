package edu.pkmon.capture.error;

import edu.pkmon.capture.dto.CaptureDtos.ApiEnvelope;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** 把业务异常映射为统一响应体；业务代码不关心 HTTP 状态码。 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiEnvelope> handle(ApiException e) {
        ApiErrorCode code = e.errorCode();
        return ResponseEntity.status(code.httpStatus())
                .body(ApiEnvelope.error(code.code(), e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiEnvelope> handle(IllegalArgumentException e) {
        return ResponseEntity.status(ApiErrorCode.INVALID_PARAM.httpStatus())
                .body(ApiEnvelope.error(ApiErrorCode.INVALID_PARAM.code(), e.getMessage()));
    }
}
