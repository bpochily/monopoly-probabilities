package com.benpochily.monopoly.ai.heuristic.probability.probabilitytable;

import com.benpochily.monopoly.board.space.Jail;

/**
 * A ProbSpace that represents being in jail.
 * There will be multiple JailProbSpaces on the board,
 * each representing one turn spent in jail.
 */
/*package*/ class JailProbSpace extends ProbSpace {
	private final int justVisitingID;
	private final int id;

	//do not use, use static factory method in ProbSpace
	JailProbSpace(Jail space, ProbBoard board, int id) {
		super(space, board);
		this.id = id;
		justVisitingID = space.justVisiting().ID();
	}

	@Override
	public int ID() {
		return id;
	}
	
	public int justVisitingID() {
		return justVisitingID;
	}
	
	/**
	 * Override parent method to handle jail behavior
	 */
	@Override
	public void getProbFrom(boolean stayInJail) {
		if (stayInJail)
			getProbabilitiesStayInJail();
		else
			board().getSpace(justVisitingID).getProbFrom(stayInJail);
	}
	
	/**
	 * Updates the probabilities of each ending square, assuming
	 * the player wishes to stay in jail (does not pay or play
	 * Get Out of Jail Free card)
	 */
	protected void getProbabilitiesStayInJail() {		
		board().dice().forEachPossibleRoll((roll, size) -> {			
			//if doubles, we're forced to leave jail (but our doubles are accounted for, we don't roll again)
			if (roll.isDoubles())
				board().nextSpace(board().getSpace(justVisitingID), roll.getTotal()).updateProbAndRoll(1, (double) 1 / size, false);
			else
				board().getSpace(this.ID() + 1).updateProbAndRoll(0, (double)1 / size, false);
		});
	}
	
	@Override
	protected void updateProbAndRoll(int numDoubles, double multiplier, boolean rollAgain) {
		//Turn ends upon going to jail, so it doesn't matter if we had doubles
		addToEndProb(multiplier);
	}
}
