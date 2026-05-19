package games;

import net.dv8tion.jda.api.entities.Message;
import java.util.ArrayList;
import java.util.List;

public class WordleSession {
    private final String channelId;
    private Message message;

    private final String playerId;
    private final String playerName;

    private final String secretWord;
    private final List<String> guesses;
    private final int maxAttempts = 6;

    public WordleSession(String channelId, String playerId, String playerName, String secretWord) {
        this.channelId = channelId;
        this.playerId = playerId;
        this.playerName = playerName;
        this.secretWord = secretWord.toUpperCase(); // Ensure uppercase for easy comparison
        this.guesses = new ArrayList<>();
    }

    /**
     * Adds a validated guess to the player's list.
     */
    public void addGuess(String guess) {
        guesses.add(guess.toUpperCase());
    }

    /**
     * Checks if the game is over (either they guessed the word, or ran out of tries).
     */
    public boolean isGameOver() {
        if (guesses.isEmpty()) return false;

        String lastGuess = guesses.get(guesses.size() - 1);
        return lastGuess.equals(secretWord) || guesses.size() >= maxAttempts;
    }

    public boolean isGameWon() {
        if (guesses.isEmpty()) return false;
        return guesses.get(guesses.size() - 1).equals(secretWord);
    }

    // --- Getters & Setters ---
    public String getChannelId() { return channelId; }

    public Message getMessage() { return message; }
    public void setMessage(Message message) { this.message = message; }

    public String getPlayerId() { return playerId; }
    public String getPlayerName() { return playerName; }

    public String getSecretWord() { return secretWord; }
    public List<String> getGuesses() { return guesses; }
    public int getMaxAttempts() { return maxAttempts; }
}