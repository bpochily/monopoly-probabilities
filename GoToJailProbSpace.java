package com.benpochily.monopoly.ai.heuristic.probability.probabilitytable;

import com.benpochily.monopoly.board.space.GoToSpace;

/**
 * A ProbSpace which represents the Go To Jail space on the Monopoly board
 */
/*package*/ final class GoToJailProbSpace extends ProbSpace {
	
	//do not use, use static factory method in ProbSpace
	GoToJailProbSpace(GoToSpace space, ProbBoard board) {
		super(space, board);
	}

	/**
	 * It's impossible to end a turn on this space because the player will
	 * be immediately sent to jail. So we will never need to get probabilities
	 * from here.
	 */
	@Override
	public final void getProbFrom(boolean stayInJail) {
		return;
	}
	
	/**
	 * Override parent method - 
	 * If we land here, go to jail and end turn
	 */
	@Override
	public final void updateProbAndRoll(int numDoubles, double multiplier, boolean rollAgain) {
		addToMidProb(multiplier);
		board().jail().updateProbAndRoll(numDoubles, multiplier, false);
	}
}
