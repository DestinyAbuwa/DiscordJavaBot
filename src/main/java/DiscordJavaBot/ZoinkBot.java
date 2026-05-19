package DiscordJavaBot;

import javax.security.auth.login.LoginException;

import events.InteractionEventListener;
import events.MessageEventListener;
import events.ReadyEventListener;
import games.PigDiceGame;
import games.Connect4Game; // 👈 Add this import!
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class ZoinkBot {
    public static void main(String[] args) throws LoginException {
        final String TOKEN = System.getenv("BOT_TOKEN");
        JDABuilder jdabuilder = JDABuilder.createDefault(TOKEN);

        // 1. Instantiate the Connect 4 game container
        Connect4Game c4Game = new Connect4Game();

        JDA jda = jdabuilder
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES)
                .addEventListeners(
                        new ReadyEventListener(),
                        new MessageEventListener(),
                        new InteractionEventListener(c4Game), // 👈 2a. Pass it here to handle the /connect-4 slash command
                        new PigDiceGame(),
                        c4Game // 👈 2b. Pass it here so it can listen for button clicks!
                )
                .build();

        // This updates Discord's database with your clean master list
        jda.updateCommands().addCommands(
                Commands.slash("tic-tac-toe", "Play Tic-Tac-Toe").setGuildOnly(true),
                Commands.slash("pig-dice", "Play Pig Dice").setGuildOnly(true),
                Commands.slash("connect-4", "Play Connect 4").setGuildOnly(true),
                Commands.slash("wordle", "Play Wordle").setGuildOnly(true)
        ).queue();
    }
}