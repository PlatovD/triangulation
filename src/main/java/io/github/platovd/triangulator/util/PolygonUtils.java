package io.github.platovd.triangulator.util;

import io.github.platovd.triangulator.model.Polygon;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PolygonUtils {
    public static Polygon createNewPolygon(
            List<Integer> vertexIndexes,
            Map<Integer, Integer> textureIndexesMap,
            Map<Integer, Integer> normalsIndexesMap
    ) {
        Polygon polygon = new Polygon(vertexIndexes);
        List<Integer> textureIndices = new ArrayList<>();
        List<Integer> normalsIndices = new ArrayList<>();
        vertexIndexes.forEach(index -> {
            textureIndices.add(textureIndexesMap.get(index));
            normalsIndices.add(normalsIndexesMap.get(index));
        });
        polygon.setTextureVertexIndices(textureIndices);
        polygon.setNormalIndices(normalsIndices);
        return polygon;
    }

    public static Polygon deepCopyOfPolygon(Polygon polygon) {
        List<Integer> verticesIndexes = new ArrayList<>(polygon.getVertexIndices());
        List<Integer> normalIndexes = new ArrayList<>(polygon.getNormalIndices());
        List<Integer> textureVerticesIndexes = new ArrayList<>(polygon.getTextureVertexIndices());
        return new Polygon(verticesIndexes, textureVerticesIndexes, normalIndexes);
    }
}
