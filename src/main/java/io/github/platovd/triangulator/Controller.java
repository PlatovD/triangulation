package io.github.platovd.triangulator;

import io.github.platovd.triangulator.math.Vector3f;
import io.github.platovd.triangulator.model.Model;
import io.github.platovd.triangulator.model.Polygon;
import io.github.platovd.triangulator.tools.EarCuttingTriangulator;
import io.github.platovd.triangulator.tools.SimpleTriangulator;
import io.github.platovd.triangulator.tools.Triangulator;
import io.github.platovd.triangulator.util.Constants;
import io.github.platovd.triangulator.util.TriangulationType;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Controller {
    private final List<Vector3f> pointsOfPolygon = new ArrayList<>();
    private final Map<TriangulationType, Triangulator> triangulators = Map.of(
            TriangulationType.SIMPLE, new SimpleTriangulator(),
            TriangulationType.EAR_CUTTING, new EarCuttingTriangulator()
    );
    private Triangulator currentTriangulator;

    @FXML
    private Canvas canvas;

    @FXML
    private Button clearButton;

    @FXML
    private ChoiceBox<TriangulationType> triangulationTypeChoiceBox;

    @FXML
    private Button triangulateButton;

    @FXML
    public void initialize() {
        clearButton.setOnAction(event -> {
            clearCanvas();
            pointsOfPolygon.clear();
        });
        triangulateButton.setOnAction(event -> {
            triangulationListener();
        });
        triangulationTypeChoiceBox.getItems().addAll(TriangulationType.SIMPLE, TriangulationType.EAR_CUTTING);
        triangulationTypeChoiceBox.setValue(TriangulationType.EAR_CUTTING);
        currentTriangulator = triangulators.get(triangulationTypeChoiceBox.getValue());
        triangulationTypeChoiceBox.setOnAction(e -> {
            currentTriangulator = triangulators.get(triangulationTypeChoiceBox.getValue());
        });
    }

    @FXML
    public void addPointListener(MouseEvent mouseEvent) {
        double x = mouseEvent.getX();
        double y = mouseEvent.getY();

        Vector3f vector3f = new Vector3f((float) x, (float) y, 0);
        pointsOfPolygon.add(vector3f);
        clearCanvas();
        drawEdges(pointsOfPolygon);
        drawPoints(pointsOfPolygon);
    }

    private void drawPoints(List<Vector3f> drawingPolygonPoints) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Constants.POINT_COLOR);
        for (Vector3f vector3f : drawingPolygonPoints) {
            gc.fillOval(vector3f.getX() - (double) Constants.POINT_RADIUS / 2, vector3f.getY() - (double) Constants.POINT_RADIUS / 2, Constants.POINT_RADIUS, Constants.POINT_RADIUS);
        }
    }

    private void drawEdges(List<Vector3f> drawingPolygonPoints) {
        if (drawingPolygonPoints.size() < 3) return;
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Constants.LINE_COLOR);
        Vector3f start;
        Vector3f stop = null;
        for (int i = 1; i < drawingPolygonPoints.size(); i++) {
            stop = drawingPolygonPoints.get(i);
            start = drawingPolygonPoints.get(i - 1);
            gc.strokeLine(start.getX(), start.getY(), stop.getX(), stop.getY());
        }
        start = drawingPolygonPoints.get(0);
        gc.strokeLine(stop.getX(), stop.getY(), start.getX(), start.getY());
    }

    public void clearCanvas() {
        canvas.getGraphicsContext2D().clearRect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
    }

    public void triangulationListener() {
        Model model = new Model();
        model.vertices.addAll(pointsOfPolygon);
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < pointsOfPolygon.size(); i++) {
            indexes.add(i);
        }
        model.polygons.add(new Polygon(indexes));
        List<Polygon> polygons = currentTriangulator.triangulatePolygon(model, model.polygons.get(0));
        drawPolygons(model, polygons);
    }

    private void drawPolygons(Model model, List<Polygon> polygonsToDraw) {
        clearCanvas();
        for (Polygon polygon : polygonsToDraw) {
            List<Vector3f> points = new ArrayList<>();
            for (Integer vertexIndex : polygon.getVertexIndices()) {
                points.add(model.vertices.get(vertexIndex));
            }
            drawEdges(points);
            drawPoints(points);
        }
    }
}