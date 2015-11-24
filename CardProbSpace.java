package com.benpochily.monopoly.ai.heuristic.probability.probabilitytable;

import com.benpochily.monopoly.board.deck.Card;
import com.benpochily.monopoly.board.deck.Deck;
import com.benpochily.monopoly.board.space.CardSpace;
import com.benpochily.monopoly.board.space.Space;

/**
 * A ProbSpace which represents a Chance or Community Chest space
 */
/*package*/ final class CardProbSpace extends ProbSpace {
	private final Deck deck;
	private final CardSpace space;
	
	//do not use, use static factory in ProbSpace
	CardProbSpace(CardSpace space, ProbBoard board) {
		super(space, board);
		this.space = space;
		this.deck = space.getDeck();
	}	
	
	/**
	 * overrides updateProbAndRoll in ProbSpace to simulate drawing a card,
	 * which may send us elsewhere on the board and influence probabilities
	 */
	@Override
	protected final void updateProbAndRoll(int numDoubles, double multiplier, boolean rollAgain) {		
		/*this loop cycles through each card in the deck
		 * and applies its action, which may involve moving us to
		 * another space.
		 */
		for (Card c : deck) {
			Space destination = c.applyCard(space);			
			/* If we haven't moved, we can let our parent handle it from here.
			 * If we have moved, we continue from our new location. */
			if (destination.equals(space)) 
				super.updateProbAndRoll(numDoubles, multiplier / deck.size(), rollAgain);
			else {
				addToMidProb(multiplier / deck.size());
				board().getSpace(destination.ID()).updateProbAndRoll(numDoubles, multiplier / deck.size(), rollAgain);
			}
		}
	}
}
