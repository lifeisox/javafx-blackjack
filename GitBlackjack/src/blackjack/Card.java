package blackjack;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Card {
	enum Suits {
        HEARTS, DIAMONDS, CLUBS, SPADES
    };
	private ImageView front;
	private ImageView back;
	private int number;
	private Suits suit;
	
	final static int NUMOFEACHSUIT = 13;
	final static int NUMOFCARDS = 52;

	public Card() {
		this(Suits.SPADES, 1);
	}
	
	public Card(Suits suit, int number) {
		this.suit = suit;
		this.number = number;
		front = new ImageView(new Image(getClass().getResource("\\images\\" + suit.name().toLowerCase() + number +".png").toString(), 108, 0, true, true));
		back = new ImageView(new Image(getClass().getResource("\\images\\back.png").toString(), 108, 0, true, true));
	}
	
	public Card(Card card) {
		this(card.getSuit(), card.getNumber());
	}
	
	public Suits getSuit() { return suit; }
	public int getNumber() { return number; }
	public int getValue() {
		if (number == 1) return 11;
		if (number > 10) return 10;
		return number;
	}
	public ImageView getFront(double x, double y) {
		front.setX(x);
		front.setY(y);
		front.setTranslateX(0.0);
		front.setTranslateY(0.0);
		return front;
	}
	
	public ImageView getBack(double x, double y) {
		back.setX(x);
		back.setY(y);
		back.setTranslateX(0.0);
		back.setTranslateY(0.0);
		return back;
	}
}
