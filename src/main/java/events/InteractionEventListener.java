package events;

import java.util.Scanner;

import org.jetbrains.annotations.NotNull;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import pigDiceGame.GameController;


public class InteractionEventListener extends ListenerAdapter
{
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

			
		}
	
	}

}
