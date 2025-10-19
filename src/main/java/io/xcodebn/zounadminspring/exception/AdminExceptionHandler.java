package io.xcodebn.zounadminspring.exception;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Global exception handler for the Zoun Admin UI.
 * Catches exceptions thrown by controllers and displays user-friendly error messages.
 */
@ControllerAdvice(basePackages = "io.xcodebn.zounadminspring.web")
public class AdminExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(AdminExceptionHandler.class);

    /**
     * Handle entity not found exceptions.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleEntityNotFound(EntityNotFoundException ex, Model model) {
        log.error("Entity not found: {}", ex.getMessage());
        model.addAttribute("errorMessage", "The requested entity was not found.");
        model.addAttribute("errorDetails", ex.getMessage());
        return "zoun-admin-ui/error";
    }

    /**
     * Handle illegal argument exceptions (e.g., invalid model name).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgument(IllegalArgumentException ex, Model model) {
        log.error("Invalid argument: {}", ex.getMessage());
        model.addAttribute("errorMessage", "Invalid request: " + ex.getMessage());
        model.addAttribute("errorDetails", ex.getMessage());
        return "zoun-admin-ui/error";
    }

    /**
     * Handle all other exceptions.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception ex, Model model) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        model.addAttribute("errorMessage", "An unexpected error occurred.");
        model.addAttribute("errorDetails", ex.getMessage());
        return "zoun-admin-ui/error";
    }
}
