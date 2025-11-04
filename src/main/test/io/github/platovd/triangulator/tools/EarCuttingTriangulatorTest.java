package io.github.platovd.triangulator.tools;

import io.github.platovd.triangulator.math.Vector3f;
import io.github.platovd.triangulator.model.Model;
import io.github.platovd.triangulator.model.Polygon;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

public class EarCuttingTriangulatorTest {
    private static Model oneSimplePolygonModel;
    private static final Triangulator earClippingTriangulator = new EarCuttingTriangulator();

    @BeforeAll
    public static void setup() {
        oneSimplePolygonModel = new Model();
        Vector3f v1 = new Vector3f(10, 10, 0);
        Vector3f v2 = new Vector3f(5, 40, 0);
        Vector3f v3 = new Vector3f(20, 30, 0);
        Vector3f v4 = new Vector3f(12, 70, 0);
        oneSimplePolygonModel.vertices.addAll(List.of(v1, v2, v3, v4));
        // по часовой
        oneSimplePolygonModel.polygons.add(new Polygon(List.of(3, 2, 0, 1)));
    }

    @Test
    public void testAllIsTrianglesPolygonTriangulating() {
        earClippingTriangulator.triangulateModel(oneSimplePolygonModel);
        for (Polygon polygon : oneSimplePolygonModel.polygons) {
            Assertions.assertEquals(3, polygon.getVertexIndices().size());
        }
    }

    @Test
    public void testCountPolygonsPolygonTriangulating() {
        earClippingTriangulator.triangulateModel(oneSimplePolygonModel);
        Assertions.assertEquals(oneSimplePolygonModel.vertices.size() - 2, oneSimplePolygonModel.polygons.size());
    }
}
