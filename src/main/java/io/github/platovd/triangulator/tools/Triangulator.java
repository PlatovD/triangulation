package io.github.platovd.triangulator.tools;


import io.github.platovd.triangulator.model.Model;
import io.github.platovd.triangulator.model.Polygon;
import io.github.platovd.triangulator.model.TriangulatedModel;

import java.util.ArrayList;
import java.util.List;

public interface Triangulator {
    /**
     * Триангулирует переданную модель
     * @param model - пользовательская модель
     */
    default void triangulateModel(Model model) {
        ArrayList<Polygon> newPolygons = new ArrayList<>(model.vertices.size() - 2);
        for (Polygon polygon : model.polygons) {
            List<Polygon> clipped = triangulatePolygon(model, polygon);
            newPolygons.addAll(clipped);
        }
        model.polygons = newPolygons;
    }

    default TriangulatedModel createTriangulatedModel(Model model) {
        TriangulatedModel triangulatedModel = new TriangulatedModel();
        triangulatedModel.vertices = new ArrayList<>(model.vertices);
        triangulatedModel.normals = new ArrayList<>(model.normals);
        triangulatedModel.textureVertices = new ArrayList<>(model.textureVertices);
        triangulatedModel.polygons = new ArrayList<>(model.polygons);
        triangulateModel(triangulatedModel);
        return triangulatedModel;
    }

    List<Polygon> triangulatePolygon(Model model, Polygon polygon);
}
