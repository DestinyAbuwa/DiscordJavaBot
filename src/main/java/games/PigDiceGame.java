package games;

import java.awt.Color;
import org.jetbrains.annotations.NotNull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import pigDiceGame.PigDice;

public class PigDiceGame extends ListenerAdapter
{
    private EmbedBuilder eb = new EmbedBuilder();

    private PigDice pd1 = new PigDice();
    private PigDice pd2 = new PigDice();
    private int maxScore = 100;
    private int activePlayer = 1; // 1 = Player 1, 2 = Player 2

    private Message msg = null;
    private String pig = "https://static-00.iconduck.com/assets.00/pig-emoji-2048x1814-ohcetx18.png";
    private String player1 = "blank";
    private String player2 = "blank";
    private String uniC1 = "U+0031 U+20E3";
    private String uniC2 = "U+0032 U+20E3";
    private String uniCx = "U+274C";
    private String uniCcheck = "U+2705";
    private String uniCdice = "U+1F3B2";
    private String uniCgreenX = "U+274E";
    private String emj1 = "1⃣";
    private String emj2 = "2⃣";
    private String emjX = "❌";
    private String emjCheck = "✅";
    private String emjDice = "🎲";
    private String emjgreenX = "❎";

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event)
    {
        String message = event.getMessage().getContentRaw();

        if (message.equals("Piggy Dice Time") && event.getMessage().getAuthor().isBot())
        {
            eb.clear();
            eb.setTitle("🐷 Piggy Dice Challenge");
            eb.setDescription("### 📥 Lobby Setup\nReact with 1⃣ to step up as **Player 1**!\n\n*Click ❌ at any time to scrap this game.*");
            eb.addField("📜 Official Rules",
                    "1️⃣ Take turns rolling as many times as you dare.\n"
                            + "2️⃣ Earn the **sum** of both rolled dice faces.\n"
                            + "3️⃣ Roll a single **1**? You **Pig Out** and lose all turn points!\n"
                            + "4️⃣ Roll **Double 1s**? Bank a massive **+25 points** instantly.", false);

            eb.setColor(Color.ORANGE); // Warm lobby color
            eb.setImage(pig);
            msg = event.getChannel().sendMessageEmbeds(eb.build()).complete();
            msg.addReaction(Emoji.fromUnicode(uniC1)).queue();
            msg.addReaction(Emoji.fromUnicode(uniCx)).queue();
        }
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (event.getUser().isBot()) return;

        // Auto-remove reaction to instantly reset UI buttons
        event.getReaction().removeReaction(event.getUser()).queue();

        String reactionCode = event.getEmoji().getAsReactionCode();
        String userNickname = event.getMember().getEffectiveName();

        // 1. JOINING LOGIC
        if (reactionCode.equals(emj1)) {
            player1 = userNickname;
            eb.clear();
            eb.setTitle("🐷 Piggy Dice Challenge");
            eb.setColor(Color.ORANGE);
            eb.setDescription("### 📥 Lobby Setup\nReact with 2⃣ to join as **Player 2**!");
            eb.addField("⚔️ Contenders", "• **Player 1:** `" + player1 + "`\n• **Player 2:** *Waiting...*", false);

            msg.editMessageEmbeds(eb.build()).queue();

            msg.clearReactions().queue(success -> {
                msg.addReaction(Emoji.fromUnicode(uniC2)).queue();
                msg.addReaction(Emoji.fromUnicode(uniCx)).queue();
            });
        }

        if (reactionCode.equals(emj2)) {
            player2 = userNickname;

            eb.clear();
            eb.setTitle("🐷 Piggy Dice Challenge");
            eb.setColor(Color.ORANGE);
            eb.setDescription("### 🎲 Lobby Full!\nAre you both ready to start the match?\nClick ✅ to initiate the board.");
            eb.addField("⚔️ Contenders", "• **Player 1:** `" + player1 + "`\n• **Player 2:** `" + player2 + "`", false);

            msg.editMessageEmbeds(eb.build()).queue();

            msg.clearReactions().queue(success -> {
                msg.addReaction(Emoji.fromUnicode(uniCcheck)).queue();
                msg.addReaction(Emoji.fromUnicode(uniCx)).queue();
            });
        }

        if (reactionCode.equals(emjX)) {
            msg.delete().queue();
            return;
        }

        if (reactionCode.equals(emjCheck)) {
            activePlayer = 1;

            pd1 = new PigDice();
            pd2 = new PigDice();

            renderGameBoard(player1, "🏁 The match has officially commenced! Safe rolling out there.", Color.MAGENTA);

            msg.clearReactions().queue(success -> {
                msg.addReaction(Emoji.fromUnicode(uniCdice)).queue();
                msg.addReaction(Emoji.fromUnicode(uniCgreenX)).queue();
                msg.addReaction(Emoji.fromUnicode(uniCx)).queue();
            });
        }

        // 2. DICE ROLLING LOGIC
        if (reactionCode.equals(emjDice)) {
            // PLAYER 1 TURN
            if (activePlayer == 1 && userNickname.equals(player1)) {
                pd1.rollDice();

                if (pd1.piggedOut()) {
                    activePlayer = 2;
                    pd1.clearTurnPool();
                    renderGameBoard(player2, "🚨 **" + player1 + "** pigged out! That turn pool turned to dust. Turn hands over to **" + player2 + "**.", Color.RED);
                } else {
                    int oldRoundScore = pd1.currentRound();
                    int newRoundScore = pd1.evaluate();
                    int pointsThisThrow = newRoundScore - oldRoundScore;

                    renderRollResult(player1, pd1.lastRoll(), pointsThisThrow, newRoundScore, pd1.currentTotal());
                }
            }
            // PLAYER 2 TURN
            else if (activePlayer == 2 && userNickname.equals(player2)) {
                pd2.rollDice();

                if (pd2.piggedOut()) {
                    activePlayer = 1;
                    pd2.clearTurnPool();
                    renderGameBoard(player1, "🚨 **" + player2 + "** pigged out! That turn pool turned to dust. Turn hands over to **" + player1 + "**.", Color.RED);
                } else {
                    int oldRoundScore = pd2.currentRound();
                    int newRoundScore = pd2.evaluate();
                    int pointsThisThrow = newRoundScore - oldRoundScore;

                    renderRollResult(player2, pd2.lastRoll(), pointsThisThrow, newRoundScore, pd2.currentTotal());
                }
            }
        }

        // 3. BANKING / SAVE LOGIC
        if (reactionCode.equals(emjgreenX)) {
            if (activePlayer == 1 && userNickname.equals(player1)) {
                pd1.save();

                if (pd1.currentTotal() >= maxScore) {
                    handleGameOver(player1);
                    return;
                }

                activePlayer = 2;
                renderGameBoard(player2, "✨ **" + player1 + "** played it safe and banked points total! Current Standing: `" + pd1.currentTotal() + "` points.", Color.GREEN);
            }
            else if (activePlayer == 2 && userNickname.equals(player2)) {
                pd2.save();

                if (pd2.currentTotal() >= maxScore) {
                    handleGameOver(player2);
                    return;
                }

                activePlayer = 1;
                renderGameBoard(player1, "✨ **" + player2 + " ** played it safe and banked points total! Current Standing: `" + pd2.currentTotal() + "` points.", Color.GREEN);
            }
        }
    }

    private void renderGameBoard(String activePlayerName, String statusUpdate, Color sideBorderColor) {
        eb.clear();
        eb.setTitle("🎲 Pig Dice Arena");
        eb.setColor(sideBorderColor);
        eb.setDescription("### 🔴 Active Turn: **" + activePlayerName + "**\nUse the buttons below to **Roll** (🎲) or **Bank** (❎).");

        eb.addField("📢 Live Feed Updates", statusUpdate, false);
        eb.addField("📊 Leaderboard Standings",
                "🏆 **" + player1 + ":** `" + pd1.currentTotal() + " / 100` pts\n" +
                        "🥈 **" + player2 + ":** `" + pd2.currentTotal() + " / 100` pts", false);

        eb.setThumbnail(pig);
        eb.setFooter("System Stats Engine | P1 Banked: " + pd1.currentTotal() + " | P2 Banked: " + pd2.currentTotal());
        msg.editMessageEmbeds(eb.build()).queue();
    }

    private void renderRollResult(String rollerName, String diceRoll, int rollVal, int currentPool, int totalBanked) {
        eb.clear();
        eb.setTitle("🎲 Pig Dice Arena");
        eb.setColor(Color.BLUE); // Steady blue color while hot rolling
        eb.setDescription("### ⚡ Active Turn: **" + rollerName + "**\nKeep rolling or bank your points safely!");

        eb.addField("🎲 Last Throw Action", "Rolled: **" + diceRoll + "** (Gained `+" + rollVal + "` points)", false);
        eb.addField("💰 Current Turn Pool", "Accumulated Risk Pool: `" + currentPool + "` points.", true);
        eb.addField("🏦 Permanent Bank", "Safe Banked Total: `" + totalBanked + "` points.", true);

        eb.setThumbnail(pig);
        eb.setFooter("System Stats Engine | Turn Pool: " + currentPool + " | Account Total: " + totalBanked);
        msg.editMessageEmbeds(eb.build()).queue();
    }

    private void handleGameOver(String winner) {
        eb.clear();
        eb.setTitle("👑 MATCH COMPLETE 🎉");
        eb.setColor(Color.GREEN);
        eb.setDescription("## **" + winner + "** has crushed the field!\n" + winner + " successfully crossed `" + maxScore + "` points and won the crown!");

        eb.addField("🏁 Final Leaderboard Records",
                "• **" + player1 + ":** `" + pd1.currentTotal() + "` points\n" +
                        "• **" + player2 + ":** `" + pd2.currentTotal() + "` points", false);

        eb.setImage(pig);
        msg.editMessageEmbeds(eb.build()).queue();
        msg.clearReactions().queue();
    }
}