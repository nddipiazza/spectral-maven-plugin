package io.github.nddipiazza.spectral;

import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SpectralExecutor
 */
class SpectralExecutorTest {

    @Mock
    private Log mockLog;

    @TempDir
    File tempDir;

    private SpectralExecutor spectralExecutor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        spectralExecutor = new SpectralExecutor(mockLog);
    }

    @Test
    void testConstructorSetsLog() {
        // Given & When
        SpectralExecutor executor = new SpectralExecutor(mockLog);
        
        // Then
        assertNotNull(executor);
        // We can't directly test the log field since it's private, but we can verify it's used
    }

    @Test
    void testValidateWithNoFilesFound() throws Exception {
        // Given
        File emptyDir = new File(tempDir, "empty");
        emptyDir.mkdirs();
        
        // Create a testable executor that doesn't fail on executable extraction
        TestableSpectralExecutor testableExecutor = new TestableSpectralExecutor(mockLog, tempDir);

        // When
        SpectralResult result = testableExecutor.validate(
            emptyDir, null, null, "text", null, false, tempDir
        );
        
        // Then
        assertEquals(0, result.getViolationCount());
        assertEquals("", result.getOutput());
        verify(mockLog).warn("No OpenAPI files found to validate");
    }

    @Test
    void testValidateWithSpecificFiles() throws Exception {
        // Given
        File yamlFile = createTestFile("test.yaml", "openapi: 3.0.0\ninfo:\n  title: Test\n  version: 1.0.0");
        List<String> files = Collections.singletonList(yamlFile.getAbsolutePath());
        
        // Create a testable executor that doesn't fail on executable extraction
        TestableSpectralExecutor testableExecutor = new TestableSpectralExecutor(mockLog, tempDir);

        // When & Then - This will fail because we don't have the actual spectral executable
        // But we can test that it attempts to process the file
        assertThrows(SpectralExecutionException.class, () -> {
            testableExecutor.validate(tempDir, files, null, "text", null, false, tempDir);
        });
        
        verify(mockLog).info(contains("Validating 1 OpenAPI file(s)"));
    }

    @Test
    void testValidateWithNonExistentSpecificFile() throws Exception {
        // Given
        List<String> files = Collections.singletonList("nonexistent.yaml");
        
        // Create a testable executor that doesn't fail on executable extraction
        TestableSpectralExecutor testableExecutor = new TestableSpectralExecutor(mockLog, tempDir);

        // When
        SpectralResult result = testableExecutor.validate(
            tempDir, files, null, "text", null, false, tempDir
        );
        
        // Then
        assertEquals(0, result.getViolationCount());
        verify(mockLog).warn(contains("Specified file not found"));
    }

    @Test
    void testValidateFindsYamlFiles() throws Exception {
        // Given
        createTestFile("api1.yaml", "openapi: 3.0.0");
        createTestFile("api2.yml", "openapi: 3.0.0");
        createTestFile("api3.json", "{}");
        createTestFile("readme.txt", "not an api file"); // Should be ignored
        
        // Create a testable executor that doesn't fail on executable extraction
        TestableSpectralExecutor testableExecutor = new TestableSpectralExecutor(mockLog, tempDir);

        // When & Then - This will fail because we don't have spectral executable
        assertThrows(SpectralExecutionException.class, () -> {
            testableExecutor.validate(tempDir, null, null, "text", null, false, tempDir);
        });
        
        // But verify it found the correct number of files
        verify(mockLog).info("Validating 3 OpenAPI file(s)");
    }

    @Test
    void testValidateFindsFilesRecursively() throws Exception {
        // Given
        File subDir = new File(tempDir, "subdir");
        subDir.mkdirs();
        createTestFile("api1.yaml", "openapi: 3.0.0");
        createTestFile(subDir, "api2.yaml", "openapi: 3.0.0");
        
        // Create a testable executor that doesn't fail on executable extraction
        TestableSpectralExecutor testableExecutor = new TestableSpectralExecutor(mockLog, tempDir);

        // When & Then
        assertThrows(SpectralExecutionException.class, () -> {
            testableExecutor.validate(tempDir, null, null, "text", null, false, tempDir);
        });
        
        verify(mockLog).info("Validating 2 OpenAPI file(s)");
    }

    @Test
    void testValidateWithRelativeFilePath() throws Exception {
        // Given
        createTestFile("api.yaml", "openapi: 3.0.0");
        List<String> files = Collections.singletonList("api.yaml");
        
        // Create a testable executor that doesn't fail on executable extraction
        TestableSpectralExecutor testableExecutor = new TestableSpectralExecutor(mockLog, tempDir);

        // When & Then
        assertThrows(SpectralExecutionException.class, () -> {
            testableExecutor.validate(tempDir, files, null, "text", null, false, tempDir);
        });
        
        verify(mockLog).info("Validating 1 OpenAPI file(s)");
    }

    @Test
    void testValidateHandlesNullInputDirectory() throws Exception {
        // Given
        List<String> files = Collections.singletonList("nonexistent.yaml");
        
        // Create a testable executor that doesn't fail on executable extraction
        TestableSpectralExecutor testableExecutor = new TestableSpectralExecutor(mockLog, tempDir);

        // When
        SpectralResult result = testableExecutor.validate(
            null, files, null, "text", null, false, tempDir
        );
        
        // Then
        assertEquals(0, result.getViolationCount());
        verify(mockLog).warn(contains("Specified file not found"));
    }

    @Test
    void testExtractSpectralExecutableFailsWhenResourceNotFound() {
        // Given - SpectralExecutor will try to extract the executable but fail because resources don't exist in test
        
        // When & Then
        assertThrows(SpectralExecutionException.class, () -> {
            spectralExecutor.validate(tempDir, null, null, "text", null, false, tempDir);
        });
    }

    @Test
    void testIsOpenApiFileDetection() throws Exception {
        // Given - We'll test this indirectly by checking which files get picked up
        createTestFile("api.yaml", "openapi: 3.0.0");
        createTestFile("api.yml", "openapi: 3.0.0");
        createTestFile("api.json", "{}");
        createTestFile("readme.md", "# Documentation");
        createTestFile("config.xml", "<xml/>");
        createTestFile("data.txt", "text file");
        
        // Create a testable executor that doesn't fail on executable extraction
        TestableSpectralExecutor testableExecutor = new TestableSpectralExecutor(mockLog, tempDir);

        // When & Then
        assertThrows(SpectralExecutionException.class, () -> {
            testableExecutor.validate(tempDir, null, null, "text", null, false, tempDir);
        });
        
        // Should only find 3 OpenAPI files (yaml, yml, json)
        verify(mockLog).info("Validating 3 OpenAPI file(s)");
    }

    /**
     * Helper method to create test files
     */
    private File createTestFile(String filename, String content) throws IOException {
        return createTestFile(tempDir, filename, content);
    }

    private File createTestFile(File directory, String filename, String content) throws IOException {
        File file = new File(directory, filename);
        Files.write(file.toPath(), content.getBytes());
        return file;
    }

    /**
     * Testable version of SpectralExecutor that overrides extractSpectralExecutable
     * to avoid the resource loading issue in tests
     */
    private static class TestableSpectralExecutor extends SpectralExecutor {
        private final File mockExecutableFile;

        public TestableSpectralExecutor(Log log, File tempDir) throws IOException {
            super(log);
            // Create a mock executable file for testing
            this.mockExecutableFile = new File(tempDir, "spectral.exe");
            this.mockExecutableFile.createNewFile();
        }

        @Override
        protected File extractSpectralExecutable(File targetDirectory) throws SpectralExecutionException {
            // Return our mock executable instead of trying to extract from resources
            return mockExecutableFile;
        }
    }
}