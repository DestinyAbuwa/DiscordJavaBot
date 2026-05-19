package games;

import java.awt.Color;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

public class WordleGame extends ListenerAdapter {
    private final Map<String, WordleSession> activeGames = new ConcurrentHashMap<>();
    private final EmbedBuilder eb = new EmbedBuilder();

    // 🕵️‍♂️ This map holds the ID of the human who used the slash command in this channel
    private final Map<String, PendingPlayer> pendingPlayers = new ConcurrentHashMap<>();

    // A small helper class to pass both name and ID together
    public static class PendingPlayer {
        public final String id;
        public final String name;
        public PendingPlayer(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    // Your receptionist calls this to register who clicked the slash command
    public void registerPendingPlayer(String channelId, String userId, String userName) {
        pendingPlayers.put(channelId, new PendingPlayer(userId, userName));
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw();

        if (message.equalsIgnoreCase("Wordy Word Time")) {
            String channelId = event.getChannel().getId();

            if (activeGames.containsKey(channelId)) {
                event.getChannel().sendMessage("⚠️ A Wordle game is already active in this channel!").queue();
                return;
            }

            // Grab the real human data from our cache map!
            PendingPlayer human = pendingPlayers.remove(channelId);

            // Fallback default variables just in case someone types the text manually instead of using the slash command
            String playerId = (human != null) ? human.id : event.getAuthor().getId();
            String playerName = (human != null) ? human.name : event.getMember().getEffectiveName();

            // Generate a fresh secret word from our engine list
            String secretWord = WordleEngine.generateSecretWord();
            WordleSession session = new WordleSession(channelId, playerId, playerName, secretWord);

            renderGameBoard(session, "🟩 Welcome to Wordle! Click **Submit Guess** to make your first move.");

            event.getChannel().sendMessageEmbeds(eb.build())
                    .addActionRow(
                            Button.primary("wd_open_modal", "Submit Guess📝"),
                            Button.danger("wd_cancel", Emoji.fromUnicode("❌"))
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
        if (!componentId.startsWith("wd_")) return;

        String channelId = event.getChannel().getId();
        WordleSession session = activeGames.get(channelId);

        if (session == null || !event.getMessageId().equals(session.getMessage().getId())) {
            event.reply("This game session is no longer valid.").setEphemeral(true).queue();
            return;
        }

        // Only allow the player who initiated the /wordle command to interact
        if (!event.getUser().getId().equals(session.getPlayerId())) {
            event.reply("⚠️ This is a solo game session for **" + session.getPlayerName() + "**! Start your own with `/wordle`.").setEphemeral(true).queue();
            return;
        }

        if (componentId.equals("wd_cancel")) {
            event.deferEdit().queue();
            event.getHook().deleteMessageById(session.getMessage().getId()).queue();
            activeGames.remove(channelId);
            return;
        }

        // 📝 POP UP DISCORD MODAL FORM
        if (componentId.equals("wd_open_modal")) {
            TextInput guessInput = TextInput.create("wd_input_text", "Your Guess", TextInputStyle.SHORT)
                    .setPlaceholder("Enter a 5-letter word...")
                    .setMinLength(5)
                    .setMaxLength(5)
                    .setRequired(true)
                    .build();

            Modal modal = Modal.create("wd_guess_modal", "Wordle Guess Submission")
                    .addActionRows(ActionRow.of(guessInput))
                    .build();

            event.replyModal(modal).queue(); // Opens the native form modal overlay on the user's app screen
        }
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if (!event.getModalId().equals("wd_guess_modal")) return;

        String channelId = event.getChannel().getId();
        WordleSession session = activeGames.get(channelId);

        if (session == null) {
            event.reply("Your game session has timed out or expired.").setEphemeral(true).queue();
            return;
        }

        // Acknowledge the modal submittal instantly
        event.deferEdit().queue();

        String rawGuess = event.getValue("wd_input_text").getAsString().toUpperCase().trim();

        // ❌ Validation Check: Is it a valid dictionary word?
        if (!WordleEngine.isValidWord(rawGuess)) {
            renderGameBoard(session, "❌ `" + rawGuess + "` is not a valid word in the dictionary database! Try another combination.");
            return;
        }

        // Save guess to session history lists
        session.addGuess(rawGuess);

        // Check Match Status
        if (session.isGameWon()) {
            handleGameOver(session, true);
        } else if (session.getGuesses().size() >= session.getMaxAttempts()) {
            handleGameOver(session, false);
        } else {
            // Game continues -> redraw board layout screen
            renderGameBoard(session, "✅ Evaluated guess: `" + rawGuess + "`");
        }
    }

    private void renderGameBoard(WordleSession session, String liveFeedText) {
        eb.clear();
        eb.setTitle("🟩 Wordle Challenge Arena 🟩");
        eb.setColor(Color.GREEN);
        eb.setDescription("### 👤 Player: **" + session.getPlayerName() + "**\n" +
                "Remaining Attempts: `" + (session.getMaxAttempts() - session.getGuesses().size()) + " / 6`\n\n" +
                "📢 **System Log:** " + liveFeedText + "\n\n");

        StringBuilder gridBuilder = new StringBuilder();
        String secret = session.getSecretWord();

        // 🟦 Loop 6 times to render all rows
        for (int i = 0; i < session.getMaxAttempts(); i++) {
            if (i < session.getGuesses().size()) {
                String guess = session.getGuesses().get(i);
                gridBuilder.append(evaluateWordBlocks(guess, secret)).append("  ->  `").append(guess).append("`\n");
            } else {
                // Empty row lines if no guess has filled the slot yet
                gridBuilder.append("⬜ ⬜ ⬜ ⬜ ⬜  ->  `_ _ _ _ _`\n");
            }
        }

        eb.addField("🧩 Game Progress Board", gridBuilder.toString(), false);
        eb.setFooter("Wordle Engine | Think carefully before submitting!");

        // If the message exists, edit it. Otherwise, wait for startLobby queue mapping
        if (session.getMessage() != null) {
            session.getMessage().editMessageEmbeds(eb.build()).queue();
        }
    }

    /**
     * Helper algorithm to calculate and color-code the individual text-blocks.
     */
    private String evaluateWordBlocks(String guess, String secret) {
        String[] rowBlocks = new String[5];

        // Arrays to handle accurate matching flags for duplicate letters
        boolean[] secretMatched = new boolean[5];
        boolean[] guessMatched = new boolean[5];

        // First Pass: Match Green exact indices
        for (int i = 0; i < 5; i++) {
            if (guess.charAt(i) == secret.charAt(i)) {
                rowBlocks[i] = "🟩";
                secretMatched[i] = true;
                guessMatched[i] = true;
            }
        }

        // Second Pass: Match Yellow matching indices elsewhere
        for (int i = 0; i < 5; i++) {
            if (guessMatched[i]) continue; // Skip green slots

            for (int j = 0; j < 5; j++) {
                if (!secretMatched[j] && guess.charAt(i) == secret.charAt(j)) {
                    rowBlocks[i] = "🟨";
                    secretMatched[j] = true;
                    guessMatched[i] = true;
                    break;
                }
            }

            // If still no match discovered, color it empty gray/black block
            if (rowBlocks[i] == null) {
                rowBlocks[i] = "⬛";
            }
        }

        return String.join(" ", rowBlocks);
    }

    private void handleGameOver(WordleSession session, boolean won) {
        eb.clear();
        eb.setTitle(won ? "🎉 WORDLE VICTORY 👑" : "💥 GAME OVER 💥");
        eb.setColor(won ? Color.GREEN : Color.RED);

        if (won) {
            eb.setDescription("## **Congratulations!**\n**" + session.getPlayerName() + "** solved the puzzle correctly in `" + session.getGuesses().size() + "` tries!");
        } else {
            eb.setDescription("## **Out of Turns!**\nBetter luck next time. The secret word hidden on the board was: **`" + session.getSecretWord() + "`**");
        }

        // Draw final display layout map string
        StringBuilder gridBuilder = new StringBuilder();
        String secret = session.getSecretWord();
        for (int i = 0; i < session.getMaxAttempts(); i++) {
            if (i < session.getGuesses().size()) {
                String guess = session.getGuesses().get(i);
                gridBuilder.append(evaluateWordBlocks(guess, secret)).append("  ->  `").append(guess).append("`\n");
            } else {
                gridBuilder.append("⬜ ⬜ ⬜ ⬜ ⬜\n");
            }
        }

        eb.addField("🏁 Final Guess Recaps", gridBuilder.toString(), false);
        eb.setFooter("Wordle Engine | Session Cleared Successfully");

        // Overwrite old state buttons completely to disable cheating inputs after closure
        session.getMessage().editMessageEmbeds(eb.build()).setComponents().queue();

        // Wipe session from background cache map memory
        activeGames.remove(session.getChannelId());
    }
}