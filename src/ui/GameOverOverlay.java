package ui;

import Gamestates.Gamestate;
import Gamestates.Playing;
import main.Game;
import utilz.LoadSave;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import static utilz.Constants.UI.URMBUTTON.*;

public class GameOverOverlay {
    private Playing playing;
    private BufferedImage img;
    private int imgX,imgY,imgW, imgH;
    private UrmButton menu, play;
    public GameOverOverlay(Playing playing)
    {
        this.playing = playing;
        createImg();
        createButtons();
    }
    private void createButtons(){
        int menuX = (int) (335 * Game.SCALE);
        int playX = (int) (440 * Game.SCALE);
        int y = (int) (195 * Game.SCALE);
        play = new UrmButton(playX, y, URM_SIZE, URM_SIZE, 0);
        menu = new UrmButton(menuX, y, URM_SIZE, URM_SIZE, 2);

    }
    private void createImg() {
        img = LoadSave.GetSpriteAtlas(LoadSave.DEATH_ATLAS);
        imgW = (int)(img.getWidth() * Game.SCALE);
        imgH = (int)(img.getHeight() * Game.SCALE);
        imgX = Game.GAME_WIDTH/2 - imgW/2;
        imgY = (int)(100*Game.SCALE);
    }
    public void draw(Graphics g)
    {
        g.setColor(new Color(0,0,0,200));
        g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);

        g.drawImage(img, imgX, imgY, imgW, imgH, null);
        menu.draw(g);
        play.draw(g);
        // g.setColor(Color.white);
        // g.drawString("Game Over", Game.GAME_WIDTH/2, 150);
        // g.drawString("press esc to enter the main menu", Game.GAME_WIDTH/2, 300);

    }
    public void update(){
        menu.update();
        play.update();
    }
    public void keyPressed(KeyEvent e)
    {

    }   
    private boolean isIn(UrmButton b, MouseEvent e) {
        return b.getBounds().contains(e.getX(), e.getY());
    }

    public void mouseMoved(MouseEvent e) {
        play.setMouseOver(false);
        menu.setMouseOver(false);

        if (isIn(menu, e)) {
            menu.setMouseOver(true);
        } else if (isIn(play, e)) {
            play.setMouseOver(true);
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (isIn(menu, e)) {
            if (menu.isMousePressed()){
                playing.resetAll();
                playing.setGameState(Gamestate.MENU);
            }
                // playing.resetAll();
                // Gamestate.state = Gamestate.MENU;
        } else if (isIn(play, e)) {
            if (play.isMousePressed()){
                playing.resetAll();
                playing.getGame().getAudioPlayer().setLevelSong(playing.getLevelManager().getLevelIndex());
            }
                // playing.loadNextLevel();
        }

        menu.resetBools();
        play.resetBools();
    }

    public void mousePressed(MouseEvent e) {
        if (isIn(menu, e)) {
            menu.setMousePressed(true);
        } else if (isIn(play, e)) {
            play.setMousePressed(true);
        }
    }
}
