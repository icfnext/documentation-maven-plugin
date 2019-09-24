package com.icfnext.documentation.plugin;

import com.google.common.io.Files;
import com.icfnext.documentation.plugin.html.HtmlRenderer;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.KeepType;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

@Mojo(name = "markdown-to-html")
public class MarkdownToHtmlMojo extends AbstractMojo {

    private static final String CHARSET = "UTF-8";
    private static final String REL_PATH_SEGMENT = "../";

    @Parameter(required = true)
    private File baseDir;

    @Parameter(defaultValue = "true")
    private boolean failOnError;

    @Parameter(defaultValue = "true")
    private boolean recursive;

    @Parameter(defaultValue = "true")
    private boolean fixMarkdownLinks;

    @Parameter(defaultValue = "*.md")
    private String fileMask;

    @Parameter(defaultValue = "${project.outputDirectory}")
    private File outputDir;

    @Parameter
    private File headerHtmlFile;
    private String headerHtml = "<html><header></header><body>";

    @Parameter
    private File footerHtmlFile;
    private String footerHtml = "</body></html>";

    public void execute() throws MojoExecutionException {
        if (!baseDir.exists()) {
            throw new MojoExecutionException("Parameter baseDir doesn't exist: " + baseDir.getAbsolutePath());
        } else if (!baseDir.isDirectory()) {
            throw new MojoExecutionException("Parameter baseDir is not a directory: " + baseDir.getAbsolutePath());
        }
        final Charset charset = Charset.forName(CHARSET);
        try {
            if (headerHtmlFile != null) {
                headerHtml = Files.asCharSource(headerHtmlFile, charset).read();
            }
            if (footerHtmlFile != null) {
                footerHtml = Files.asCharSource(footerHtmlFile, charset).read();
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to get html header/footer", e);
        }
        final String fileRegex = getFileRegex();
        handleDirectory(baseDir, fileRegex, "");
    }

    private void handleDirectory(final File directory, final String fileRegex, final String relativePath)
            throws MojoExecutionException {
        for (final File file : directory.listFiles()) {
            if (file.isDirectory() && recursive) {
                handleDirectory(file, fileRegex, REL_PATH_SEGMENT + relativePath);
            } else if (file.isFile() && file.getName().matches(fileRegex)) {
                try {
                    convertFile(file, relativePath);
                } catch (IOException e) {
                    if (failOnError) {
                        throw new MojoExecutionException("Failure adding table of contents", e);
                    }
                }
            }
        }
    }

    private void convertFile(final File file, final String relativePath) throws IOException {
        final Charset charset = Charset.forName(CHARSET);
        final String markdown = Files.asCharSource(file, charset).read();
        final String html = markdownToHtml(markdown);
        final String basePath = baseDir.getPath();
        final String relPath = file.getPath().replace(basePath, "");
        final String htmlRelPath = relPath.replaceAll("\\.[a-zA-Z0-9]+$", ".html");
        final String htmlAbsolutePath = outputDir.getPath() + htmlRelPath;
        final File htmlFile = new File(htmlAbsolutePath);
        Files.createParentDirs(htmlFile);
        final String correctedHeader = headerHtml.replaceAll("\\$\\{site-root}", relativePath);
        final String correctedFooter = footerHtml.replaceAll("\\$\\{site-root}", relativePath);
        Files.asCharSink(htmlFile, charset).write(correctedHeader + html + correctedFooter);
    }

    private String markdownToHtml(final String markdown) {
        final MutableDataHolder options = new MutableDataSet()
                .set(Parser.REFERENCES_KEEP, KeepType.LAST)
                .set(Parser.HTML_BLOCK_PARSER, false)
                .set(Parser.HTML_BLOCK_DEEP_PARSER, false)
                .set(TablesExtension.COLUMN_SPANS, false)
                .set(TablesExtension.APPEND_MISSING_COLUMNS, true)
                .set(TablesExtension.DISCARD_EXTRA_COLUMNS, true)
                .set(TablesExtension.HEADER_SEPARATOR_COLUMN_MATCH, true)
                .set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create()));
        final Parser parser = Parser.builder(options).build();
        final Document document = parser.parse(markdown);
        final HtmlRenderer htmlRenderer = new HtmlRenderer(getLog(), fixMarkdownLinks);
        return htmlRenderer.render(document);
    }

    private String getFileRegex() {
        return fileMask.replaceAll("\\.", "\\\\.").replaceAll("[*]", ".*");
    }

}
