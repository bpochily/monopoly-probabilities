package com.benpochily.monopoly.ai.heuristic.probability.probabilitytable;

import java.util.ArrayList;
import java.util.List;

import com.benpochily.monopoly.board.Board;
import com.benpochily.monopoly.game.Dice;

/** Represents a board used to calculate landing probabilities from a single space over the course of a single turn
 * Basic usage:
 * 1. Construct a ProbBoard, passing a regular board to be used as a model.
 * 2. use getProbFrom() to build a recursive tree of all possible moves
 *    and save the probability of each.
 * 3. Use getCalculatedProb() repeatedly, passing each space in turn, to find the chance
 *    of landing on each space.
 * 4. If running simulations from other starting spaces, call resetProb() to clear probabilities
 *    from the last getProbFrom() call.
*/
/*package*/ final class ProbBoard {	
	private final Board model; //the board object this ProbBoard decorates
	
	/*
	 * Contains all the ProbabilitySpace objects which make up this board, one for each physical space
	 * and one for each possible turn in jail. For example, if the max number of turns in jail is three,
	 * the last three ProbSpaces in the array represent the first, second, and third turns in jail.
	 * It is important that we keep track of this state information so our probabilities are accurate
	 * for many turns in the future.
	 */
	private final List<ProbSpace> board = new ArrayList<ProbSpace>(); 
	
	/**
	 * Constructs a new ProbBoard
	 * @param model the board object this ProbBoard should calculate probabilities for
	 */
	public ProbBoard(Board model) {
		this.model = model;
		for (int i = 0; i < model.physicalSize() + model.maxTurnsInJail(); i++)
			board.add(i < model.physicalSize() ? ProbSpace.createSpace(model.getSpace(i), this) : ProbSpace.createSpace(model.jail(), this, i));
	}

	public final ProbSpace getSpace(int id) {
		return board.get(id);
	}
	
	public final Dice dice() {
		return model.dice();
	}
	
	/**
	 * Returns the total number of the spaces on the ProbBoard,
	 * including "virtual" spaces for each turn in jail
	 * @return 
	 */
	public final int size() {
		return board.size();
	}
	
	/**
	 * Returns the total number of actual spaces on the ProbBoard,
	 * not counting virtual jail spaces.
	 * @return
	 */
	public final int physicalSize() {
		return model.physicalSize();
	}
	
	public final int maxTurnsInJail() {
		return model.maxTurnsInJail();
	}
	
	public final ProbSpace jail() {
		return getSpace(model.jail().ID());
	}
	
	/**
	 * Initiates a probability simulation from the specified space.
	 * Once this method returns, the results can be obtained by calling
	 * getProb() on each space for which results are desired.
	 * @param spaceID The unique ID of the starting space
	 * @param stayInJail Whether the probabilities should be calculated assuming
	 * the player pays to leave jail immediately or stays for the full duration
	 */
	public final void getProbFrom(int spaceID, boolean stayInJail) {
		board.get(spaceID).getProbFrom(stayInJail);
	}
	
	/**
	 * Returns the probability of the space being landed on after running
	 * a turn simulation with getProbFrom.
	 * @param spaceID the ID of the 
	 * @param mid If true, returns the probability that this space was landed on,
	 * but not ended on (because of doubles). If false, returns the probability that
	 * the turn ends on this space.
	 * @return the probability
	 */
	public final double getCalculatedProb(int spaceID, boolean mid) {
		return mid ? board.get(spaceID).midProb() : board.get(spaceID).endProb();
	}
	
	/**
	 * Resets all probabilities on the board to zero
	 */
	public final void resetProb() {
		for (ProbSpace s : board)
			s.resetProb();
	}

	/**
	 * Utility method used to determine the space landed on after moving a certain number of spaces
	 * @param current The space to start from
	 * @param numSpaces The number of spaces to move
	 * @return The landed on space
	 */
	public final ProbSpace nextSpace(ProbSpace current, int numSpaces) {
		return getSpace((current.ID() + numSpaces) % physicalSize());
	}
}
