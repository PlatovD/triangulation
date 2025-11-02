package io.github.platovd.triangulator.tools;

import io.github.platovd.triangulator.math.Vector3f;
import io.github.platovd.triangulator.model.Model;
import io.github.platovd.triangulator.model.Polygon;
import io.github.platovd.triangulator.model.Triangle;
import io.github.platovd.triangulator.model.TriangulatedModel;

import java.util.ArrayList;
import java.util.List;

public class EarCuttingTriangulator implements Triangulator {
    @Override
    public Model triangulateModel(Model model) {
        return null;
    }

    @Override
    public TriangulatedModel createTriangulatedModel(Model model) {
        return null;
    }

    @Override
    public List<Polygon> triangulatePolygon(Model model, Polygon polygon) {
        // todo: сортировать по окружности точки полигона
        if (polygon.getVertexIndices().size() < 4) return new ArrayList<>(List.of(polygon));

        ArrayList<Integer> verticesIndexes = polygon.getVertexIndices();
        List<Vector3f> vertices = new ArrayList<>();
        for (Integer verticesIndex : verticesIndexes) {
            vertices.add(model.vertices.get(verticesIndex));
        }

        // начинаю обработку вершин и создание новых полигонов
        List<Polygon> newPolygons = new ArrayList<>();
        int leftPointIndex = 0;
        int middlePointIndex = 1;
        int rightPointIndex = 2;
        int module = verticesIndexes.size();
        while (verticesIndexes.size() != 3) {
            // делаю левый поворот и определяю, является ли точка middle выпуклой
            double leftTurn = leftTurn(
                    vertices.get(leftPointIndex),
                    vertices.get(rightPointIndex),
                    vertices.get(middlePointIndex)
            );
            // значит не выпуклая или не является ухом если идти по часовой
            if (leftTurn < 0 || !checkEar(leftPointIndex, rightPointIndex, middlePointIndex, vertices)) {
                // обновляю индексы рассматриваемых вершин и просто иду дальше
                leftPointIndex++;
                middlePointIndex++;
                rightPointIndex++;
                leftPointIndex %= module;
                middlePointIndex %= module;
                rightPointIndex %= module;
            }
            // все сошлось и я могу создавать новый треугольник
            newPolygons.add(new Polygon(List.of(leftPointIndex, middlePointIndex, rightPointIndex)));
            verticesIndexes.remove(middlePointIndex);
            module = verticesIndexes.size();
            middlePointIndex++;
            rightPointIndex++;
            middlePointIndex %= module;
            rightPointIndex %= module;
        }
        newPolygons.add(new Polygon(verticesIndexes));
        return newPolygons;
    }

    protected boolean checkEar(int leftPointIndex, int rightPointIndex, int middlePointIndex, List<Vector3f> vertices) {
        int n = vertices.size();
        Triangle triangle = new Triangle(vertices.get(leftPointIndex), vertices.get(rightPointIndex), vertices.get(middlePointIndex));
        for (int i = 0; i < n; i++) {
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
