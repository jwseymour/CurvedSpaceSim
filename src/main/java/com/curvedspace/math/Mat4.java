package com.curvedspace.math;

public class Mat4 {
    // Row-major: m[row][col]
    private final double[][] m;

    public Mat4(double[][] values) {
        if (values.length != 4 || values[0].length != 4) {
            throw new IllegalArgumentException("Matrix must be 4x4");
        }
        this.m = new double[4][4];
        for (int i = 0; i < 4; i++) {
            System.arraycopy(values[i], 0, this.m[i], 0, 4);
        }
    }

    public static Mat4 identity() {
        return new Mat4(new double[][] {
                { 1, 0, 0, 0 },
                { 0, 1, 0, 0 },
                { 0, 0, 1, 0 },
                { 0, 0, 0, 1 }
        });
    }

    public Vec4 mul(Vec4 v) {
        return new Vec4(
                m[0][0] * v.x + m[0][1] * v.y + m[0][2] * v.z + m[0][3] * v.w,
                m[1][0] * v.x + m[1][1] * v.y + m[1][2] * v.z + m[1][3] * v.w,
                m[2][0] * v.x + m[2][1] * v.y + m[2][2] * v.z + m[2][3] * v.w,
                m[3][0] * v.x + m[3][1] * v.y + m[3][2] * v.z + m[3][3] * v.w);
    }

    public Mat4 mul(Mat4 other) {
        double[][] res = new double[4][4];
        for (int i = 0; i < 4; i++) {
            for (int k = 0; k < 4; k++) { // k is inner dimension
                for (int j = 0; j < 4; j++) {
                    res[i][j] += this.m[i][k] * other.m[k][j];
                }
            }
        }
        return new Mat4(res);
    }

    // Rotation in the plane defined by axis1 and axis2.
    // axis1, axis2 are indices 0=x, 1=y, 2=z, 3=w
    // Rotates axis1 towards axis2 by angle theta.
    public static Mat4 rotation(int axis1, int axis2, double theta) {
        double[][] v = new double[4][4];
        // Initialize identity
        for (int i = 0; i < 4; i++)
            v[i][i] = 1.0;

        double c = Math.cos(theta);
        double s = Math.sin(theta);

        v[axis1][axis1] = c;
        v[axis1][axis2] = -s;
        v[axis2][axis1] = s;
        v[axis2][axis2] = c;

        return new Mat4(v);
    }

    public double get(int r, int c) {
        return m[r][c];
    }
}
