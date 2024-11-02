package consensus.presentation.pages_controllers;


import java.net.URL;
import java.util.ResourceBundle;

import consensus.data.NodesThreadsStarter;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

public class DiscussionRoomController implements Initializable {

  @FXML
  private Button FOWARD_BUTTON, START_BUTTON;

  @FXML
  private GridPane botoes;

  @FXML
  private AnchorPane AnchorPane_TelaIncial;

  @FXML
  private TextArea Explicacao_TextArea;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    FOWARD_BUTTON.setOnAction(event -> { // Inicio do controle do botao Iniciar
      try {
        AnchorPane a = (AnchorPane) FXMLLoader.load(getClass().getResource("../pages/discussion_room.fxml")); // Chamando o fxml
        AnchorPane_TelaIncial.getChildren().setAll(a);
      } catch (Exception e) { // Fim do try, inicio do catch
        System.out.println("Error: Tentativa de mudar a cena para a tela de simulacao \n" + e.getMessage());
      }

    });


    START_BUTTON.setOnAction(event -> { // Inicio do controle do botao Iniciar
      new NodesThreadsStarter().start();
    });
  } // Fim do metodo initialize
} // Fim da classe HomeController