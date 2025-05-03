package org.visage.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.visage.backend.util.R;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 自定义业务异常处理
     */
    @ExceptionHandler(ServiceException.class)
    public R<Void> handleServiceException(ServiceException e, HttpServletRequest request) {
        log.warn("业务异常: [{}] - [{}]", e.getCode(), e.getMessage());
        return R.fail(e.getCode() != null ? e.getCode() : R.FAIL, e.getMessage());
    }

    /**
     * 兜底异常处理（可记录堆栈信息）
     */
    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常: [{}] 请求地址: [{}]", e.getMessage(), request.getRequestURI(), e);
        return R.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "系统内部错误，请联系管理员");
    }
}
