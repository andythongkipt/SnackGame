package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.audio.Music;

public class SnakeGame extends ApplicationAdapter {
    private static final int GRID_CELL = 32;
    private Texture bodyTexture;
    private Texture snakeTexture;
    private Texture appleTexture;
    private Texture specialItemTexture;
    private Texture backgroundTexture;
    private Texture wallTexture;
    private SpriteBatch batch;
    private Array<Coordinate> snake;
    private Coordinate apple;
    private Coordinate specialItem;
    private int snakeDirection;
    private float timeSinceLastMove;
    private float snakeSpeed;
    private int score;
    private float timeSinceLastSpecialItem;
    private float specialItemDuration;
    private boolean gameOver;
    private boolean specialItemVisible;
    private Music backgroundMusic;
    private BitmapFont font;
    private Array<Coordinate> obstacles;
    private float timeSinceLastWallRespawn;
    private static final float WALL_RESPAWN_INTERVAL = 10f; // Interval for wall respawn in seconds
    private boolean wallsVisible;
    private float wallVisibilityTimer;
    private static final float WALL_VISIBLE_DURATION = 5f; // Duration walls stay visible in seconds
    private GameState currentState;

    private enum GameState {
        MENU,
        PLAYING,
        GAME_OVER
    }

    @Override
    public void create() {
        snakeTexture = new Texture(Gdx.files.internal("Upsnake.png"));
        bodyTexture = new Texture(Gdx.files.internal("snake.png"));
        appleTexture = new Texture(Gdx.files.internal("apple.png"));
        specialItemTexture = new Texture(Gdx.files.internal("special_item.png"));
        backgroundTexture = new Texture(Gdx.files.internal("background.png"));
        wallTexture = new Texture(Gdx.files.internal("wall.jpg"));
        batch = new SpriteBatch();
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("musicbg.mp3"));
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(0.75f);
        backgroundMusic.play();

        snake = new Array<>();
        font = new BitmapFont();
        currentState = GameState.MENU;
    }

    @Override
    public void render() {
        switch (currentState) {
            case MENU:
                drawMenu();
                handleMenuInput();
                break;
            case PLAYING:
                if (!gameOver) {
                    handleInput();
                    update(Gdx.graphics.getDeltaTime());
                    draw();
                } else {
                    currentState = GameState.GAME_OVER;
                }
                break;
            case GAME_OVER:
                drawGameOver();
                handleGameOverInput();
                break;
        }
    }

    private void drawMenu() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        font.setColor(Color.WHITE);
        font.getData().setScale(3);
        font.draw(batch, "Snake Game", Gdx.graphics.getWidth() / 2 - 90, Gdx.graphics.getHeight() / 2 + 40);
        font.getData().setScale(2);
        font.draw(batch, "Press ENTER to Start", Gdx.graphics.getWidth() / 2 - 110, Gdx.graphics.getHeight() / 2 - 10);
        batch.end();
    }

    private void handleMenuInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            startGame();
        }
    }

    private void startGame() {
        snake.clear();
        snake.add(new Coordinate(5, 5));
        placeApple();
        placeSpecialItem();
        snakeDirection = Input.Keys.RIGHT;
        timeSinceLastMove = 0f;
        snakeSpeed = 0.15f;
        score = 0;
        timeSinceLastSpecialItem = 0f;
        specialItemDuration = 10f;
        specialItemVisible = true;
        gameOver = false;
        obstacles = new Array<>();
        placeObstacles();
        timeSinceLastWallRespawn = 0f;
        wallsVisible = true;
        wallVisibilityTimer = 0f;
        currentState = GameState.PLAYING;
    }
    /*start */
    private void drawGameOver() {
        batch.begin();
        font.setColor(Color.RED);
        font.getData().setScale(2);
        font.draw(batch, "Game Over! Press ENTER to Restart", Gdx.graphics.getWidth() / 2 - 180, Gdx.graphics.getHeight() / 2);
        batch.end();
    }

    private void handleGameOverInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {  /* check user Enter */
            startGame(); /* ถ้ากดเรียกตัวนี้ เพื่อเริ่มเกมใหม่ */
        }
    }
    /*ส่วนของการควบคุมทิศทางของงู*/
    private void handleInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.UP) && snakeDirection != Input.Keys.DOWN) { /*เช็คว่าลูกศรถูกกดไหม  snakeDirection อัปเดตทิศทางของงู*/
            snakeDirection = Input.Keys.UP;
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN) && snakeDirection != Input.Keys.UP) {
            snakeDirection = Input.Keys.DOWN;
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT) && snakeDirection != Input.Keys.RIGHT) {
            snakeDirection = Input.Keys.LEFT;
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) && snakeDirection != Input.Keys.LEFT) {
            snakeDirection = Input.Keys.RIGHT;
        }
    }

    private void update(float deltaTime) { /*ตัวแปรที่เก็บเวลาที่ผ่านไปสำหรับเหตุการณ์ต่างๆ */
        timeSinceLastMove += deltaTime;        
        timeSinceLastSpecialItem += deltaTime;
        timeSinceLastWallRespawn += deltaTime;
        wallVisibilityTimer += deltaTime;

        if (timeSinceLastSpecialItem >= specialItemDuration) {  /*ถ้าเวลาที่ผ่านไปมากกว่าระยะเวลาที่กำหนดสำหรับ special item จะสร้าง special item ใหม่*/
            specialItem = null;
            placeSpecialItem();
            timeSinceLastSpecialItem = 0;
        } else if (timeSinceLastSpecialItem >= specialItemDuration - 5f) {
            specialItemVisible = ((int) (timeSinceLastSpecialItem * 10) % 2) == 0;
        } else {
            specialItemVisible = true;
        }

        if (timeSinceLastWallRespawn >= WALL_RESPAWN_INTERVAL) {  /*ถ้าเวลาที่ผ่านไปมากกว่าระยะเวลาที่กำหนดสำหรับการเกิดใหม่ของผนัง จะวางตำแหน่งผนังใหม่*/
            placeObstacles();
            timeSinceLastWallRespawn = 0f;
            wallVisibilityTimer = 0f;
            wallsVisible = true;
        }

        if (wallVisibilityTimer >= WALL_VISIBLE_DURATION) { /*ถ้าเวลาที่ผ่านไปมากกว่าระยะเวลาที่ผนังควรจะปรากฏ จะซ่อนผนัง*/
            wallsVisible = false;
        }

        if (timeSinceLastMove >= snakeSpeed) { /* ถ้าเวลาที่ผ่านไปมากกว่าความเร็วของงู จะเคลื่อนย้ายงูและรีเซ็ตเวลา  ส่วนของการเคลื่อนย้าย*/
            moveSnake();
            timeSinceLastMove = 0;
        }
    }

    private void moveSnake() {
        Coordinate head = snake.first();
        int newX = head.x;
        int newY = head.y;
      /* E */
        switch (snakeDirection) {
            case Input.Keys.UP:
                newY++;
                break;
            case Input.Keys.DOWN:
                newY--;
                break;
            case Input.Keys.LEFT:
                newX--;
                break;
            case Input.Keys.RIGHT:
                newX++;
                break;
        }

        if (newX < 0 || newX >= Gdx.graphics.getWidth() / GRID_CELL ||
                newY < 0 || newY >= Gdx.graphics.getHeight() / GRID_CELL) {
            gameOver = true;
            return;
        }

        for (Coordinate part : snake) {
            if (part.x == newX && part.y == newY) {
                gameOver = true;
                return;
            }
        }

        if (wallsVisible) {
            for (Coordinate obstacle : obstacles) {
                if (obstacle.x == newX && obstacle.y == newY) {
                    gameOver = true;
                    return;
                }
            }
        }

        if (newX == apple.x && newY == apple.y) {
            placeApple();
            score++;
        } else if (specialItem != null && newX == specialItem.x && newY == specialItem.y) {
            specialItem = null;
            snakeSpeed = Math.max(0.01f, snakeSpeed - 0.02f);
            if (snake.size > 1) {
                snake.removeIndex(snake.size - 1);
            }
            if (score > 0) {
                score--;
            }
        } else {
            snake.removeIndex(snake.size - 1);
        }

        snake.insert(0, new Coordinate(newX, newY));
    }

    private void draw() { /*ฟังก์ชั่น draw() ในคลาส SnakeGame เป็นส่วนที่ทำหน้าที่ในการวาดฉากเกมบนหน้าจอ*/
        Gdx.gl.glClearColor(0, 0, 0, 1); /*กำหนดสีพื้นหลังของหน้าจอเป็นสีดำ (glClearColor(0, 0, 0, 1)) และลบข้อมูลบนหน้าจอ (glClear(GL20.GL_COLOR_BUFFER_BIT)).*/
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        for (int i = 0; i < snake.size; i++) {
            Coordinate part = snake.get(i);
            if (i == 0) {
                batch.draw(snakeTexture, part.x * GRID_CELL, part.y * GRID_CELL, GRID_CELL, GRID_CELL);
            } else {
                batch.draw(bodyTexture, part.x * GRID_CELL, part.y * GRID_CELL, GRID_CELL, GRID_CELL);
            }
        }

        batch.draw(appleTexture, apple.x * GRID_CELL, apple.y * GRID_CELL, GRID_CELL, GRID_CELL);

        if (specialItem != null && specialItemVisible) {
            batch.draw(specialItemTexture, specialItem.x * GRID_CELL, specialItem.y * GRID_CELL, GRID_CELL, GRID_CELL);
        }

        if (wallsVisible) {
            for (Coordinate obstacle : obstacles) {
                batch.draw(wallTexture, obstacle.x * GRID_CELL, obstacle.y * GRID_CELL, GRID_CELL, GRID_CELL);
            }
        }

        font.setColor(1, 1, 1, 1);
        font.getData().setScale(2);
        font.draw(batch, "Score: " + score, 25, Gdx.graphics.getHeight() - 25);
        batch.end();
    }

    private void placeApple() {
        apple = new Coordinate((int) (Math.random() * (Gdx.graphics.getWidth() / GRID_CELL)),
                (int) (Math.random() * (Gdx.graphics.getHeight() / GRID_CELL)));
    }

    private void placeSpecialItem() {
        specialItem = new Coordinate((int) (Math.random() * (Gdx.graphics.getWidth() / GRID_CELL)),
                (int) (Math.random() * (Gdx.graphics.getHeight() / GRID_CELL)));
        timeSinceLastSpecialItem = 0;
    }

    private void placeObstacles() {
        obstacles.clear();
        int numObstacles = 5;
        for (int i = 0; i < numObstacles; i++) {
            Coordinate obstacle = new Coordinate((int) (Math.random() * (Gdx.graphics.getWidth() / GRID_CELL)),
                    (int) (Math.random() * (Gdx.graphics.getHeight() / GRID_CELL)));
            obstacles.add(obstacle);
        }
    }

    @Override
    public void dispose() { /*ใช้ dispose() เพื่อทำความสะอาดและปล่อยทรัพยากรหลังจากใช้เสร็จ*/
        bodyTexture.dispose();
        appleTexture.dispose();
        specialItemTexture.dispose();
        backgroundTexture.dispose();
        wallTexture.dispose();
        backgroundMusic.dispose();
        batch.dispose();
        font.dispose();
    }

    private static class Coordinate {
        int x, y;

        Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
