package com.simon.smile.common.exception;

import com.simon.smile.common.Result;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;


@RestControllerAdvice
public class ExceptionHandleAdvice {

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    Result handleAccessDeniedException(Exception ex) {
        return Result.fail()
                .setCode(HttpStatus.FORBIDDEN.value())
                .setMessage("access denied")
                .setData(ex.getMessage());
    }

    @ExceptionHandler({UsernameNotFoundException.class, BadCredentialsException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    Result handleAuthenticationException(Exception ex) {
        return Result.fail()
                .setCode(HttpStatus.UNAUTHORIZED.value())
                .setMessage("username or password is incorrect")
                .setData(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Result handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, String> errorMap = e.getBindingResult()
                .getAllErrors()
                .stream()
                .collect(Collectors.toMap(
                        objectError -> ((FieldError) objectError).getField(),
                        ObjectError::getDefaultMessage
                ));
        return Result.fail()
                .setCode(HttpStatus.BAD_REQUEST.value())
                .setMessage("Provided arguments are invalid, set data for details")
                .setData(errorMap);
    }

    @ExceptionHandler(ObjectNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    Result handleNotFoundException(ObjectNotFoundException e) {
        return Result.fail()
                .setCode(HttpStatus.NOT_FOUND.value())
                .setMessage(e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Result handleNotFoundException(IllegalArgumentException e) {
        return Result.fail()
                .setCode(HttpStatus.BAD_REQUEST.value())
                .setMessage(e.getMessage());
    }

    // Fallback handles any unhandled exceptions.
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    Result handleOtherException(Exception ex) {
        return Result.fail()
                .setCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .setMessage("A server internal error occurs")
                .setData(ex.getMessage());
    }

    @ExceptionHandler(AccountStatusException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    Result handlerAccountStatusException(Exception ex) {
        return Result.fail()
                .setCode(HttpStatus.UNAUTHORIZED.value())
                .setMessage("user account is abnormal")
                .setData(ex.getMessage());
    }

    @ExceptionHandler(InsufficientAuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    Result handlerInsufficientAuthenticationException(Exception ex) {
        return Result.fail()
                .setCode(HttpStatus.UNAUTHORIZED.value())
                .setMessage("username and password are mandatory")
                .setData(ex.getMessage());
    }

    @ExceptionHandler(InvalidBearerTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    Result handlerInvalidBearerTokenException(Exception ex) {
        return Result.fail()
                .setCode(HttpStatus.UNAUTHORIZED.value())
                .setMessage("The access token provided is expired, revoked, malformed, or invalid for other reasons")
                .setData(ex.getMessage());
    }
}
