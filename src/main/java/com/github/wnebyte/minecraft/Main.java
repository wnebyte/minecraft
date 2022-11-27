package com.github.wnebyte.minecraft;

import com.github.wnebyte.minecraft.core.Application;
import com.github.wnebyte.minecraft.scenes.MainMenuScene;

public class Main extends Application {

    public static void main(String[] args) {
        Application.launch(new Main());
    }

    @Override
    protected void configure(Configuration conf) {
        conf.setTitle("Minecraft");
        conf.setScene(MainMenuScene::new);
    }
}
