package com.worldbuilder.mapgame;

public class PerlinNoise {
    private final int[] p = new int[512];

    public PerlinNoise(long seed) {
        int[] permutation = new int[256];
        for (int i = 0; i < 256; i++) {
            permutation[i] = i;
        }

        java.util.Random random = new java.util.Random(seed);
        for (int i = 255; i > 0; i--) {
            int index = random.nextInt(i + 1);
            int temp = permutation[i];
            permutation[i] = permutation[index];
            permutation[index] = temp;
        }

        System.arraycopy(permutation, 0, p, 0, 256);
        System.arraycopy(permutation, 0, p, 256, 256);
    }
    public float noise(float x, float y, float frequency, float amplitude) {
        // Scale the input coordinates to control the frequency of the noise
        x *= frequency;
        y *= frequency;

        int X = (int) Math.floor(x) & 255;
        int Y = (int) Math.floor(y) & 255;
        x -= Math.floor(x);
        y -= Math.floor(y);
        float u = fade(x);
        float v = fade(y);
        int A = p[X] + Y, AA = p[A], AB = p[A + 1];
        int B = p[X + 1] + Y, BA = p[B], BB = p[B + 1];

        // Scale the output value to control the amplitude of the noise
        return amplitude * lerp(v, lerp(u, grad(p[AA], x, y),
                grad(p[BA], x - 1, y)),
                lerp(u, grad(p[AB], x, y - 1),
                        grad(p[BB], x - 1, y - 1)));
    }


    private float fade(float t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private float lerp(float t, float a, float b) {
        return a + t * (b - a);
    }

    private float grad(int hash, float x, float y) {
        int h = hash & 3;
        float u = h < 2 ? x : y;
        float v = h < 2 ? y : x;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? 2 * v : -2 * v);
    }
}

//    public float noise(float x, float y) {
//        int X = (int) Math.floor(x) & 255;
//        int Y = (int) Math.floor(y) & 255;
//        x -= Math.floor(x);
//        y -= Math.floor(y);
//        float u = fade(x);
//        float v = fade(y);
//        int A = p[X] + Y, AA = p[A], AB = p[A + 1];
//        int B = p[X + 1] + Y, BA = p[B], BB = p[B + 1];
//
//        return lerp(v, lerp(u, grad(p[AA], x, y),
//                grad(p[BA], x - 1, y)),
//                lerp(u, grad(p[AB], x, y - 1),
//                        grad(p[BB], x - 1, y - 1)));
//    }