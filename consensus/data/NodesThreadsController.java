package consensus.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import consensus.presentation.pages_controllers.DiscussionRoomController;

public class NodesThreadsController {
  private DiscussionRoomController guiController;

  public NodesThreadsController(DiscussionRoomController guiController) {
    this.guiController = guiController;
  }

  public void start() {
    int nodeCount = 10;
    List<Node> nodes = new ArrayList<>();
    List<Integer> ports = new ArrayList<>();
    Random random = new Random();

    // Definir números base para o array de números
    ArrayList<Integer> baseNumbers = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

    // Configurar portas
    for (int i = 0; i < nodeCount; i++) {
      ports.add(5000 + i);
    }

    // Criar e iniciar cada nó com números em ordem aleatória
    for (int i = 0; i < nodeCount; i++) {
      Collections.shuffle(baseNumbers, random);
      ArrayList<Integer> nodeNumbers = new ArrayList<>(baseNumbers);

      List<Integer> otherPorts = new ArrayList<>(ports);
      otherPorts.remove(i); // Remover a própria porta do nó

      Node node = new Node(this, i, nodeNumbers, ports.get(i), otherPorts);
      nodes.add(node);
      node.start();
    }
  }

  public void animateSendMessage(String message, int from, int to) {
    guiController.animateSendMessage(message, from, to);
  }

  public void setSoldierSubtitle(int soldier, String subtitle, String color) {
    guiController.setSoldierSubtitle(soldier, subtitle, color);
  }
}
