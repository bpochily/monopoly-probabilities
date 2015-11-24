package com.benpochily.monopoly.ai.heuristic.probability.probabilitytable;

import org.ejml.simple.SimpleMatrix;

import com.benpochily.monopoly.board.Board;

/**
 * Does the work of building and solving the Markov matrix of probabilities
 */
/* package */ final class ProbabilityCalculator {
	private ProbBoard board;
	private double[][] markovMatrix;
	private double[][] midMatrix;
	private double[][] markovMatrixJail;
	private double[][] midMatrixJail;
	private double[] steadyState;
	private double[] steadyStateJail;

	/**
	 * Constructs a new ProbabilityCalculator and immediately calculates the
	 * exact probabilities for the specified board
	 * 
	 * @param board
	 *            The board for which probabilities should be calculated
	 */
	public ProbabilityCalculator(Board board) {
		this.board = new ProbBoard(board);

		/*
		 * It's okay to do all this calculation in the constructor so the
		 * containing ProbabilityTable can initialize its immutable state. Lazy
		 * calculation is tempting but we'd rather have immutability.
		 */
		buildTransitionTable(false);
		buildTransitionTable(true);
		solveSteadyState(false);
		solveSteadyState(true);
	}
	
	/**
	 * Returns a comprehensive table of probabilities for an arbitrary number of
	 * turns in the future
	 * 
	 * @param depth
	 *            How many turns in the future the table should be calculated for
	 * @return a three-dimensional array of doubles, indexed as follows: To find
	 *         the probability that a player starting on the space with ID A
	 *         will land on space with ID B x turns from now, use [x][A][B]
	 */
	public final double[][][] getTable(int depth, boolean stayInJail) {
		double[][][] result = new double[depth][board.size()][board.size()];
		SimpleMatrix markovMatrix = new SimpleMatrix(stayInJail ? markovMatrixJail : this.markovMatrix);
		SimpleMatrix midMatrix = new SimpleMatrix(stayInJail ? midMatrixJail : this.midMatrix);
		SimpleMatrix intermediateMarkov = markovMatrix.copy();
		SimpleMatrix intermediateMid = midMatrix.copy();

		for (int turn = 0; turn < depth; turn++) {
			/*
			 * For each turn: 
			 * 1. copy the Markov matrix into final storage in
			 * the appropriate place, adding middle probabilities 
			 * 2. Set the mid probabilities to be added next turn, by multiplying the current
			 * Markov matrix by midMatrix 
			 * 3. calculate the next turn's Markov matrix by multiplying this turn's by our 
			 * original, one-turn matrix
			 */
			for (int i = 0; i < board.size(); i++) {
				for (int j = 0; j < board.size(); j++) {
					result[turn][i][j] = intermediateMarkov.get(i, j) + intermediateMid.get(i, j);
				}
			}
			intermediateMid = intermediateMarkov.mult(midMatrix);
			intermediateMarkov = intermediateMarkov.mult(markovMatrix);
		}
		return result;
	}

	/**
	 * returns an array of doubles representing the steady state probability
	 * of landing on each space
	 * @param jail true to get the steady state with long jail stay, false otherwise
	 * @return the array
	 */
	public final double[] getSteadyState(boolean jail) {
		return jail ? steadyStateJail : steadyState;
	}

	// runs probability simulations from each space on the board to produce
	// Markov matrix
	private void buildTransitionTable(boolean stayInJail) {
		double[][] tableMid = new double[this.board.size()][this.board.size()];
		double[][] tableEnd = new double[this.board.size()][this.board.size()];
		for (int i = 0; i < board.size(); i++) {
			board.getProbFrom(i, stayInJail);
			for (int j = 0; j < board.size(); j++) {
				tableMid[i][j] = board.getCalculatedProb(j, true);
				tableEnd[i][j] = board.getCalculatedProb(j, false);
			}
			board.resetProb();
		}

		if (stayInJail) {
			markovMatrixJail = tableEnd;
			midMatrixJail = tableMid;
		} else {
			markovMatrix = tableEnd;
			midMatrix = tableMid;
		}
	}

	// solves the Markov matrix for steady state probabilities
	private void solveSteadyState(boolean stayInJail) {
		// build main part of matrix by translating Markov matrix and adjusting coefficients
		SimpleMatrix toSolve = new SimpleMatrix(board.size() + 1, board.size());
		for (int i = 0; i < board.size(); i++)
			for (int j = 0; j < board.size(); j++)
				toSolve.set(i, j, i == j ? stayInJail ? markovMatrixJail[j][i] - 1 : markovMatrix[j][i] - 1 : stayInJail ? markovMatrixJail[j][i] : markovMatrix[j][i]);

		// add final equation, a + b + c + d... = 1
		for (int i = 0; i < board.size(); i++)
			toSolve.set(board.size(), i, 1);

		// right side of linear system
		SimpleMatrix rightMatrix = new SimpleMatrix(board.size() + 1, 1);
		rightMatrix.set(board.size(), 0, 1);

		SimpleMatrix result = toSolve.solve(rightMatrix);

		/*
		 * add the mid probabilities back in: For each calculated steady state
		 * probability, we add every mid probability starting from that space,
		 * times the steady state probability (the steady state chance of
		 * starting a turn from that space)
		 */
		SimpleMatrix added = result.copy();
		for (int i = 0; i < board.size(); i++)
			for (int j = 0; j < board.size(); j++)
				added.set(j, 0, added.get(j, 0) + result.get(i, 0) * (stayInJail ? midMatrixJail[i][j] : midMatrix[i][j]));

		// copy the results back into an array for storage
		double[] temp = new double[board.size()];
		for (int i = 0; i < board.size(); i++)
			temp[i] = added.get(i, 0);

		if (stayInJail)
			steadyStateJail = temp;
		else
			steadyState = temp;
	}
}
