package io.github.platovd.triangulator.model;


import io.github.platovd.triangulator.math.Vector2f;
import io.github.platovd.triangulator.math.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class Model {
    public Model() {
    }

    public Model(List<Vector3f> vertices, List<Vector2f> textureVertices, List<Vector3f> normals, List<Polygon> polygons) {
        this.vertices = vertices;
        this.textureVertices = textureVertices;
        this.normals = normals;
        this.polygons = polygons;
    }

    public List<Vector3f> vertices = new ArrayList<Vector3f>();
    public List<Vector2f> textureVertices = new ArrayList<Vector2f>();
    public List<Vector3f> normals = new ArrayList<Vector3f>();
    public List<Polygon> polygons = new ArrayList<Polygon>();
}
