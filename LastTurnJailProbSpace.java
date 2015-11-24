package com.benpochily.monopoly.ai.heuristic.probability.probabilitytable;

import com.benpochily.monopoly.board.space.Jail;

/**
 * A ProbSpace representing the state of being in jail, but required to leave this turn
 */
/*package*/ final class LastTurnJailProbSpace extends JailProbSpace {

	//do not use, use static factory in ProbSpace
	LastTurnJailProbSpace(Jail jail, ProbBoard board, int id) {
		super(jail, board, id);
	}
	
	/**
	 * override parent method to handle leaving jail
	 */
	@Override
	public void getProbabilitiesStayInJail() {
		board().dice().forEachPossibleRoll((roll, size) -> {
				board().nextSpace(board().getSpace(justVisitingID()), roll.getTotal()).updateProbAndRoll(roll.isDoubles() ? 1 : 0, (double) 1 / size, false);
		});
	}
}
