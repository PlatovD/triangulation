package io.github.platovd.triangulator.util;

public enum TriangulationType {
    SIMPLE("Простая"),
    EAR_CUTTING("Отсечение ушей");

    private final String desc;

    TriangulationType(String desc) {
        this.desc = desc;

    }

    @Override
    public String toString() {
        return desc;
    }
}
