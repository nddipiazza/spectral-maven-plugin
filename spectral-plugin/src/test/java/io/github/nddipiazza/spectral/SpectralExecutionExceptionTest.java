package io.github.nddipiazza.spectral;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SpectralExecutionException
 */
class SpectralExecutionExceptionTest {

    @Test
    void testConstructorWithMessage() {
        // Given
        String message = "Spectral execution failed";
        
        // When
        SpectralExecutionException exception = new SpectralExecutionException(message);
        
        // Then
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        // Given
        String message = "Spectral execution failed";
        RuntimeException cause = new RuntimeException("Root cause");
        
        // When
        SpectralExecutionException exception = new SpectralExecutionException(message, cause);
        
        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testConstructorWithCause() {
        // Given
        RuntimeException cause = new RuntimeException("Root cause");
        
        // When
        SpectralExecutionException exception = new SpectralExecutionException(cause);
        
        // Then
        assertEquals(cause, exception.getCause());
        assertTrue(exception.getMessage().contains("RuntimeException"));
    }

    @Test
    void testExceptionInheritance() {
        // Given
        SpectralExecutionException exception = new SpectralExecutionException("test");
        
        // When & Then
        assertTrue(exception instanceof Exception);
        assertTrue(exception instanceof Throwable);
    }
}