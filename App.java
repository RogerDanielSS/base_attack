import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import consensus.presentation.pages_controllers.DiscussionRoomController;

public class App extends Application { // Inicio da classe App
  public static void main(String[] args) throws Exception { // inicio do metodo main
    launch(args);
    DiscussionRoomController DISCUSSION_ROOM_CONTROLLER = new DiscussionRoomController();
  }// fim do metodo main

  /*
   * ***************************************************************
   * Metodo: start
   * Funcao: metodo de Aplication sobrescrito que carrega o palco
   * Parametros: Um Stage, do javaFX
   * Retorno: vazio
   */
  @Override
  public void start(Stage stage) throws Exception { // inicio do metodo start
    Parent root = FXMLLoader.load(getClass().getResource("consensus/presentation/pages/discussion_room.fxml"));
    Scene Scene = new Scene(root);
    stage.setScene(Scene);
    stage.setResizable(false);
    stage.show();
  } // fim do metodo start
} // Fim da classe App