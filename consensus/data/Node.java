package consensus.data;

import java.io.*;
import java.net.*;
import java.util.*;

import javafx.application.Platform;

public class Node extends Thread {
    private NodesThreadsController threadsController;
    private final int nodeId;
    private ArrayList<Integer> numbers;
    private final int port;
    private final List<Integer> otherNodePorts;
    private final Map<List<Integer>, Integer> receivedOrders = new HashMap<>(); // Contador de ordens recebidas
    private volatile boolean consensusReached = false; // Indica se o nó alcançou consenso
    private ServerSocket serverSocket; // ServerSocket do nó

    public Node(NodesThreadsController threadsController, int nodeId, ArrayList<Integer> numbers, int port,
            List<Integer> otherNodePorts) {
        this.nodeId = nodeId;
        this.numbers = numbers;
        this.port = port;
        this.otherNodePorts = otherNodePorts;
        this.threadsController = threadsController;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            // System.out.println("Node " + nodeId + " listening on port " + port);

            // Inicia o processo de consenso até que o consenso seja alcançado
            while (!consensusReached) {
                Platform.runLater(() -> {
                    threadsController.setSoldierSubtitle(nodeId, numbers.toString(), "black");
                });

                sendProposalToOtherNodes();

                // Limpa as ordens recebidas para uma nova rodada e aguarda propostas dos outros
                // nós
                receivedOrders.clear();
                receiveProposalsFromOtherNodes();

                // Decide consenso após receber todas as propostas
                decideConsensus();

                // Espera um pouco antes da próxima rodada (simulação de espera)
                Thread.sleep(3000);
            }

            Platform.runLater(() -> {
                threadsController.setSoldierSubtitle(nodeId, numbers.toString(), "green");
            });
            System.out.println("Node " + nodeId + " reached final consensus on order: " + numbers);
        } catch (IOException | InterruptedException e) {
            System.out.println("Node " + nodeId + " could not start: " + e.getMessage());
        }
    }

    private void sendProposalToOtherNodes() {
        for (int otherPort : otherNodePorts) {
            new Thread(() -> {
                try (Socket socket = new Socket("localhost", otherPort);
                        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
                    out.writeObject(numbers);
                    Platform.runLater(() -> {
                        threadsController.animateSendMessage(numbers.toString(), nodeId, otherPort - 5000);
                    });

                    System.out.println("Node " + nodeId + " sent proposal to port " + otherPort);
                } catch (IOException e) {
                    System.out.println(
                            "Node " + nodeId + " could not connect to port " + otherPort + ": " + e.getMessage());
                }
            }).start();
        }
    }

    private void receiveProposalsFromOtherNodes() {
        for (int i = 0; i < otherNodePorts.size(); i++) { // Recebe de cada nó
            try (Socket socket = serverSocket.accept();
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
                @SuppressWarnings("unchecked")
                ArrayList<Integer> receivedOrder = (ArrayList<Integer>) in.readObject();
                System.out.println("Node " + nodeId + " received order: " + receivedOrder);

                // Registra e conta a ordem recebida
                receivedOrders.merge(receivedOrder, 1, Integer::sum);
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Node " + nodeId + " connection error: " + e.getMessage());
            }
        }
    }

    private void decideConsensus() {
        // Encontra a ordem mais popular
        Map.Entry<List<Integer>, Integer> consensusOrder = receivedOrders.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElseThrow(() -> new RuntimeException("Consensus decision not found")); // Deve sempre retornar uma
                                                                                          // entrada válida

        // Atualiza o array para a ordem consensual se necessário
        ArrayList<Integer> newOrder = new ArrayList<>(consensusOrder.getKey());
        if (!newOrder.equals(numbers)) {
            numbers = newOrder;
        } else if (checkReachedGeneralConsensus()) {
            // Se o array já estiver em consenso, marca o nó como tendo alcançado consenso
            consensusReached = true;
        }
    }

    private boolean checkReachedGeneralConsensus() {
        if (receivedOrders.size() == 1) {
            // Get the only entry in the map
            int count = receivedOrders.values().iterator().next();
            // Check if the count is 9 or greater
            return count >= 9;
        }
        return false;
    }
}
