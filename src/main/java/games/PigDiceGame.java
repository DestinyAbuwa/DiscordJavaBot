package games;

import java.awt.Color;
import org.jetbrains.annotations.NotNull;
import events.MessageEventListener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageEmbedEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import pigDiceGame.EntryPoint;
import pigDiceGame.GameController;
import pigDiceGame.PigDice;

public class PigDiceGame extends ListenerAdapter
{
	private EmbedBuilder eb = new EmbedBuilder();
	
	private PigDice pd1 = new PigDice();
	private PigDice pd2 = new PigDice();
	private int maxScore = 100;
	
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
		String[] messageSent = event.getMessage().getContentRaw().split(" ");
		
		if (message.equals("Piggy Dice Time") && event.getMessage().getAuthor().isBot()) 
		{
			eb.clear();
			eb.setTitle("First to 100");
			eb.setDescription("REACT TO JOIN AS PLAYER 1 \r\nRed X to quit");
			eb.addField("Rules",  "1. Take turns to roll as many times as you want in one turn.\r\n"
					+ "2. A player scores the sum of the two dice thrown\r\n"
					+ "3. If a single number 1 is thrown on either die, the score for that whole turn is lost. (Pigged Out!)\r\n"
					+ "4. However, a double 1 counts as 25 points. ", true);
			
			eb.setColor(Color.MAGENTA);
			eb.setImage(pig);
			msg = event.getChannel().sendMessageEmbeds(eb.build()).complete();
			msg.addReaction(Emoji.fromUnicode(uniC1)).queue();
			msg.addReaction(Emoji.fromUnicode(uniCx)).queue();
			

			
		}
	}
	
	@Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event)
    {

    	if (!event.getMember().getUser().isBot())
    	{
    		if (event.getEmoji().getAsReactionCode().equals(emj1))
    		{    			
    			msg.delete().queue();
    			
    			eb.setDescription("REACT TO JOIN AS PLAYER 2 \r\nRed X to quit");
    			player1 = event.getMember().getNickname();	
    			eb.addField("Player 1", player1, true);
    			msg = event.getChannel().sendMessageEmbeds(eb.build()).complete();
    			msg.addReaction(Emoji.fromUnicode(uniC2)).queue();
    			msg.addReaction(Emoji.fromUnicode(uniCx)).queue();

    		}
    		if(event.getEmoji().getAsReactionCode().equals(emj2))
    		{
    			
    			
    			player2 = event.getMember().getNickname();
    			
    			if (player1.equals(player2))
    			{
    				event.getChannel().sendMessage("you already joined " + player2).queue();
    			}
    			else
    			{
    				msg.delete().queue();
        			eb.setDescription("READY TO PLAY?? \r\nRed X to quit");
        			eb.addField("Player 2", player2, true);
        			msg = event.getChannel().sendMessageEmbeds(eb.build()).complete();
        			msg.addReaction(Emoji.fromUnicode(uniCcheck)).queue();		
        			msg.addReaction(Emoji.fromUnicode(uniCx)).queue();
    			}
    			

    		}
    		if (event.getEmoji().getAsReactionCode().equals(emjX))
    		{
    			msg.delete().queue();
    		}
    		if (event.getEmoji().getAsReactionCode().equals(emjCheck))
    		{
    			msg.delete().queue();
    			eb.clear();
    			eb.setDescription("ROLL THE DICE\r\n" + player1);
    			eb.setThumbnail(pig);
    			msg = event.getChannel().sendMessageEmbeds(eb.build()).complete();
    			msg.addReaction(Emoji.fromUnicode(uniCdice)).queue();		
    			msg.addReaction(Emoji.fromUnicode(uniCx)).queue();
    		}
    		if (event.getEmoji().getAsReactionCode().equals(emjDice))
    		{
    			if (eb.getDescriptionBuilder().toString().equals("ROLL THE DICE\r\n" + player1))
    			{
    				if(event.getMember().getNickname().equals(player1))
        			{
        				msg.delete().queue();

    					
        				pd1.rollDice();
        				// Report the result
        				eb.clear();
        				eb.setDescription("ROLL THE DICE\r\n" + player1);
        				eb.addField("Points Scored", pd1.lastRoll() + " scored " + pd1.evaluate() + " points.", true);

        				// Did the player pig out?
        				if (pd1.piggedOut())
        				{	
        					eb.clear();
        					eb.addField("PIGGED OUT!", player1 + " pigged out this turn, yikes", true);
        					eb.addField("NEXT TURN", "It's " + player2 + "'s turn to roll", true);	
    	        			eb.setThumbnail(pig);
        					msg = event.getChannel().sendMessageEmbeds(eb.build()).complete();
    	        			
    	        			eb.clear();
    	        			eb.setDescription("ROLL THE DICE\r\n" + player2);
    	        			eb.setThumbnail(pig);
    	        			msg = event.getChannel().sendMessageEmbeds(eb.build()).complete();
    	        			msg.addReaction(Emoji.fromUnicode(uniCdice)).queue();		
    	        			msg.addReaction(Emoji.fromUnicode(uniCx)).queue();

        				}
            			
            			else
            			{
            				//
            				// Roll again; see if the user wants to roll again to add to total or pass and keep current points
            				//
    
        					eb.addField("ROLL AGAIN?", "Your current roll is " + pd1.currentRound() + " points. \r\nGreen X to end turn", true);
        					eb.setThumbnail(pig);
                			msg = event.getChannel().sendMessageEmbeds(eb.build()).complete();
                			msg.addReaction(Emoji.fromUnicode(uniCdice)).queue();
                			msg.addReaction(Emoji.fromUnicode(uniCgreenX)).queue();		
                			msg.addReaction(Emoji.fromUnicode(uniCx)).queue();

            				
            			} 

        			}
        			else
            		{
            			event.getChannel().sendMessage("uh you're not " + player1 + " lol").queue();
            		}
    			}
    			if (eb.getDescriptionBuilder().toString().equals("ROLL THE DICE\r\n" + player2))
    			{
    				if(event.getMember().getNickname().equals(player2))
    				{
    					msg.delete().queue();

    					
        				pd2.rollDice();
        				// Report the result
        				eb.clear();
        				eb.setDescription("ROLL THE DICE\r\n" + player2);
        				eb.addField("Points Scored", pd2.lastRoll() + " scored " + pd2.evaluate() + " points.", true);

        				// Did the player pig out?
        				if (pd2.piggedOut())
        				{	
        					eb.clear();
        					eb.addField("PIGGED OUT!", player2 + " pigged out this turn, yikes", true);
							eb.addField("NEXT TURN", "It's " + player1 + "'s turn to roll", true);
							eb.setThumbnail(pig);
        					msg = event.getChannel().sendMessageEmbeds(eb.build()).complete();

        					eb.clear();
        					eb.addField("Current score", "Player 1: "+ pd1.currentTotal() + "\r\nPlayer 2: " + pd2.currentTotal() + "\r\nThe goal is " + maxScore + " points." , true );
        					eb.setThumbnail(pig);
        					msg = event.getChannel().sendMessageEmbeds(eb.build()).complete();

        					
							if (pd1.currentTotal() >= maxScore && pd2.currentTotal() < maxScore)
							{

								eb.clear();
								eb.setTitle("GAME OVER");
								eb.setDescription(player1 + " wins!");
								eb.setImage(pig);
	        					msg = event.getChannel().sendMessageEmbeds(eb.build()).complete();
	        					return;

								
							}
							else if (pd2.currentTotal() >= maxScore && pd1.currentTotal() < maxScore)
							{
								eb.clear();
								eb.setTitle("GAME OVER");
								eb.setDescription(player2 + " wins!");
								eb.setImage(pig);
	        					msg = event.getChannel().sendMessageEmbeds(eb.build()).complete();
	        					return;
							}
							else if((pd2.currentTotal() >= maxScore && pd1.currentTotal() >= maxScore))
							{
								eb.clear();
								eb.setTitle("GAME OVER");
								eb.setDescription("TIE GAME!");
								eb.setImage(pig);
	        					msg = event.getChannel().sendMessageEmbeds(eb.build()).complete();
	        					return;
							}
							else
							{
	    	        			
	    	        			eb.clear();
	    	        			eb.setDescription("ROLL THE DICE\r\n" + player1);
	    	        			eb.setThumbnail(pig);
	    	        			msg = event.getChannel().sendMessageEmbeds(eb.build()).complete();
	    	        			msg.addReaction(Emoji.fromUnicode(uniCdice)).queue();		
	    	        			msg.addReaction(Emoji.fromUnicode(uniCx)).queue();
							}

        					
    	        			
    	    				
    	    				

        				}
            			
            			else
            			{
            				//
            				// Roll again; see if the user wants to roll again to add to total or pass and keep current points
            				//
    
        					eb.addField("ROLL AGAIN?", "Your current roll is " + pd2.currentRound() + " points. \r\nGreen X to end turn", true);
        					eb.setThumbnail(pig);
                			msg = event.getChannel().sendMessageEmbeds(eb.build()).complete();
                			msg.addReaction(Emoji.fromUnicode(uniCdice)).queue();
                			msg.addReaction(Emoji.fromUnicode(uniCgreenX)).queue();		
                			msg.addReaction(Emoji.fromUnicode(uniCx)).queue();

            				
            			} 

    				}
    				else
            		{
            			event.getChannel().sendMessage("uh you're not " + player2 + " lol").queue();
            		}
    			}
    			
    		}
    		if (event.getEmoji().getAsReactionCode().equals(emjgreenX))
    		{
    			if(eb.getDescriptionBuilder().toString().equals("ROLL THE DICE\r\n" + player1))
    			{
    				if(event.getMember().getNickname().equals(player1))
        			{
	    				msg.delete().queue();
	    				int roundScore = pd1.save();
	    				eb.clear();
	    				eb.setTitle(player1 + "'s Round Results");
	    				eb.addField("Round Total", "" + roundScore, true);
	    				eb.addField("Total Score", "" + pd1.currentTotal(), true);
	    				eb.addField("NEXT TURN", "It's " + player2 + "'s turn to roll", true);
	    				eb.setThumbnail(pig);
	        			msg = event.getChannel().sendMessageEmbeds(eb.build()).complete();
	        			
	        			eb.clear();
	        			eb.setDescription("ROLL THE DICE\r\n" + player2);
	        			eb.setThumbnail(pig);
	        			msg = event.getChannel().sendMessageEmbeds(eb.build()).complete();
	        			msg.addReaction(Emoji.fromUnicode(uniCdice)).queue();		
	        			msg.addReaction(Emoji.fromUnicode(uniCx)).queue();
        			}
    
    			}
    			if(eb.getDescriptionBuilder().toString().equals("ROLL THE DICE\r\n" + player2))
    			{
    				if(event.getMember().getNickname().equals(player2))
        			{
	    				msg.delete().queue();
	    				int roundScore = pd2.save();
	    				eb.clear();
	    				eb.setTitle(player2 + "'s Round Results");
	    				eb.addField("Round Total", "" + roundScore, true);
	    				eb.addField("Total Score", "" + pd2.currentTotal(), true);
	    				eb.addField("NEXT TURN", "It's " + player1 + "'s turn to roll", true);
	    				eb.setThumbnail(pig);
    					msg = event.getChannel().sendMessageEmbeds(eb.build()).complete();

	    				eb.clear();
	    				eb.addField("Current score", "Player 1: "+ pd1.currentTotal() + "\r\nPlayer 2: " + pd2.currentTotal() + "\r\nThe goal is " + maxScore + " points." , true );
    					eb.setThumbnail(pig);

    					msg = event.getChannel().sendMessageEmbeds(eb.build()).complete();

						if (pd1.currentTotal() >= maxScore && pd2.currentTotal() < maxScore)
						{
        					msg = event.getChannel().sendMessageEmbeds(eb.build()).complete();

							eb.clear();
							eb.setTitle("GAME OVER");
							eb.setDescription(player1 + " wins!");
							eb.setImage(pig);
        					msg = event.getChannel().sendMessageEmbeds(eb.build()).complete();
        					return;

							
						}
						else if (pd2.currentTotal() >= maxScore && pd1.currentTotal() < maxScore)
						{
							eb.clear();
							eb.setTitle("GAME OVER");
							eb.setDescription(player2 + " wins!");
							eb.setImage(pig);
        					msg = event.getChannel().sendMessageEmbeds(eb.build()).complete();
        					return;
						}
						else if((pd2.currentTotal() >= maxScore && pd1.currentTotal() >= maxScore))
						{
							eb.clear();
							eb.setTitle("GAME OVER");
							eb.setDescription("TIE GAME!");
							eb.setImage(pig);
        					msg = event.getChannel().sendMessageEmbeds(eb.build()).complete();
        					return;
						}
	    				
	    				
	    				
	        			
	        			eb.clear();
	        			eb.setDescription("ROLL THE DICE\r\n" + player1);
	        			eb.setThumbnail(pig);
	        			msg = event.getChannel().sendMessageEmbeds(eb.build()).complete();
	        			msg.addReaction(Emoji.fromUnicode(uniCdice)).queue();		
	        			msg.addReaction(Emoji.fromUnicode(uniCx)).queue();
	        			
	        			
	        			
	        			
	        			
	        			
	        			
	        			
        			}
    
    			}
    			

   
    		}




    	}
    }
}
