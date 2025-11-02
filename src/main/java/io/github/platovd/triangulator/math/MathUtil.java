package io.github.platovd.triangulator.math;

public class MathUtil {
    public static double[] solveByKramer(double a, double b, double c, double d, double v1, double v2) {
        double deltaMain = calcDetermination(
                a, b,
                c, d
        );
        if (deltaMain == 0) return new double[]{Integer.MIN_VALUE, Integer.MIN_VALUE};
        double delta1 = calcDetermination(
                v1, b,
                v2, d);
        double delta2 = calcDetermination(
                a, v1,
                c, v2
        );
        return new double[]{delta1 / deltaMain, delta2 / deltaMain};
    }

    private static double calcDetermination(double a11, double a12, double a21, double a22) {
        return a11 * a22 - a12 * a21;
    }
}
