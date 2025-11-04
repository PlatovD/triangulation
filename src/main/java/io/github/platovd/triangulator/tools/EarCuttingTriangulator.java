package io.github.platovd.triangulator.tools;

import io.github.platovd.triangulator.math.Vector3f;
import io.github.platovd.triangulator.model.Model;
import io.github.platovd.triangulator.model.Polygon;
import io.github.platovd.triangulator.model.Triangle;
import io.github.platovd.triangulator.model.TriangulatedModel;

import java.util.*;

public class EarCuttingTriangulator implements Triangulator {
    @Override
    public Model triangulateModel(Model model) {
        ArrayList<Polygon> newPolygons = new ArrayList<>(model.vertices.size() - 2);
        for (Polygon polygon : model.polygons) {
            List<Polygon> clipped = triangulatePolygon(model, polygon);
            newPolygons.addAll(clipped);
        }
        model.polygons = newPolygons;
        return model;
    }

    @Override
    public TriangulatedModel createTriangulatedModel(Model model) {
        TriangulatedModel triangulatedModel = new TriangulatedModel();
        triangulatedModel.vertices = new ArrayList<>(model.vertices);
        triangulatedModel.normals = new ArrayList<>(model.normals);
        triangulatedModel.textureVertices = new ArrayList<>(model.textureVertices);
        triangulatedModel.polygons = new ArrayList<>(model.polygons);
        triangulateModel(triangulatedModel);
        return triangulatedModel;
    }

    @Override
    public List<Polygon> triangulatePolygon(Model model, Polygon polygon) {
        if (polygon.getVertexIndices().size() < 4) return new ArrayList<>(List.of(polygon));

        Queue<Integer> verticesIndexes = new LinkedList<>(polygon.getVertexIndices());
        Map<Integer, Vector3f> vertices = new HashMap<>();
        for (Integer verticesIndex : verticesIndexes) {
            vertices.put(verticesIndex, model.vertices.get(verticesIndex));
        }

        // начинаю обработку вершин и создание новых полигонов
        List<Polygon> newPolygons = new ArrayList<>();
        int leftPointIndex = verticesIndexes.poll();
        int middlePointIndex = verticesIndexes.poll();
        int rightPointIndex = verticesIndexes.poll();
        while (!verticesIndexes.isEmpty()) {
            // делаю левый поворот и определяю, является ли точка middle выпуклой
            double leftTurn = leftTurn(
                    vertices.get(leftPointIndex),
                    vertices.get(rightPointIndex),
                    vertices.get(middlePointIndex)
            );
            // значит не выпуклая или не является ухом если идти по часовой
            if (leftTurn < 0 || !checkEar(leftPointIndex, rightPointIndex, middlePointIndex, vertices)) {
                // обновляю индексы рассматриваемых вершин и просто иду дальше
                var tmp = rightPointIndex;
                rightPointIndex = verticesIndexes.poll();
                verticesIndexes.add(leftPointIndex);
                leftPointIndex = middlePointIndex;
                middlePointIndex = tmp;
            }
            // все сошлось и я могу создавать новый треугольник
            newPolygons.add(new Polygon(List.of(leftPointIndex, middlePointIndex, rightPointIndex)));
            middlePointIndex = rightPointIndex;
            rightPointIndex = verticesIndexes.poll();
        }
        newPolygons.add(new Polygon(List.of(leftPointIndex, middlePointIndex, rightPointIndex)));
        return newPolygons;
    }

    protected boolean checkEar(int leftPointIndex, int rightPointIndex, int middlePointIndex, Map<Integer, Vector3f> vertices) {
        Triangle triangle = new Triangle(vertices.get(leftPointIndex), vertices.get(rightPointIndex), vertices.get(middlePointIndex));
        for (int i : vertices.keySet()) {
            if (i != leftPointIndex && i != rightPointIndex && i != middlePointIndex) {
                if (triangle.isInsideTriangle(vertices.get(i))) return false;
            }
        }
        return true;
    }

    protected double leftTurn(Vector3f a, Vector3f b, Vector3f c) {
        return (c.getX() - a.getX()) * (b.getY() - a.getY()) - (c.getY() - a.getY()) * (b.getX() - a.getX());
    }
}
