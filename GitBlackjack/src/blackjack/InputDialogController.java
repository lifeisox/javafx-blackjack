package blackjack;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class InputDialogController implements Initializable {

	@FXML Button btnConfirm;
	@FXML TextField howManyDecks;
	
	@FXML
	private void handleButtonAction(ActionEvent event) {
		BlackJack.numOfDeck = Integer.parseInt(howManyDecks.getText());
		Stage stage = (Stage) btnConfirm.getScene().getWindow();
		stage.close();
    }
	
	@Override
    public void initialize(URL url, ResourceBundle rb) {
		/* add Event Filter to your TextFields **************************************************/
		howManyDecks.addEventFilter(KeyEvent.KEY_TYPED, numeric_Validation(1));
	}
	
	/* Numeric Validation Limit the characters to maxLengh AND to ONLY DigitS ***************/
	public EventHandler<KeyEvent> numeric_Validation(final Integer max_Lengh) {
		return new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent e) {
				TextField txt_TextField = (TextField) e.getSource();
				if (txt_TextField.getText().length() >= max_Lengh) {
					e.consume();
				}
				
				if (e.getCharacter().matches("[0-9.]")){
					if (txt_TextField.getText().contains(".") && e.getCharacter().matches("[.]")){
						e.consume();
					} else if (txt_TextField.getText().length() == 0 && e.getCharacter().matches("[.]")){
						e.consume();
					}
				} else {
					e.consume();
				}
			}
		};
	}
	
	/* Letters Validation Limit the characters to maxLengh AND to ONLY Letters *************************************/
	public EventHandler<KeyEvent> letter_Validation(final Integer max_Lengh) {
		return new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent e) {
				TextField txt_TextField = (TextField) e.getSource();
				if (txt_TextField.getText().length() >= max_Lengh) {
					e.consume();
				}
				if (e.getCharacter().matches("[A-Za-z]")){
				} else {
					e.consume();
				}
			}
		};
	}
}
