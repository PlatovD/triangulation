package io.github.platovd.triangulator.model;

import java.util.ArrayList;
import java.util.List;

public class TriangulatedModel extends Model {
    List<Triangle> getTriangles() {
        List<Triangle> triangles = new ArrayList<>();
        for (Polygon polygon : polygons) {
            if (polygon.getVertexIndices().size() > 3)
                throw new IllegalArgumentException("Model is not good triangulated!");
            triangles.add(new Triangle(
                    vertices.get(polygon.getVertexIndices().get(0)),
                    vertices.get(polygon.getVertexIndices().get(1)),
                    vertices.get(polygon.getVertexIndices().get(2)
                    )));
        }
        return triangles;
    }
}
