package events;
import games.WordleGame;
import org.jetbrains.annotations.NotNull;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;



public class InteractionEventListener extends ListenerAdapter
{

    private final WordleGame wGame; // 👈 Add this field

    // Update the constructor to accept both game containers
    public InteractionEventListener(WordleGame wGame) {

        this.wGame = wGame;
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

                // 1. Secretly log your human details into the game cache first
                wGame.registerPendingPlayer(
                        event.getChannel().getId(),
                        event.getUser().getId(),
                        event.getMember().getEffectiveName()
                );

                event.reply("Wordy Word Time").queue();
                break;
		}
	
	}

}
