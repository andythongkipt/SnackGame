package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.Gdx;

public class GameImages {
    // เมทอดสำหรับโหลดรูปภาพและคืนค่า Texture object
    public static Texture loadSnakeImage() { 
        return new Texture(Gdx.files.internal("assets/snake.png"));
    }

    public static Texture loadAppleImage() {
        return new Texture(Gdx.files.internal("assets/apple.png"));
    }

    public static Texture loadSpecialItemImage() {
        return new Texture(Gdx.files.internal("assets/special_item.png"));
    }
}