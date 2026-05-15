/*
 * @Destiny Abuwa
 * <p> PigDice
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

// this class manages the state of the dice and the scoring
package pigDiceGame;

public class PigDice
{
	// keep track of total and round scores as well as the two dice.
    // Replace your old score variables with just these two clear metrics:
    private int _totalScore = 0;   // The permanent banked score
    private int _roundScore = 0;   // The running score for the active turn
	private Die _die1;
	private Die _die2;
	
	
	public PigDice()
	{
		_die1 = new Die();
		_die2 = new Die();
	}

	// accessor for total score
	public int currentTotal()
	{
		return _totalScore;
	}

	// accessor for this round score
    public int currentRound() {
        return _roundScore; // Just returns the value safely without altering it
    }

	// accessor to see if the user has rolled a single "1" and loses turn
	public boolean piggedOut()
	{
		
		if (singleOneRolled())
			{
				return true;
			}
		else
			return false;
	}

	// mutator that simulates rolling two dice and evaluating the resulting score
	public void rollDice()
	{
		// Roll the die
		_die1.roll();
		_die2.roll();
	}

	// accessor for a formatted string of what the last roll looked like
	public String lastRoll()
	{
		return "D1 (" + _die1.faceValue() + "), D2 (" + _die2.faceValue() + ")";
	}

    public int evaluate() {
        if (piggedOut()) {
            _roundScore = 0;
        } else if (doubleOnesRolled()) {
            _roundScore += 25; // Accumulate double 1s to current turn
        } else {
            _roundScore += (_die1.faceValue() + _die2.faceValue()); // Accumulate standard rolls
        }
        return _roundScore;
    }

	private boolean singleOneRolled()
	{
		if ((_die1.faceValue()==1) && ((_die2.faceValue()!=1)) || (_die1.faceValue()!=1 && _die2.faceValue()==1))
		{
			return true;
		}
		else
			return false;
	}

	private boolean doubleOnesRolled()
	{
		if ((_die1.faceValue()==1) && (_die2.faceValue()==1))
		{
			return true;
		}
		else
			return false;
	}

	//
	// mutator to end a round and keep the add this round to the total
	// also returns the total value of the round and resets the round total for next time
	//
    public int save() {
        int pointsSaved = _roundScore;
        _totalScore += _roundScore; // Bank the round points permanently
        _roundScore = 0;            // Reset the turn counter back to zero
        return pointsSaved;
    }
}
