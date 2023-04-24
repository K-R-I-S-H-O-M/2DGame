package ui;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import utilz.LoadSave;
import static utilz.Constants.UI.URMBUTTON.*;
public class UrmButton extends PauseButtons {
    private BufferedImage[] imgs;
    private int rowIndex,index;
    private boolean mouseOver, mousePressed;
    public UrmButton(int x, int y, int width, int height, int rowIndex){
        super(x, y, width, height);  
        this.rowIndex = rowIndex;
        loadImgs();
    }
    public void loadImgs(){
        BufferedImage temp = LoadSave.GetSpriteAtlas(LoadSave.URM_BUTTON);
        imgs = new BufferedImage[3];
        for(int i =0; i<imgs.length; i++){
            imgs[i] = temp.getSubimage(i*URM__DEFAULT_SIZE, rowIndex*URM__DEFAULT_SIZE, URM__DEFAULT_SIZE, URM__DEFAULT_SIZE);
        }
    }
    public void update(){
        index =0;
        if(mouseOver)
        {
            index = 1;
        }
        if(mousePressed)
        {
            index = 2;
        }
    }
    public void draw(Graphics g)
    {
        g.drawImage(imgs[index], x, y, URM_SIZE, URM_SIZE, null );
    }
    public void resetBools(){
        mouseOver = false;
        mousePressed = false;
    }
    public boolean isMouseOver() {
        return mouseOver;
    }
    public void setMouseOver(boolean mouseOver) {
        this.mouseOver = mouseOver;
    }
    public boolean isMousePressed() {
        return mousePressed;
    }
    public void setMousePressed(boolean mousePressed) {
        this.mousePressed = mousePressed;
    }
    
}
