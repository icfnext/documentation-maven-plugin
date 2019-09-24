package com.icfnext.documentation.plugin;

import com.google.common.io.Files;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;

@Mojo(name = "copy-frontend-artifacts")
public class CopyFrontendArtifactsMojo extends AbstractMojo {

    @Parameter(required = true)
    private File baseDir;

    @Parameter(defaultValue = "true")
    private boolean failOnError;

    @Parameter(defaultValue = "true")
    private boolean recursive;

    @Parameter(defaultValue = "*")
    private String fileMask;

    @Parameter(defaultValue = "${project.outputDirectory}")
    private File outputDir;


    public void execute() throws MojoExecutionException {
        if (!baseDir.exists()) {
            throw new MojoExecutionException("Parameter baseDir doesn't exist: " + baseDir.getAbsolutePath());
        } else if (!baseDir.isDirectory()) {
            throw new MojoExecutionException("Parameter baseDir is not a directory: " + baseDir.getAbsolutePath());
        }
        final String fileRegex = getFileRegex();
        handleDirectory(baseDir, fileRegex);
    }

    private void handleDirectory(final File directory, final String fileRegex)
            throws MojoExecutionException {
        for (final File file : directory.listFiles()) {
            if (file.isDirectory() && recursive) {
                handleDirectory(file, fileRegex);
            } else if (file.isFile() && file.getName().matches(fileRegex)) {
                try {
                    copyFile(file);
                } catch (IOException e) {
                    if (failOnError) {
                        throw new MojoExecutionException("Failure adding table of contents", e);
                    }
                }
            }
        }
    }

    private void copyFile(final File source) throws IOException {
        final String sourcePath = source.getAbsolutePath();
        final String baseDir = this.baseDir.getAbsolutePath();
        final String relPath = sourcePath.substring(baseDir.length());
        final String outputDir = this.outputDir.getAbsolutePath();
        final String destinationPath = outputDir + relPath;
        final File destination = new File(destinationPath);
        Files.createParentDirs(destination);
        Files.copy(source, destination);
    }

    private String getFileRegex() {
        return fileMask.replaceAll("\\.", "\\\\.").replaceAll("[*]", ".*");
    }
}
