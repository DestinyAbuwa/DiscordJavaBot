package games;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WordleEngine {
    private static final List<String> WORD_BANK = new ArrayList<>();
    private static final Random RANDOM = new Random();

    // Static block runs ONCE when the bot starts up to download the dictionary
    static {
        try {
            // A clean, public raw text file of official 5-letter Wordle target words
            URL url = new URL("https://raw.githubusercontent.com/DestinyAbuwa/DiscordJavaBot/refs/heads/master/assets/words.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String word;
            while ((word = reader.readLine()) != null) {
                if (word.trim().length() == 5) {
                    WORD_BANK.add(word.trim().toUpperCase());
                }
            }
            reader.close();
            System.out.println("✅ Wordle Engine initialized! Loaded " + WORD_BANK.size() + " words.");
        } catch (Exception e) {
            System.out.println("❌ Failed to load Wordle word bank online, using fallback words.");
            // Quick fallback list in case GitHub is down
            WORD_BANK.addAll(List.of("PIGGY", "DISCO", "MATCH", "ARENA", "DICES", "ZOINK"));
        }
    }

    /**
     * Grabs a completely random 5-letter word from the bank.
     */
    public static String generateSecretWord() {
        return WORD_BANK.get(RANDOM.nextInt(WORD_BANK.size()));
    }

    /**
     * Optional utility to check if a word typed by the user is a valid real English word.
     */
    public static boolean isValidWord(String word) {
        return WORD_BANK.contains(word.toUpperCase());
    }
}