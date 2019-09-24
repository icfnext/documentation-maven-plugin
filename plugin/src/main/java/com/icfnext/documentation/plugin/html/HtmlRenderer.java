package com.icfnext.documentation.plugin.html;

import com.google.common.html.HtmlEscapers;
import com.vladsch.flexmark.ast.BlockQuote;
import com.vladsch.flexmark.ast.BulletList;
import com.vladsch.flexmark.ast.BulletListItem;
import com.vladsch.flexmark.ast.Code;
import com.vladsch.flexmark.ast.Emphasis;
import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.HtmlInline;
import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.ast.IndentedCodeBlock;
import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.ast.OrderedList;
import com.vladsch.flexmark.ast.OrderedListItem;
import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.ast.SoftLineBreak;
import com.vladsch.flexmark.ast.StrongEmphasis;
import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.ext.tables.TableBlock;
import com.vladsch.flexmark.ext.tables.TableBody;
import com.vladsch.flexmark.ext.tables.TableCell;
import com.vladsch.flexmark.ext.tables.TableHead;
import com.vladsch.flexmark.ext.tables.TableRow;
import com.vladsch.flexmark.ext.tables.TableSeparator;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.IRender;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.collection.iteration.ReversiblePeekingIterable;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.apache.maven.plugin.logging.Log;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class HtmlRenderer implements IRender {

    private final Log log;
    private final boolean fixMarkdownLinks;

    public HtmlRenderer(final Log log, final boolean fixMarkdownLinks) {
        this.log = log;
        this.fixMarkdownLinks = fixMarkdownLinks;
    }

    @Override
    public void render(final Node node, final Appendable appendable) {
        final String nodeName = node.getNodeName();
        boolean omitChildren = false;
        try {
            switch (nodeName) {
                case "Document":
                    openDocument((Document) node, appendable);
                    break;
                case "Heading":
                    openHeading((Heading) node, appendable);
                    break;
                case "Text":
                    openText((Text) node, appendable);
                    break;
                case "Emphasis":
                    openEmphasis((Emphasis) node, appendable);
                    break;
                case "Link":
                    openLink((Link) node, appendable);
                    break;
                case "Code":
                    openCode((Code) node, appendable);
                    break;
                case "Paragraph":
                    openParagraph((Paragraph) node, appendable);
                    break;
                case "FencedCodeBlock":
                    openFencedCodeBlock((FencedCodeBlock) node, appendable);
                    break;
                case "IndentedCodeBlock":
                    openIndentedCodeBlock((IndentedCodeBlock) node, appendable);
                    break;
                case "OrderedList":
                    openOrderedList((OrderedList) node, appendable);
                    break;
                case "OrderedListItem":
                    openOrderedListItem((OrderedListItem) node, appendable);
                    break;
                case "BulletList":
                    openBulletList((BulletList) node, appendable);
                    break;
                case "BulletListItem":
                    openBulletListItem((BulletListItem) node, appendable);
                    break;
                case "BlockQuote":
                    openBlockQuote((BlockQuote) node, appendable);
                    break;
                case "StrongEmphasis":
                    openStrongEmphasis((StrongEmphasis) node, appendable);
                    break;
                case "Image":
                    openImage((Image) node, appendable);
                    break;
                case "SoftLineBreak":
                    openSoftLineBreak((SoftLineBreak) node, appendable);
                    break;
                case "TableBlock":
                    openTableBlock((TableBlock) node, appendable);
                    break;
                case "TableHead":
                    openTableHead((TableHead) node, appendable);
                    break;
                case "TableRow":
                    openTableRow((TableRow) node, appendable);
                    break;
                case "TableCell":
                    openTableCell((TableCell) node, appendable);
                    break;
                case "TableSeparator":
                    openTableSeparator((TableSeparator) node, appendable);
                    omitChildren = true;
                    break;
                case "TableBody":
                    openTableBody((TableBody) node, appendable);
                    break;
                case "HtmlInline":
                    openHtmlInline((HtmlInline) node, appendable);
                    break;
                default:
                    log.warn("Unhandled document component: " + nodeName);
            }
            if (!omitChildren) {
                final ReversiblePeekingIterable<Node> children = node.getChildren();
                children.forEach(child -> render(child, appendable));
            }

            switch (nodeName) {
                case "Document":
                    closeDocument((Document) node, appendable);
                    break;
                case "Heading":
                    closeHeading((Heading) node, appendable);
                    break;
                case "Text":
                    closeText((Text) node, appendable);
                    break;
                case "Emphasis":
                    closeEmphasis((Emphasis) node, appendable);
                    break;
                case "Link":
                    closeLink((Link) node, appendable);
                    break;
                case "Code":
                    closeCode((Code) node, appendable);
                    break;
                case "Paragraph":
                    closeParagraph((Paragraph) node, appendable);
                    break;
                case "FencedCodeBlock":
                    closeFencedCodeBlock((FencedCodeBlock) node, appendable);
                    break;
                case "IndentedCodeBlock":
                    closeIndentedCodeBlock((IndentedCodeBlock) node, appendable);
                    break;
                case "OrderedList":
                    closeOrderedList((OrderedList) node, appendable);
                    break;
                case "OrderedListItem":
                    closeOrderedListItem((OrderedListItem) node, appendable);
                    break;
                case "BulletList":
                    closeBulletList((BulletList) node, appendable);
                    break;
                case "BulletListItem":
                    closeBulletListItem((BulletListItem) node, appendable);
                    break;
                case "BlockQuote":
                    closeBlockQuote((BlockQuote) node, appendable);
                    break;
                case "StrongEmphasis":
                    closeStrongEmphasis((StrongEmphasis) node, appendable);
                    break;
                case "Image":
                    closeImage((Image) node, appendable);
                    break;
                case "SoftLineBreak":
                    closeSoftLineBreak((SoftLineBreak) node, appendable);
                    break;
                case "TableBlock":
                    closeTableBlock((TableBlock) node, appendable);
                    break;
                case "TableHead":
                    closeTableHead((TableHead) node, appendable);
                    break;
                case "TableRow":
                    closeTableRow((TableRow) node, appendable);
                    break;
                case "TableCell":
                    closeTableCell((TableCell) node, appendable);
                    break;
                case "TableSeparator":
                    closeTableSeparator((TableSeparator) node, appendable);
                    break;
                case "TableBody":
                    closeTableBody((TableBody) node, appendable);
                    break;
                case "HtmlInline":
                    closeHtmlInline((HtmlInline) node, appendable);
                    break;
                default:
                    log.warn("Unhandled document component: " + nodeName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String render(final Node node) {
        final StringBuilder out = new StringBuilder();
        render(node, out);
        return out.toString();
    }

    @Override
    public IRender withOptions(final DataHolder dataHolder) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DataHolder getOptions() {
        throw new UnsupportedOperationException();
    }


    private void openDocument(final Document document, final Appendable appendable) throws IOException {
        appendable.append("<main>");
    }

    private void closeDocument(final Document document, final Appendable appendable) throws IOException {
        appendable.append("</main>");
    }

    private void openHeading(final Heading heading, final Appendable appendable) throws IOException {
        final int level = heading.getLevel();
        appendable.append("<h").append(String.valueOf(level)).append(">");
    }

    private void closeHeading(final Heading heading, final Appendable appendable) throws IOException {
        final int level = heading.getLevel();
        appendable.append("</h").append(String.valueOf(level)).append(">");
    }

    private void openText(final Text text, final Appendable appendable) throws IOException {
        appendable.append(escape(text.getChars().toString()));
    }

    private void closeText(final Text text, final Appendable appendable) throws IOException {

    }

    private void openEmphasis(final Emphasis emphasis, final Appendable appendable) throws IOException {
        appendable.append("<em>");
    }

    private void closeEmphasis(final Emphasis emphasis, final Appendable appendable) throws IOException {
        appendable.append("</em>");
    }

    private void openLink(final Link link, final Appendable appendable) throws IOException {
        appendable.append("<a href='").append(link.getUrl()).append("'>");
    }

    private void closeLink(final Link link, final Appendable appendable) throws IOException {
        appendable.append("</a>");
    }

    private void openCode(final Code code, final Appendable appendable) throws IOException {
        appendable.append("<code>");
    }

    private void closeCode(final Code code, final Appendable appendable) throws IOException {
        appendable.append("</code>");
    }

    private void openParagraph(final Paragraph paragraph, final Appendable appendable) throws IOException {
        appendable.append("<p>");
    }

    private void closeParagraph(final Paragraph paragraph, final Appendable appendable) throws IOException {
        appendable.append("</p>");
    }

    private void openFencedCodeBlock(final FencedCodeBlock block, final Appendable appendable) throws IOException {
        final BasedSequence info = block.getInfo();
        appendable.append("<pre><code class='language-").append(info).append("'>");
    }

    private void closeFencedCodeBlock(final FencedCodeBlock block, final Appendable appendable) throws IOException {
        appendable.append("</code></pre>");
    }

    private void openIndentedCodeBlock(final IndentedCodeBlock block, final Appendable appendable) throws IOException {
        appendable.append("<pre><code>");
    }

    private void closeIndentedCodeBlock(final IndentedCodeBlock block, final Appendable appendable) throws IOException {
        appendable.append("</code></pre>");
    }

    private void openOrderedList(final OrderedList orderedList, final Appendable appendable) throws IOException {
        appendable.append("<ol>");
    }

    private void closeOrderedList(final OrderedList orderedList, final Appendable appendable) throws IOException {
        appendable.append("</ol>");
    }

    private void openOrderedListItem(final OrderedListItem item, final Appendable appendable) throws IOException {
        appendable.append("<li>");
    }

    private void closeOrderedListItem(final OrderedListItem item, final Appendable appendable) throws IOException {
        appendable.append("</li>");
    }

    private void openBulletList(final BulletList bulletList, final Appendable appendable) throws IOException {
        appendable.append("<ul>");
    }

    private void closeBulletList(final BulletList bulletList, final Appendable appendable) throws IOException {
        appendable.append("</ul>");
    }

    private void openBulletListItem(final BulletListItem item, final Appendable appendable) throws IOException {
        appendable.append("<li>");
    }

    private void closeBulletListItem(final BulletListItem item, final Appendable appendable) throws IOException {
        appendable.append("</li>");
    }

    private void openBlockQuote(final BlockQuote blockQuote, final Appendable appendable) throws IOException {
        appendable.append("<quote>");
    }

    private void closeBlockQuote(final BlockQuote blockQuote, final Appendable appendable) throws IOException {
        appendable.append("</quote>");
    }

    private void openStrongEmphasis(final StrongEmphasis emphasis, final Appendable appendable) throws IOException {
        appendable.append("<strong>");
    }

    private void closeStrongEmphasis(final StrongEmphasis emphasis, final Appendable appendable) throws IOException {
        appendable.append("</strong>");
    }

    private void openImage(final Image image, final Appendable appendable) throws IOException {
        final BasedSequence urlContent = image.getUrlContent();
        appendable.append("<img src='").append(String.valueOf(urlContent)).append("'>");
    }

    private void closeImage(final Image image, final Appendable appendable) throws IOException {

    }

    private void openSoftLineBreak(final SoftLineBreak softLineBreak, final Appendable appendable) throws IOException {
        appendable.append("\n");
    }

    private void closeSoftLineBreak(final SoftLineBreak softLineBreak, final Appendable appendable) throws IOException {

    }

    private void openTableBlock(final TableBlock tableBlock, final Appendable appendable) throws IOException {
        appendable.append("<table>");
    }

    private void closeTableBlock(final TableBlock tableBlock, final Appendable appendable) throws IOException {
        appendable.append("</table>");
    }

    private void openTableHead(final TableHead tableHead, final Appendable appendable) throws IOException {
        appendable.append("<thead>");
    }

    private void closeTableHead(final TableHead tableHead, final Appendable appendable) throws IOException {
        appendable.append("</thead>");
    }

    private void openTableCell(final TableCell tableCell, final Appendable appendable) throws IOException {
        appendable.append("<td>");
    }

    private void closeTableCell(final TableCell tableCell, final Appendable appendable) throws IOException {
        appendable.append("</td>");
    }

    private void openTableRow(final TableRow tableRow, final Appendable appendable) throws IOException {
        appendable.append("<tr>");
    }

    private void closeTableRow(final TableRow tableRow, final Appendable appendable) throws IOException {
        appendable.append("</tr>");
    }

    private void openTableSeparator(final TableSeparator separator, final Appendable appendable) throws IOException {

    }

    private void closeTableSeparator(final TableSeparator separator, final Appendable appendable) throws IOException {

    }

    private void openTableBody(final TableBody tableBody, final Appendable appendable) throws IOException {
        appendable.append("<tbody>");
    }

    private void closeTableBody(final TableBody tableBody, final Appendable appendable) throws IOException {
        appendable.append("<tbody>");
    }

    private void openHtmlInline(final HtmlInline htmlInline, final Appendable appendable) throws IOException {
        appendable.append(escape(htmlInline.getChars().toString()));
    }

    private void closeHtmlInline(final HtmlInline htmlInline, final Appendable appendable) throws IOException {

    }
    private String correctLink(final String url) {
        if (fixMarkdownLinks && url.endsWith(".md")) {
            try {
                // ensure that the link is local -- do not correct links to external MD files
                final URI uri = new URI(url);
                if (uri.getHost() == null) {
                    log.info("Correcting link to markdown file: " + url);
                    return url.replaceAll("\\.md$", ".html");
                }
            } catch (URISyntaxException e) {
                log.warn("Invalid link URL encountered: " + url);
            }
        }
        return url;
    }

    private static String escape(final String text) {
        return HtmlEscapers.htmlEscaper().escape(text);
    }
}