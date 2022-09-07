package com.github.wnebyte.minecraft;

import com.github.wnebyte.minecraft.renderer.Renderer;

public class Test {

    public static void main(String[] args) {
        Renderer renderer = new Renderer(null);

    }

    /*
        public float[] genVertices() {
        List<Float> c = new ArrayList<>(INDICES.length * 5);

        for (int i = 0; i < INDICES.length; i++) {
            int index = INDICES[i];
            float[] vertex = POS_2D[index];
            for (float f : vertex) {
                c.add(f);
            }
            float[] texCoords = TEX_COORDS_2D[i % 6];
            /*
            for (float f : texCoords) {
                c.add(f);
            }

            System.out.println("index: " + index + ", texCoords: " + Arrays.toString(texCoords));
            if ((i + 1) % 6 == 0) {
        System.out.println();
    }
}

        System.out.println(c.size());
                return toArray(c);
                }
     */
}
