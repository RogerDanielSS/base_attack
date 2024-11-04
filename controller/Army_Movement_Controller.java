package controller;

import javafx.animation.KeyFrame;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import domain.Node;

public class Army_Movement_Controller {

    @FXML
    private Region Point1_region;

    @FXML
    private Region Point2_region;

    @FXML
    private Region Point3_region;

    @FXML
    private Region Point4_region;

    @FXML
    private Region Point5_region;

    @FXML
    private Region Point6_region;

    @FXML
    private Region Point7_region;

    @FXML
    private Region Point8_region;

    @FXML
    private Region Point9_region;

    @FXML
    private Region Point10_region;

    @FXML
    private ImageView Army_Image; // Grupo que contém a imagem do exército

    private Node node; // Instância de Node
    private double eixoX; // Posição no eixo X
    private double eixoY; // Posição no eixo Y
    private double speed = 1.0; // Velocidade da animação (ajuste conforme necessário)

    public void setNode(Node node) {
        this.node = node;
    }

    @FXML
    public void initialize() {
        List<Point2D> points = gather_coordinates();
        ArrayList<Integer> numbers = new ArrayList<>(Arrays.asList(5, 4, 1, 3, 2, 0, 6, 7, 8, 9));

        pathTransition(numbers, points);
    }

    public static Point2D converPoint2d(Region regiao) {
        double x = regiao.getLayoutX();
        double y = regiao.getLayoutY();
        return new Point2D(x, y);
    }

    public List<Point2D> gather_coordinates() {
        List<Point2D> points = new ArrayList<>();
        points.add(converPoint2d(Point1_region));
        points.add(converPoint2d(Point2_region));
        points.add(converPoint2d(Point3_region));
        points.add(converPoint2d(Point4_region));
        points.add(converPoint2d(Point5_region));
        points.add(converPoint2d(Point6_region));
        points.add(converPoint2d(Point7_region));
        points.add(converPoint2d(Point8_region));
        points.add(converPoint2d(Point9_region));
        points.add(converPoint2d(Point10_region));
        // System.out.println(points);
        return points;
    }

    public void pathTransition(ArrayList<Integer> numbers, List<Point2D> points) {
        SequentialTransition seqTransition = new SequentialTransition();

        // Get the initial coordinates of the Army_Image
        double startCoordX = Army_Image.getLayoutX();
        double startCoordY = Army_Image.getLayoutY();
        System.out.println("Initial Position: x = " + startCoordX + " y = " + startCoordY);

        for (int i : numbers) {
            Point2D destine = points.get(i);

            // Create a new TranslateTransition for the current movement
            TranslateTransition movement = new TranslateTransition();
            movement.setNode(Army_Image);
            movement.setDuration(Duration.seconds(1));

            // Adjust the destination coordinates by subtracting the node's layout bounds
            double targetX = destine.getX() - Army_Image.getLayoutBounds().getMinX();
            double targetY = destine.getY() - Army_Image.getLayoutBounds().getMinY();

            // Calculate the movement based on the current position and the target position
            movement.setToX(targetX - startCoordX); // Movement in X
            movement.setToY(targetY - startCoordY); // Movement in Y

            // Output for debugging
            System.out.println("Destination: x: " + targetX + " y: " + targetY +
                    " Movement to: x: " + movement.getToX() + " y: " + movement.getToY());

            // Add the movement to the transition sequence
            seqTransition.getChildren().add(movement);

            // Update the starting coordinates for the next transition
            startCoordX = Army_Image.getLayoutX();
            startCoordY = Army_Image.getLayoutY();
        }

        // Start the sequential animation
        seqTransition.play();
    }
}
