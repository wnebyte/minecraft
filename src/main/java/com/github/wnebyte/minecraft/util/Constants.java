package com.github.wnebyte.minecraft.util;

import static org.lwjgl.opengl.GL11.GL_NONE;
import static org.lwjgl.opengl.GL30.*;

public class Constants {

    public static final float[] ZERO_FILLER_VEC   = { 0.0f, 0.0f, 0.0f, 0.0f };

    public static final float[] ONE_FILLER_VEC    = { 1.0f, 1.0f, 1.0f, 1.0f };

    public static final int[] BUFS_NONE_NONE_NONE = { GL_NONE, GL_NONE, GL_NONE };

    public static final int[] BUFS_ZERO_NONE_NONE = { GL_COLOR_ATTACHMENT0, GL_NONE, GL_NONE };

    public static final int[] BUFS_NONE_ONE_TWO   = { GL_NONE, GL_COLOR_ATTACHMENT1, GL_COLOR_ATTACHMENT2 };
}
