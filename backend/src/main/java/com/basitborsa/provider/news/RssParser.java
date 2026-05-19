package com.basitborsa.provider.news;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Minimal, dependency-free RSS 2.0 / Atom 1.0 parser tolerant enough for KAP and
 * Turkish finance portals. Returns NewsArticle records.
 */
public final class RssParser {

    private static final Logger log = LoggerFactory.getLogger(RssParser.class);

    private static final DateTimeFormatter[] DATE_FORMATS = {
            DateTimeFormatter.RFC_1123_DATE_TIME,
            DateTimeFormatter.ISO_OFFSET_DATE_TIME,
            DateTimeFormatter.ISO_ZONED_DATE_TIME,
            DateTimeFormatter.ISO_DATE_TIME,
            DateTimeFormatter.ISO_DATE,
            DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss zzz", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
    };

    private RssParser() {}

    public static List<NewsArticle> parse(byte[] payload, String sourceName, String language) {
        if (payload == null || payload.length == 0) return List.of();
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(false);
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(payload));
            doc.getDocumentElement().normalize();

            NodeList rssItems = doc.getElementsByTagName("item");
            if (rssItems.getLength() > 0) {
                return parseRssItems(rssItems, sourceName, language);
            }
            NodeList atomEntries = doc.getElementsByTagName("entry");
            if (atomEntries.getLength() > 0) {
                return parseAtomEntries(atomEntries, sourceName, language);
            }
            return List.of();
        } catch (Exception e) {
            log.warn("RSS parse failed source={}: {}", sourceName, e.getMessage());
            return List.of();
        }
    }

    private static List<NewsArticle> parseRssItems(NodeList items, String sourceName, String language) {
        List<NewsArticle> out = new ArrayList<>(items.getLength());
        for (int i = 0; i < items.getLength(); i++) {
            Node n = items.item(i);
            if (!(n instanceof Element el)) continue;
            String title = text(el, "title");
            String link = text(el, "link");
            String desc = text(el, "description");
            String pub = text(el, "pubDate");
            if (pub == null || pub.isBlank()) pub = text(el, "dc:date");
            String category = text(el, "category");
            LocalDate date = parseDate(pub);
            if (title == null || title.isBlank() || date == null) continue;
            out.add(new NewsArticle(stripTags(title), stripTags(desc), link, date,
                    sourceName, language, category));
        }
        return out;
    }

    private static List<NewsArticle> parseAtomEntries(NodeList entries, String sourceName, String language) {
        List<NewsArticle> out = new ArrayList<>(entries.getLength());
        for (int i = 0; i < entries.getLength(); i++) {
            Node n = entries.item(i);
            if (!(n instanceof Element el)) continue;
            String title = text(el, "title");
            String link = attr(el, "link", "href");
            String desc = text(el, "summary");
            if (desc == null || desc.isBlank()) desc = text(el, "content");
            String pub = text(el, "updated");
            if (pub == null || pub.isBlank()) pub = text(el, "published");
            LocalDate date = parseDate(pub);
            if (title == null || title.isBlank() || date == null) continue;
            out.add(new NewsArticle(stripTags(title), stripTags(desc), link, date,
                    sourceName, language, null));
        }
        return out;
    }

    private static String text(Element el, String tag) {
        NodeList nl = el.getElementsByTagName(tag);
        if (nl.getLength() == 0) return null;
        String t = nl.item(0).getTextContent();
        return t == null ? null : t.trim();
    }

    private static String attr(Element el, String tag, String attr) {
        NodeList nl = el.getElementsByTagName(tag);
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element ee) {
                String v = ee.getAttribute(attr);
                if (v != null && !v.isBlank()) return v;
            }
        }
        return null;
    }

    private static LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String trimmed = raw.trim();
        for (DateTimeFormatter f : DATE_FORMATS) {
            try {
                if (f == DateTimeFormatter.ISO_DATE) {
                    return LocalDate.parse(trimmed, f);
                }
                try {
                    return ZonedDateTime.parse(trimmed, f).toLocalDate();
                } catch (Exception ignored) {}
                try {
                    return OffsetDateTime.parse(trimmed, f).toLocalDate();
                } catch (Exception ignored) {}
            } catch (Exception ignored) {}
        }
        try {
            return LocalDate.parse(trimmed.substring(0, 10));
        } catch (Exception e) {
            return null;
        }
    }

    private static String stripTags(String s) {
        if (s == null) return null;
        String noCdata = s.replace("<![CDATA[", "").replace("]]>", "");
        String noTags = noCdata.replaceAll("<[^>]+>", " ");
        return noTags.replaceAll("\\s+", " ").trim();
    }

    @SuppressWarnings("unused")
    public static byte[] toBytes(String s) {
        return s == null ? new byte[0] : s.getBytes(StandardCharsets.UTF_8);
    }
}
