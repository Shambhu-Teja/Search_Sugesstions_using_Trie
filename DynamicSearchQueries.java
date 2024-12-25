import java.io.*;
import java.util.*;

// Trie Node Class
class TrieNode {
    Map<Character, TrieNode> children;
    List<String> words; // Holds words added at this node
    Map<String, Integer> wordFrequency; // Tracks word frequency
    boolean isEndOfWord;

    public TrieNode() {
        children = new HashMap<>();
        words = new ArrayList<>();
        wordFrequency = new HashMap<>();
        isEndOfWord = false;
    }
}

// Trie Class for Search Functionality
class Trie {
    private final TrieNode root;

    public Trie() {
        root = new TrieNode();
    }

    // Insert a word into the Trie and increment its frequency
    public void insert(String word) {
        TrieNode current = root;
        for (char c : word.toCharArray()) {
            current = current.children.computeIfAbsent(c, k -> new TrieNode());
            current.words.add(word);
        }
        current.isEndOfWord = true;
        
        // Increment the word frequency at the end of the word
        current.wordFrequency.put(word, current.wordFrequency.getOrDefault(word, 0) + 1);
    }

    // Get all words starting with the given prefix
    public List<String> searchByPrefix(String prefix) {
        TrieNode current = root;

        // Navigate to the end of the prefix in the Trie
        for (char c : prefix.toCharArray()) {
            if (!current.children.containsKey(c)) {
                return new ArrayList<>(); // Return empty list if prefix not found
            }
            current = current.children.get(c);
        }

        // Return all words from this node
        return current.words;
    }

    // Search for words containing the given substring, limiting to top 5 and sorting by frequency
    public List<String> searchBySubstring(String substring) {
        List<String> results = new ArrayList<>();
        searchSubstringDFS(root, substring, results);
        
        // Sort results by frequency using their actual frequency from the TrieNode
        results.sort((a, b) -> {
            int freqA = getWordFrequency(a);
            int freqB = getWordFrequency(b);
            return Integer.compare(freqB, freqA); // Sort descending by frequency
        });
        
        return results;
    }

    // Helper function to search words containing a substring using DFS
    private void searchSubstringDFS(TrieNode node, String substring, List<String> results) {
        if (node == null) return;

        // Add all words at this node that contain the substring
        for (String word : node.words) {
            if (word.contains(substring) && !results.contains(word)) {
                results.add(word);
            }
        }

        // Recur for all children
        for (TrieNode child : node.children.values()) {
            searchSubstringDFS(child, substring, results);
        }
    }

    // Public method to get frequency of the word from the TrieNode where it ends
    public int getWordFrequency(String word) {
        TrieNode current = root;
        for (char c : word.toCharArray()) {
            current = current.children.get(c);
        }
        return current.wordFrequency.getOrDefault(word, 0);
    }
}

// Main Class
public class DynamicSearchQueries {
    public static void main(String[] args) {
        Trie trie = new Trie();

        // Load search queries from a CSV file
        String csvFile = "queries.csv"; // Replace with the path to your CSV file
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                trie.insert(line.trim());
            }
        } catch (IOException e) {
            System.out.println("Error reading CSV file: " + e.getMessage());
            return;
        }

        // Simulate user input for dynamic search
        Scanner scanner = new Scanner(System.in);
        System.out.println("Type a search term:");
        String input = "";

        while (true) {
            System.out.print("Enter next character (or type 'exit' to quit): ");
            String next = scanner.nextLine();
            if (next.equalsIgnoreCase("exit")) {
                break;
            }

            if (!next.isEmpty()) {
                input += next;
                
                // Get prefix and substring suggestions
                List<String> prefixSuggestions = trie.searchByPrefix(input);
                List<String> substringSuggestions = trie.searchBySubstring(input);
                
                // Combine both prefix and substring results
                Set<String> combinedResults = new HashSet<>(prefixSuggestions);
                combinedResults.addAll(substringSuggestions);

                // Sort combined results by frequency
                List<String> sortedResults = new ArrayList<>(combinedResults);
                sortedResults.sort((a, b) -> {
                    int freqA = trie.getWordFrequency(a);
                    int freqB = trie.getWordFrequency(b);
                    return Integer.compare(freqB, freqA); // Sort descending by frequency
                });

                // Limit results to top 5
                List<String> top5Results = sortedResults.size() > 5 ? sortedResults.subList(0, 5) : sortedResults;

                System.out.println("Top 5 suggestions: " + top5Results);
            }
        }

        scanner.close();
    }
}
