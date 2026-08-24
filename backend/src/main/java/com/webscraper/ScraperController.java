package com.webscraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

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

            Elements quotes = document.select("div.quote");

            for (Element quote : quotes) {

                String text = quote.select("span.text").text();
                String author = quote.select("small.author").text();

                results.add(new Quote(text, author));
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to scrape website: " + e.getMessage()
            );
        }

        return results;
    }

    public record Quote(String text, String author) {
    }
}
