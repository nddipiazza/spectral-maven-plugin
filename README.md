# Spectral Maven Plugin

A Maven plugin for validating OpenAPI specifications using [Spectral](https://stoplight.io/open-source/spectral).

## Features

- Validates OpenAPI 3.x specifications using Spectral
- Supports multiple output formats (text, json, yaml, junit, html, teamcity)
- Cross-platform support (Windows, Linux, macOS, Alpine)
- Configurable rulesets
- Fail build on violations
- Verbose output option

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
