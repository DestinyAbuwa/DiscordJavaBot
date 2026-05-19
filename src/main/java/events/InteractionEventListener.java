package events;


import org.jetbrains.annotations.NotNull;
import games.Connect4Game;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;



public class InteractionEventListener extends ListenerAdapter
{

    private final Connect4Game c4Game;

    // The constructor that accepts your game engine from ZoinkBot.java
    public InteractionEventListener(Connect4Game c4Game) {
        this.c4Game = c4Game;
    }

	@Override
	public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event)
	{
		super.onSlashCommandInteraction(event);
		System.out.println("Interaction!");
		
		switch (event.getName())
		{
            case "tic-tac-toe":
                event.reply("Ticky Tac Time").queue();
                break;
            case "pig-dice":
                event.reply("Piggy Dice Time").queue();
                break;
            case "connect-4":
                event.reply("Connect 4 Time").queue();
                break;
            case "wordle":
                event.reply("Wordy Word Time").queue();
                break;
		}
	
	}

}
