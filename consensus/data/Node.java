package consensus.data;

import java.io.*;
import java.net.*;
import java.util.*;

import javafx.application.Platform;

public class Node extends Thread {
    private final NodesThreadsController threadsController;
    private final int nodeId;
    private final int port;
    private final List<Integer> otherNodePorts;
    private ArrayList<Integer> numbers;
    private ServerSocket serverSocket; // ServerSocket do nó

    private int leaderPort = -1;
    private boolean consensusReached = false; // Indica se o nó alcançou consenso
    private boolean listening = true;

    private String expectedLeaderPort = "";
    private boolean expectingForLeaderResponse = false;
    private boolean openVoting = false;
    private int leaderCandidatationResponses = 0;
    private int positiveLeaderCandidatationResponses = 0;

    private int orderVotingResponses = 0;
    private int positiveOrderVotingResponses = 0;

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
            // Thread to constantly listen for incoming proposals
            new Thread(this::listen).start();

            // Inicia o processo de consenso até que o consenso seja alcançado
            while (!consensusReached) {
                try {
                    Thread.sleep(getRandomNumber());
                } catch (Exception e) {
                }

                if (leaderPort == -1 && !openVoting)
                    handleLackOfLeader();

                if (this.leaderPort == this.port)
                    handleOrderVoting();

                Thread.sleep(5000);
            }

            System.out.println(nodeId + ": " + numbers);

        } catch (IOException | InterruptedException e) {
            System.out.println("Node " + nodeId + " could not start: " + e.getMessage());
        }
    }

    public int getRandomNumber() {
        Random random = new Random();
        // Generate a random number between 0 and 4500, then add 500 to shift the range
        return 500 + random.nextInt(4501);
    }

    private void handleLackOfLeader() {
        Platform.runLater(() -> {
            threadsController.setSoldierSubtitle(nodeId, nodeId + ": Candidate", "yellow");
        });
        positiveLeaderCandidatationResponses = 0;
        leaderCandidatationResponses = 0;

        HashMap<String, String> message = new HashMap<String, String>();

        message.put("type", "candidatation");
        message.put("content", "");
        message.put("origin", "" + port);

        for (int otherPort : otherNodePorts) {
            Platform.runLater(() -> {
                threadsController.animateSendMessage(nodeId + " candidated", nodeId, otherPort - 5000);
            });
            sendMessage(message, otherPort);
        }
        try {
            Thread.sleep(2100);

        } catch (Exception e) {
        }

        try {
            while (leaderCandidatationResponses != 9 && positiveLeaderCandidatationResponses <= 4) {
                Thread.sleep(500);
            }
            Thread.sleep(2100);
        } catch (Exception e) {
        }

        defineLeadership();
    }

    private void handleOrderVoting() {
        orderVotingResponses = 0;
        positiveOrderVotingResponses = 0;

        HashMap<String, String> message = new HashMap<String, String>();

        message.put("type", "order-voting");
        message.put("content", numbers.toString().replaceAll("[^0-9]", ""));
        message.put("origin", "" + port);

        for (int otherPort : otherNodePorts) {
            sendMessage(message, otherPort);
        }

        try {
            while (orderVotingResponses != 9 && positiveOrderVotingResponses <= 4) {
                System.out.println(nodeId + " orderVotingResponses: " + orderVotingResponses);
                System.out.println(nodeId + " positiveOrderVotingResponses: " + positiveOrderVotingResponses);
                Thread.sleep(500);
            }
            Thread.sleep(2100);
        } catch (Exception e) {
        }

        if (positiveOrderVotingResponses > 4)
            sendMyOrderToFollowers();
        else
            dropLeadership();

    }

    private void defineLeadership() {
        System.out.println("candidate " + port + " has " + positiveLeaderCandidatationResponses + " votes");
        HashMap<String, String> message = new HashMap<String, String>();

        String content = "IM-NOT-THE-LEADER";

        if (this.positiveLeaderCandidatationResponses > 4) {
            leaderPort = port;
            System.out.println(nodeId + " is leader");

            Platform.runLater(() -> {
                threadsController.setSoldierSubtitle(nodeId, nodeId + ": Leader", "green");
            });
            content = "IM-THE-LEADER";
        } else {
            Platform.runLater(() -> {
                threadsController.setSoldierSubtitle(nodeId, nodeId + ": Lost election", "red");
            });
            try {
                Thread.sleep(1000);

            } catch (Exception e) {
            }
            Platform.runLater(() -> {
                threadsController.setSoldierSubtitle(nodeId, "", "transparent");
            });
        }

        message.put("type", "leader-definition");
        message.put("content", content);
        message.put("origin", "" + port);

        for (int otherPort : otherNodePorts)
            sendMessage(message, otherPort);
    }

    private void dropLeadership() {
        Platform.runLater(() -> {
            threadsController.setSoldierSubtitle(nodeId, nodeId + ": Order refused", "red");
        });
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        }
        Platform.runLater(() -> {
            threadsController.setSoldierSubtitle(nodeId, "", "transparent");
        });
        System.out.println(port + ": Order disapproved. Droping leadership");
        HashMap<String, String> message = new HashMap<String, String>();

        message.put("type", "leader-definition");
        message.put("content", "DROP-LEADERSHIP");
        message.put("origin", "" + port);

        for (int otherPort : otherNodePorts)
            sendMessage(message, otherPort);
    }

    private void sendMyOrderToFollowers() {
        System.out.println(port + ": Order approved. Broadcasting");
        HashMap<String, String> message = new HashMap<String, String>();

        message.put("type", "order-definition");
        message.put("content", numbers.toString().replaceAll("[^0-9]", ""));
        message.put("origin", "" + port);

        for (int otherPort : otherNodePorts) {
            System.out.println(nodeId + " sent order to " + otherPort);
            Platform.runLater(() -> {
                threadsController.animateSendMessage(numbers.toString(), nodeId, otherPort - 5000);
            });
            sendMessage(message, otherPort);
        }
        try {
            Thread.sleep(2100);

        } catch (Exception e) {
        }
        setReachedConsensus();
    }

    private void sendMessage(HashMap<String, String> message, int port) {
        new Thread(() -> {
            try (Socket socket = new Socket("localhost", port);
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

                out.writeObject(message);
            } catch (IOException e) {
                System.out.println(
                        "Node " + nodeId + " could not connect to port " + port + ": " +
                                e.getMessage());
            }
        }).start();
    }

    private void listen() {
        while (listening) {
            try (Socket socket = serverSocket.accept();
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
                @SuppressWarnings("unchecked")
                HashMap<String, String> receivedMessage = (HashMap<String, String>) in.readObject();

                handleReceivedMessage(receivedMessage);
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Node " + nodeId + " connection error: " +
                        e.getMessage());
            }
        }
    }

    private void handleReceivedMessage(HashMap<String, String> message) {
        switch (message.get("type")) {
            case "candidatation":
                respondCandidate(message);
                break;
            case "candidatation-response":
                storeCandidatationResponses(message);
                break;
            case "leader-definition":
                verifyAndSetLeader(message);
                break;

            case "order-voting":
                respondOrderVoting(message);
                break;
            case "order-voting-response":
                storeOrderVoteResponses(message);
                break;
            case "order-definition":
                setOrder(message);
                break;
            default:
                break;
        }
    }

    private void setOrder(HashMap<String, String> message) {
        this.numbers = charArrayToIntArrayList(message.get("content").toCharArray());
        setReachedConsensus();
        System.out.println("Node " + nodeId + "changed order to: " + message.get("content"));
    }

    private void setReachedConsensus() {
        this.consensusReached = true;
        this.listening = false;
        Platform.runLater(() -> {
            threadsController.setSoldierSubtitle(nodeId, numbers.toString(), "green");
        });
    }

    private void verifyAndSetLeader(HashMap<String, String> message) {
        if (message.get("content").equals("IM-THE-LEADER")) {

            Platform.runLater(() -> {
                threadsController.setSoldierSubtitle(nodeId,
                        nodeId + ": " + (Integer.parseInt(message.get("origin")) - 5000) + " is the Leader",
                        "purple");
            });
            System.out.println("Node " + nodeId + " defined port " + message.get("origin") + " as its leader");
            leaderPort = Integer.parseInt(message.get("origin"));
            expectingForLeaderResponse = false;
            expectedLeaderPort = "";

        } else if (message.get("content").equals("IM-NOT-THE-LEADER")
                && expectedLeaderPort.equals(message.get("origin"))) {

            expectingForLeaderResponse = false;
            expectedLeaderPort = "";

        } else if (message.get("content").equals("DROP-LEADERSHIP")
                && ("" + leaderPort).equals(message.get("origin"))) {
            leaderPort = -1;

            Platform.runLater(() -> {
                threadsController.setSoldierSubtitle(nodeId, "", "transparent");
            });
        }

        openVoting = false;
    }

    private void respondOrderVoting(HashMap<String, String> message) {
        HashMap<String, String> responseMessage = new HashMap<String, String>();

        responseMessage.put("type", "order-voting-response");
        responseMessage.put("content", "" + getRandomBoolean());
        responseMessage.put("origin", "" + nodeId);

        try {
            Thread.sleep(2100);
        } catch (Exception e) {
        }
        Platform.runLater(() -> {
            threadsController.animateSendMessage("", nodeId, Integer.parseInt(message.get("origin")) - 5000);
        });
        sendMessage(responseMessage, Integer.parseInt(message.get("origin")));
    }

    private void respondCandidate(HashMap<String, String> message) {
        openVoting = true;

        HashMap<String, String> responseMessage = new HashMap<String, String>();

        boolean content = false;

        if (!expectingForLeaderResponse) {
            content = getRandomBoolean();
        }

        if (content) {
            expectedLeaderPort = message.get("origin");

            this.expectingForLeaderResponse = true;
        }

        responseMessage.put("type", "candidatation-response");
        responseMessage.put("content", "" + content);
        responseMessage.put("origin", "" + nodeId);

        try {
            Thread.sleep(2100);
        } catch (Exception e) {
        }
        Platform.runLater(() -> {
            threadsController.animateSendMessage("", nodeId, Integer.parseInt(message.get("origin")) - 5000);
        });
        sendMessage(responseMessage, Integer.parseInt(message.get("origin")));
    }

    private void storeCandidatationResponses(HashMap<String, String> responseMessage) {
        leaderCandidatationResponses += 1;
        if (responseMessage.get("content").equals("true"))
            positiveLeaderCandidatationResponses += 1;

    }

    private void storeOrderVoteResponses(HashMap<String, String> responseMessage) {
        orderVotingResponses += 1;
        if (responseMessage.get("content").equals("true"))
            positiveOrderVotingResponses += 1;

    }

    private static boolean getRandomBoolean() {
        Random random = new Random();
        return random.nextBoolean();
    }

    private static ArrayList<Integer> charArrayToIntArrayList(char[] charArray) {
        ArrayList<Integer> intList = new ArrayList<>();

        for (char c : charArray) {
            // Convert char to int and add to the list
            if (Character.isDigit(c)) {
                intList.add(Character.getNumericValue(c));
            }
        }

        return intList;
    }
}
