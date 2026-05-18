package games;

import java.awt.Color;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import pigDiceGame.PigDice;

public class PigDiceGame extends ListenerAdapter {
    private final Map<String, PigDiceSession> activeGames = new ConcurrentHashMap<>();
    private final EmbedBuilder eb = new EmbedBuilder();
    private final String pig = "https://raw.githubusercontent.com/DestinyAbuwa/DiscordJavaBot/master/assets/pig.webp";

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw();

        // Note: Modified back to author matching standard context if needed,
        // ensure you manage your prefix/bot check definitions here.
        if (message.equals("Piggy Dice Time")) {
            String channelId = event.getChannel().getId();

            if (activeGames.containsKey(channelId)) {
                event.getChannel().sendMessage("⚠️ A Pig Dice game is already active in this channel!").queue();
                return;
            }

            PigDiceSession session = new PigDiceSession(channelId);

            eb.clear();
            eb.setTitle("🐷 Piggy Dice Challenge");
            eb.setDescription("### 📥 Lobby Setup\nClick the button below to step up as **Player 1**!\n\n*Click ❌ at any time to scrap this game.*");
            eb.addField("📜 Official Rules",
                    "1️⃣ Take turns rolling as many times as you dare.\n"
                            + "2️⃣ Earn the **sum** of both rolled dice faces.\n"
                            + "3️⃣ Roll a single **1**? You **Pig Out** and lose all turn points!\n"
                            + "4️⃣ Roll **Double 1s**? Bank a massive **+25 points** instantly.", false);

            eb.setColor(Color.ORANGE);
            eb.setImage(pig);

            event.getChannel().sendMessageEmbeds(eb.build())
                    .setActionRow(
                            Button.primary("pd_join1", "Join as P1").withEmoji(Emoji.fromUnicode("1️⃣")),
                            Button.danger("pd_cancel", Emoji.fromUnicode("❌"))
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

        // Filter out button interaction if it doesn't belong to our Pig Dice namespace
        if (!componentId.startsWith("pd_")) return;

        String channelId = event.getChannel().getId();
        PigDiceSession session = activeGames.get(channelId);

        // Security check: Ignore clicks if session doesn't exist or doesn't match the active game message ID
        if (session == null || !event.getMessageId().equals(session.getMessage().getId())) {
            event.reply("This game session is no longer valid.").setEphemeral(true).queue();
            return;
        }

        // Defer editing immediately to notify Discord we acknowledge the interaction smoothly
        event.deferEdit().queue();

        String userNickname = event.getMember().getEffectiveName();

        // 1. LOBBY JOINING & SETUP LOGIC
        if (componentId.equals("pd_join1")) {
            session.setPlayer1(userNickname);

            eb.clear();
            eb.setTitle("🐷 Piggy Dice Challenge");
            eb.setColor(Color.ORANGE);
            eb.setDescription("### 📥 Lobby Setup\nClick the button below to join as **Player 2**!");
            eb.addField("⚔️ Contenders", "• **Player 1:** `" + session.getPlayer1() + "`\n• **Player 2:** *Waiting...*", false);

            event.getHook().editMessageEmbedsById(session.getMessage().getId(), eb.build())
                    .setActionRow(
                            Button.primary("pd_join2", "Join as P2").withEmoji(Emoji.fromUnicode("2️⃣")),
                            Button.danger("pd_cancel", Emoji.fromUnicode("❌"))
                    ).queue();
        }

        if (componentId.equals("pd_join2")) {
            // Prevent Player 1 from joining as Player 2
            if (userNickname.equals(session.getPlayer1())) {
                return;
            }

            session.setPlayer2(userNickname);

            eb.clear();
            eb.setTitle("🐷 Piggy Dice Challenge");
            eb.setColor(Color.ORANGE);
            eb.setDescription("### 🎲 Lobby Full!\nAre you both ready to start the match?\nClick the checkmark to initiate the board.");
            eb.addField("⚔️ Contenders", "• **Player 1:** `" + session.getPlayer1() + "`\n• **Player 2:** `" + session.getPlayer2() + "`", false);

            event.getHook().editMessageEmbedsById(session.getMessage().getId(), eb.build())
                    .setActionRow(
                            Button.success("pd_start", Emoji.fromUnicode("✅")),
                            Button.danger("pd_cancel", Emoji.fromUnicode("❌"))
                    ).queue();
        }

        if (componentId.equals("pd_cancel")) {
            event.getHook().deleteMessageById(session.getMessage().getId()).queue();
            activeGames.remove(channelId);
            return;
        }

        if (componentId.equals("pd_start")) {
            session.setActivePlayer(1);
            session.resetPd1();
            session.resetPd2();

            renderGameBoard(session, session.getPlayer1(), "🏁 The match has officially commenced! Safe rolling out there.", Color.MAGENTA);
        }

        // 2. IN-GAME DICE ROLLING LOGIC
        if (componentId.equals("pd_roll")) {
            PigDice pd1 = session.getPd1();
            PigDice pd2 = session.getPd2();

            // PLAYER 1 TURN
            if (session.getActivePlayer() == 1 && userNickname.equals(session.getPlayer1())) {
                pd1.rollDice();

                if (pd1.piggedOut()) {
                    session.setActivePlayer(2);
                    pd1.clearTurnPool();
                    renderGameBoard(session, session.getPlayer2(), "🚨 **" + session.getPlayer1() + "** pigged out! That turn pool turned to dust. Turn hands over to **" + session.getPlayer2() + "**.", Color.RED);
                } else {
                    int oldRoundScore = pd1.currentRound();
                    int newRoundScore = pd1.evaluate();
                    int pointsThisThrow = newRoundScore - oldRoundScore;

                    renderRollResult(session, session.getPlayer1(), pd1.lastRoll(), pointsThisThrow, newRoundScore, pd1.currentTotal());
                }
            }
            // PLAYER 2 TURN
            else if (session.getActivePlayer() == 2 && userNickname.equals(session.getPlayer2())) {
                pd2.rollDice();

                if (pd2.piggedOut()) {
                    session.setActivePlayer(1);
                    pd2.clearTurnPool();
                    renderGameBoard(session, session.getPlayer1(), "🚨 **" + session.getPlayer2() + "** pigged out! That turn pool turned to dust. Turn hands over to **" + session.getPlayer1() + "**.", Color.RED);
                } else {
                    int oldRoundScore = pd2.currentRound();
                    int newRoundScore = pd2.evaluate();
                    int pointsThisThrow = newRoundScore - oldRoundScore;

                    renderRollResult(session, session.getPlayer2(), pd2.lastRoll(), pointsThisThrow, newRoundScore, pd2.currentTotal());
                }
            }
        }

        // 3. BANKING / SAVE LOGIC
        if (componentId.equals("pd_bank")) {
            PigDice pd1 = session.getPd1();
            PigDice pd2 = session.getPd2();

            if (session.getActivePlayer() == 1 && userNickname.equals(session.getPlayer1())) {
                pd1.save();

                if (pd1.currentTotal() >= session.getMaxScore()) {
                    handleGameOver(session, session.getPlayer1());
                    return;
                }

                session.setActivePlayer(2);
                renderGameBoard(session, session.getPlayer2(), "✨ **" + session.getPlayer1() + "** played it safe and banked points total! Current Standing: `" + pd1.currentTotal() + "` points.", Color.GREEN);
            }
            else if (session.getActivePlayer() == 2 && userNickname.equals(session.getPlayer2())) {
                pd2.save();

                if (pd2.currentTotal() >= session.getMaxScore()) {
                    handleGameOver(session, session.getPlayer2());
                    return;
                }

                session.setActivePlayer(1);
                renderGameBoard(session, session.getPlayer1(), "✨ **" + session.getPlayer2() + " ** played it safe and banked points total! Current Standing: `" + pd2.currentTotal() + "` points.", Color.GREEN);
            }
        }
    }

    private void renderGameBoard(PigDiceSession session, String activePlayerName, String statusUpdate, Color sideBorderColor) {
        eb.clear();
        eb.setTitle("🎲 Pig Dice Arena");
        eb.setColor(sideBorderColor);
        eb.setDescription("### 🔴 Active Turn: **" + activePlayerName + "**\nUse the buttons below to **Roll** or **Bank**.");

        eb.addField("📢 Live Feed Updates", statusUpdate, false);
        eb.addField("📊 Leaderboard Standings",
                "🏆 **" + session.getPlayer1() + ":** `" + session.getPd1().currentTotal() + " / 100` pts\n" +
                        "🥈 **" + session.getPlayer2() + ":** `" + session.getPd2().currentTotal() + " / 100` pts", false);

        eb.setThumbnail(pig);
        eb.setFooter("System Stats Engine | P1 Banked: " + session.getPd1().currentTotal() + " | P2 Banked: " + session.getPd2().currentTotal());

        session.getMessage().editMessageEmbeds(eb.build())
                .setActionRow(
                        Button.primary("pd_roll", "Roll").withEmoji(Emoji.fromUnicode("🎲")),
                        Button.success("pd_bank", "Bank").withEmoji(Emoji.fromUnicode("❎")),
                        Button.danger("pd_cancel", Emoji.fromUnicode("❌"))
                ).queue();
    }

    private void renderRollResult(PigDiceSession session, String rollerName, String diceRoll, int rollVal, int currentPool, int totalBanked) {
        eb.clear();
        eb.setTitle("🎲 Pig Dice Arena");
        eb.setColor(Color.BLUE);
        eb.setDescription("### ⚡ Active Turn: **" + rollerName + "**\nKeep rolling or bank your points safely!");

        eb.addField("🎲 Last Throw Action", "Rolled: **" + diceRoll + "** (Gained `+" + rollVal + "` points)", false);
        eb.addField("💰 Current Turn Pool", "Accumulated Risk Pool: `" + currentPool + "` points.", true);
        eb.addField("🏦 Permanent Bank", "Safe Banked Total: `" + totalBanked + "` points.", true);

        eb.setThumbnail(pig);
        eb.setFooter("System Stats Engine | Turn Pool: " + currentPool + " | Account Total: " + totalBanked);

        session.getMessage().editMessageEmbeds(eb.build())
                .setActionRow(
                        Button.primary("pd_roll", "Roll").withEmoji(Emoji.fromUnicode("🎲")),
                        Button.success("pd_bank", "Bank").withEmoji(Emoji.fromUnicode("❎")),
                        Button.danger("pd_cancel", Emoji.fromUnicode("❌"))
                ).queue();
    }

    private void handleGameOver(PigDiceSession session, String winner) {
        eb.clear();
        eb.setTitle("👑 MATCH COMPLETE 🎉");
        eb.setColor(Color.GREEN);
        eb.setDescription("## **" + winner + "** has crushed the field!\n" + winner + " successfully crossed `" + session.getMaxScore() + "` points and won the crown!");

        eb.addField("🏁 Final Leaderboard Records",
                "• **" + session.getPlayer1() + ":** `" + session.getPd1().currentTotal() + "` points\n" +
                        "• **" + session.getPlayer2() + ":** `" + session.getPd2().currentTotal() + "` points", false);

        eb.setImage(pig);

        // Remove button action rows entirely when the match completes
        session.getMessage().editMessageEmbeds(eb.build()).setComponents().queue();

        activeGames.remove(session.getChannelId());
    }
}