package io.github.platovd.triangulator.model;

import java.util.ArrayList;
import java.util.List;

public class Polygon {

    private ArrayList<Integer> vertexIndices = new ArrayList<>();
    private ArrayList<Integer> textureVertexIndices = new ArrayList<>();
    private ArrayList<Integer> normalIndices = new ArrayList<>();

    public Polygon() {

    }

    public Polygon(List<Integer> indexes) {
        vertexIndices = new ArrayList<>(indexes);
    }


    public void setVertexIndices(ArrayList<Integer> vertexIndices) {
        assert vertexIndices.size() >= 3;
        this.vertexIndices = vertexIndices;
    }

    public void setTextureVertexIndices(ArrayList<Integer> textureVertexIndices) {
        assert textureVertexIndices.size() >= 3;
        this.textureVertexIndices = textureVertexIndices;
    }

    public void setNormalIndices(ArrayList<Integer> normalIndices) {
        assert normalIndices.size() >= 3;
        this.normalIndices = normalIndices;
    }

    public ArrayList<Integer> getVertexIndices() {
        return new ArrayList<>(vertexIndices);
    }

    public ArrayList<Integer> getTextureVertexIndices() {
        return textureVertexIndices;
    }

    public ArrayList<Integer> getNormalIndices() {
        return normalIndices;
    }
}
