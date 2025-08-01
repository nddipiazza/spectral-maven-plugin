package io.github.ndipiazza.spectral;

/**
 * Represents the result of a Spectral validation execution
 */
public class SpectralResult {
    
    private final int violationCount;
    private final String output;
    
    public SpectralResult(int violationCount, String output) {
        this.violationCount = violationCount;
        this.output = output;
    }
    
    /**
     * Gets the number of violations found
     */
    public int getViolationCount() {
        return violationCount;
    }
    
    /**
     * Gets the raw output from Spectral
     */
    public String getOutput() {
        return output;
    }
    
    /**
     * Checks if there were any violations
     */
    public boolean hasViolations() {
        return violationCount > 0;
    }
    
    @Override
    public String toString() {
        return String.format("SpectralResult{violationCount=%d, hasOutput=%s}", 
                           violationCount, 
                           output != null && !output.trim().isEmpty());
    }
}
