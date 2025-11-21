package com.paymentapp.exception;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {
	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<APIError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
		  logger.error("Resource not found: {}", ex.getMessage());
		APIError err = new APIError();
		err.setStatus(HttpStatus.NOT_FOUND.value());
		err.setError(HttpStatus.NOT_FOUND.getReasonPhrase());
		err.setMessage(ex.getMessage());
		err.setPath(req.getRequestURI());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);

	}


	@ExceptionHandler(DuplicatePayrollException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicatePayrollException(
            DuplicatePayrollException ex, 
            WebRequest request) {
        
        System.out.printf("❌ DuplicatePayrollException: {}", ex.getMessage());
        
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", Instant.now().toString());
        errorDetails.put("message", ex.getMessage());
        errorDetails.put("error", "Duplicate Payroll Request");
        errorDetails.put("status", HttpStatus.BAD_REQUEST.value());
        errorDetails.put("period", ex.getPeriod());
        errorDetails.put("existingStatus", ex.getStatus());
        errorDetails.put("path", request.getDescription(false).replace("uri=", ""));
        
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<APIError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
		APIError err = new APIError();
		err.setStatus(HttpStatus.BAD_REQUEST.value());
		err.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
		err.setMessage("Validation failed");
		err.setPath(req.getRequestURI());
		List<String> details = ex.getBindingResult().getFieldErrors().stream()
				.map(fe -> fe.getField() + ": " + fe.getDefaultMessage()).toList();
		err.setValidationErrors(details);
		return ResponseEntity.badRequest().body(err);
	}


	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<APIError> handleState(IllegalStateException ex, HttpServletRequest req) {
		APIError err = new APIError();
		err.setStatus(HttpStatus.CONFLICT.value());
		err.setError(HttpStatus.CONFLICT.getReasonPhrase());
		err.setMessage(ex.getMessage());
		err.setPath(req.getRequestURI());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(err);
	}

	  // ✅ Handle Organization Name Duplicate
    @ExceptionHandler(DuplicateOrgNameException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateOrgName(DuplicateOrgNameException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.CONFLICT.value());
        errorResponse.put("error", "Duplicate Organization Name");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("field", "orgName");
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    // ✅ Handle Email Duplicate
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateEmail(DuplicateEmailException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.CONFLICT.value());
        errorResponse.put("error", "Duplicate Email");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("field", "email");
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    // ✅ Handle Phone Duplicate
    @ExceptionHandler(DuplicatePhoneException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicatePhone(DuplicatePhoneException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.CONFLICT.value());
        errorResponse.put("error", "Duplicate Phone Number");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("field", "phone");
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    // ✅ Handle Bank Account Duplicate
    @ExceptionHandler(DuplicateBankAccountException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateBankAccount(DuplicateBankAccountException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.CONFLICT.value());
        errorResponse.put("error", "Duplicate Bank Account");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("field", "bankAccountNo");
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

	@ExceptionHandler(Exception.class)
	public ResponseEntity<APIError> handleGeneric(Exception ex, HttpServletRequest req) {
	    ex.printStackTrace(); 

		APIError err = new APIError();
		err.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		err.setError(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
		err.setMessage(ex.getMessage());
		err.setPath(req.getRequestURI());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
	}

//	This is a fallback handler for any unhandled exception — runtime exceptions, programming errors, etc.
//	
//	{
//	    "status": 500,
//	    "error": "Internal Server Error",
//	    "message": "Unexpected error occurred: something went wrong!",
//	    "path": "/api/employees"
//	}


	 @ExceptionHandler(CustomConcurrentUpdateException.class)
	    public ResponseEntity<String> handleConcurrentUpdateException(CustomConcurrentUpdateException ex) {
	        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
	    }
	 

@ExceptionHandler(AuthorizationDeniedException.class)
public ResponseEntity<String> handleAccessDenied(AuthorizationDeniedException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied");
}

@ExceptionHandler(UserApiException.class)
public ResponseEntity<?> handleUserApiException(UserApiException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(Map.of("error", ex.getMessage()));
}
}


