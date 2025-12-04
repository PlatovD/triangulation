package io.github.platovd.triangulator.tools;

import io.github.platovd.triangulator.math.Vector3f;
import io.github.platovd.triangulator.model.Model;
import io.github.platovd.triangulator.model.Polygon;
import io.github.platovd.triangulator.model.Triangle;
import io.github.platovd.triangulator.model.TriangulatedModel;
import io.github.platovd.triangulator.util.ByPassDirection;

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
        // случай, когда 3 и менее вершины изначально. Возвращаю глубокую копию полигона
        if (polygon.getVertexIndices().size() < 4) {
            List<Integer> verticesIndexes = new ArrayList<>(polygon.getVertexIndices());
            List<Integer> normalIndexes = new ArrayList<>(polygon.getNormalIndices());
            List<Integer> textureVerticesIndexes = new ArrayList<>(polygon.getTextureVertexIndices());
            return List.of(new Polygon(verticesIndexes, textureVerticesIndexes, normalIndexes));
        }

        // получаю данные из оригинального объекта
        Queue<Integer> verticesIndexes = new LinkedList<>(polygon.getVertexIndices());
        Map<Integer, Vector3f> vertices = new TreeMap<>();
        List<Vector3f> verticesList = new ArrayList<>();
        for (Integer verticesIndex : verticesIndexes) {
            vertices.put(verticesIndex, model.vertices.get(verticesIndex));
            verticesList.add(model.vertices.get(verticesIndex));
        }

        // начинаю обработку вершин и создание новых полигонов
        ByPassDirection polygonDirection = findDirection(verticesList);
        List<Polygon> newPolygons = new ArrayList<>();
        int leftPointIndex = verticesIndexes.poll();
        int middlePointIndex = verticesIndexes.poll();
        int rightPointIndex = verticesIndexes.poll();
        while (!verticesIndexes.isEmpty()) {
            // есть два условия, когда я не могу отрезать ухо:
            // 1)одна из оставшихся вершин в треугольнике
            // 2)направления обхода полигона и текущего треугольника не совпадают
            if (
                    isVerticesInsideTriangle(leftPointIndex, middlePointIndex, rightPointIndex, vertices)
                            || findDirection(
                            List.of(
                                    vertices.get(leftPointIndex),
                                    vertices.get(middlePointIndex),
                                    vertices.get(rightPointIndex)))
                            != polygonDirection

            ) {
                verticesIndexes.add(leftPointIndex);
                leftPointIndex = middlePointIndex;
                middlePointIndex = rightPointIndex;
                rightPointIndex = verticesIndexes.poll();
                continue;
            }
            newPolygons.add(new Polygon(List.of(leftPointIndex, middlePointIndex, rightPointIndex)));
            middlePointIndex = rightPointIndex;
            rightPointIndex = verticesIndexes.poll();
        }
        newPolygons.add(new Polygon(List.of(leftPointIndex, middlePointIndex, rightPointIndex)));
        return newPolygons;
    }

    protected boolean isVerticesInsideTriangle(int leftPointIndex, int rightPointIndex, int middlePointIndex, Map<Integer, Vector3f> vertices) {
        Triangle triangle =
                new Triangle(vertices.get(leftPointIndex), vertices.get(rightPointIndex), vertices.get(middlePointIndex));
        for (int i : vertices.keySet()) {
            if (i != leftPointIndex && i != rightPointIndex && i != middlePointIndex) {
                if (triangle.isInsideTriangle(vertices.get(i))) return true;
            }
        }
        return false;
    }

    /**
     * Определяет порядок задания вершин в полигоне модели
     * @param vertices - список вершин полигона в определенном порядке (по или против часовой)
     * @return ByPassDirection направление задания вершин конкретного полигона
     */
    public ByPassDirection findDirection(List<Vector3f> vertices) {
        int indexOfBottomLeftVertex = 0;
        Vector3f bottomLeftVertex = vertices.get(0);
        for (int i = 1; i < vertices.size(); i++) {
            Vector3f currentVertex = vertices.get(i);
            if (currentVertex.getY() <= bottomLeftVertex.getY()) {
                if (currentVertex.getY() == bottomLeftVertex.getY() && currentVertex.getX() > bottomLeftVertex.getX())
                    continue;
                indexOfBottomLeftVertex = i;
                bottomLeftVertex = currentVertex;
            }
        }

        int leftVertexIndex = indexOfBottomLeftVertex - 1 < 0 ? vertices.size() - 1 : indexOfBottomLeftVertex - 1;
        int rightVertexIndex = indexOfBottomLeftVertex + 1 >= vertices.size() ? 0 : indexOfBottomLeftVertex + 1;
        Vector3f vectorA = new Vector3f(
                vertices.get(leftVertexIndex).getX() - bottomLeftVertex.getX(),
                vertices.get(leftVertexIndex).getY() - bottomLeftVertex.getY(),
                vertices.get(leftVertexIndex).getZ() - bottomLeftVertex.getZ()
        );

        Vector3f vectorB = new Vector3f(
                vertices.get(rightVertexIndex).getX() - bottomLeftVertex.getX(),
                vertices.get(rightVertexIndex).getY() - bottomLeftVertex.getY(),
                vertices.get(rightVertexIndex).getZ() - bottomLeftVertex.getZ()
        );
        return vectorA.getX() * vectorB.getY() - vectorA.getY() * vectorB.getX() > 0 ?
                ByPassDirection.REVERSE : ByPassDirection.CLOCKWISE;
    }
}
