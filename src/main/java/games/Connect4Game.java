package games;

import java.awt.Color;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Connect4Game extends ListenerAdapter {
    private final Map<String, Connect4Session> activeGames = new ConcurrentHashMap<>();
    private final EmbedBuilder eb = new EmbedBuilder();

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw();

        // Using equalsIgnoreCase so it works whether they capitalize it or not
        if (message.equalsIgnoreCase("Connect 4 Time")) {
            String channelId = event.getChannel().getId();

            if (activeGames.containsKey(channelId)) {
                event.getChannel().sendMessage("⚠️ A Connect 4 game is already active in this channel!").queue();
                return;
            }

            Connect4Session session = new Connect4Session(channelId);

            eb.clear();
            eb.setTitle("🔴 Connect 4 Arena 🟡");
            eb.setDescription("### 📥 Lobby Setup\nClick the button below to step up as **Player 1**!\n\n*Line up 4 discs vertically, horizontally, or diagonally to win.*");
            eb.setColor(Color.BLUE);

            event.getChannel().sendMessageEmbeds(eb.build())
                    .addActionRow(
                            Button.primary("c4_join1", "Join as P1").withEmoji(Emoji.fromUnicode("1️⃣")),
                            Button.danger("c4_cancel", Emoji.fromUnicode("❌"))
                    )
                    .queue(sentMessage -> {
                        session.setMessage(sentMessage);
                        activeGames.put(channelId, session);
                    });
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String componentId = event.getComponentId();

        // Filter out non-Connect 4 button interactions
        if (!componentId.startsWith("c4_")) return;

        String channelId = event.getChannel().getId();
        Connect4Session session = activeGames.get(channelId);

        if (session == null || !event.getMessageId().equals(session.getMessage().getId())) {
            event.reply("This game session is no longer valid.").setEphemeral(true).queue();
            return;
        }

        event.deferEdit().queue();
        String userNickname = event.getMember().getEffectiveName();

        // --- Lobby Setup Controls ---
        if (componentId.equals("c4_join1")) {
            session.setPlayer1(userNickname);
            updateLobbyP2(session);
        }
        else if (componentId.equals("c4_join2")) {
            if (userNickname.equals(session.getPlayer1())) return; // Un-comment to test with friends
            session.setPlayer2(userNickname);
            updateLobbyReady(session);
        }
        else if (componentId.equals("c4_cancel")) {
            event.getHook().deleteMessageById(session.getMessage().getId()).queue();
            activeGames.remove(channelId);
        }
        else if (componentId.equals("c4_start")) {
            session.setGameActive(true);
            renderGameBoard(session, "🏁 Match started! **" + session.getPlayer1() + "** takes the first move.");
        }

        // --- Core Gameplay Controls ---
        else if (componentId.startsWith("c4_col_")) {
            String expectedHitter = (session.getActivePlayer() == 1) ? session.getPlayer1() : session.getPlayer2();
            if (!userNickname.equals(expectedHitter)) {
                return; // Blocks spectators or out-of-turn inputs
            }

            int chosenColumn = Integer.parseInt(componentId.replace("c4_col_", ""));
            boolean moveSuccessful = session.makeMove(chosenColumn);

            if (!moveSuccessful) {
                renderGameBoard(session, "⚠️ Column " + (chosenColumn + 1) + " is completely full! Try another column.");
                return;
            }

            // Check for a win
            if (session.checkWinCondition()) {
                handleGameOver(session, expectedHitter, false);
                return;
            }

            // Check for a draw
            if (session.checkDrawCondition()) {
                handleGameOver(session, "No one", true);
                return;
            }

            // Switch turns and update game board layout
            session.switchTurn();
            renderGameBoard(session, "**" + userNickname + "** dropped a piece in Column " + (chosenColumn + 1) + "!");
        }
    }

    private void updateLobbyP2(Connect4Session session) {
        eb.clear();
        eb.setTitle("🔴 Connect 4 Arena 🟡");
        eb.setDescription("### 📥 Lobby Setup\nClick below to join as **Player 2**!");
        eb.addField("⚔️ Contenders", "• **Player 1:** `" + session.getPlayer1() + "`\n• **Player 2:** *Waiting...*", false);
        eb.setColor(Color.BLUE);

        session.getMessage().editMessageEmbeds(eb.build())
                .setActionRow(
                        Button.primary("c4_join2", "Join as P2").withEmoji(Emoji.fromUnicode("2️⃣")),
                        Button.danger("c4_cancel", Emoji.fromUnicode("❌"))
                ).queue();
    }

    private void updateLobbyReady(Connect4Session session) {
        eb.clear();
        eb.setTitle("🔴 Connect 4 Arena 🟡");
        eb.setDescription("### 🎲 Lobby Full!\nAre you both ready to start the match?");
        eb.addField("⚔️ Contenders", "• **Player 1:** `" + session.getPlayer1() + "`\n• **Player 2:** `" + session.getPlayer2() + "`", false);
        eb.setColor(Color.BLUE);

        session.getMessage().editMessageEmbeds(eb.build())
                .setActionRow(
                        Button.success("c4_start", Emoji.fromUnicode("✅")),
                        Button.danger("c4_cancel", Emoji.fromUnicode("❌"))
                ).queue();
    }

    private void renderGameBoard(Connect4Session session, String statusMessage) {
        eb.clear();
        eb.setTitle("🔴 Connect 4 Arena 🟡");
        eb.setColor(Color.BLUE);

        String activePlayerName = (session.getActivePlayer() == 1) ? session.getPlayer1() : session.getPlayer2();
        String playerColor = (session.getActivePlayer() == 1) ? "🔴 RED" : "🟡 YELLOW";

        eb.setDescription("### ⚡ Active Turn: **" + activePlayerName + "** (" + playerColor + ")\n" +
                "Select a column button below to drop your disc!\n\n" +
                "📢 **Live Feed:** " + statusMessage + "\n\n");

        StringBuilder gridBuilder = new StringBuilder();
        int[][] board = session.getBoard();

        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                int piece = board[row][col];
                if (piece == 1) {
                    gridBuilder.append("🔴 ");
                } else if (piece == 2) {
                    gridBuilder.append("🟡 ");
                } else {
                    gridBuilder.append("⚫ ");
                }
            }
            gridBuilder.append("\n");
        }
        gridBuilder.append("1️⃣ 2️⃣ 3️⃣ 4️⃣ 5️⃣ 6️⃣ 7️⃣");

        eb.addField("🕹️ Match Board", gridBuilder.toString(), false);
        eb.setFooter("Connect 4 Engine | P1: " + session.getPlayer1() + " | P2: " + session.getPlayer2());

        session.getMessage().editMessageEmbeds(eb.build())
                .setComponents(
                        ActionRow.of(
                                Button.secondary("c4_col_0", "1"),
                                Button.secondary("c4_col_1", "2"),
                                Button.secondary("c4_col_2", "3"),
                                Button.secondary("c4_col_3", "4")
                        ),
                        ActionRow.of(
                                Button.secondary("c4_col_4", "5"),
                                Button.secondary("c4_col_5", "6"),
                                Button.secondary("c4_col_6", "7"),
                                Button.danger("c4_cancel", Emoji.fromUnicode("❌"))
                        )
                ).queue();
    }

    private void handleGameOver(Connect4Session session, String winnerName, boolean isDraw) {
        eb.clear();
        eb.setTitle("👑 CONNECT 4 MATCH COMPLETE 🎉");
        eb.setColor(Color.GREEN);

        if (isDraw) {
            eb.setDescription("## **It's a Draw!** 🤝\nThe board is full and the battle ends in a stalemate.");
        } else {
            String winColor = (session.getActivePlayer() == 1) ? "🔴" : "🟡";
            eb.setDescription("## " + winColor + " **" + winnerName + "** has won the match!\nThey successfully lined up 4 discs!");
        }

        StringBuilder gridBuilder = new StringBuilder();
        int[][] board = session.getBoard();
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                int piece = board[row][col];
                if (piece == 1) gridBuilder.append("🔴 ");
                else if (piece == 2) gridBuilder.append("🟡 ");
                else gridBuilder.append("⚫ ");
            }
            gridBuilder.append("\n");
        }
        gridBuilder.append("1️⃣ 2️⃣ 3️⃣ 4️⃣ 5️⃣ 6️⃣ 7️⃣");

        eb.addField("🏁 Final Match Board Layout", gridBuilder.toString(), false);
        eb.setFooter("Connect 4 Engine | Match Closed Successfully");

        // Strip the interactive action rows completely when the game ends
        session.getMessage().editMessageEmbeds(eb.build()).setComponents().queue();

        // Clear session from active map memory
        activeGames.remove(session.getChannelId());
    }
}