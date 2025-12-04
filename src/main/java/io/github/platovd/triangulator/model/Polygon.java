package io.github.platovd.triangulator.model;

import java.util.ArrayList;
import java.util.List;

public class Polygon {

    private List<Integer> vertexIndices = new ArrayList<>();
    private List<Integer> textureVertexIndices = new ArrayList<>();
    private List<Integer> normalIndices = new ArrayList<>();

    public Polygon() {

    }

    public Polygon(List<Integer> indexes) {
        vertexIndices = new ArrayList<>(indexes);
    }

    public Polygon(List<Integer> vertexIndices, List<Integer> textureVertexIndices, List<Integer> normalIndices) {
        this.vertexIndices = vertexIndices;
        this.textureVertexIndices = textureVertexIndices;
        this.normalIndices = normalIndices;
    }

    public void setVertexIndices(ArrayList<Integer> vertexIndices) {
        assert vertexIndices.size() >= 3;
        this.vertexIndices = vertexIndices;
    }

    public void setTextureVertexIndices(ArrayList<Integer> textureVertexIndices) {
        assert textureVertexIndices.size() >= 3;
        this.textureVertexIndices = textureVertexIndices;
    }

    public void setNormalIndices(List<Integer> normalIndices) {
        assert normalIndices.size() >= 3;
        this.normalIndices = normalIndices;
    }

    public ArrayList<Integer> getVertexIndices() {
        return new ArrayList<>(vertexIndices);
    }

    public List<Integer> getTextureVertexIndices() {
        return textureVertexIndices;
    }

    public List<Integer> getNormalIndices() {
        return normalIndices;
    }
}
