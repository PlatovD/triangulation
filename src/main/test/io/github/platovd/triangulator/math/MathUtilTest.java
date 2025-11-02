package io.github.platovd.triangulator.math;


import io.github.platovd.triangulator.model.Triangle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MathUtilTest {
    private Triangle triangle = new Triangle(
            new Vector3f(0, 0, 0),
            new Vector3f(0, 50, 0),
            new Vector3f(50, 0, 0)
    );

    @Test
    public void insideTriangleTest() {
        Assertions.assertTrue(triangle.isInsideTriangle(new Vector3f(10, 10, 0)));
        Assertions.assertTrue(triangle.isInsideTriangle(new Vector3f(3, 40, 0)));
        Assertions.assertFalse(triangle.isInsideTriangle(new Vector3f(-10, 10, 0)));
        Assertions.assertFalse(triangle.isInsideTriangle(new Vector3f(10, -10, 0)));
    }
}
