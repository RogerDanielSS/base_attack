package consensus.presentation.pages_controllers;

import java.net.URL;
import java.util.ResourceBundle;

import consensus.data.NodesThreadsController;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public class DiscussionRoomController implements Initializable {

  @FXML
  private Button PLAY;

  @FXML
  private AnchorPane BACKGROUND;

  @FXML
  private ImageView SOLDIER0, SOLDIER1, SOLDIER2, SOLDIER3, SOLDIER4, SOLDIER5, SOLDIER6, SOLDIER7, SOLDIER8, SOLDIER9;

  private Label[] soldierSubtitles = new Label[10];

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    // FOWARD_BUTTON.setOnAction(event -> { // Inicio do controle do botao Iniciar
    // try {
    // AnchorPane a = (AnchorPane)
    // FXMLLoader.load(getClass().getResource("../pages/discussion_room.fxml")); //
    // Chamando o fxml
    // AnchorPane_TelaIncial.getChildren().setAll(a);
    // } catch (Exception e) { // Fim do try, inicio do catch
    // System.out.println("Error: Tentativa de mudar a cena para a tela de simulacao
    // \n" + e.getMessage());
    // }

    // });

    PLAY.setOnAction(event -> {
      // animateSendMessage("test", 0, 4);
      new NodesThreadsController(this).start();
    });

  }

  public void animateSendMessage(String message, int from, int to) {
    // Identify the starting and ending soldiers
    ImageView fromSoldier = getSoldierImageView(from);
    ImageView toSoldier = getSoldierImageView(to);

    // Get the exact screen positions of the starting and destination soldiers
    double startX = fromSoldier.localToScene(fromSoldier.getBoundsInLocal()).getMinX() + fromSoldier.getFitWidth() / 2;
    double startY = fromSoldier.localToScene(fromSoldier.getBoundsInLocal()).getMinY() + fromSoldier.getFitHeight() / 2;
    double endX = toSoldier.localToScene(toSoldier.getBoundsInLocal()).getMinX() + toSoldier.getFitWidth() / 2;
    double endY = toSoldier.localToScene(toSoldier.getBoundsInLocal()).getMinY() + toSoldier.getFitHeight() / 2;

    // Create a color for the message based on the digits in the message
    String colorHex = generateColorFromMessage(message);
    Color messageColor = Color.web(colorHex);

    // Create a circle with the generated color for animation
    Circle messageBall = new Circle(5, messageColor);
    messageBall.setLayoutX(startX);
    messageBall.setLayoutY(startY);

    // Add the circle to the background
    BACKGROUND.getChildren().add(messageBall);

    // Create a TranslateTransition to animate from start to end
    TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(1), messageBall);
    translateTransition.setByX(endX - startX);
    translateTransition.setByY(endY - startY);

    // Set up animation completion actions
    translateTransition.setOnFinished(event -> {
      // Remove the ball after it reaches the destination
      BACKGROUND.getChildren().remove(messageBall);

      // Display the message above the destination soldier
      Label messageLabel = new Label(message);
      messageLabel.setStyle("-fx-background-color: " + toHexColor(messageColor) + "; -fx-text-fill: black;");
      messageLabel.setLayoutX(endX);
      messageLabel.setLayoutY(endY - 20);
      // BACKGROUND.getChildren().add(messageLabel);

      // Pause to show the message for 2 seconds
      PauseTransition pause = new PauseTransition(Duration.seconds(2));
      pause.setOnFinished(e -> BACKGROUND.getChildren().remove(messageLabel));
      pause.play();
    });

    // Start the animation
    translateTransition.play();
  }

  private String toHexColor(Color color) {
    return String.format("#%02X%02X%02X",
        (int) (color.getRed() * 255),
        (int) (color.getGreen() * 255),
        (int) (color.getBlue() * 255));
  }

  // Helper method to generate a color based on message digits
  private String generateColorFromMessage(String message) {
    // Map each digit to a specific hex color value
    String[] colors = {
        "#FF0000", "#FFA500", "#FFFF00", "#008000", "#0000FF",
        "#4B0082", "#EE82EE", "#A52A2A", "#8A2BE2", "#5F9EA0"
    };

    // Extract digits and form a color string based on unique digits
    StringBuilder colorHex = new StringBuilder("#");
    for (char ch : message.toCharArray()) {
      if (Character.isDigit(ch)) {
        int digit = Character.getNumericValue(ch);
        colorHex.append(colors[digit].substring(1, 3)); // Get first byte of each color for simplicity
        if (colorHex.length() >= 7)
          break; // Limit to hex code length
      }
    }
    return colorHex.length() < 7 ? colorHex.toString() : colorHex.substring(0, 7);
  }

  public void setSoldierSubtitle(int soldier, String subtitle, String color) {
    ImageView soldierImage = getSoldierImageView(soldier);

    // Calculate position for the subtitle below the soldier
    double xPos = soldierImage.getLayoutX() + soldierImage.getFitWidth() / 2 - 50;
    double yPos = soldierImage.getLayoutY() + soldierImage.getFitHeight() + 5;

    String colorHex = generateColorFromMessage(subtitle);
    Color messageColor = Color.web(colorHex);
    // Check if a subtitle already exists for this soldier
    if (soldierSubtitles[soldier] == null) {
      // Create a new subtitle label
      Label subtitleLabel = new Label(subtitle);
      subtitleLabel.setTextFill(Color.WHITE);
      subtitleLabel
          .setStyle("-fx-background-color: " + toHexColor(messageColor) + "; -fx-padding: 2;");
      subtitleLabel.setLayoutX(xPos);
      subtitleLabel.setLayoutY(yPos);
      subtitleLabel.setTextAlignment(TextAlignment.CENTER);

      // Store the label in the array
      soldierSubtitles[soldier] = subtitleLabel;

      // Add the label to the background
      BACKGROUND.getChildren().add(subtitleLabel);
    } else {
      // Update the existing subtitle text and color
      soldierSubtitles[soldier].setText(subtitle);
      soldierSubtitles[soldier].setStyle("-fx-background-color: " + toHexColor(messageColor) + "; -fx-padding: 2;");
    }
  }

  private ImageView getSoldierImageView(int index) {
    switch (index) {
      case 0:
        return SOLDIER0;
      case 1:
        return SOLDIER1;
      case 2:
        return SOLDIER2;
      case 3:
        return SOLDIER3;
      case 4:
        return SOLDIER4;
      case 5:
        return SOLDIER5;
      case 6:
        return SOLDIER6;
      case 7:
        return SOLDIER7;
      case 8:
        return SOLDIER8;
      case 9:
        return SOLDIER9;
      default:
        throw new IllegalArgumentException("Invalid soldier index: " + index);
    }
  }
} // Fim da classe HomeController