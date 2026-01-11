package io.github.platovd.triangulator.tools;

import io.github.platovd.triangulator.math.MathUtil;
import io.github.platovd.triangulator.math.Vector3f;
import io.github.platovd.triangulator.model.Model;
import io.github.platovd.triangulator.model.Polygon;
import io.github.platovd.triangulator.model.Triangle;
import io.github.platovd.triangulator.util.ByPassDirection;
import io.github.platovd.triangulator.util.Constants;
import io.github.platovd.triangulator.util.PolygonUtil;

import java.util.*;
import java.util.function.Function;

import static java.lang.Math.*;

public class EarCuttingTriangulator implements Triangulator {
    private final static int MAX_LOOP_SIZE = 1000000;

    @Override
    public List<Polygon> triangulatePolygon(Model model, Polygon polygon) {
        float[] barycentric = new float[3];
        // когда 3 и менее вершины изначально. Возвращаю deep копию полигона
        if (polygon.getVertexIndices().size() < 4) {
            return List.of(PolygonUtil.deepCopyOfPolygon(polygon));
        }

        // получаю данные из оригинального объекта
        Queue<Integer> verticesIndexes = new LinkedList<>(polygon.getVertexIndices());
        // создаю ассоциативные коллекции, которые связывают индексы, используемые в полигонах с объектами меша
        Map<Integer, Vector3f> vertices = new HashMap<>();
        Map<Integer, Integer> textureIndexesMap = new HashMap<>();
        Map<Integer, Integer> normalsIndexesMap = new HashMap<>();
        // вспомогательный список вершин, хранимый для определения направления задания полигона
        List<Vector3f> verticesList = new ArrayList<>();
        for (Integer vertexIndex : verticesIndexes) {
            vertices.put(vertexIndex, model.vertices.get(vertexIndex));
            if (vertexIndex < polygon.getTextureVertexIndices().size())
                textureIndexesMap.put(vertexIndex, polygon.getTextureVertexIndices().get(vertexIndex));
            if (vertexIndex < polygon.getNormalIndices().size())
                normalsIndexesMap.put(vertexIndex, polygon.getNormalIndices().get(vertexIndex));
            verticesList.add(model.vertices.get(vertexIndex));
        }

        // Подготовка. Подбираю оси, по которым буду триангулировать
        List<Function<Vector3f, Float>> axes = chooseAxes(verticesList);
        // если невозможно триангулировать
        if (axes == null) {
            System.err.println("Unenviable triangulate polygon");
            return List.of(PolygonUtil.deepCopyOfPolygon(polygon));
        }
        ByPassDirection polygonDirection = findDirection(verticesList, axes.get(0), axes.get(1));
        List<Polygon> newPolygons = new ArrayList<>();
        int leftPointIndex = verticesIndexes.poll();
        int middlePointIndex = verticesIndexes.poll();
        int rightPointIndex = verticesIndexes.poll();
        int iterationsCount = 0;
        while (!verticesIndexes.isEmpty()) {
            // есть два условия, когда я не могу отрезать ухо:
            // 1)одна из оставшихся вершин в треугольнике
            // 2)направления обхода полигона и текущего треугольника не совпадают
            if (
                    isAnyVertexInsideTriangleByBarycentric(barycentric, leftPointIndex, middlePointIndex, rightPointIndex, vertices,
                            axes.get(0), axes.get(1))
                            || findDirection(
                            List.of(
                                    vertices.get(leftPointIndex),
                                    vertices.get(middlePointIndex),
                                    vertices.get(rightPointIndex)), axes.get(0), axes.get(1))
                            != polygonDirection

            ) {
                verticesIndexes.add(leftPointIndex);
                leftPointIndex = middlePointIndex;
                middlePointIndex = rightPointIndex;
                rightPointIndex = verticesIndexes.poll();
                iterationsCount++;
                if (iterationsCount > MAX_LOOP_SIZE)
                    throw new RuntimeException("Bad polygons model. Unable to triangulate. Try SimpleTriangulator");
                continue;
            }
            newPolygons.add(PolygonUtil.createNewPolygon(
                    List.of(leftPointIndex, middlePointIndex, rightPointIndex),
                    textureIndexesMap,
                    normalsIndexesMap
            ));
            middlePointIndex = rightPointIndex;
            rightPointIndex = verticesIndexes.poll();
            iterationsCount = 0;
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
    @Deprecated
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

    protected boolean isAnyVertexInsideTriangleByBarycentric(
            float[] barycentric,
            int leftPointIndex, int rightPointIndex, int middlePointIndex, Map<Integer, Vector3f> vertices,
            Function<Vector3f, Float> getterFirst, Function<Vector3f, Float> getterSecond
    ) {
        for (int i : vertices.keySet()) {
            if (i == leftPointIndex || i == rightPointIndex || i == middlePointIndex) continue;
            float point0cord0 = getterFirst.apply(vertices.get(leftPointIndex));
            float point0cord1 = getterSecond.apply(vertices.get(leftPointIndex));

            float point1cord0 = getterFirst.apply(vertices.get(middlePointIndex));
            float point1cord1 = getterSecond.apply(vertices.get(middlePointIndex));

            float point2cord0 = getterFirst.apply(vertices.get(rightPointIndex));
            float point2cord1 = getterSecond.apply(vertices.get(rightPointIndex));

            float pointCurrentCord0 = getterFirst.apply(vertices.get(i));
            float pointCurrentCord1 = getterSecond.apply(vertices.get(i));

            findBarycentricCords(
                    barycentric, pointCurrentCord0, pointCurrentCord1,
                    point0cord0, point0cord1,
                    point1cord0, point1cord1,
                    point2cord0, point2cord1
            );

            if (barycentric[0] < 0 || barycentric[1] < 0 || barycentric[2] < 0) continue;
            if (abs(1 - barycentric[0] + barycentric[1] + barycentric[2]) > Constants.EPS) continue;
            return true;
        }
        return false;
    }

    @Deprecated
    protected boolean isVerticesInsideTriangleByGeroneSquare(
            int leftPointIndex, int rightPointIndex, int middlePointIndex, Map<Integer, Vector3f> vertices,
            Function<Vector3f, Float> getterFirst, Function<Vector3f, Float> getterSecond
    ) {
        float mainS = (float) MathUtil.calcSquareByGeroneByVertices(
                getterFirst.apply(vertices.get(leftPointIndex)),
                getterSecond.apply(vertices.get(leftPointIndex)),

                getterFirst.apply(vertices.get(middlePointIndex)),
                getterSecond.apply(vertices.get(middlePointIndex)),

                getterFirst.apply(vertices.get(rightPointIndex)),
                getterSecond.apply(vertices.get(rightPointIndex))
        );
        if (abs(mainS) < Constants.EPS) return false;
        for (int i : vertices.keySet()) {
            if (i != leftPointIndex && i != rightPointIndex && i != middlePointIndex) {
                float leftMidS = (float) MathUtil.calcSquareByGeroneByVertices(
                        getterFirst.apply(vertices.get(leftPointIndex)),
                        getterSecond.apply(vertices.get(leftPointIndex)),

                        getterFirst.apply(vertices.get(middlePointIndex)),
                        getterSecond.apply(vertices.get(middlePointIndex)),

                        getterFirst.apply(vertices.get(i)),
                        getterSecond.apply(vertices.get(i))
                );
                float leftRightS = (float) MathUtil.calcSquareByGeroneByVertices(
                        getterFirst.apply(vertices.get(leftPointIndex)),
                        getterSecond.apply(vertices.get(leftPointIndex)),

                        getterFirst.apply(vertices.get(rightPointIndex)),
                        getterSecond.apply(vertices.get(rightPointIndex)),

                        getterFirst.apply(vertices.get(i)),
                        getterSecond.apply(vertices.get(i))
                );

                float midRightS = (float) MathUtil.calcSquareByGeroneByVertices(
                        getterFirst.apply(vertices.get(middlePointIndex)),
                        getterSecond.apply(vertices.get(middlePointIndex)),

                        getterFirst.apply(vertices.get(rightPointIndex)),
                        getterSecond.apply(vertices.get(rightPointIndex)),

                        getterFirst.apply(vertices.get(i)),
                        getterSecond.apply(vertices.get(i))
                );
                if (abs(mainS - (leftMidS + midRightS + leftRightS)) < Constants.EPS) return true;
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

        boolean XYbad = false;
        boolean XZbad = false;
        boolean YZbad = false;

        for (int i = 0; i < vertices.size() - 1; i++) {
            Vector3f v1 = vertices.get(i);
            maxX = max(maxX, v1.getX());
            maxY = max(maxY, v1.getY());
            maxZ = max(maxZ, v1.getZ());
            minX = min(minX, v1.getX());
            minY = min(minY, v1.getY());
            minZ = min(minZ, v1.getZ());
            for (int j = i + 1; j < vertices.size(); j++) {
                Vector3f v2 = vertices.get(j);
                if (
                        abs(v1.getX() - v2.getX()) <= Constants.EPS
                                && abs(v1.getY() - v2.getY()) <= Constants.EPS
                                && abs(v1.getZ() - v2.getZ()) <= Constants.EPS
                ) {
                    return null;
                }
                if (abs(v1.getX() - v2.getX()) <= Constants.EPS
                        && abs(v1.getY() - v2.getY()) <= Constants.EPS) {
                    XYbad = true;
                }
                if (abs(v1.getX() - v2.getX()) <= Constants.EPS
                        && abs(v1.getZ() - v2.getZ()) <= Constants.EPS) {
                    XZbad = true;
                }
                if (abs(v1.getY() - v2.getY()) <= Constants.EPS
                        && abs(v1.getZ() - v2.getZ()) <= Constants.EPS) {
                    YZbad = true;
                }
            }
        }

        if (XYbad && XZbad && YZbad) return null;

        float dx = maxX - minX;
        float dy = maxY - minY;
        float dz = maxZ - minZ;

        if (dx <= Constants.EPS && dy <= Constants.EPS) return null;
        if (dy <= Constants.EPS && dz <= Constants.EPS) return null;
        if (dx <= Constants.EPS && dz <= Constants.EPS) return null;

        Function<Vector3f, Float> dxGetter = Vector3f::getX;
        Function<Vector3f, Float> dyGetter = Vector3f::getY;
        Function<Vector3f, Float> dzGetter = Vector3f::getZ;

        if (dx > Constants.EPS && dy > Constants.EPS && !XYbad)
            return List.of(dxGetter, dyGetter);
        if (dx > Constants.EPS && dz > Constants.EPS && !XZbad)
            return List.of(dxGetter, dzGetter);
        return List.of(dyGetter, dzGetter);
    }

    /**
     * Определяет порядок задания вершин в полигоне модели
     * @param vertices - список вершин полигона в определенном порядке (по или против часовой)
     * @return ByPassDirection направление задания вершин конкретного полигона
     */
    public ByPassDirection findDirection(List<Vector3f> vertices, Function<Vector3f, Float> getterFirst,
                                         Function<Vector3f, Float> getterSecond) {
        int indexOfFoundingVertex = 0;
        Vector3f bottomLeftVertex = vertices.get(0);
        for (int i = 1; i < vertices.size(); i++) {
            Vector3f currentVertex = vertices.get(i);
            if (getterFirst.apply(currentVertex) <= getterFirst.apply(bottomLeftVertex)) {
                if (Objects.equals(getterFirst.apply(currentVertex), getterFirst.apply(bottomLeftVertex)) && getterSecond.apply(currentVertex) > getterSecond.apply(bottomLeftVertex))
                    continue;
                indexOfFoundingVertex = i;
                bottomLeftVertex = currentVertex;
            }
        }

        int leftVertexIndex = indexOfFoundingVertex - 1 < 0 ? vertices.size() - 1 : indexOfFoundingVertex - 1;
        int rightVertexIndex = indexOfFoundingVertex + 1 >= vertices.size() ? 0 : indexOfFoundingVertex + 1;
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

        float cross = getterFirst.apply(vectorA) * getterSecond.apply(vectorB) - getterSecond.apply(vectorA) * getterFirst.apply(vectorB);
        return cross > 0 ?
                ByPassDirection.SECOND : ByPassDirection.FIRST;
    }

    public static void findBarycentricCords(float[] barycentric, float xCur, float yCur, float x0, float y0, float x1, float y1, float x2, float y2) {
        float mainDet = findThirdOrderDeterminant(
                x0, x1, x2,
                y0, y1, y2,
                1, 1, 1
        );
        if (mainDet == 0) {
            barycentric[0] = 0;
            barycentric[1] = 0;
            barycentric[2] = 0;
            return;
        }

        float detForAlpha = findThirdOrderDeterminant(
                xCur, x1, x2,
                yCur, y1, y2,
                1, 1, 1
        );
        float detForBeta = findThirdOrderDeterminant(
                x0, xCur, x2,
                y0, yCur, y2,
                1, 1, 1
        );
        float detForLambda = findThirdOrderDeterminant(
                x0, x1, xCur,
                y0, y1, yCur,
                1, 1, 1
        );
        barycentric[0] = detForAlpha / mainDet;
        barycentric[1] = detForBeta / mainDet;
        barycentric[2] = detForLambda / mainDet;
    }

    public static float findThirdOrderDeterminant(
            float a00, float a01, float a02,
            float a10, float a11, float a12,
            float a20, float a21, float a22
    ) {
        return ((a00 * a11 * a22) + (a10 * a21 * a02) + (a01 * a12 * a20)) - ((a02 * a11 * a20) + (a01 * a10 * a22) + (a12 * a21 * a00));
    }
}
