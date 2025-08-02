package io.github.nddipiazza.spectral;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SpectralResult
 */
class SpectralResultTest {

    @Test
    void testConstructorAndGetters() {
        // Given
        int violationCount = 5;
        String output = "Some spectral output";
        
        // When
        SpectralResult result = new SpectralResult(violationCount, output);
        
        // Then
        assertEquals(violationCount, result.getViolationCount());
        assertEquals(output, result.getOutput());
    }

    @Test
    void testHasViolationsWithZeroViolations() {
        // Given
        SpectralResult result = new SpectralResult(0, "No violations found");
        
        // When & Then
        assertFalse(result.hasViolations());
    }

    @Test
    void testHasViolationsWithPositiveViolations() {
        // Given
        SpectralResult result = new SpectralResult(3, "3 violations found");
        
        // When & Then
        assertTrue(result.hasViolations());
    }

    @Test
    void testHasViolationsWithNegativeViolations() {
        // Given - This might happen in edge cases
        SpectralResult result = new SpectralResult(-1, "Error occurred");
        
        // When & Then
        assertFalse(result.hasViolations());
    }

    @Test
    void testToStringWithViolations() {
        // Given
        SpectralResult result = new SpectralResult(2, "Some output");
        
        // When
        String toString = result.toString();
        
        // Then
        assertTrue(toString.contains("violationCount=2"));
        assertTrue(toString.contains("hasOutput=true"));
    }

    @Test
    void testToStringWithoutOutput() {
        // Given
        SpectralResult result = new SpectralResult(1, null);
        
        // When
        String toString = result.toString();
        
        // Then
        assertTrue(toString.contains("violationCount=1"));
        assertTrue(toString.contains("hasOutput=false"));
    }

    @Test
    void testToStringWithEmptyOutput() {
        // Given
        SpectralResult result = new SpectralResult(0, "   ");
        
        // When
        String toString = result.toString();
        
        // Then
        assertTrue(toString.contains("violationCount=0"));
        assertTrue(toString.contains("hasOutput=false"));
    }

    @Test
    void testToStringWithEmptyStringOutput() {
        // Given
        SpectralResult result = new SpectralResult(0, "");
        
        // When
        String toString = result.toString();
        
        // Then
        assertTrue(toString.contains("violationCount=0"));
        assertTrue(toString.contains("hasOutput=false"));
    }
}