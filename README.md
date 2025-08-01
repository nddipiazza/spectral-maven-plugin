# Spectral Maven Plugin

A Maven plugin for validating OpenAPI specifications using [Spectral](https://stoplight.io/open-source/spectral).

## Features

- Validates OpenAPI 3.x specifications using Spectral
- Supports multiple output formats (text, json, yaml, junit, html, teamcity)
- Cross-platform support (Windows, Linux, macOS, Alpine)
- Configurable rulesets
- Fail build on violations
- Verbose output option
- **Binary-safe executable extraction** - Properly handles executable permissions on Linux/Unix systems

## Known Issues and Solutions

### Linux Executable Permissions Issue

**Problem**: When Spectral executables are packaged as JAR resources, they can lose their executable permissions on Linux/Unix systems, causing the error:
```
Permission denied (os error 13)
```

**Solution**: The plugin now uses a binary-safe extraction method that:
1. Copies the executable using byte streams (not text streams) to preserve binary integrity
2. Automatically sets executable permissions (`chmod +x`) on Unix-like systems
3. Verifies the file is actually executable before proceeding
4. Provides clear error messages if permission setting fails

### Resource Organization

The plugin organizes platform-specific executables in a structured resource hierarchy:
```
src/main/resources/
└── spectral/
    ├── windows/
    │   └── spectral.exe
    ├── linux-x64/
    │   └── spectral
    ├── linux-arm64/
    │   └── spectral
    ├── macos-x64/
    │   └── spectral
    ├── macos-arm64/
    │   └── spectral
    ├── alpine-x64/
    │   └── spectral
    └── alpine-arm64/
        └── spectral
```

This organization allows multiple platform executables to coexist in the same environment without conflicts.

The fixed implementation replaces the problematic:
```java
Files.copy(inputStream, executablePath, StandardCopyOption.REPLACE_EXISTING);
```

With a binary-safe approach:
```java
try (InputStream is = inputStream;
     OutputStream os = Files.newOutputStream(executablePath)) {
    byte[] buffer = new byte[8192];
    int bytesRead;
    while ((bytesRead = is.read(buffer)) != -1) {
        os.write(buffer, 0, bytesRead);
    }
}
// Then set executable permissions on Unix systems
if (!isWindows()) {
    executableFile.setExecutable(true);
    executableFile.setReadable(true);
}
```

## Usage

### Basic Configuration

Add the plugin to your Maven project's `pom.xml`:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>io.github.ndipiazza</groupId>
            <artifactId>spectral-maven-plugin</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <executions>
                <execution>
                    <goals>
                        <goal>validate</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

### Configuration Options

| Parameter | Property | Default | Description |
|-----------|----------|---------|-------------|
| `inputDirectory` | `spectral.inputDirectory` | `${project.basedir}/src/main/resources/openapi` | Directory containing OpenAPI files |
| `files` | `spectral.files` | - | Specific files to validate (if not set, all YAML/JSON files in inputDirectory) |
| `ruleset` | `spectral.ruleset` | - | Path to custom Spectral ruleset file |
| `format` | `spectral.format` | `text` | Output format (text, json, yaml, junit, html, teamcity) |
| `outputFile` | `spectral.outputFile` | - | File to write results to (console if not specified) |
| `failOnViolations` | `spectral.failOnViolations` | `true` | Fail build if violations found |
| `skip` | `spectral.skip` | `false` | Skip validation |
| `verbose` | `spectral.verbose` | `false` | Enable verbose output |

### Advanced Configuration

```xml
<plugin>
    <groupId>io.github.ndipiazza</groupId>
    <artifactId>spectral-maven-plugin</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <executions>
        <execution>
            <goals>
                <goal>validate</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <inputDirectory>${project.basedir}/src/main/resources/api</inputDirectory>
        <files>
            <file>petstore.yaml</file>
            <file>users.yaml</file>
        </files>
        <ruleset>${project.basedir}/spectral-rules.yaml</ruleset>
        <format>junit</format>
        <outputFile>${project.build.directory}/spectral-report.xml</outputFile>
        <failOnViolations>true</failOnViolations>
        <verbose>true</verbose>
    </configuration>
</plugin>
```

### Command Line Usage

You can also run the plugin directly from the command line:

```bash
# Validate with default settings
mvn spectral:validate

# Validate specific files
mvn spectral:validate -Dspectral.files=api.yaml,users.yaml

# Use custom ruleset
mvn spectral:validate -Dspectral.ruleset=my-rules.yaml

# Generate JUnit report
mvn spectral:validate -Dspectral.format=junit -Dspectral.outputFile=target/spectral-report.xml

# Skip validation
mvn spectral:validate -Dspectral.skip=true
```

## Troubleshooting

### Common Issues

1. **"Permission denied" on Linux**
   - This should be automatically resolved with version 1.0.0+
   - If you still encounter this, ensure you're using the latest version
   - Check that your Java process has permission to create temporary files

2. **"Spectral executable not found"**
   - Ensure you're using the correct plugin version for your platform
   - The plugin automatically detects your OS and architecture
   - Supported platforms are listed in the "Supported Platforms" section

3. **Timeout errors**
   - Large OpenAPI files may take longer to validate
   - The plugin has a 60-second timeout per file
   - Consider splitting large specifications into smaller files

4. **JAR packaging issues**
   - When building from source, ensure the `copy-spectral-executables.sh` script ran successfully
   - Verify that platform-specific executables are present in the JAR resources

### Debug Mode

Enable Maven debug logging to see detailed execution information:
```bash
mvn spectral:validate -X
```

This will show:
- Which executable is being used
- Temporary file locations
- Full command line arguments
- Detailed error messages

## Supported Platforms

The plugin includes platform-specific Spectral executables for:

- Windows (x64)
- Linux (x64, ARM64)
- macOS (x64, ARM64)  
- Alpine Linux (x64, ARM64)

The correct executable is automatically selected based on your operating system and architecture.

## Custom Rulesets

You can use custom Spectral rulesets by specifying the `ruleset` parameter:

```yaml
# spectral-rules.yaml
extends: ["spectral:oas"]
rules:
  operation-operationId: error
  operation-summary: error
  operation-description: warn
  no-eval-in-markdown: error
  no-script-tags-in-markdown: error
```

## Integration with CI/CD

### JUnit Format

Generate JUnit-compatible reports for CI/CD integration:

```xml
<configuration>
    <format>junit</format>
    <outputFile>${project.build.directory}/spectral-report.xml</outputFile>
</configuration>
```

### TeamCity Format

For TeamCity integration:

```xml
<configuration>
    <format>teamcity</format>
</configuration>
```

## Building the Plugin

To build the plugin from source:

```bash
git clone https://github.com/ndipiazza/spectral-maven-plugin.git
cd spectral-maven-plugin

# Download Spectral executables (requires curl)
./copy-spectral-executables.sh

# Or specify a specific version
./copy-spectral-executables.sh v6.15.0

# Build the plugin
mvn clean install
```

## Testing

To test the plugin with the sample project:

```bash
cd test-project
mvn clean validate
```

## License

This project is licensed under the Apache License 2.0.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
