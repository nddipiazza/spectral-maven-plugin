package io.github.ndipiazza.spectral;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Executes Spectral CLI for OpenAPI validation
 */
public class SpectralExecutor {
    
    private final Log log;
    private static final String SPECTRAL_EXECUTABLE_NAME = getSpectralExecutableName();
    
    public SpectralExecutor(Log log) {
        this.log = log;
    }
    
    /**
     * Validates OpenAPI files using Spectral
     */
    public SpectralResult validate(File inputDirectory, 
                                 List<String> files, 
                                 File ruleset, 
                                 String format, 
                                 File outputFile, 
                                 boolean verbose) throws SpectralExecutionException {
        
        // Extract the appropriate Spectral executable
        File spectralExecutable = extractSpectralExecutable();
        
        List<File> filesToValidate = determineFilesToValidate(inputDirectory, files);
        
        if (filesToValidate.isEmpty()) {
            log.warn("No OpenAPI files found to validate");
            return new SpectralResult(0, "");
        }
        
        log.info(String.format("Validating %d OpenAPI file(s)", filesToValidate.size()));
        
        int totalViolations = 0;
        StringBuilder allOutput = new StringBuilder();
        
        for (File file : filesToValidate) {
            log.info("Validating: " + file.getPath());
            
            List<String> command = buildSpectralCommand(spectralExecutable, file, ruleset, format, verbose);
            SpectralResult result = executeSpectral(command, outputFile);
            
            totalViolations += result.getViolationCount();
            allOutput.append(result.getOutput()).append("\n");
        }
        
        return new SpectralResult(totalViolations, allOutput.toString());
    }
    
    /**
     * Extracts the platform-specific Spectral executable from resources
     */
    private File extractSpectralExecutable() throws SpectralExecutionException {
        try {
            String resourcePath = "/" + SPECTRAL_EXECUTABLE_NAME;
            InputStream inputStream = getClass().getResourceAsStream(resourcePath);
            
            if (inputStream == null) {
                throw new SpectralExecutionException("Could not find Spectral executable: " + resourcePath);
            }
            
            // Create temp file
            Path tempDir = Files.createTempDirectory("spectral-maven-plugin");
            Path executablePath = tempDir.resolve(SPECTRAL_EXECUTABLE_NAME);
            
            try (FileOutputStream outputStream = new FileOutputStream(executablePath.toFile())) {
                IOUtils.copy(inputStream, outputStream);
            }
            // Copy executable to temp location using binary-safe method
            try (InputStream is = inputStream;
                 OutputStream os = Files.newOutputStream(executablePath)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }
            
            // Make executable (Unix/Linux/macOS)
            File executableFile = executablePath.toFile();
            if (!isWindows()) {
                executableFile.setExecutable(true);
                // Also set read permission to ensure it's accessible
                executableFile.setReadable(true);
                
                // Verify the file is actually executable
                if (!executableFile.canExecute()) {
                    throw new SpectralExecutionException("Failed to make Spectral executable runnable: " + executableFile.getAbsolutePath());
                }
                
                log.debug("Made executable: " + executableFile.getAbsolutePath());
            }
            
            // // Clean up on exit
            // executableFile.deleteOnExit();
            // tempDir.toFile().deleteOnExit();
            
            return executableFile;
            
        } catch (IOException e) {
            throw new SpectralExecutionException("Failed to extract Spectral executable", e);
        }
    }
    
    /**
     * Builds the command line for Spectral execution
     */
    private List<String> buildSpectralCommand(File executable, File inputFile, File ruleset, String format, boolean verbose) {
        List<String> command = new ArrayList<>();
        command.add(executable.getAbsolutePath());
        command.add("lint");
        
        if (ruleset != null && ruleset.exists()) {
            command.add("--ruleset");
            command.add(ruleset.getAbsolutePath());
        }
        
        if (format != null && !format.trim().isEmpty()) {
            command.add("--format");
            command.add(format);
        }
        
        if (verbose) {
            command.add("--verbose");
        }
        
        command.add(inputFile.getAbsolutePath());
        
        return command;
    }
    
    /**
     * Executes Spectral command and captures output
     */
    private SpectralResult executeSpectral(List<String> command, File outputFile) throws SpectralExecutionException {
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            
            log.debug("Executing: " + String.join(" ", command));
            
            Process process = pb.start();
            
            // Capture output
            String output = IOUtils.toString(process.getInputStream(), "UTF-8");
            
            // Wait for process to complete
            boolean finished = process.waitFor(60, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new SpectralExecutionException("Spectral execution timed out after 60 seconds");
            }
            
            int exitCode = process.exitValue();
            
            // Write output to console
            if (!output.trim().isEmpty()) {
                log.info("Spectral output:");
                log.info(output);
            }
            
            // Write output to file if specified
            if (outputFile != null) {
                writeOutputToFile(output, outputFile);
            }
            
            // Count violations (rough estimate based on exit code and output)
            int violationCount = countViolations(output, exitCode);
            
            return new SpectralResult(violationCount, output);
            
        } catch (IOException | InterruptedException e) {
            throw new SpectralExecutionException("Failed to execute Spectral", e);
        }
    }
    
    /**
     * Determines which files to validate
     */
    private List<File> determineFilesToValidate(File inputDirectory, List<String> files) {
        List<File> filesToValidate = new ArrayList<>();
        
        if (files != null && !files.isEmpty()) {
            // Use specified files
            for (String fileName : files) {
                File file = new File(fileName);
                if (!file.isAbsolute()) {
                    file = new File(inputDirectory, fileName);
                }
                if (file.exists() && file.isFile()) {
                    filesToValidate.add(file);
                } else {
                    log.warn("Specified file not found: " + file.getPath());
                }
            }
        } else if (inputDirectory != null && inputDirectory.exists() && inputDirectory.isDirectory()) {
            // Find all YAML/JSON files in input directory
            findOpenApiFiles(inputDirectory, filesToValidate);
        }
        
        return filesToValidate;
    }
    
    /**
     * Recursively finds OpenAPI files (YAML/JSON) in directory
     */
    private void findOpenApiFiles(File directory, List<File> result) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    findOpenApiFiles(file, result);
                } else if (isOpenApiFile(file)) {
                    result.add(file);
                }
            }
        }
    }
    
    /**
     * Checks if file is likely an OpenAPI file based on extension
     */
    private boolean isOpenApiFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".yaml") || name.endsWith(".yml") || name.endsWith(".json");
    }
    
    /**
     * Writes output to specified file
     */
    private void writeOutputToFile(String output, File outputFile) throws IOException {
        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write(output);
        }
        log.info("Spectral output written to: " + outputFile.getAbsolutePath());
    }
    
    /**
     * Counts violations from Spectral output
     */
    private int countViolations(String output, int exitCode) {
        // Spectral typically returns exit code 1 if there are violations
        if (exitCode == 0) {
            return 0;
        }
        
        // Count lines that look like violations (rough heuristic)
        String[] lines = output.split("\n");
        int count = 0;
        for (String line : lines) {
            line = line.trim();
            if (line.contains("error") || line.contains("warning") || line.contains("info")) {
                if (line.matches(".*\\d+:\\d+.*")) { // Contains line:column numbers
                    count++;
                }
            }
        }
        
        return count > 0 ? count : (exitCode != 0 ? 1 : 0);
    }
    
    /**
     * Determines the platform-specific executable name
     */
    private static String getSpectralExecutableName() {
        String os = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch").toLowerCase();
        
        if (os.contains("win")) {
            return "spectral.exe";
        } else if (os.contains("mac")) {
            if (arch.contains("aarch64") || arch.contains("arm")) {
                return "spectral"; // macos-arm64
            } else {
                return "spectral"; // macos-x64
            }
        } else if (os.contains("nix") || os.contains("nux")) {
            if (arch.contains("aarch64") || arch.contains("arm")) {
                return "spectral"; // linux-arm64
            } else {
                return "spectral"; // linux-x64
            }
        } else if (os.contains("alpine")) {
            if (arch.contains("aarch64") || arch.contains("arm")) {
                return "spectral"; // alpine-arm64
            } else {
                return "spectral"; // alpine-x64
            }
        }
        
        // Default to linux-x64
        return "spectral";
    }
    
    /**
     * Checks if running on Windows
     */
    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
}
