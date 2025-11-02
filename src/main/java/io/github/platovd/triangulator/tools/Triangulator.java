package io.github.platovd.triangulator.tools;


import io.github.platovd.triangulator.model.Model;
import io.github.platovd.triangulator.model.Polygon;
import io.github.platovd.triangulator.model.TriangulatedModel;

import java.util.List;

public interface Triangulator {
    Model triangulateModel(Model model);

    TriangulatedModel createTriangulatedModel(Model model);

    List<Polygon> triangulatePolygon(Model model, Polygon polygon);
}
