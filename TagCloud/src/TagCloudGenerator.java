import java.util.Comparator;

import components.map.Map;
import components.map.Map2;
import components.set.Set;
import components.set.Set1L;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;
import components.sortingmachine.SortingMachine;
import components.sortingmachine.SortingMachine2;

/**
 * Put a short phrase describing the program here.
 *
 * @author Put your name here
 *
 */
public final class TagCloudGenerator {

    /**
     * Private constructor so this utility class cannot be instantiated.
     */
    private TagCloudGenerator() {
    }

    /**
     * Utility class to sort by count for Map.Pairs.
     */
    private static class MapPairCountSort
            implements Comparator<Map.Pair<String, Integer>> {
        @Override
        public int compare(Map.Pair<String, Integer> o1,
                Map.Pair<String, Integer> o2) {
            int o1Count = o1.value();
            int o2Count = o2.value();
            return Integer.compare(o1Count, o2Count);
        }
    }

    /**
     * Utility Class to sort in descending order.
     *
     * @author mralp
     *
     */
    private static class MapPairDescSort
            implements Comparator<Map.Pair<String, Integer>> {
        @Override
        public int compare(Map.Pair<String, Integer> o1,
                Map.Pair<String, Integer> o2) {
            String o1Key = o1.key();
            String o2Key = o2.key();
            return (-1 * o1Key.compareTo(o2Key));
        }
    }

    /**
     * Generate list of separators.
     *
     * @param separators
     */
    public static void generateSeparators(Set<Character> separators) {
        // Method to generate the separators Set

        separators.add(',');
        separators.add('\t');
        separators.add(';');
        separators.add('.');
        separators.add('\n');
        separators.add('-');
        separators.add('\r');

    }

    /**
     * Find the next word, terminate when we find a separator.
     *
     * @param line
     * @param separators
     * @param out
     * @return index
     */
    public static int nextWord(String line, Set<Character> separators) {
        /*
         * Loop till we hit a separator, and then terminate when we do, return
         * the int corresponding to the index of the firsr separator.
         */
        int i = 0;
        char testChar = line.charAt(i);
        while (!separators.contains(testChar)
                && !Character.isWhitespace(testChar)
                && (i + 1) < line.length()) {
            i++;
            testChar = line.charAt(i);
        }
        // Special case for EOL conditions: last char would not be added
        // so if we are not over line length and last char is a char, make sure
        // i increases so we get the last character in the line
        if ((i + 1) >= line.length() && (!separators.contains(testChar)
                && !Character.isWhitespace(testChar))) {
            i++;
        }

        return i;
    }

    /**
     * Find the next character (i.e non separator).
     *
     * @param line
     * @param separators
     * @param out
     * @return index of first character
     */
    public static int findNextNonSep(String line, Set<Character> separators) {
        /*
         * Loop until we find a non separator case, and return i (the index
         * where the first nonsep was found)
         */
        int i = 0;
        char testChar = line.charAt(i);
        while ((separators.contains(testChar)
                || Character.isWhitespace(testChar))
                && (i + 1) < line.length()) {
            i++;
            testChar = line.charAt(i);
        }
        // Special case for EOL conditions: last char would not be "chopped"
        // so if we are not over line length and last char is a sep, make sure
        // i increases so we chop the separator off.
        if ((i + 1) >= line.length() && (separators.contains(testChar)
                || Character.isWhitespace(testChar))) {
            i++;
        }

        return i;
    }

    /**
     * Iterate through all lines and add found words to the map to count them.
     *
     * @param in
     * @param wordCounter
     * @param out
     * @param queueToSort
     * @param bigToSmall
     */

    public static void iterateThroughLines(SimpleReader in,
            Map<String, Integer> wordCounter) {
        // Define and generate a set of separator characters
        Set<Character> separators = new Set1L<Character>();

        generateSeparators(separators);
        // While not at EOS, keep reading the file
        // When we hit EOS, exit the method and return nothing (void)
        while (!in.atEOS()) {
            // Fetches the next line when the line length becomes 0
            String line = in.nextLine();
            while (line.length() > 0) {
                /*
                 * As long as length > 0, keep reading. This section will call
                 * nextWord until a sep is reached, and then "cut" the string.
                 * Then findNextNonSep will read to find where the next
                 * character is, or reach the end of the line, and cut till
                 * there. Repeat until line length is 0 and we need a new line
                 */
                int wordIndex = nextWord(line, separators);
                String word = line.substring(0, wordIndex).toLowerCase();

                //out.println(word);
                if (word.length() > 0) {
                    // Add to queue to sort it
                    // If we've seen this before, add to the count
                    if (wordCounter.hasKey(word)) {
                        int count = wordCounter.value(word);
                        count++;
                        wordCounter.replaceValue(word, count);

                    } else {
                        //If not, add it to the map
                        wordCounter.add(word, 1);
                    }
                }
                // Code to go through all the seps until we find the next character
                // or we hit EOL
                line = line.substring(wordIndex, line.length());
                if (line.length() > 0) {
                    int nextNonSep = findNextNonSep(line, separators);
                    line = line.substring(nextNonSep, line.length());
                }

            }

        }

    }

    /**
     * Method to generate top HTML.
     *
     * @param out
     *            : the output stream
     */
    private static void generateHTML(SimpleWriter out, String filename) {
        out.println("<!DOCTYPE html><html><head>");
        out.println(
                "<link href=\"http://web.cse.ohio-state.edu/software/2231/web-sw2/assignments/projects/tag-cloud-generator/data/tagcloud.css\" rel=\"stylesheet\" type=\"text/css\"> \n");
        out.println(
                "<link href=\"tagcloud.css\" rel=\"stylesheet\" type=\"text/css\"> \n");
        out.println("</head><body>\n");
        out.println("<h2>Top 100 words in " + filename + "</h2>");
        out.println("<hr>\n<div class=\"cdiv\">\r\n" + "<p class=\"cbox\">");
    }

    /**
     * Adds the specified word to the output file.
     *
     * @param word
     *            : word to add to HTML file
     * @param count
     *            : count, sourced from map
     * @param out
     *            : output stream
     */
    private static void addWordToCloud(String word, int count,
            SimpleWriter out) {
        out.println();
    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();
        out.println(
                "Please enter the name of the file, including the extension.\n");
        String filename = in.nextLine();
        SimpleReader inFile = new SimpleReader1L(filename);
        SimpleWriter outputFile = new SimpleWriter1L("output.html");
        Map<String, Integer> wordCounter = new Map2<String, Integer>();
        iterateThroughLines(inFile, wordCounter);
        Comparator<Map.Pair<String, Integer>> countSort = new MapPairCountSort();
        Comparator<Map.Pair<String, Integer>> descSort = new MapPairDescSort();
        SortingMachine<Map.Pair<String, Integer>> sortCounts = new SortingMachine2<Map.Pair<String, Integer>>(
                countSort);
        for (Map.Pair<String, Integer> val : wordCounter) {
            sortCounts.add(val);
        }
        sortCounts.changeToExtractionMode();
        generateHTML(outputFile, filename);

        inFile.close();
        outputFile.close();
        in.close();
        out.close();
    }

}
