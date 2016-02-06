package blackjack;

import java.io.IOException;
import java.util.ArrayList;

import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class BlackJack extends Application {
	private final int MAXBET = 100;
	private final int DECKTABLEWIDTH = 370;
	private final int TBLWIDTH = 580;
	private final int CARD_GAP_X = 20;
	private final int CARD_GAP_Y = 10;
	public static int numOfDeck = 1;
	private Deck deck;
	Pane deckTable, playTable;
	private double dragAnchorX, dragAnchorY;
	Button btnDouble, btnStand, btnHit, btnSplit, btnInsurance, btnSurrender, btnPlay;
	ImageView chip1IV, chip5IV, chip10IV, chip25IV, chip50IV, chip100IV;
	private final StringProperty cash = new SimpleStringProperty("10000");
	private final StringProperty betting = new SimpleStringProperty("0");
	private SequentialTransition seqTrans;
	ArrayList<Card> dealerCards;
	ArrayList<Card>[] playerCards;
	private boolean[] checkDone = { false, false, false };
	double insuranceMoney;
	
	private boolean evenMoneySw = false;
	private boolean insuranceSw = false;
	private boolean playingSw = false;
	private int splitCount = 0;
	private int currentHand = 0;
	
	private void animatedDeal(ImageView one, double x, double y) {
		playTable.getChildren().add(one);
		TranslateTransition tt = new TranslateTransition(Duration.millis(200), one); 
		tt.setToX(x - one.getX());
		tt.setToY(y);
		seqTrans.getChildren().add(tt);
	}
	
	private void dealOnePlayerCard() {
		int cardNumber = 0;
		double basicX, basicY = 220;
		int result;
		
		if (splitCount == 0) 
			basicX = 190.0;
		else if (splitCount == 1) 
			basicX = 100.0 + 200.0 * currentHand;
		else 
			basicX = 190.0 * currentHand;
		
		playerCards[currentHand].add(deck.dealCard(deckTable));
		cardNumber = playerCards[currentHand].size()-1;
		basicX = basicX + cardNumber * CARD_GAP_X;
		basicY = basicY + cardNumber * CARD_GAP_Y;
		ImageView one = playerCards[currentHand].
				get(playerCards[currentHand].size()-1).getFront(TBLWIDTH, 0.0);
		playTable.getChildren().add(one);
		TranslateTransition tt = new TranslateTransition(Duration.millis(200), one); 
		tt.setToX(basicX - one.getX());
		tt.setToY(basicY);
		tt.play();
		
		result = valueOfCards(playerCards[currentHand]);
		if (result == 0) 
			youLost();
	}
	
	private void dealDealerCard() {
		int result;
		
		playTable.getChildren().remove(3);
		playTable.getChildren().add(3, dealerCards.get(1).getFront(190 + CARD_GAP_X, 0 + CARD_GAP_Y));
		result = valueOfCards(dealerCards);
		if (result != 999) { // is not blackjack?
			if (result < 17) {
				seqTrans.getChildren().clear();
				int cardNumber = dealerCards.size() - 1;
				while (result < 17 && result != 0) {
					dealerCards.add(deck.dealCard(deckTable));
					cardNumber++;
					animatedDeal(dealerCards.get(dealerCards.size()-1).getFront(TBLWIDTH, 0.0), 
							190 + cardNumber * CARD_GAP_X, cardNumber * CARD_GAP_Y);
					result = valueOfCards(dealerCards);
				}
				seqTrans.play();
			}
		}
		lastJudge(result);
	}
	
	private void playGame() {
		setChipButtonDisable(true);
		btnPlay.setDisable(true);
		playingSw = true;
		evenMoneySw = false;
		insuranceSw = false;
		btnInsurance.setText("Insurance");
		insuranceMoney = 0.0;
		splitCount = 0;	currentHand = 0;
		double dealerX = 190.0;
		double dealerY = 0.0;
		double playerX = 190.0;
		double playerY = 220.0;
		
		dealerCards.clear();
		for (int i = 0; i < 3; i++) {
			playerCards[i].clear();
			checkDone[i] = false;
		}
		playTable.getChildren().clear();
		
		playerCards[currentHand].add(deck.dealCard(deckTable));
		dealerCards.add(deck.dealCard(deckTable));
		playerCards[currentHand].add(deck.dealCard(deckTable));
		dealerCards.add(deck.dealCard(deckTable));
		
		seqTrans.getChildren().clear();
		animatedDeal(playerCards[currentHand].get(0).getFront(TBLWIDTH, 0.0), playerX, playerY);
		playerX += CARD_GAP_X;	playerY += CARD_GAP_Y;
		animatedDeal(dealerCards.get(0).getFront(TBLWIDTH, 0.0), dealerX, dealerY);
		dealerX += CARD_GAP_X;	dealerY += CARD_GAP_Y;
		animatedDeal(playerCards[currentHand].get(1).getFront(TBLWIDTH, 0.0), playerX, playerY);
		playerX += CARD_GAP_X;	playerY += CARD_GAP_Y;
		animatedDeal(dealerCards.get(1).getBack(TBLWIDTH, 0.0), dealerX, dealerY);
		dealerX += CARD_GAP_X;	dealerY += CARD_GAP_Y;
		seqTrans.play();
		
		// if player card is not Blackjack
		btnStand.setDisable(false);
		if (valueOfCards(playerCards[currentHand]) != 999) { // not black jack
			btnHit.setDisable(false);
			btnDouble.setDisable(false);
			btnSurrender.setDisable(false);
		}
		
		if (dealerCards.get(0).getValue() == 11) { // if a shown card of dealer is Ace
			btnInsurance.setDisable(false);
			if (valueOfCards(playerCards[currentHand]) == 999) { // is black jack
				evenMoneySw = true;
				btnInsurance.setText("Even Money");
			}
			btnSurrender.setDisable(true);
		}
		
		if (playerCards[currentHand].get(0).getNumber() == playerCards[currentHand].get(1).getNumber()) {
			btnSplit.setDisable(false);
		}
	}
	
	@Override
	public void start(Stage stage) throws IOException {

		dealerCards = new ArrayList<Card>();
		playerCards = new ArrayList[3];
		for (int i = 0; i < 3; i++) {
			playerCards[i] = new ArrayList<Card>();
		}
		seqTrans = new SequentialTransition();
		
		// Top Area
		final Label cashLabel = new Label("MONEY  ");
		final Label cashText = new Label();
        cashText.textProperty().bind(cash);
        final Label batLabel = new Label("BET  ");
        final Label batText = new Label();
        batText.textProperty().bind(betting);
        Label introLabel = new Label();
        
        cashLabel.getStyleClass().add("display-label");
		cashText.getStyleClass().add("display-text");
		batLabel.getStyleClass().add("display-label");
		batText.getStyleClass().add("display-text");
		introLabel.getStyleClass().add("display-label");
		
        HBox topLeft = new HBox(cashLabel, cashText);
        topLeft.setAlignment(Pos.CENTER_LEFT);
        
        HBox topCenter = new HBox(batLabel, batText);
        topCenter.setAlignment(Pos.CENTER_LEFT);
        
        HBox topCenter2 = new HBox(introLabel);
        topCenter2.setAlignment(Pos.CENTER_LEFT);
        
        btnPlay = new Button("Play");
        btnPlay.setId("bevel-grey-nopadding");
        btnPlay.setOnAction(new EventHandler<ActionEvent>() {
        	public void handle(ActionEvent ae) {
        		playGame();
        	}
        });
        HBox topRight = new HBox(btnPlay);
        topRight.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(topRight, Priority.ALWAYS);
        
		HBox topHbox = new HBox(topLeft, topCenter, topCenter2, topRight);
		topHbox.setPrefSize(1000, 50);
		topHbox.setPadding(new Insets(10, 30, 10, 30));
		topHbox.setSpacing(50);
		topHbox.setStyle("-fx-background-color: rgba(20, 0, 0, 0.7);");
		
		// Middle Area
		playTable = new Pane();
		playTable.setPrefSize(580, 440);
		
		VBox leftVbox = new VBox(playTable);
		leftVbox.setPadding(new Insets(10, 0, 0, 20));
		
		deckTable = new Pane();
		deckTable.setPrefSize(DECKTABLEWIDTH, 215);
		VBox noteVbox = new VBox();
		VBox.setVgrow(noteVbox, Priority.ALWAYS);
		VBox rightVbox = new VBox(deckTable, noteVbox);
		rightVbox.setPadding(new Insets(10, 20, 10, 10));
		HBox.setHgrow(rightVbox, Priority.ALWAYS);
		
		HBox middleHbox = new HBox(leftVbox, rightVbox);
		
		// Bottom Area
		// Button Group
		btnDouble = new Button("Double");
		btnStand = new Button("Stand");
		btnHit = new Button("Hit");
		btnSplit = new Button("Split");
		btnInsurance = new Button("Insurance");
		btnSurrender = new Button("Surrender");
		btnDouble.setId("bevel-grey");
		btnStand.setId("bevel-grey");
		btnHit.setId("bevel-grey");
		btnSplit.setId("bevel-grey");
		btnInsurance.setId("bevel-grey");
		btnSurrender.setId("bevel-grey");
		HBox buttonArea = new HBox(btnSurrender, btnDouble, btnStand, btnHit, btnSplit, btnInsurance);
		buttonArea.setPrefHeight(80);
		buttonArea.setSpacing(10);
		buttonArea.setAlignment(Pos.CENTER);
		
		// Chip Image Group
		Image img1Chip = new Image(getClass().getResource("\\images\\chip1.png").toString());
		chip1IV = new ImageView(img1Chip);
		chip1IV.setFitWidth(50);
		chip1IV.setPreserveRatio(true);
		chip1IV.setId("chip");
		Image img5Chip = new Image(getClass().getResource("\\images\\chip5.png").toString());
		chip5IV = new ImageView(img5Chip);
		chip5IV.setFitWidth(50);
		chip5IV.setPreserveRatio(true);
		chip5IV.setId("chip");
		Image img10Chip = new Image(getClass().getResource("\\images\\chip10.png").toString());
		chip10IV = new ImageView(img10Chip);
		chip10IV.setFitWidth(50);
		chip10IV.setPreserveRatio(true);
		chip10IV.setId("chip");
		Image img25Chip = new Image(getClass().getResource("\\images\\chip25.png").toString());
		chip25IV = new ImageView(img25Chip);
		chip25IV.setFitWidth(50);
		chip25IV.setPreserveRatio(true);
		chip25IV.setId("chip");
		Image img50Chip = new Image(getClass().getResource("\\images\\chip50.png").toString());
		chip50IV = new ImageView(img50Chip);
		chip50IV.setFitWidth(50);
		chip50IV.setPreserveRatio(true);
		chip50IV.setId("chip");
		Image img100Chip = new Image(getClass().getResource("\\images\\chip100.png").toString());
		chip100IV = new ImageView(img100Chip);
		chip100IV.setFitWidth(50);
		chip100IV.setPreserveRatio(true);
		chip100IV.setId("chip");
		HBox chipArea = new HBox(chip1IV, chip5IV, chip10IV, chip25IV, chip50IV, chip100IV);
		chipArea.setSpacing(10);
		chipArea.setAlignment(Pos.CENTER);
		
		// Close Button Area
		Button btnExit = new Button("Close");
		btnExit.setOnAction(e -> { Platform.exit(); });
		btnExit.setId("bevel-grey");
		HBox exitArea = new HBox(btnExit);
		exitArea.setAlignment(Pos.CENTER_RIGHT);
		
		HBox bottomHbox = new HBox(buttonArea, chipArea, exitArea);
		bottomHbox.setPadding(new Insets(10, 30, 10, 30));
		bottomHbox.setSpacing(30);
		HBox.setHgrow(exitArea, Priority.ALWAYS);
		
		VBox lastVbox = new VBox(topHbox, middleHbox, bottomHbox);
		VBox.setVgrow(lastVbox, Priority.ALWAYS);
		lastVbox.setAlignment(Pos.CENTER_LEFT);
		
		Image image = new Image(getClass().getResource("\\images\\table.jpg").toString());
		ImageView tableImage = new ImageView(image);
		Group background = new Group(tableImage, lastVbox);
		
		Scene scene = new Scene(background);
		scene.getStylesheets().add(getClass().getResource("\\blackjack.css").toString());
		
		// Move Screen by Mouse dragging
		topHbox.setOnMousePressed(e -> {
			dragAnchorX = e.getScreenX() - stage.getX();
			dragAnchorY = e.getScreenY() - stage.getY();
		});
		topHbox.setOnMouseDragged(e -> {
			stage.setX(e.getScreenX() - dragAnchorX);
			stage.setY(e.getScreenY() - dragAnchorY);
		});
		
		stage.setTitle("BlackJack");
		stage.setScene(scene);
		stage.initStyle(StageStyle.TRANSPARENT);
		stage.show();
		stage.setResizable(false);
		
		Parent root = FXMLLoader.load(getClass().getResource("InputDialog.fxml"));
        Stage diagStage = new Stage();
        diagStage.initModality(Modality.APPLICATION_MODAL);
        diagStage.setTitle("User Input");
        diagStage.setScene(new Scene(root));  
        diagStage.show();
        // when dialog closed
        diagStage.setOnHiding(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                    	introLabel.setText(numOfDeck + " Deck Play, Minimun $1, Maximum $100");
                        deck = new Deck(numOfDeck);
                        deck.shuffle(deckTable);
                        setPlayerButtonDisable(true);
                    }
                });
            }
        }); 		

        EventHandler<MouseEvent> handler = new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
            	if (playingSw = true) {
            		playTable.getChildren().clear();
            		setPlayerButtonDisable(true);
            		playingSw = false;
            	}
            	if (btnPlay.isDisabled())
            		btnPlay.setDisable(false);
            	if (me.getSource().equals(chip1IV))
            		betting.set(String.valueOf(Integer.parseInt(betting.get())+1));
            	else if (me.getSource().equals(chip5IV)) 
            		betting.set(String.valueOf(Integer.parseInt(betting.get())+5));
            	else if (me.getSource().equals(chip10IV)) 
            		betting.set(String.valueOf(Integer.parseInt(betting.get())+10));
            	else if (me.getSource().equals(chip25IV)) 
            		betting.set(String.valueOf(Integer.parseInt(betting.get())+25));
            	else if (me.getSource().equals(chip50IV)) 
            		betting.set(String.valueOf(Integer.parseInt(betting.get())+50));
            	else
            		betting.set(String.valueOf(Integer.parseInt(betting.get())+100));
            	int bet = Integer.parseInt(betting.get());
            	int cashval = Integer.parseInt(cash.get());
            	if (bet > MAXBET)
            		bet = MAXBET;
            	if (bet > cashval)
            		bet = cashval;
            	betting.set(String.valueOf(bet));
            	cash.set(String.valueOf(cashval-bet));
            	me.consume();
            }
        };
        
        /*chip1IV.setOnMouseClicked(new EventHandler<MouseEvent>() {
        	public void handle(MouseEvent me) {
        		btnPlay.setDisable(false);
        		int value = Integer.parseInt(betting.get());
        		if (value < 100)
        			betting.set(String.valueOf(Integer.parseInt(betting.get())+1));
        	}
        });*/
        chip1IV.addEventHandler(MouseEvent.MOUSE_PRESSED, handler);
        chip5IV.addEventHandler(MouseEvent.MOUSE_PRESSED, handler);
        chip10IV.addEventHandler(MouseEvent.MOUSE_PRESSED, handler);
        chip25IV.addEventHandler(MouseEvent.MOUSE_PRESSED, handler);
        chip50IV.addEventHandler(MouseEvent.MOUSE_PRESSED, handler);
        chip100IV.addEventHandler(MouseEvent.MOUSE_PRESSED, handler);

        // Surrender Button
        btnSurrender.setOnAction(new EventHandler<ActionEvent>() {
        	@Override
        	public void handle(ActionEvent e) {
        		youLost();
        	}
        });
        
        // Double down Button
        btnDouble.setOnAction(new EventHandler<ActionEvent>() {
        	@Override
        	public void handle(ActionEvent e) {
        		dealOnePlayerCard();
        		nextDeal();
        	}
        });
        
        // Stand Button
        btnStand.setOnAction(new EventHandler<ActionEvent>() {
        	@Override
        	public void handle(ActionEvent e) {
        		nextDeal();
        	}
        });
        
        // Hit Button
        btnHit.setOnAction(new EventHandler<ActionEvent>() {
        	@Override
        	public void handle(ActionEvent e) {
        		dealOnePlayerCard();
        	}
        });
        
        // Insurance Button
        btnInsurance.setOnAction(new EventHandler<ActionEvent>() {
        	@Override
        	public void handle(ActionEvent e) {
        		btnInsurance.setDisable(true);
        		btnSplit.setDisable(true);
        		insuranceSw = true;
        		if (evenMoneySw == false) {
        			int bet = Integer.parseInt(betting.get());
            		int money = Integer.parseInt(cash.get());
            		if (money >= bet / 2) {
            			insuranceMoney = bet / 2;
            			bet += insuranceMoney;
            			money -= insuranceMoney;
            			cash.set(String.valueOf(money));
            			betting.set(String.valueOf(bet));
            		} else
            			System.out.println("You cannot put Insurance because of lack of your betting money!!!");
        		} 
        		
        	}
        });
        
     // Split Button
        btnSplit.setOnAction(new EventHandler<ActionEvent>() {
        	@Override
        	public void handle(ActionEvent e) {
        		btnSplit.setDisable(true);
        		btnInsurance.setDisable(true);
        		if (splitCount < 2) {
        			splitCount++;
        			playerCards[splitCount].add(playerCards[currentHand].get(1));
        			playerCards[currentHand].remove(1);
        			redrawPlayerCards();
        			dealOnePlayerCard();
        		}
        	}
        });
	}

	public static void main(String[] args) {
		Application.launch(args);
	}
	
	private int valueOfCards(ArrayList<Card> cards) {
		int value, sum = 0;
		int numOfAce = 0;
		
		for (Card card : cards) {
			value = card.getValue();
			if (value == 11) numOfAce++;
			sum += value;
		}
		
		if (sum > 21) {
			if (numOfAce > 0) {
				for (int j = 0; j < numOfAce; j++) {
					sum -= 10;
					if (sum <= 21)
						return sum;
				}
				if (sum > 21) 
					return 0;
				else
					return sum;
			} else
				return 0; // bust
		} else if (sum == 21) {
			if (cards.size() == 2)
				return 999; // black jack
			else
				return sum;
		} 
		return sum;
	}
	
	private void setPlayerButtonDisable(boolean disable) {
		btnDouble.setDisable(disable);
		btnStand.setDisable(disable);
		btnHit.setDisable(disable);
		btnSplit.setDisable(disable);
		btnInsurance.setDisable(disable);
		btnPlay.setDisable(disable);
		btnSurrender.setDisable(disable);
	}

	private void setChipButtonDisable(boolean disable) {
		chip1IV.setDisable(disable);	
		chip5IV.setDisable(disable);	
		chip10IV.setDisable(disable);
		chip25IV.setDisable(disable);	
		chip50IV.setDisable(disable);	
		chip100IV.setDisable(disable);
	}
	
	private void displayPlayerResult(String message) {
		Label resultText = new Label(message);
		resultText.getStyleClass().add("display-text");
		resultText.setLayoutY(420);
		playTable.getChildren().add(resultText);
		if (splitCount == 0) {
			resultText.setLayoutX(190);
		} else if (splitCount == 1) {
			resultText.setLayoutX(100 + 200 * currentHand);
		} else {
			resultText.setLayoutX(190 * currentHand);
		}
	}
	
	private void redrawPlayerCards() {
		for (int i=playTable.getChildren().size()-1; i >= 0; i--) {
			if (i != 1 && i != 3)
				playTable.getChildren().remove(i);
		}
		
		if (splitCount == 2) {
			for (int i=0; i < 3; i++) {
				for (int j=0; j < playerCards[i].size(); j++) {
					playTable.getChildren().add(playerCards[i].get(j).getFront(190.0 * i + j * CARD_GAP_X, 220 + j * CARD_GAP_Y));
				}
			}
		} else {
			for (int i=0; i < 2; i++) {
				for (int j=0; j < playerCards[i].size(); j++) {
					playTable.getChildren().add(playerCards[i].get(j).getFront(100.0 + 200.0 * i + j * CARD_GAP_X, 220 + j * CARD_GAP_Y));
				}
			}
		}
	}
	
	private void youLost() {
		displayPlayerResult("YOU LOST");
		int bet = Integer.parseInt(betting.get());
		int money = Integer.parseInt(cash.get());
		if (splitCount == currentHand) {
			cash.set(String.valueOf(money + bet / 2));
			checkDone[currentHand] = true;
			betting.set("0");
			setPlayerButtonDisable(true);
			setChipButtonDisable(false);
			currentHand = 0;	splitCount = 0;
		} else {
			int eachbet = bet / (splitCount - currentHand + 1);
			cash.set(String.valueOf(money + eachbet / 2));
			betting.set(String.valueOf(bet - eachbet));
			currentHand++;
			dealOnePlayerCard();
		}
	}
	
	private void nextDeal() {
		if (splitCount == currentHand) {
			dealDealerCard();
			currentHand = 0;	splitCount = 0;
		} else {
			currentHand++;
			dealOnePlayerCard();
		}
	}
	
	private void lastJudge(int dealerResult) {
		int bet = Integer.parseInt(betting.get());
		int money = Integer.parseInt(cash.get());
		// if me == blackjack and even money : lost 50%
		if (dealerResult == 999) { // Dealer has blackjack?
			int result = valueOfCards(playerCards[0]);
			if (insuranceSw) {
				if (evenMoneySw) { // return 1.5 x bet
					money += bet * 1.5;
					bet = 0;
					displayPlayerResult("EVEN MONEY WIN");
				} else { // return 2 x insuranceMoney
					money += insuranceMoney * 2;
					bet = 0;
					displayPlayerResult("YOU GOT INSURANCE");
				}
			} else {
				if (result != 999) {
					bet = 0;
					displayPlayerResult("DEALER BLACKJACK");
				} else {
					money += bet;
					bet = 0;
					displayPlayerResult("SAME BLACKJACK");
				}
			}
			cash.set(String.valueOf(money));
			betting.set(String.valueOf(bet));
		} else {
			int playCount = 0;
			for (int i = 0; i < splitCount + 1; i++) {
				if (checkDone[i] == false) playCount++;
			}
			for (int i = 0; i < splitCount + 1; i++) {
				if (checkDone[i]) continue;
				currentHand = i;
				int result = valueOfCards(playerCards[i]);
				int tempMoney = 0;
				if (result == 999) {
					if (evenMoneySw) {
						tempMoney = (bet / playCount) / 2;
						money += tempMoney;
						displayPlayerResult("EVEN MONEY 50% LOST");
					} else {
						tempMoney = (int) ((bet / playCount) * 2.5);
						money += tempMoney;
						displayPlayerResult("YOU BLACKJACK");
					}
				} else {
					if (dealerResult > result) { 
						displayPlayerResult("DEALER WIN");
					} else if (dealerResult == result) {
						tempMoney = bet / playCount;
						money += tempMoney;
						displayPlayerResult("TIE CARD");
					} else {
						tempMoney = (bet / playCount) * 2;
						money += tempMoney;
						displayPlayerResult("YOU WIN");
					}
				}
			}
			bet = 0;
			cash.set(String.valueOf(money));
			betting.set(String.valueOf(bet));
		}
		setPlayerButtonDisable(true);
		setChipButtonDisable(false);
	}
}
