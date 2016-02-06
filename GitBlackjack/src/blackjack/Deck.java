package blackjack;

import java.util.ArrayList;
import java.util.Random;

import javafx.scene.layout.Pane;

public class Deck {
	private final int CARDWIDTH = 108;
	private ArrayList<Card> cards;
	private int numOfDeck;
	public static int currentIndex;

	public Deck() {
		this(1);
	}

	public Deck(int numOfDeck) {
		this.numOfDeck = numOfDeck;
		cards = new ArrayList<Card>();
		
		for (int i = 0; i < numOfDeck; i++) {
			for (Card.Suits suit : Card.Suits.values()) {
				for (int j = 1; j <= Card.NUMOFEACHSUIT; j++) {
					cards.add(new Card(suit, j));
				}
			}
		}
		currentIndex = 0;
	}

	public void shuffle(Pane deckTable) {
		int max = numOfDeck * Card.NUMOFCARDS * 3;
		int index1, index2;
		Card temp;
		Random rnd = new Random();
		
		for (int i=0; i < max; i++) {
			index1 = rnd.nextInt(numOfDeck * Card.NUMOFCARDS - 1);
			index2 = rnd.nextInt(numOfDeck * Card.NUMOFCARDS - 1);
			temp = cards.get(index1);
			cards.set(index1, cards.get(index2));
			cards.set(index2, temp);
		}
		
		deckTable.getChildren().clear();
		double x1 = deckTable.getPrefWidth() - CARDWIDTH;
		int count = numOfDeck * Card.NUMOFCARDS;
		double gap = x1 / (double) count;
		for (int i = count - 1; i >= 0; i--) {
			deckTable.getChildren().add(cards.get(i).getBack(x1, 0));
			x1 -= gap;
			if (x1 < 0.0)
				x1 = 0.0;
		}
		
		currentIndex = 0;
	}
	
	public Card dealCard(Pane deckTable) {
		if (currentIndex == numOfDeck * Card.NUMOFCARDS) {
			shuffle(deckTable);
		}
		deckTable.getChildren().remove(deckTable.getChildren().size() - 1);
		return cards.get(currentIndex++);
	}
	
}
