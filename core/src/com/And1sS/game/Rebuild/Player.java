package com.And1sS.game.Rebuild;

import com.And1sS.game.Rebuild.GameObjects.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Rectangle;

public class Player extends GameObject {

    public enum PlayerType {
        MARIO, SUPER_MARIO, FIRE_MARIO, INVINCIBLE_MARIO
    }

    private PlayerType playerType = PlayerType.MARIO;

    private int score = 0;
    private int lives = 3;

    private int defaultMarioHeight;

    private boolean alive = true;

    public Player(float x, float y, float width, float height) {
        bounds = new Rectangle(x, y, width, height);
        this.x = x;
        this.y = y;

        defaultMarioHeight = (int) height;
        defaultMario();

        updater = new PlayerUpdater();
        animationUpdater = new PlayerAnimationUpdater();
        levelCollisionDetector = new PlayerCollider();
        renderer = new PlayerRenderer();
    }

    public void die() {
        velocityY = -25 * defaultMarioHeight;

        alive = false;
    }

    public void resetPowerUps() {
        if (playerType == PlayerType.MARIO) {
            die();
        } else {
            invincibleMario();
        }
    }

    public void moveLeft() {
        velocityX = -7 * defaultMarioHeight * Gdx.graphics.getWidth() / 1920.0f;
    }

    public void moveRight() {
        velocityX = 7 * defaultMarioHeight * Gdx.graphics.getWidth() / 1920.0f;
    }

    public void jump() {
        velocityY = -20 * defaultMarioHeight;
    }

    public void smallJump() {
        velocityY = -15 * defaultMarioHeight;
    }

    public void invincibleMario() {
        defaultMario();
        playerType = PlayerType.INVINCIBLE_MARIO;
    }

    public void defaultMario() {
        playerType = PlayerType.MARIO;

        animation = new Animation("images/mario.png", 3, 81, 32, 16, 16, 0, true);
        if (bounds.getHeight() == 2 * defaultMarioHeight) {
            y += defaultMarioHeight;
        }
        bounds.setY((float) y);
        bounds.setHeight(defaultMarioHeight);
    }

    public void superMario() {
        if(playerType == PlayerType.MARIO) {
            playerType = PlayerType.SUPER_MARIO;

            animation.setTextureY(0);
            animation.setTextureHeight(31);

            velocityX = 0;
            bounds.setHeight(2 * defaultMarioHeight);
            y -= defaultMarioHeight;
            bounds.setY((float) y);

            score += 100;
        }
    }

    public PlayerType getType() { return playerType; }

    public Rectangle getBounds() { return bounds; }

    public boolean isAlive() { return alive; }

    private class PlayerUpdater implements IUpdatable {

        float invincibilityTimer = 3;

        @Override
        public void update(float deltaTime, Level level) {
           if (alive) {
               if (velocityX > 0) {
                   flipAnimationXAxis = false;
               } else if (velocityX < 0) {
                   flipAnimationXAxis = true;
               }

               if (deltaTime < 0.025) {
                   //updating velocityY
                   velocityY += Level.GRAVITATIONAL_ACCELERATION * deltaTime;

                   //updating velocityX;
                   if (velocityX > 100) {
                       velocityX -= 3000 * deltaTime;
                   } else if (velocityX < -100) {
                       velocityX += 3000 * deltaTime;
                   }

                   //updating X
                   x += velocityX * deltaTime;
                   bounds.setX((float) x);
                   performLevelCollisionDetectionX(level);

                   // updating X offset for level
                   if(x <= offsetX) {
                       x = offsetX;
                       bounds.setX((float) x);
                       velocityX = 0;
                   }

                   //updating Y
                   y += velocityY * deltaTime;
                   bounds.setY((float) y);
                   performLevelCollisionDetectionY(level);

                   if (Math.abs(velocityX) < 100)
                       velocityX = 0;
               }

               //offset
               float screenCenterX = Gdx.graphics.getWidth() / 2;
               float levelWidth = level.getWidth() * level.getCellSize();
               if (x < levelWidth - screenCenterX && x > offsetX + screenCenterX
                       && velocityX > 0) {
                   offsetX = x - screenCenterX;
               }

               if (playerType == PlayerType.INVINCIBLE_MARIO) {
                   updateInvincibilityTimer();
               }
           } else {
               velocityY += Level.GRAVITATIONAL_ACCELERATION * deltaTime;
               y += velocityY * deltaTime;
               bounds.setY((float) y);
           }
        }

        private void updateInvincibilityTimer() {
            invincibilityTimer -= Gdx.graphics.getDeltaTime();

            if(invincibilityTimer <= 0) {
                invincibilityTimer = 3;

                defaultMario();
            }
        }
    }

    private class PlayerCollider implements ILevelCollidable {
        @Override
        public void performCollisionDetectionX(Level level) {
            if (alive) {
                try {
                    _performCollisionDetectionX(level);
                } catch(Exception e) {}
            }
        }

        private void _performCollisionDetectionX(Level level) {
            int cellSize = level.getCellSize();
            for (int i = (int) (y) / cellSize; i < (y + bounds.getHeight()) / cellSize; i++) {
                for (int j = (int) (x) / cellSize; j < (x + bounds.getWidth()) / cellSize; j++) {
                    switch(level.getCell(j, i)) {
                        case TileId.TRANSPARENT_COLLIDABLE_BLOCK: case 10: case 21:
                        case 12: case 14: case 15:
                        case TileId.SECRET_BLOCK_EMPTY: case 22: case 23:
                            if (velocityX > 0) {
                                x = j * cellSize - bounds.getWidth() - 0.001;
                                velocityX = 0;
                                bounds.setX((float) x);
                            } else if (velocityX < 0) {
                                x = (j + 1) * cellSize;
                                velocityX = 0;
                                bounds.setX((float) x);
                            }
                            break;

                        default:
                            break;
                    }
                }
            }
        }

        @Override
        public void performCollisionDetectionY(Level level) {
            if(alive) {
                try {
                    _performCollisionDetectionY(level);
                } catch(Exception e) {}
            }
        }

        private void _performCollisionDetectionY(Level level) {
            int cellSize = level.getCellSize();

            for (int i = (int) (y) / cellSize; i < (y + bounds.getHeight()) / cellSize; i++) {
                for (int j = (int) (x) / cellSize; j < (x + bounds.getWidth()) / cellSize; j++) {
                    int cellType = level.getCell(j, i);
                    switch(cellType) {
                        case 1: case 10:
                        case 12:
                            defaultCollisionY(i, cellSize);
                            break;

                        case TileId.BROWN_IRON_BLOCK:
                            defaultCollisionY(i, cellSize);
                            break;

                        case TileId.BROWN_BRICK:
                            defaultCollisionY(i, cellSize);
                            if (y >= (i + 1) * cellSize) {

                                switch (playerType) {
                                    case INVINCIBLE_MARIO:
                                    case MARIO:
                                        level.setCell(j, i, TileId.TRANSPARENT_COLLIDABLE_BLOCK);
                                        level.addObject(new BouncingBrownBrick(j, i, cellSize));
                                        break;
                                    case FIRE_MARIO:
                                    case SUPER_MARIO:
                                        level.setCell(j, i, TileId.TRANSPARENT_NOT_COLLIDABLE_BLOCK);
                                        level.addObject(new BrokenBrick(j, i, cellSize));
                                        break;

                                }
                            }
                            break;

                        case TileId.SECRET_BLOCK_EMPTY:
                            defaultCollisionY(i, cellSize);
                            if (y >= (i + 1) * cellSize) {
                                score += 100;
                                level.setCell(j, i, 0);
                                level.addObject(new Coin(j, i, cellSize));
                                level.addObject(new BouncingBrownIronBlock(j, i, cellSize));
                            }
                            break;

                        case TileId.SECRET_BLOCK_POWERUP_SUPERMARIO:
                            defaultCollisionY(i, cellSize);
                            if (y >= (i + 1) * cellSize) {
                                level.setCell(j, i, TileId.TRANSPARENT_COLLIDABLE_BLOCK);
                                level.addObject(new BouncingBrownIronBlock(j, i, cellSize));
                                level.addObject(new Mushroom(j, i - 1, cellSize));
                            }
                            break;

                        case 15: case 23:
                            defaultCollisionY(i, cellSize);
                            break;


                        default:
                            break;
                    }
                }
            }
        }

        private void defaultCollisionY(int i, float cellSize) {
            if(velocityY > 0) {
                y = i * cellSize - bounds.getHeight();
                velocityY = 0;
                bounds.setY((float) y);
            } else if (velocityY < 0) {
                y = (i + 1) * cellSize;
                velocityY = 0;
                bounds.setY((float) y);
            }
        }
    }

    private class PlayerRenderer implements IRenderable {

        @Override
        public void render(SpriteBatch spriteBatch) {
            _render(spriteBatch);

            if(playerType == PlayerType.INVINCIBLE_MARIO) {
                ShaderProgram shader = new ShaderProgram(PlayerShaders.VERTEX_SHADER, PlayerShaders.FRAGMENT_SHADER);
                spriteBatch.setShader(shader);

                _render(spriteBatch);
                spriteBatch.setShader(null);
            }
        }

        private void _render(SpriteBatch spriteBatch) {
            float windowHeight = Gdx.graphics.getHeight();
            if (flipAnimationXAxis) {
                spriteBatch.draw(animation.getCurrentRegion(),
                        (float) (x + bounds.getWidth() -  offsetX),
                        (float) (windowHeight - y - bounds.getHeight()),
                        -bounds.getWidth() * 1.1f, bounds.getHeight());
            } else {
                spriteBatch.draw(animation.getCurrentRegion(),
                        (float) (x - offsetX),
                        (float) (windowHeight - y - bounds.getHeight()),
                        bounds.getWidth() * 1.1f, bounds.getHeight());
            }
        }
    }

    private class PlayerAnimationUpdater implements IAnimationUpdatable {

        @Override
        public void updateAnimation(float deltaTime) {
            manageAnimation();
            animation.update(deltaTime);
        }

        private void manageAnimation() {
            if(!alive) {                                            // Died
                animation.setTextureX(176);
                animation.setSpeed(0);
                animation.setCurrentFrame(0);
            } else if (Math.abs(velocityY) > 0.001) {              // flying
                animation.setTextureX(160);
                animation.setSpeed(0);
                animation.setCurrentFrame(0);
            } else if (Math.abs(velocityX + velocityY) <= 0.001) { // Staying
                animation.setTextureX(80);
                animation.setSpeed(0);
                animation.setCurrentFrame(0);
            } else if(Math.abs(velocityX) > 0.001 && Math.abs(velocityY) <= 10) { // Running
                animation.setTextureX(96);
                animation.setSpeed(7);
            }
        }
    }
}
