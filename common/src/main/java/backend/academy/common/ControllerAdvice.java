package backend.academy.common;

import backend.academy.common.exceptions.ChatNotFoundException;
import backend.academy.common.exceptions.InvalidRequestArgumentsException;
import backend.academy.common.exceptions.LinkNotFoundException;
import backend.academy.common.model.ApiErrorResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ControllerAdvice {
    private List<String> stackTraceTolist(StackTraceElement[] stackTraceElements) {
        return Arrays.stream(stackTraceElements)
                .map(StackTraceElement::toString)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private ResponseEntity<ApiErrorResponse> buildErrorResponse(
            Exception e, HttpStatus status, String description, String code) {
        return ResponseEntity.status(status)
                .body(ApiErrorResponse.builder()
                        .description(description)
                        .code(code)
                        .exceptionName(e.getClass().getSimpleName())
                        .exceptionMessage(e.getMessage())
                        .stacktrace(stackTraceTolist(e.getStackTrace()))
                        .build());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidRequestArgumentsException.class)
    public ResponseEntity<ApiErrorResponse> handleException(InvalidRequestArgumentsException e) {
        return buildErrorResponse(
                e,
                HttpStatus.BAD_REQUEST,
                "Некорректные параметры запроса",
                String.valueOf(HttpStatus.BAD_REQUEST.value()));
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(LinkNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleException(LinkNotFoundException e) {
        return buildErrorResponse(
                e,
                HttpStatus.NOT_FOUND,
                "Ссылка не найдена",
                String.valueOf(HttpStatus.NOT_FOUND.value()));
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ChatNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleException(ChatNotFoundException e) {
        return buildErrorResponse(
                e,
                HttpStatus.NOT_FOUND,
                "Чат не существует",
                String.valueOf(HttpStatus.NOT_FOUND.value()));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(Exception e) {
        return buildErrorResponse(
                e,
                HttpStatus.BAD_REQUEST,
                "Некорректные параметры запроса",
                String.valueOf(HttpStatus.BAD_REQUEST.value()));
    }
}
