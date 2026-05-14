package events;

import java.awt.Color;

import org.jetbrains.annotations.NotNull;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageEmbedEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.user.UserTypingEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageEventListener extends ListenerAdapter
{
	
	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent event)
	{		
		if (event.getMember() == null)
		{
			event.getChannel().deleteMessageById(event.getMessageId()).queue();
		}
		
//		message log
		if (event.getMember().getNickname() != null)
		{
			System.out.println(event.getMember().getNickname() + ": " + event.getMessage().getContentDisplay());
		}
		else
		{
			System.out.println(event.getMember().getEffectiveName() + ": " + event.getMessage().getContentDisplay());
		}
		
//		bot responses
		String message = event.getMessage().getContentRaw();
		String[] messageSent = event.getMessage().getContentRaw().split(" ");
		if (messageSent[0].equalsIgnoreCase("hey") && !event.getMessage().getAuthor().isBot())
		{
			event.getChannel().sendMessage("hey pookie").queue();
		}
		if (messageSent[0].equalsIgnoreCase("like") && !event.getMessage().getAuthor().isBot())
		{
			event.getChannel().sendMessage("like GRAAH keep it a stack").queue();
		}
		
		if (message.contains("final") && !event.getMessage().getAuthor().isBot())
		{
			event.getChannel().sendMessage("yeah your final breath on this earth").queue();
		}
		if (message.contains("why") && !event.getMessage().getAuthor().isBot())
		{
			event.getChannel().sendMessage("why not").queue();
		}
		
		
		if (event.getAuthor().isBot() && event.getAuthor().getName().contains("Jadey"))
		{
		
			event.getChannel().deleteMessageById(event.getMessageId()).queue();
			
		}
		
		if (!event.getAuthor().isBot() && event.getAuthor().getName().contains("zoink"))
		{
			// event.getChannel().sendMessage("shut up").queue();
		}
		
		

		


			
		

		
	
	}
	
	@Override
    public void onMessageUpdate(@NotNull MessageUpdateEvent event)
	{
		//event.getChannel().sendMessage("nah dont try to edit now").queue();
	}
   

    

    
  

 

	
}
