package io.github.ndipiazza.spectral;

/**
 * Exception thrown when Spectral execution fails
 */
public class SpectralExecutionException extends Exception {
    
    public SpectralExecutionException(String message) {
        super(message);
    }
    
    public SpectralExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public SpectralExecutionException(Throwable cause) {
        super(cause);
    }
}
