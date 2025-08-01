package io.github.ndipiazza.spectral;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.List;

/**
 * Validates OpenAPI YAML files using Spectral
 */
@Mojo(name = "validate", defaultPhase = LifecyclePhase.VALIDATE)
public class SpectralValidateMojo extends AbstractMojo {

    /**
     * The Maven project.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * Directory containing OpenAPI files to validate.
     */
    @Parameter(property = "spectral.inputDirectory", defaultValue = "${project.basedir}/src/main/resources/openapi")
    private File inputDirectory;

    /**
     * List of OpenAPI files to validate. If not specified, all YAML/JSON files in inputDirectory will be validated.
     */
    @Parameter(property = "spectral.files")
    private List<String> files;

    /**
     * Spectral ruleset file to use for validation.
     * Defaults to .spectral.yaml in the project root directory.
     * If the default file doesn't exist, Spectral will use its built-in ruleset.
     */
    @Parameter(property = "spectral.ruleset", defaultValue = "${project.basedir}/.spectral.yaml")
    private File ruleset;

    /**
     * Output format for Spectral results (json, yaml, junit, html, text, teamcity).
     */
    @Parameter(property = "spectral.format", defaultValue = "text")
    private String format;

    /**
     * Output file for Spectral results. If not specified, results will be printed to console.
     */
    @Parameter(property = "spectral.outputFile")
    private File outputFile;

    /**
     * Fail the build if Spectral finds any violations.
     */
    @Parameter(property = "spectral.failOnViolations", defaultValue = "true")
    private boolean failOnViolations;

    /**
     * Skip Spectral validation.
     */
    @Parameter(property = "spectral.skip", defaultValue = "false")
    private boolean skip;

    /**
     * Verbose output from Spectral.
     */
    @Parameter(property = "spectral.verbose", defaultValue = "false")
    private boolean verbose;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("Spectral validation is skipped.");
            return;
        }

        getLog().info("Starting Spectral OpenAPI validation...");

        try {
            SpectralExecutor executor = new SpectralExecutor(getLog());
            SpectralResult result = executor.validate(
                inputDirectory, 
                files, 
                ruleset, 
                format, 
                outputFile, 
                verbose
            );

            if (result.hasViolations() && failOnViolations) {
                throw new MojoFailureException(
                    String.format("Spectral validation failed with %d violations. See output above for details.", 
                                result.getViolationCount())
                );
            }

            if (result.hasViolations()) {
                getLog().warn(String.format("Spectral validation completed with %d violations.", 
                            result.getViolationCount()));
            } else {
                getLog().info("Spectral validation completed successfully with no violations.");
            }

        } catch (SpectralExecutionException e) {
            throw new MojoExecutionException("Failed to execute Spectral validation", e);
        }
    }
}
