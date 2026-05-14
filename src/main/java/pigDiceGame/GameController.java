/*
 * @Destiny Abuwa
 * <p> GameController
 * <p> Project 3
 * <p> 
 */
 
//
// The rules to the dice game Pig
//
// Number of Players: 2 + 
// Game Duration: 30 mins
// Players Aged: 6 +
//
// You will need: 2 dice and paper to score on.
//
// To Play: The players take turns to roll both dice, 
// they can roll as many times as they want in one turn.
//
// A player scores the sum of the two dice thrown and 
// gradually reaches a higher score as they continue to roll.
//
// If a single number 1 is thrown on either die, the score 
// for that whole turn is lost. However a double 1 counts as 25.
// The first player to 100 wins unless a player scores more 
// subsequently in the same round. This means that everyone in 
// the game must have the same number of turns.
//
package pigDiceGame;

import java.util.Scanner;

public class GameController
{
	// central method to start and manage game play
	public void play()
	{
		PigDice player1 = new PigDice();
        PigDice player2 = new PigDice();
        	
		Scanner kb = new Scanner( System.in );
		
		try
		{
			int maxScore = getInitialMax( kb );
		
		
			while (player1.currentTotal()<= maxScore && player2.currentTotal() <= maxScore)
			{
				
				
				System.out.println();
				System.out.println("Player 1");

				
				takeTurn(kb, player1);
				System.out.println();
				System.out.println("Player 2");
				takeTurn(kb, player2);
				
				System.out.println();
				System.out.println("Current score: \nPlayer 1: " + player1.currentTotal() + " -- Player 2: " + player2.currentTotal() + "\nThe goal is " + maxScore + " points.");

				
				if (player1.currentTotal() >= maxScore && player2.currentTotal() < maxScore)
				{
					System.out.println("Player 1 wins!");
				}
				else if (player2.currentTotal() >= maxScore && player1.currentTotal() < maxScore)
				{
					System.out.println("Player 2 wins!");
				}
				else if((player2.currentTotal() >= maxScore && player1.currentTotal() >= maxScore))
				{
					System.out.println("The result is a tie.");
				}
				
			}
		
			System.out.println("Would you like to play again? Respond (Y/N) only");
			if (yesResponse(kb))
			{
				play();
			}
			else
			{
				System.out.println("Goodbye");
			}
			
		}
		catch (NumberFormatException exc)
		{
			System.out.println("That was not a number, try again.");
			play();
		}
		
	}

	//
	// Returns the initial max score (loops until a value between 1 <= score <= 100 is entered) 
	//
	private int getInitialMax(Scanner kb)
	{

		int highScore;
        do
        {
        	System.out.println("What score do you want to play up to?");
   
    		String scoreString = kb.nextLine();
    		highScore = Integer.parseInt( scoreString ) ;
        }
        while (( highScore < 1) || ( highScore > 100));
        
        return highScore;
	}
	
	//
	// method for managing a single session of rolling dice
	//
	private void takeTurn(Scanner kb, PigDice pd)
	{
		String response;
		boolean keepRolling = true;

		do
		{
			
			// Roll the dice
			pd.rollDice();

			// Report the result
			System.out.println(pd.lastRoll() + " scored " + pd.evaluate() + " points.");

			// Did the player pig out?
			if (pd.piggedOut())
			{	
				System.out.println("You pigged out this turn.");
				
			}
			else
			{
				//
				// Roll again; see if the user wants to roll again to add to total or pass and keep current points
				//
				System.out.println("Your current roll is " + pd.currentRound() + " points. Keep rolling? Respond (Y/N) only.");
				
				if (!yesResponse(kb))
				{
					keepRolling = false;
					int roundScore = pd.save();
				
					System.out.printf("Your total for the round was %d and your total score is %d.\r\n", roundScore, pd.currentTotal());

				}
			} 

		} while (!pd.piggedOut() && keepRolling);
		
		
		
		
	}
	
	//
	// Returns true if the user enters a 'y' or 'Y'
	//
	final String _YES = "Y";
	public boolean yesResponse(Scanner kb)
	{
		return kb.nextLine().substring(0, 1).toUpperCase().equals(_YES);
	}
}
