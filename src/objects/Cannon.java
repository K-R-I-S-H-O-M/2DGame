package objects;


import main.Game;
public class Cannon extends GameObject {
    
    private int tileY;
    public Cannon(int x, int y, int objType) {
        super(x, y, objType);
        initHitbox(40, 26);
        tileY= y/Game.TILES_SIZE;
        hitbox.x -= (int)(4*Game.SCALE);
        hitbox.y += (int)(6*Game.SCALE);

    }

    public void update(){
        if(doAnimation) 
            updateAnimationTick();
    }
    public int getTileY(){
        return tileY;
    }
}
