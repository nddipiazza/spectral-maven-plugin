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

## Usage

### Basic Configuration

Add the plugin to your Maven project's `pom.xml`. You need to include both the plugin and the platform-specific
dependency for your operating system:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>io.github.nddipiazza</groupId>
            <artifactId>spectral-maven-plugin</artifactId>
           <version>6.15.0</version>
           <dependencies>
              <!-- Include the platform-specific dependency for your OS -->
              <!-- For Windows: -->
              <dependency>
                 <groupId>io.github.nddipiazza</groupId>
                 <artifactId>spectral-win</artifactId>
                 <version>6.15.0</version>
              </dependency>

              <!-- For Linux x64: -->
              <!--
              <dependency>
                  <groupId>io.github.nddipiazza</groupId>
                  <artifactId>spectral-linux-x64</artifactId>
                  <version>6.15.0</version>
              </dependency>
              -->

              <!-- For Linux ARM64: -->
              <!--
              <dependency>
                  <groupId>io.github.nddipiazza</groupId>
                  <artifactId>spectral-linux-arm64</artifactId>
                  <version>6.15.0</version>
              </dependency>
              -->

              <!-- For macOS x64: -->
              <!--
              <dependency>
                  <groupId>io.github.nddipiazza</groupId>
                  <artifactId>spectral-macos-x64</artifactId>
                  <version>6.15.0</version>
              </dependency>
              -->

              <!-- For macOS ARM64 (Apple Silicon): -->
              <!--
              <dependency>
                  <groupId>io.github.nddipiazza</groupId>
                  <artifactId>spectral-macos-arm64</artifactId>
                  <version>6.15.0</version>
              </dependency>
              -->

              <!-- For Alpine Linux x64: -->
              <!--
              <dependency>
                  <groupId>io.github.nddipiazza</groupId>
                  <artifactId>spectral-alpine-x64</artifactId>
                  <version>6.15.0</version>
              </dependency>
              -->

              <!-- For Alpine Linux ARM64: -->
              <!--
              <dependency>
                  <groupId>io.github.nddipiazza</groupId>
                  <artifactId>spectral-alpine-arm64</artifactId>
                  <version>6.15.0</version>
              </dependency>
              -->
           </dependencies>
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

**Note:** Uncomment only the dependency that matches your operating system and architecture. The plugin will
automatically detect and use the correct Spectral executable for your platform.

**Multi-Platform Support:** If you need to support multiple platforms (e.g., for CI/CD environments or team development
across different operating systems), you can include multiple platform dependencies. The plugin will automatically
select the correct executable for the current platform and ignore the others. While this increases the download size and
build artifact size due to unused executables, it ensures compatibility across different environments without requiring
platform-specific configuration.

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
    <groupId>io.github.nddipiazza</groupId>
    <artifactId>spectral-maven-plugin</artifactId>
   <version>6.15.0</version>
   <dependencies>
      <!-- Include platform-specific dependencies as needed -->
      <dependency>
         <groupId>io.github.nddipiazza</groupId>
         <artifactId>spectral-win</artifactId>
         <version>6.15.0</version>
      </dependency>
   </dependencies>
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

## Troubleshooting

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
git clone https://github.com/nddipiazza/spectral-maven-plugin.git
cd spectral-maven-plugin

# Download Spectral executables (requires curl on Linux/macOS or PowerShell on Windows)
# On Linux/macOS:
./copy-spectral-executables.sh

# On Windows (PowerShell):
.\copy-spectral-executables.ps1

# Or specify a specific version
# Linux/macOS:
./copy-spectral-executables.sh v6.15.0-rc1

# Windows:
.\copy-spectral-executables.ps1 -SpectralVersion v6.15.0-rc1

# Build the plugin
mvn clean install
```

### Download Script Options

Both scripts support the same functionality:

**Linux/macOS (Bash):**
```bash
# Show help
./copy-spectral-executables.sh --help

# Download default version (v6.15.0-rc1)
./copy-spectral-executables.sh

# Download specific version
./copy-spectral-executables.sh v6.11.0
```

**Windows (PowerShell):**
```powershell
# Show help
.\copy-spectral-executables.ps1 -Help

# Download default version (v6.15.0-rc1)
.\copy-spectral-executables.ps1

# Download specific version
.\copy-spectral-executables.ps1 -SpectralVersion v6.11.0
# or
.\copy-spectral-executables.ps1 v6.11.0
```

### Requirements

- **Linux/macOS**: `curl` must be installed
- **Windows**: PowerShell 5.0 or later (included with Windows 10+)
- **All platforms**: Internet connection required

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
