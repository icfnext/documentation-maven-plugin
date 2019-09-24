package com.icfnext.documentation.plugin.html;

import com.google.common.io.ByteStreams;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.Base64;

public class IcfNextTransformer implements HtmlTransformer {

    private static final String DEFAULT_FOOTER_MESSAGE_TPL = "Copyright %s ICF Next";

    @Override
    public void transform(final Document document) {
        final Element header = new Element("header");
        final Element titleContainer = new Element("div");
        final Element logoContainer = new Element("div");
        final Element logo = new Element("img");
        final Element article = new Element("article");
        final Element aside = new Element("aside");
        final Element nav = new Element("nav");
        final Element content = new Element("section");
        final Element footer = new Element("footer");
        final Element copyright = new Element("div");

        final String base64Image;
        try (final InputStream inputStream = getClass().getClassLoader().getResourceAsStream("logo.png")) {
            final byte[] bytes = ByteStreams.toByteArray(inputStream);
            base64Image = Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read logo");
        }

        logo.attr("src", "data:image/png;base64," + base64Image);
        logoContainer.appendChild(logo);
        header.appendChild(titleContainer).appendChild(logoContainer);
        article.appendChild(aside).appendChild(content);
        aside.appendChild(nav);
        footer.appendChild(copyright);

        final Elements h1 = document.select("h1");
        if (!h1.isEmpty()) {
            final Element title = h1.first();
            final Elements headTitles = document.select("head title");
            final Element headTitle;
            if (!headTitles.isEmpty()) {
                headTitle = headTitles.first();
            } else {
                headTitle = new Element("title");
                document.head().appendChild(headTitle);
            }
            titleContainer.appendChild(title);
            final String text = title.text();
            headTitle.text(text);
            final int i = text.indexOf(':');
            if (i >= 0) {
                final String titleText = text.substring(0, i);
                final String subtitleText = text.substring(i + 1);
                title.text(titleText);
                final Element subtitle = new Element("h2");
                subtitle.text(subtitleText);
                title.after(subtitle);
            }
        }

        final Elements contentElements = document.body().children();
        for (final Element contentElement : contentElements) {
            content.appendChild(contentElement);
        }

        document.body().insertChildren(0, header, article, footer);
        header.addClass("row no-gutters");
        article.addClass("row no-gutters");
        footer.addClass("row no-gutters");
        logoContainer.addClass("col-md-3 logo-container d-none d-lg-block");
        titleContainer.addClass("col-lg-9 title-container");
        aside.addClass("col-lg-3");
        content.addClass("col-lg-9");

        copyright.text(getFooterText());
    }

    private String getFooterText() {
        final ZonedDateTime now = ZonedDateTime.now();
        final int year = now.getYear();
        return String.format(DEFAULT_FOOTER_MESSAGE_TPL, year);
    }

}
