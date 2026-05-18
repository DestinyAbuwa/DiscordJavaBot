package games;

import java.awt.Color;
import net.dv8tion.jda.api.entities.Message;
import pigDiceGame.PigDice;

public class PigDiceSession {
    private final String channelId;
    private Message message;

    private PigDice pd1;
    private PigDice pd2;
    private int maxScore;
    private int activePlayer; // 1 = Player 1, 2 = Player 2

    private String player1;
    private String player2;

    public PigDiceSession(String channelId) {
        this.channelId = channelId;
        this.maxScore = 100;
        this.activePlayer = 1;
        this.player1 = "blank";
        this.player2 = "blank";
        this.pd1 = new PigDice();
        this.pd2 = new PigDice();
    }

    // --- Getters and Setters ---
    public String getChannelId() { return channelId; }

    public Message getMessage() { return message; }
    public void setMessage(Message message) { this.message = message; }

    public PigDice getPd1() { return pd1; }
    public void resetPd1() { this.pd1 = new PigDice(); }

    public PigDice getPd2() { return pd2; }
    public void resetPd2() { this.pd2 = new PigDice(); }

    public int getMaxScore() { return maxScore; }

    public int getActivePlayer() { return activePlayer; }
    public void setActivePlayer(int activePlayer) { this.activePlayer = activePlayer; }

    public String getPlayer1() { return player1; }
    public void setPlayer1(String player1) { this.player1 = player1; }

    public String getPlayer2() { return player2; }
    public void setPlayer2(String player2) { this.player2 = player2; }
}