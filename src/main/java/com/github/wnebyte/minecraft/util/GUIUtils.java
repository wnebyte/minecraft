package com.github.wnebyte.minecraft.util;

import com.github.wnebyte.minecraft.core.Application;

public class GUIUtils {

    public static void runSafe(Runnable task) {
        if (Application.isApplicationThread()) {
            task.run();
        } else {
            Application.runLater(task);
        }
    }
}
