package io.github.platovd.triangulator.tools;


import io.github.platovd.triangulator.math.Vector3f;
import io.github.platovd.triangulator.model.Model;
import io.github.platovd.triangulator.model.Polygon;
import io.github.platovd.triangulator.model.TriangulatedModel;

import java.util.ArrayList;
import java.util.List;

public class SimpleTriangulator implements Triangulator {
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
        // получаю вершины полигона в виде точек
        ArrayList<Integer> verticesIndexes = polygon.getVertexIndices();
        List<Vector3f> vertices = new ArrayList<>();
        for (Integer verticesIndex : verticesIndexes) {
            vertices.add(model.vertices.get(verticesIndex));
        }

        // начинаю обработку вершин и создание новых полигонов
        List<Polygon> newPolygons = new ArrayList<>();
        int n = vertices.size();
        int firstVertexIndex = 0;
        int secondVertexIndex = 1;
        int thirdVertexIndex = 2;
        while (thirdVertexIndex < n) {
            Polygon newPolygon = new Polygon(List.of(
                    verticesIndexes.get(firstVertexIndex),
                    verticesIndexes.get(secondVertexIndex),
                    verticesIndexes.get(thirdVertexIndex)
            ));
            newPolygons.add(newPolygon);
            secondVertexIndex++;
            thirdVertexIndex++;
        }
        return newPolygons;
    }
}
