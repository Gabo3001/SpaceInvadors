/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tutorial1;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.util.LinkedList;

/**
 *
 * @author HOME
 */
public class Game implements Runnable {

    private BufferStrategy bs;
    private Graphics g;
    private Display display;
    String title;
    private int width;
    private int height;
    private Thread thread;
    private boolean running;
    private Player player;
    private Bullet bullets;
    private KeyManager keyManager;
    private int contBullet;      //counter for all the bullets
    private boolean bordes;
    private LinkedList<Alien> aliens;

    public Game(String title, int width, int height) {
        this.title = title;
        this.width = width;
        this.height = height;
        running = false;
        keyManager = new KeyManager();
        contBullet = 0;
        bordes = false;
        aliens = new LinkedList<Alien>();
    }

    public void setBordes(boolean bordes) {
        this.bordes = bordes;
    }

    public boolean isBordes() {
        return bordes;
    }

    public void setContBullet(int contBullet) {
        this.contBullet = contBullet;
    }

    public int getContBullet() {
        return contBullet;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    private void init() {
        display = new Display(title, getWidth(), getHeight());
        Assets.init();
        player = new Player(380, getHeight() - 120, 1, 50, 50, this);
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 4; j++) {
                aliens.add(new Alien(i * 50, (j * 40)+60, 1, 30, 30, this, j));
            }
        }
        bullets = new Bullet(getWidth() + 10, -10, 1, 10, 20, this);
        display.getJframe().addKeyListener(keyManager);
    }

    @Override
    public void run() {
        init();
        //frames per seconds
        int fps = 60;
        //time for each tick in nano seconds
        double timeTick = 1000000000 / fps;
        // inizialing delta
        double delta = 0;
        // define now to use inside the loop
        long now;
        // initializing last time to the computer time in nanosecs
        long lastTime = System.nanoTime();
        while (running) {
            // setting the time now to the actual time
            now = System.nanoTime();
            // acumulating to delta the difference between times in timeTick units
            delta += (now - lastTime) / timeTick;
            //updating the last time
            lastTime = now;

            // if delta is positive we tick the game
            if (delta >= 1) {
                tick();
                render();
                delta--;
            }
        }
        stop();
    }

    public KeyManager getKeyManager() {
        return keyManager;
    }

    private void tick() {
        keyManager.tick();
        // avanceing player with colision
        player.tick();
        bullets.tick();
        for (int i = 0; i < aliens.size(); i++) {
            Alien alien = aliens.get(i);
            alien.tick();

            if (alien.getX() + 30 >= getWidth() - 10 && alien.getDirection() == 1) {
                setBordes(true);
            }
            else if(alien.getX() <= 10 && alien.getDirection() == 2){
                setBordes(true);
            }
            
            if(bullets.intersecta(alien)){
                alien.setY(-30);
                alien.setVisible(false);
                bullets.setVisible(false);
                bullets.setY(-30);
            }
        }
        if(isBordes()){
            for (int i = 0; i < aliens.size(); i++) {
                Alien alien = aliens.get(i);
                alien.setDirection(3);
            }
            setBordes(false);
        }
        //When the space key is press
        if (getKeyManager().space) {
            //kStop is called to put space in false
            getKeyManager().kStop();
            if (!bullets.isVisible()) {
                //A bullet is set infront of the ship
                bullets.setVisible(true);
                bullets.setX(player.getX() + 20);
                bullets.setY(player.getY() - 5);
                //kStop is called to put space in false
                getKeyManager().kStop();
            }
        }

    }

    private void render() {
        bs = display.getCanvas().getBufferStrategy();

        if (bs == null) {
            display.getCanvas().createBufferStrategy(3);
        } else {
            g = bs.getDrawGraphics();
            g.drawImage(Assets.background, 0, 0, width, height, null);
            player.render(g);
            for (int i = 0; i < aliens.size(); i++) {
                Alien alien = aliens.get(i);
                alien.render(g);
            }
            bullets.render(g);
            bs.show();
            g.dispose();
        }
    }

    public synchronized void start() {
        if (!running) {
            running = true;
            thread = new Thread(this);
            thread.start();
        }
    }

    public synchronized void stop() {
        if (running) {
            running = false;
            try {
                thread.join();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }
}
