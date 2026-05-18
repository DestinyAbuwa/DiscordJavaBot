package DiscordJavaBot.DiscordJavaBot;

import javax.security.auth.login.LoginException;

//import events.MessageEventListener;
//import events.ReadyEventListener;
import events.InteractionEventListener;
import events.MessageEventListener;
import events.ReadyEventListener;
import games.PigDiceGame;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;


public class ZoinkBot 
{
    public static void main( String[] args ) throws LoginException
    {
        System.out.println( "Hello World!" );
        
        final String TOKEN = System.getenv("BOT_TOKEN");
        JDABuilder jdabuilder = JDABuilder.createDefault(TOKEN);
  
       JDA jda = jdabuilder
        .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES)
        .addEventListeners(new ReadyEventListener(), new MessageEventListener(), new InteractionEventListener(), new PigDiceGame())
        .build();
       
       jda.upsertCommand("tic-tac-toe", "Play Tic-Tac-Toe").setGuildOnly(true).queue();
       jda.upsertCommand("pig-dice", "Play Pig Dice").setGuildOnly(true).queue();

       
    }
}
