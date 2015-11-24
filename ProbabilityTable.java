package com.benpochily.monopoly.ai.heuristic.probability.probabilitytable;

import com.benpochily.monopoly.board.Board;

/**
 * Stores an immutable table of cached probability information for a particular board
 * This is the main access point to all probability calculation.
 */
public final class ProbabilityTable {	
	/*three dimensional array which contains all stored probabilities
	To reference the probability of ending on y in z turns, starting from x,
	use probabilities[z][x][y].*/
	private final double[][][] probabilities;
	private final double[] steadyState;
	private final double[][][] probabilitiesJail;
	private final double[] steadyStateJail;	
	
	/**
	 * Constructs and returns a complete table of probabilities for the given board and depth.
	 * @param board The board for which probabilities should be calculated
	 * @param depth How many turns in the future exact data should be calculated for
	 * @return an immutable, thread-safe table of probabilities 
	 */
	public static final ProbabilityTable makeTable(Board board, int depth) {
	    ProbabilityCalculator calc = new ProbabilityCalculator(board);
		ProbabilityTable table = new ProbabilityTable(calc.getTable(depth, false), calc.getSteadyState(false), calc.getTable(depth, true), calc.getSteadyState(true));
		return table;
	}
	
	private ProbabilityTable(double[][][] probabilities, double[] steadyState, double[][][] probabilitiesJail, double[] steadyStateJail) {
		this.probabilities = probabilities;
		this.steadyState = steadyState;
		this.probabilitiesJail = probabilitiesJail;
		this.steadyStateJail = steadyStateJail;
	}
	
	/**
	 * returns the probability that a player starting from origin will land on destination
	 * turn turns in the future, given their jail strategy.
	 * @param origin the id of the starting space
	 * @param destination the id of the target space
	 * @param turn the particular turn to check
	 * @param stayInJail true if the player will stay in jail for the full duration;
	 * false if they will pay to leave at the first opportunity
	 * @return the probability
	 */
	public final double getProbability(int origin, int destination, int turn, boolean stayInJail) {
		return stayInJail ? probabilitiesJail[turn][origin][destination] : probabilities[turn][origin][destination];
	}
	
	/**
	 * returns the steady-state probability of a particular space (that is, the overall probability
	 * over an infinite number of games), given a particular jail strategy
	 * @param space the target space
	 * @param stayInJail true if the player will stay in jail for the full duration;
	 * false if they will pay to leave at the first opportunity
	 * @return the probability
	 */
	public final double getSteadyState(int space, boolean stayInJail) {
		return stayInJail ? steadyStateJail[space] : steadyState[space];
	}
}
