package com.icfnext.documentation.plugin;

import com.google.common.io.CharSink;
import com.google.common.io.Files;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Stack;

@Mojo(name = "add-table-of-contents")
public class AddTableOfContentsMojo extends AbstractMojo {

    private static final String CHARSET = "UTF-8";
    private static final String ID_PREFIX = "section-";

    @Parameter(required = true)
    private File baseDir;

    @Parameter(defaultValue = "true")
    private boolean failOnError;

    @Parameter(defaultValue = "true")
    private boolean recursive;

    @Parameter(defaultValue = "*.html")
    private String fileMask;

    @Parameter(defaultValue = "true")
    private boolean excludeH1;

    @Parameter(defaultValue = "3")
    private int levelsToInclude;

    @Parameter(defaultValue = "nav")
    private String targetSelector;

    @Parameter
    private String title;

    @Parameter(defaultValue = "h4")
    private String titleTag;

    public void execute() throws MojoExecutionException {
        if (!baseDir.exists()) {
            throw new MojoExecutionException("Parameter baseDir doesn't exist: " + baseDir.getAbsolutePath());
        } else if (!baseDir.isDirectory()) {
            throw new MojoExecutionException("Parameter baseDir is not a directory: " + baseDir.getAbsolutePath());
        }
        final String fileRegex = getFileRegex();
        final String cssSelector = getCssSelector();
        handleDirectory(baseDir, fileRegex, cssSelector);
    }

    private void handleDirectory(final File directory, final String fileRegex, final String headingSelector)
            throws MojoExecutionException {
        for (final File file : directory.listFiles()) {
            if (file.isDirectory() && recursive) {
                handleDirectory(file, fileRegex, headingSelector);
            } else if (file.isFile() && file.getName().matches(fileRegex)) {
                try {
                    addTableOfContents(file, headingSelector);
                } catch (IOException e) {
                    if (failOnError) {
                        throw new MojoExecutionException("Failure adding table of contents", e);
                    }
                }
            }
        }
    }

    private void addTableOfContents(final File file, final String headingSelector)
            throws IOException, MojoExecutionException {

        final Charset charset = Charset.forName(CHARSET);
        final String html = Files.asCharSource(file, charset).read();
        final Document document = Jsoup.parse(html);
        final Elements target = document.select(targetSelector);
        if (!target.isEmpty()) {
            final Element targetElement = target.first();
            if (title != null) {
                targetElement.appendElement(titleTag).text(title);
            }
            final Stack<Element> tocListStack = new Stack<Element>();
            final Element root = new Element("ol");
            targetElement.appendChild(root);
            tocListStack.push(root);
            final Elements headingElements = document.select(headingSelector);
            Element previousListItem = targetElement;
            for (final Element headingElement : headingElements) {
                final String text = headingElement.text();
                final String tagName = headingElement.tagName();
                final int level = elementTagToLevel(tagName);
                int currentLevel = tocListStack.size();
                if (level > currentLevel + 2) {
                    // levels can only go up by one at a time
                    throw new IllegalStateException("Illegal Heading: " + text);
                }
                while (currentLevel > level) {
                    // we are coming from a deeper level -- close out lists
                    tocListStack.pop();
                    currentLevel = tocListStack.size();
                }
                final Element tocItem = new Element("li");
                final Element parent;
                if (level > currentLevel) {
                    // increase level by nesting a new list below the previous item
                    parent = new Element("ol");
                    previousListItem.appendChild(parent);
                    tocListStack.push(parent);
                } else {
                    // We're at the same level, so just add to the top-level list element
                    parent = tocListStack.peek();
                }
                parent.appendChild(tocItem);
                previousListItem = tocItem;
                final String id = generateElementId(tocListStack);
                headingElement.attr("id", id);
                previousListItem.append("<a href='#" + id + "'>" + text + "</a>");
            }
        } else {
            getLog().warn("Target selector found no elements: " + targetSelector);
            if (failOnError) {
                throw new MojoExecutionException("Target selector not found: " + targetSelector);
            }
        }
        final String updatedHtml = document.outerHtml();
        final CharSink charSink = Files.asCharSink(file, charset);
        charSink.write(updatedHtml);
    }

    private String getCssSelector() {
        final int start = excludeH1 ? 2 : 1;
        final int end = start + levelsToInclude;
        final StringBuilder out = new StringBuilder();
        for (int i = start; i < end; i++) {
            out.append("h").append(i);
            if (i < end - 1) {
                out.append(", ");
            }
        }
        return out.toString();
    }

    private String getFileRegex() {
        return fileMask.replaceAll("[*]", ".*");
    }

    /**
     * Converts a heading tag into a 1-indexed level, where 1 is the highest level shown in the ToC
     * @param tag the element tag (h1-h6)
     * @return the relative level
     */
    private int elementTagToLevel(final String tag) {
        final String lowerTag = tag.toLowerCase();
        if (!lowerTag.matches("h[0-6]")) {
            throw new IllegalStateException("Illegal heading tag: " + tag);
        }
        final String levelString = tag.substring(1);
        final int absoluteLevel = Integer.parseInt(levelString);
        return absoluteLevel - (excludeH1 ? 1 : 0);
    }

    private static String generateElementId(final Stack<Element> tocStack) {
        final StringBuilder out = new StringBuilder(ID_PREFIX);
        for (int i = 0; i < tocStack.size(); i++) {
            final Element element = tocStack.get(i);
            out.append(element.childNodeSize());
            if (i < tocStack.size() - 1) {
                out.append(".");
            }
        }
        return out.toString();
    }
}
