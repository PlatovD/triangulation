package io.github.platovd.triangulator.tools;

import io.github.platovd.triangulator.math.Vector3f;
import io.github.platovd.triangulator.model.Model;
import io.github.platovd.triangulator.model.Polygon;
import io.github.platovd.triangulator.model.Triangle;
import io.github.platovd.triangulator.util.ByPassDirection;
import io.github.platovd.triangulator.util.Constants;
import io.github.platovd.triangulator.util.Pair;
import io.github.platovd.triangulator.util.PolygonUtils;

import java.util.*;
import java.util.function.Function;

import static java.lang.Math.*;

public class EarCuttingTriangulator implements Triangulator {
    @Override
    public List<Polygon> triangulatePolygon(Model model, Polygon polygon) {
        // когда 3 и менее вершины изначально. Возвращаю deep копию полигона
        if (polygon.getVertexIndices().size() < 4) {
            return List.of(PolygonUtils.deepCopyOfPolygon(polygon));
        }

        // получаю данные из оригинального объекта
        Queue<Integer> verticesIndexes = new LinkedList<>(polygon.getVertexIndices());
        // создаю ассоциативные коллекции, которые связывают индексы, используемые в полигонах с объектами меша
        Map<Integer, Vector3f> vertices = new HashMap<>();
        Map<Integer, Integer> textureIndexesMap = new HashMap<>();
        Map<Integer, Integer> normalsIndexesMap = new HashMap<>();
        // вспомогательный список вершин, хранимый для определения направления задания полигона
        List<Vector3f> verticesList = new ArrayList<>();
        int indexOfVertexInPolygon = 0;
        for (Integer vertexIndex : verticesIndexes) {
            vertices.put(vertexIndex, model.vertices.get(vertexIndex));
            if (vertexIndex < polygon.getTextureVertexIndices().size())
                textureIndexesMap.put(vertexIndex, polygon.getTextureVertexIndices().get(indexOfVertexInPolygon));
            if (vertexIndex < polygon.getNormalIndices().size())
                normalsIndexesMap.put(vertexIndex, polygon.getNormalIndices().get(indexOfVertexInPolygon));
            verticesList.add(model.vertices.get(vertexIndex));
            indexOfVertexInPolygon++;
        }

        // Подготовка. Подбираю оси, по которым буду триангулировать
        List<Function<Vector3f, Float>> axes = chooseAxes(verticesList);
        // если невозможно триангулировать
        if (axes == null) return List.of(PolygonUtils.deepCopyOfPolygon(polygon));
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
            newPolygons.add(PolygonUtils.createNewPolygon(
                    List.of(leftPointIndex, middlePointIndex, rightPointIndex),
                    textureIndexesMap,
                    normalsIndexesMap
            ));
            middlePointIndex = rightPointIndex;
            rightPointIndex = verticesIndexes.poll();
        }
        newPolygons.add(new Polygon(List.of(leftPointIndex, middlePointIndex, rightPointIndex)));
        return newPolygons;
    }

    /**
     * Проверяет список точек на принадлежность треугольнику
     * @param leftPointIndex первая вершина треугольника
     * @param rightPointIndex вторая вершина треугольника
     * @param middlePointIndex третья вершина треугольника
     * @param vertices список вершин на проверку
     * @return true, если одна из вершин в треугольнике, иначе false
     */
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

    protected List<Function<Vector3f, Float>> chooseAxes(List<Vector3f> vertices) {
        float minX = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE;
        float maxZ = -Float.MAX_VALUE;

        for (Vector3f vertex : vertices) {
            minX = min(vertex.getX(), minX);
            maxX = max(vertex.getX(), maxX);

            minY = min(vertex.getY(), minY);
            maxY = max(vertex.getY(), maxY);

            minZ = min(vertex.getZ(), minZ);
            maxZ = max(vertex.getZ(), maxZ);
        }

        float dx = maxX - minX;
        float dy = maxY - minY;
        float dz = maxZ - minZ;

        if (dx < Constants.EPS && dy < Constants.EPS && dz < Constants.EPS) return null;

        Pair<Float, Function<Vector3f, Float>> dxPair = new Pair<>(dx, Vector3f::getX);
        Pair<Float, Function<Vector3f, Float>> dyPair = new Pair<>(dy, Vector3f::getY);
        Pair<Float, Function<Vector3f, Float>> dzPair = new Pair<>(dz, Vector3f::getZ);

        List<Pair<Float, Function<Vector3f, Float>>> pairs = new ArrayList<>(List.of(dxPair, dyPair, dzPair));
        pairs.sort((t1, t2) -> Float.compare(t1.first, t2.first));
        return List.of(pairs.get(2).second, pairs.get(1).second);
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
