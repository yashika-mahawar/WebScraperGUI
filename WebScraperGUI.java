import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebScraperGUI extends JFrame implements ActionListener {

    // COMPONENTS
    JLabel titleLabel, urlLabel, searchLabel, statusLabel, totalLabel;

    JTextField urlField, searchField;

    JButton scrapeButton, clearButton, exportButton;

    JTable table;

    DefaultTableModel model;

    JScrollPane scrollPane;

    WebScraperGUI() {

        // FRAME SETTINGS
        setTitle("SmartScrape Pro");
        setSize(950, 650);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(new Color(245, 247, 250));

        // TITLE
        titleLabel = new JLabel("SMARTSCRAPE PRO");
        titleLabel.setBounds(300, 20, 400, 40);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 30));
        titleLabel.setForeground(new Color(25, 25, 112));
        add(titleLabel);

        // URL LABEL
        urlLabel = new JLabel("Website URL:");
        urlLabel.setBounds(50, 90, 150, 30);
        urlLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(urlLabel);

        // URL FIELD
        urlField = new JTextField();
        urlField.setBounds(200, 90, 600, 35);
        urlField.setFont(new Font("Arial", Font.PLAIN, 16));

        // DEFAULT URL
        urlField.setText("https://quotes.toscrape.com");

        add(urlField);

        // SEARCH LABEL
        searchLabel = new JLabel("Search Author:");
        searchLabel.setBounds(50, 150, 150, 30);
        searchLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(searchLabel);

        // SEARCH FIELD
        searchField = new JTextField();
        searchField.setBounds(200, 150, 250, 35);
        searchField.setFont(new Font("Arial", Font.PLAIN, 16));
        add(searchField);

        // SCRAPE BUTTON
        scrapeButton = new JButton("SCRAPE DATA");
        scrapeButton.setBounds(500, 145, 150, 45);
        scrapeButton.setBackground(new Color(30, 144, 255));
        scrapeButton.setForeground(Color.WHITE);
        scrapeButton.setFont(new Font("Arial", Font.BOLD, 15));
        scrapeButton.addActionListener(this);
        add(scrapeButton);

        // CLEAR BUTTON
        clearButton = new JButton("CLEAR");
        clearButton.setBounds(680, 145, 120, 45);
        clearButton.setBackground(new Color(220, 20, 60));
        clearButton.setForeground(Color.WHITE);
        clearButton.setFont(new Font("Arial", Font.BOLD, 15));
        clearButton.addActionListener(this);
        add(clearButton);

        // EXPORT BUTTON
        exportButton = new JButton("EXPORT");
        exportButton.setBounds(820, 145, 100, 45);
        exportButton.setBackground(new Color(34, 139, 34));
        exportButton.setForeground(Color.WHITE);
        exportButton.setFont(new Font("Arial", Font.BOLD, 15));
        exportButton.addActionListener(this);
        add(exportButton);

        // TABLE
        String columns[] = { "No", "Quote", "Author" };

        model = new DefaultTableModel(columns, 0);

        table = new JTable(model);

        table.setRowHeight(28);

        table.setFont(new Font("Arial", Font.PLAIN, 14));

        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 15));

        scrollPane = new JScrollPane(table);

        scrollPane.setBounds(40, 240, 860, 280);

        add(scrollPane);

        // STATUS LABEL
        statusLabel = new JLabel("Status: Waiting...");
        statusLabel.setBounds(50, 550, 400, 30);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusLabel.setForeground(new Color(0, 100, 0));
        add(statusLabel);

        // TOTAL LABEL
        totalLabel = new JLabel("Total Quotes: 0");
        totalLabel.setBounds(700, 550, 200, 30);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalLabel.setForeground(new Color(25, 25, 112));
        add(totalLabel);

        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {

        // SCRAPE BUTTON
        if (e.getSource() == scrapeButton) {

            try {

                statusLabel.setText("Status: Fetching quotes...");

                model.setRowCount(0);

                String url = urlField.getText();

                String searchText = searchField.getText().toLowerCase();

                // CONNECT WEBSITE
                Document doc = Jsoup.connect(url).get();

                // GET ALL QUOTES
                Elements quotes = doc.select("div.quote");

                int count = 1;

                for (Element quote : quotes) {

                    String quoteText = quote.select("span.text").text();

                    String author = quote.select("small.author").text();

                    // SEARCH FILTER
                    if (searchText.isEmpty()
                            || author.toLowerCase()
                                    .contains(searchText)) {

                        model.addRow(new Object[] {
                                count,
                                quoteText,
                                author
                        });

                        count++;
                    }
                }

                totalLabel.setText(
                        "Total Quotes: " + (count - 1));

                statusLabel.setText(
                        "Status: Quotes Loaded Successfully!");

            }

            catch (Exception ex) {

                statusLabel.setText(
                        "Status: Error loading website!");

            }
        }

        // CLEAR BUTTON
        if (e.getSource() == clearButton) {

            searchField.setText("");

            model.setRowCount(0);

            totalLabel.setText("Total Quotes: 0");

            statusLabel.setText("Status: Cleared");
        }

        // EXPORT BUTTON
        if (e.getSource() == exportButton) {

            try {

                FileWriter writer = new FileWriter("quotes.txt");

                for (int i = 0; i < model.getRowCount(); i++) {

                    writer.write(
                            model.getValueAt(i, 0)
                                    + ". "
                                    + model.getValueAt(i, 1)
                                    + " - "
                                    + model.getValueAt(i, 2)
                                    + "\n\n");
                }

                writer.close();

                statusLabel.setText(
                        "Status: Exported to quotes.txt");

            }

            catch (Exception ex) {

                statusLabel.setText(
                        "Status: Export Failed!");

            }
        }
    }

    public static void main(String[] args) {

        new WebScraperGUI();
    }
}