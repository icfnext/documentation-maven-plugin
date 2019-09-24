package com.icfnext.documentation.plugin;

import com.google.common.io.Files;
import com.icfnext.documentation.plugin.html.HtmlTransformer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@Mojo(name = "transform-html")
public class HtmlTransformMojo extends AbstractMojo {

    private static final String CHARSET = "UTF-8";

    @Parameter(required = true)
    private File baseDir;

    @Parameter(defaultValue = "true")
    private boolean failOnError;

    @Parameter(defaultValue = "true")
    private boolean recursive;

    @Parameter(defaultValue = "*.html")
    private String fileMask;

    @Parameter(required = true)
    private List<String> transformers = new ArrayList<>();
    private List<HtmlTransformer> transformerInstances = new ArrayList<>();

    public void execute() throws MojoExecutionException {
        if (!baseDir.exists()) {
            throw new MojoExecutionException("Parameter baseDir doesn't exist: " + baseDir.getAbsolutePath());
        } else if (!baseDir.isDirectory()) {
            throw new MojoExecutionException("Parameter baseDir is not a directory: " + baseDir.getAbsolutePath());
        }
        for (final String transformer : transformers) {
            try {
                final Class<?> transformerClass = getClass().getClassLoader().loadClass(transformer);
                if (transformerClass.isAssignableFrom(HtmlTransformer.class)) {
                    getLog().warn("Transformer does not implement HtmlTransformer: " + transformer);
                    if (failOnError) {
                        throw new MojoExecutionException("Invalid transformer class: " + transformer);
                    }
                }
                final Object transformerInstance = transformerClass.newInstance();
                transformerInstances.add((HtmlTransformer) transformerInstance);
            } catch (ClassNotFoundException e) {
                getLog().warn("Transformer not found: " + transformer);
                if (failOnError) {
                    throw new MojoExecutionException("Failed to load transformer class: " + transformer, e);
                }
            } catch (IllegalAccessException | InstantiationException e) {
                getLog().warn("Transformer instantiation failed: " + transformer);
                if (failOnError) {
                    throw new MojoExecutionException("Failed to instantiate transformer: " + transformer, e);
                }
            }
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
                    transformFile(file);
                } catch (IOException e) {
                    if (failOnError) {
                        throw new MojoExecutionException("Failure adding table of contents", e);
                    }
                }
            }
        }
    }

    private void transformFile(final File file) throws IOException {
        final Charset charset = Charset.forName(CHARSET);
        final String html = Files.asCharSource(file, charset).read();
        final Document document = Jsoup.parse(html);
        for (final HtmlTransformer transformerInstance : transformerInstances) {
            transformerInstance.transform(document);
        }
        final String updatedHtml = document.outerHtml();
        Files.asCharSink(file, charset).write(updatedHtml);
    }

    private String getFileRegex() {
        return fileMask.replaceAll("\\.", "\\\\.").replaceAll("[*]", ".*");
    }
}
