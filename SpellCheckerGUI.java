 import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class SpellCheckerGUI extends JFrame {
    private HashSet<String> dictionary = new HashSet<>();
    private JTextField wordInputField;
    private JLabel resultLabel;
    private JList<String> suggestionBox;
    private DefaultListModel<String> suggestionModel;
    private BufferedImage backgroundImage;

    public SpellCheckerGUI() {
        setTitle("Spell Checker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);

        loadDictionaryFromFile("English_words.txt");
        loadBackgroundImage("Spell checker.png");

        // Main Panel with padding and background image
        setContentPane(new BackgroundPanel());
        setLayout(new BorderLayout(10, 10));

        // Title Label
        JLabel titleLabel = new JLabel("Spell Checker", JLabel.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setForeground(new Color(0, 102, 204));
        add(titleLabel, BorderLayout.NORTH);

        // Input Panel for word input and check button
        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.setOpaque(false);

        wordInputField = new JTextField(20);
        wordInputField.setFont(new Font("SansSerif", Font.PLAIN, 18));
        wordInputField.setPreferredSize(new Dimension(250, 40));

        JButton checkButton = new JButton("Check");
        checkButton.setFont(new Font("SansSerif", Font.BOLD, 18));
        checkButton.setBackground(new Color(0, 153, 76));
        checkButton.setForeground(Color.WHITE);
        checkButton.setFocusPainted(false);
        checkButton.setPreferredSize(new Dimension(120, 40));

        inputPanel.add(new JLabel("Enter a word:"));
        inputPanel.add(wordInputField);
        inputPanel.add(checkButton);

        add(inputPanel, BorderLayout.CENTER);

        // Result Label to notify correctness of spelling
        resultLabel = new JLabel("Result will be displayed here...");
        resultLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        resultLabel.setForeground(Color.BLACK);
        resultLabel.setHorizontalAlignment(SwingConstants.CENTER);
        resultLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(resultLabel, BorderLayout.SOUTH);

        // Suggestion Box with fixed size and center alignment
        suggestionModel = new DefaultListModel<>();
        suggestionBox = new JList<>(suggestionModel);
        suggestionBox.setFont(new Font("SansSerif", Font.PLAIN, 16));
        suggestionBox.setBorder(BorderFactory.createTitledBorder("-----Suggestions-----"));
        suggestionBox.setVisibleRowCount(5);

        // Make the suggestion box scrollable
        JScrollPane suggestionScrollPane = new JScrollPane(suggestionBox);
        suggestionScrollPane.setPreferredSize(new Dimension(250, 150));

        JPanel suggestionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        suggestionPanel.setOpaque(false);
        suggestionPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        suggestionPanel.add(suggestionScrollPane, BorderLayout.CENTER);

        add(suggestionPanel, BorderLayout.EAST);

        // Button Action for checking spelling
        checkButton.addActionListener(e -> checkSpelling());

        // Add mouse click listener to the suggestion box to update input field
        suggestionBox.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                String selectedSuggestion = suggestionBox.getSelectedValue();
                if (selectedSuggestion != null && !selectedSuggestion.equals("No suggestions found.")) {
                    wordInputField.setText(selectedSuggestion);
                }
            }
        });

        setVisible(true);
    }

    // Background Panel to display background image
    private class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    // Load background image
    private void loadBackgroundImage(String imagePath) {
        try {
            backgroundImage = ImageIO.read(new File(imagePath));
        } catch (IOException e) {
            System.out.println("Error loading background image: " + e.getMessage());
        }
    }

    // Load dictionary from file
    private void loadDictionaryFromFile(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String word;
            while ((word = br.readLine()) != null) {
                dictionary.add(word.toLowerCase());
            }
            System.out.println("Dictionary loaded successfully.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading dictionary file: " + e.getMessage(),
                    "File Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Check spelling and display suggestions if incorrect
    private void checkSpelling() {
        String word = wordInputField.getText().trim().toLowerCase();
        suggestionModel.clear();

        if (dictionary.contains(word)) {
            resultLabel.setText("Correct spelling: " + word);
            resultLabel.setForeground(new Color(0, 153, 76));
        } else {
            resultLabel.setText("Incorrect spelling: " + word);
            resultLabel.setForeground(Color.RED);

            // Display suggestions for incorrect spelling
            List<String> suggestions = getSuggestions(word);
            if (!suggestions.isEmpty()) {
                suggestions.forEach(suggestionModel::addElement);
            } else {
                suggestionModel.addElement("No suggestions found.");
            }
        }
    }

    // Generate suggestions based on edit distance
    private List<String> getSuggestions(String word) {
        List<String> suggestions = new ArrayList<>();
        int maxSuggestions = 5;
        int maxDistance = 2;

        for (String dictWord : dictionary) {
            if (calculateEditDistance(word, dictWord) <= maxDistance) {
                suggestions.add(dictWord);
                if (suggestions.size() >= maxSuggestions) break;
            }
        }
        return suggestions;
    }

    // Calculate edit distance between two words
    private int calculateEditDistance(String word1, String word2) {
        int[][] dp = new int[word1.length() + 1][word2.length() + 1];
        for (int i = 0; i <= word1.length(); i++) {
            for (int j = 0; j <= word2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1]));
                }
            }
        }
        return dp[word1.length()][word2.length()];
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SpellCheckerGUI::new);
    }
}
