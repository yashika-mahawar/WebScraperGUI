package com.webscraper;
 
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.web.bind.annotation.*;
 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
 
@RestController
@CrossOrigin
public class ScraperController {
 
    @GetMapping("/api/scrape")
    public List<Quote> scrape(@RequestParam String url) {
 
        List<Quote> results = new ArrayList<>();
 
        try {
 
            Document document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();
 
            // Site 1: quotes.toscrape.com structure -> div.quote
            Elements quotes = document.select("div.quote");
 
            if (!quotes.isEmpty()) {
 
                for (Element quote : quotes) {
 
                    String text = quote.select("span.text").text();
                    String author = quote.select("small.author").text();
 
                    results.add(new Quote(text, author));
                }
 
            } else {
 
                // Site 2: books.toscrape.com structure -> article.product_pod
                Elements books = document.select("article.product_pod");
 
                if (!books.isEmpty()) {
 
                    for (Element book : books) {
 
                        String title = book.select("h3 a").attr("title");
                        String price = book.select("p.price_color").text();
 
                        results.add(new Quote(title, price));
                    }
 
                } else {
 
                    // Site 3: unknown site -> generic fallback scraper
                    results.addAll(genericScrape(document));
                }
            }
 
        } catch (Exception e) {
 
            throw new RuntimeException(
                    "Unable to scrape website: " + e.getMessage()
            );
        }
 
        return results;
    }
 
    /**
     * Generic scraper for unknown websites.
     * Strategy:
     * 1. Find the tag+class combination that repeats the most on the page
     *    (this is usually a "card", "list item", "product", "post", etc.)
     * 2. From each repeated block, pull the first two pieces of visible text.
     * 3. If nothing repeats in a useful way, fall back to grabbing all
     *    paragraph text on the page.
     */
    private List<Quote> genericScrape(Document document) {
 
        List<Quote> results = new ArrayList<>();
 
        Elements all = document.select("body *");
 
        Map<String, List<Element>> groups = new HashMap<>();
 
        for (Element el : all) {
 
            String cls = el.className();
 
            if (cls == null || cls.trim().isEmpty()) {
                continue;
            }
 
            String key = el.tagName() + "." + cls.trim().replaceAll("\\s+", ".");
 
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(el);
        }
 
        String bestKey = null;
        List<Element> bestGroup = null;
 
        for (Map.Entry<String, List<Element>> entry : groups.entrySet()) {
 
            List<Element> group = entry.getValue();
 
            // Ignore tiny groups (not "repeating" content) and huge
            // groups matching every element on the page (layout wrappers)
            if (group.size() < 3 || group.size() > 500) {
                continue;
            }
 
            if (bestGroup == null || group.size() > bestGroup.size()) {
                bestKey = entry.getKey();
                bestGroup = group;
            }
        }
 
        if (bestGroup != null) {
 
            for (Element el : bestGroup) {
 
                List<String> texts = new ArrayList<>();
 
                for (Element child : el.select("*")) {
 
                    String t = child.ownText().trim();
 
                    if (!t.isEmpty()) {
                        texts.add(t);
                    }
                }
 
                if (texts.isEmpty()) {
                    String whole = el.text().trim();
                    if (!whole.isEmpty()) {
                        texts.add(whole);
                    }
                }
 
                if (texts.isEmpty()) {
                    continue;
                }
 
                String text = texts.get(0);
                String secondary = texts.size() > 1 ? texts.get(1) : "";
 
                results.add(new Quote(text, secondary));
            }
        }
 
        // Fallback: no repeating block pattern found, grab paragraphs instead
        if (results.isEmpty()) {
 
            Elements paragraphs = document.select("p");
 
            for (Element p : paragraphs) {
 
                String t = p.text().trim();
 
                if (!t.isEmpty()) {
                    results.add(new Quote(t, ""));
                }
            }
        }
 
        return results;
    }
 


    public record Quote(String text, String author) {
    }
}
