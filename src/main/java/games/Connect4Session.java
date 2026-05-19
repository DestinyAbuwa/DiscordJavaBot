package games;

import net.dv8tion.jda.api.entities.Message;

public class Connect4Session {
    private final String channelId;
    private Message message;

    private String player1;
    private String player2;
    private int activePlayer; // 1 = Player 1, 2 = Player 2

    // The game board: 6 rows by 7 columns
    private final int[][] board;
    private boolean gameActive;

    public Connect4Session(String channelId) {
        this.channelId = channelId;
        this.player1 = "blank";
        this.player2 = "blank";
        this.activePlayer = 1;
        this.board = new int[6][7]; // Defaults all elements to 0 (empty)
        this.gameActive = false;
    }

    /**
     * Attempts to drop a piece into a specific column.
     * Connect 4 pieces fall to the lowest available row in that column.
     * @param column The column index (0 to 6)
     * @return true if the move was valid and executed, false if the column was full.
     */
    public boolean makeMove(int column) {
        if (column < 0 || column > 6) return false;

        // Start from the bottom row (index 5) and move up to find an empty slot
        for (int row = 5; row >= 0; row--) {
            if (board[row][column] == 0) {
                board[row][column] = activePlayer;
                return true;
            }
        }
        return false; // Column is full!
    }

    /**
     * Toggles the active player turn between 1 and 2.
     */
    public void switchTurn() {
        this.activePlayer = (this.activePlayer == 1) ? 2 : 1;
    }

    /**
     * Checks the 2D matrix to see if the current active player has won the game.
     */
    public boolean checkWinCondition() {
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                if (board[row][col] != activePlayer) continue;

                // Horizontal Check (Right)
                if (col + 3 < 7 &&
                        board[row][col + 1] == activePlayer &&
                        board[row][col + 2] == activePlayer &&
                        board[row][col + 3] == activePlayer) return true;

                // Vertical Check (Down)
                if (row + 3 < 6 &&
                        board[row + 1][col] == activePlayer &&
                        board[row + 2][col] == activePlayer &&
                        board[row + 3][col] == activePlayer) return true;

                // Diagonal Check (Down-Right)
                if (row + 3 < 6 && col + 3 < 7 &&
                        board[row + 1][col + 1] == activePlayer &&
                        board[row + 2][col + 2] == activePlayer &&
                        board[row + 3][col + 3] == activePlayer) return true;

                // Diagonal Check (Up-Right)
                if (row - 3 >= 0 && col + 3 < 7 &&
                        board[row - 1][col + 1] == activePlayer &&
                        board[row - 2][col + 2] == activePlayer &&
                        board[row - 3][col + 3] == activePlayer) return true;
            }
        }
        return false;
    }

    /**
     * Checks if the board is completely full without any empty spaces.
     */
    public boolean checkDrawCondition() {
        for (int col = 0; col < 7; col++) {
            if (board[0][col] == 0) {
                return false; // Found an open space at the top row
            }
        }
        return true;
    }

    // --- Getters and Setters ---
    public String getChannelId() { return channelId; }

    public Message getMessage() { return message; }
    public void setMessage(Message message) { this.message = message; }

    public String getPlayer1() { return player1; }
    public void setPlayer1(String player1) { this.player1 = player1; }

    public String getPlayer2() { return player2; }
    public void setPlayer2(String player2) { this.player2 = player2; }

    public int getActivePlayer() { return activePlayer; }
    public void setActivePlayer(int activePlayer) { this.activePlayer = activePlayer; }

    public int[][] getBoard() { return board; }

    public boolean isGameActive() { return gameActive; }
    public void setGameActive(boolean gameActive) { this.gameActive = gameActive; }
}