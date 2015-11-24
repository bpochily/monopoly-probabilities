package com.benpochily.monopoly.ai.heuristic.probability.probabilitytable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import com.benpochily.monopoly.board.space.CardSpace;
import com.benpochily.monopoly.board.space.GoToSpace;
import com.benpochily.monopoly.board.space.Jail;
import com.benpochily.monopoly.board.space.Space;
import com.benpochily.monopoly.board.space.SpaceType;
import com.benpochily.monopoly.game.Dice;

/**
 * A representation of a board space used to recursively build a move tree.
 * Used as a component of a ProbBoard
 * @see ProbBoard
 */
/*package*/ class ProbSpace {
	private Space space;
	private ProbBoard board;
	private double midProb;
	private double endProb;
	
	//mappings for static factory
	private static final Map<SpaceType, BiFunction<Space, ProbBoard, ProbSpace>> factoryMap = new HashMap<SpaceType, BiFunction<Space, ProbBoard, ProbSpace>>() {{
		put(SpaceType.CARD, (space, board) -> { return new CardProbSpace((CardSpace)space, board);});
		put(SpaceType.GOTO, (space, board) -> { return new GoToJailProbSpace((GoToSpace)space, board);});
	}};
	
	public static final ProbSpace createSpace(Space space, ProbBoard board) {
		return factoryMap.getOrDefault(space.type(), (s, b) -> { return new ProbSpace(s, b);}).apply(space, board);
	}
	
	public static final ProbSpace createSpace(Jail space, ProbBoard board, int id) {
		return id == board.physicalSize() + board.maxTurnsInJail() - 1 ? new LastTurnJailProbSpace(space, board, id) : new JailProbSpace(space, board, id);
	}
	
	//do not use, use static factory method createSpace
	protected ProbSpace(Space space, ProbBoard board) {
		this.space = space;
		this.board = board;
		endProb = 0.0;
		midProb = 0.0;
	}
	
	/**
	 * Begins a recursive search of all possible move outcomes, starting from
	 * this space. When the search has completed, all ProbSpaces in the associated
	 * ProbabilityBoard will be populated with their probability of being landed on.
	 * @param stayInJail Whether the probabilities should be calculated assuming a maximum-length
	 * or minimum-length jail stay.
	 */
	public void getProbFrom(boolean stayInJail) {	
		doRolls(0, 1);
	}
	
	public int ID() {
		return space.ID();
	}
	
	/**
	 * Returns the probability that this space will be landed on but not ended on
	 * (likely because of doubles). This method should be used after a call to
	 * ProbSpace.getProbabilitiesFrom() has built a move tree and summed the probabilities.
	 * 
	 * We need to store endProb and midProb separately because we will need them both:
	 * midProb + endProb is the overall probability that a space will be landed on,
	 * useful for single turn profit calculation.
	 * endProb alone allows us to create a transition matrix which we can use to 
	 * find the probability for an arbitrary number of turns in the future, not just a single turn.
	 * @return the probability this space will be landed on but not ended on
	 */	
	public final double midProb() {
		return midProb;
	}
	
	/**
	 * @see midProb()
	 * @return the probability that this will be a player's final destination in their turn
	 */
	public final double endProb() {
		return endProb;
	}
	
	/**
	 * Resets probabilities to zero, in preparation for building a move tree
	 */
	public final void resetProb() {
		endProb = 0.0;
		midProb = 0.0;
	}
	
	protected final ProbBoard board() {
		return board;
	}
	
	/**
	 * Adds the specified probability to this space's total end probability.
	 * @param prob the probability to add
	 */
	protected final void addToEndProb(double prob) {
		endProb += prob;
	}
	
	/**
	 * Adds the specified probability to this space's total mid probability.
	 * @param prob the probability to add
	 */
	protected final void addToMidProb(double prob) {
		midProb += prob;
	}
	
	/**
	 * Simulates a landing on this square, updating probabilities and rolling again if necessary
	 * @param numDoubles the number of doubles rolled so far
	 * @param multiplier the probability multiplier that should 
	 * be applied to all probabilities from here on (that is,
	 * the probability this method is called)
	 * @param rollAgain whether we should roll again (because of doubles)
	 */
	protected void updateProbAndRoll(int numDoubles, double multiplier, boolean rollAgain) {
		if (!rollAgain) { //if we don't need to roll again, this is the base case and we're done here
			addToEndProb(multiplier);
			return;
		}		
		addToMidProb(multiplier);
		doRolls(numDoubles, multiplier);
	}	
	
	private void doRolls(int numDoubles, double multiplier) {
		/*
		 * This is where the meat of the recursion takes place. For each possible roll of the dice,
		 * we check for doubles-induced jail and create a branch of the recursive tree for each possible move,
		 * passing along state information and dividing the probability multiplier to reflect the chance that
		 * the branch is actually reached.
		 */
		board().dice().forEachPossibleRoll((roll, size) -> {
			boolean isDoubles = roll.isDoubles();
		    if (isDoubles && numDoubles >= Dice.maxDoubles() - 1) //too many doubles, jail time
				board.jail().updateProbAndRoll(numDoubles, multiplier / size, false);
		    else //otherwise, move a number of spaces to the next space and repeat
		    	board.nextSpace(this, roll.getTotal()).updateProbAndRoll(isDoubles ? numDoubles + 1: numDoubles, multiplier / size, isDoubles);
		});
	}
}
