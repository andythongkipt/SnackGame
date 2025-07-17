package com.mygdx.game;

import com.badlogic.gdx.Game;
 
public class App extends Game {

    @Override
    public void create() {
        setScreen(new SnakeGameScreen());
    }
}