package edu.siren.game.battle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.lwjgl.Sys;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import edu.siren.core.geom.Rectangle;
import edu.siren.core.tile.Layer;
import edu.siren.core.tile.Tile;
import edu.siren.core.tile.World;
import edu.siren.game.Player;
import edu.siren.game.gui.AcquireUnits;
import edu.siren.game.gui.BattleScreen;
import edu.siren.game.gui.PostGameStats;
import edu.siren.renderer.Perspective2D;
import edu.siren.renderer.Shader;

public class BattleManager {
    public Team red, redFull, blue, blueFull;
    public Team active, other;
    public boolean mouseDown = false;
    public BattleScreen battleScreen = null;
    public World world = null;
    public World battleWorld = null;
    public Layer filler = new Layer("fill");
    public int lastTileR = -1, lastTileC = -1;
    public boolean replaceTile = false;
    public int expGained = 0;
    
    public BattleManager(World world, Team red, Team blue) 
            throws IOException 
    {
        redFull = new Team();
        blueFull = new Team();
        for (Player member : red.players) {
            redFull.players.add(member);
        }
        
        for (Player member : blue.players) {
            blueFull.players.add(member);
        }
        
        battleScreen = new BattleScreen(this);
        this.battleWorld = world.battleWorld;
        this.world = world;
        filler.priority = 1000;
        this.battleWorld.addLayer(filler);
        this.red = red;
        this.blue = blue;
        this.active = blue;
        next();
    }
    
    /**
     * Switch to the next team
     */
    public Team next() {
        // Reset the number of moves each player can make for the next
        // frame of the game.
        for (Player player : active.players) {
            player.moves = player.maxMoves;
            player.drawPossibleMoveOverlay = false;
        }
        
        other = active;
        active = (active.equals(red) ? blue : red);
        
        for (Player member : active.players) {
            member.moves = member.maxMoves;
            member.drawPossibleMoveOverlay = true;
        }
        return active;
    }
    
    /**
     * Attempt to select a team member at x, y
     */
    public Player select(Team team, int x, int y) {
        Player returned = null;
        for (Player player : team.players) {
            if (player.moves > 0 && player.getRect().contains(x, y)) {
                returned = player;
                break;
            }
        }
        return returned;
    }
    
    /**
     * Move a player to a grid unit
     */
    public boolean actionMove(Player player, int x, int y) {
        if (player.inMovement || player.moves <= 0) return false;
        
        boolean valid = false;
        for (Player member : active.players) {
            if (member.equals(player)) {
                valid = true;
                break;
            }
        }
        
        if (!valid) return false;
        
        // Figure out the row / col selected
        int r = (int) (x / player.gridWidth);
        int c = (int) (y / player.gridHeight);
        int pr = (int) (player.sprite.spriteX / player.gridWidth);
        int pc = (int) (player.sprite.spriteY / player.gridHeight);
        
        for (Rectangle tile : filler.solids) {
            if (tile.contains(x, y)) {
                return false;
            }
        }
        
        for (Player member : other.players) {
            Rectangle rb = member.getRect().clone();
            rb.x = (rb.x / 32) * 32;
            rb.y = (rb.y / 32) * 32;
            rb.width = 32;
            rb.height = 32;
            if (rb.contains(x, y)) {
                return false;
            }
        }
        
        int dr = Math.abs(r - pr);
        int dy = Math.abs(c - pc);
        if ((dr + dy) > player.moves)
            return false;
        player.moves -= (dr + dy);
        player.moveTo(r * 32, c * 32);
        player.inMovement = true;
        player.possibleMoveOverlay.clear();
        return true;
    }
    
    public boolean actionAttack(Player player, int x, int y) {
        if (player.inMovement || player.moves <= 0) return false;
        
        boolean valid = false;
        for (Player member : active.players) {
            if (member.equals(player)) {
                valid = true;
                break;
            }
        }
        
        if (!valid) return false;
        
        // Figure out the row / col selected
        int r = (int) (x / player.gridWidth);
        int c = (int) (y / player.gridHeight);
        int pr = (int) (player.sprite.spriteX / player.gridWidth);
        int pc = (int) (player.sprite.spriteY / player.gridHeight);
        
        for (Rectangle tile : filler.solids) {
            if (tile.contains(x, y)) {
                return false;
            }
        }
        
        int dr = Math.abs(r - pr);
        int dy = Math.abs(c - pc);
        if ((dr + dy) > player.moves)
            return false;
        
        // Attacks a player
        boolean hitPlayer = false;
        for (Player member : other.players) {
            Rectangle rb = member.getRect().clone();
            rb.x = (rb.x / 32) * 32;
            rb.y = (rb.y / 32) * 32;
            rb.width = 32;
            rb.height = 32;
            int rbr = (int) (rb.x / 32);
            int rbc = (int) (rb.y / 32);
            if (rb.contains(x, y)) {
                member.health -= player.attack;
                if (rbr < pr) {
                    if (rbc > pc) {
                        player.moveTo((r + 1) * 32, (c - 1) * 32);
                    } else if (rbc < c) {
                        player.moveTo((r + 1) * 32, (c + 1) * 32);
                    } else {
                        player.moveTo((r + 1) * 32, (c) * 32);
                    }
                } else if (rbr > pr) {
                    if (rbc > pc) {
                        player.moveTo((r - 1) * 32, (c - 1) * 32);
                    } else if (rbc < pc) {
                        player.moveTo((r - 1) * 32, (c + 1) * 32);
                    } else {
                        player.moveTo((r - 1) * 32, (c) * 32);
                    }
                } else {
                    if (rbc > pc) {
                        player.moveTo((r) * 32, (c - 1) * 32);
                    } else if (rbc < c) {
                        player.moveTo((r) * 32, (c + 1) * 32);
                    }
                }
                hitPlayer = true;
                break;
            }
        }
        
        if (hitPlayer) {
            player.moves -= (dr + dy);
            player.possibleMoveOverlay.clear();
            return true;
        }
        
        // Attacks a tile
        try {
            if (r == lastTileR && c == lastTileC && replaceTile) {
                player.moves -= (dr + dy);
                player.moveTo((r - 1) * 32, (c) * 32);
                player.possibleMoveOverlay.clear();
                Tile tile = new Tile("res/game/gui/black.png", r * 32, c * 32, 32, 32, true);
                tile.solid = true;
                filler.addTile(tile);
                replaceTile = false;
                battleScreen.overlayTile.background("res/game/gui/attack-tile.png");
            } else if ((r != lastTileR || c != lastTileC) && replaceTile) {
                replaceTile = false;
                System.out.println("attack tile fail #2");
                battleScreen.overlayTile.background("res/game/gui/attack-tile.png");
            } else if (!replaceTile) {
                lastTileR = r;
                lastTileC = c;
                replaceTile = true;
                System.out.println("attack tile verify");
                battleScreen.overlayTile.background("res/game/gui/attack-tile-verify.png");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return true;
    }
    
    public void think() {
        // Get the state of the mouse and see if we are clicking anywhere
        float x = Mouse.getX();
        float y = Mouse.getY();
        boolean click = Mouse.isButtonDown(0);
        Random random = new Random();
        
        for (int i = 0; i < red.players.size(); i++) {
            Player member = red.players.get(i);

            if (member.health <= 0) {
                member.remove();
                red.players.remove(i--);
                continue;
            }
        }
        
        if (red.players.size() <= 0) {
            System.out.println("Game is over");
            close();
        }
        
        for (int i = 0; i < blue.players.size(); i++) {
            Player member = blue.players.get(i);

            if (member.health <= 0) {
                member.remove();
                blue.players.remove(i--);
                continue;
            }
        }
        
        if (blue.players.size() <= 0) {
            System.out.println("Game is over");
            close();
        }
        
        boolean valid = false;
        for (int i = 0; i < active.players.size(); i++) {
            Player member = active.players.get(i);

            if (member.health <= 0) {
                member.remove();
                active.players.remove(i--);
                continue;
            }
            
            if (member.moves > 0) {
                valid = true;
                if (member.hasAI() && member.possibleMoveOverlay.size() > 0) {
                    int n = random.nextInt(member.possibleMoveOverlay.size());
                    boolean doBreak = false;
                    for (int k = 0; k < member.possibleMoveOverlay.size(); k++) {
                        Tile tile = member.possibleMoveOverlay.get(k);
                        for (Player player : red.players) {
                            Rectangle rect = player.getRect();
                            if (tile.contains(player.x, player.y)) {
                                actionAttack(member, (int)rect.x, (int) rect.y);
                                doBreak = true;
                                break;
                            }
                        }
                    }
                    
                    if (doBreak)
                        break;
                    
                    Rectangle r = member.possibleMoveOverlay.get(n).bounds;
                    actionMove(member, (int) r.x, (int) r.y);
                }
                break;
            }
        }
        
        if (active.players.size() <= 0) {
            System.out.println("Game is over");
            close();
        }
        
        if (battleScreen.activePlayer != null && battleScreen.activePlayer.moves == 0)
            battleScreen.clear();
        
        if (!valid) {
            battleScreen.clear();
            next();
            return;
        }
        
        
        battleScreen.run();
        
        if (!click || mouseDown) {
            if (mouseDown && !click)
                mouseDown = false;
            return;
        }
        
        for (Player member : active.players) {
            if (member.getRect().contains(x, y)) {
                showPossibleActions(member);
                break;
            }
        }
    }

    public void showPossibleActions(Player member) {
        battleScreen.showPossibleActions(member);
    }
    
    private double getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }

    public void close() {
        battleWorld.cleanup();
        world.battleManager = null;
        world.battleWorld = null;
        world.getCamera().zoomIn();
        
        for (Thread thread : world.musicThreads) {
            thread.interrupt();
        }
        
        try {
            PostGameStats stats = new PostGameStats(this);
            while (stats.running()) {
                stats.run();
            }
            AcquireUnits units = new AcquireUnits(this);
            while (units.running()) {
                units.run();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        
        for (Player player : blueFull.players) {
            player.world = world;
            player.remove();
        }
        
        for (Player player : redFull.players) {
            player.world = world;
            player.controllable = true;
            player.collisionDetection = true;
            player.setPosition(player.px, player.py);
            player.snap = false;
            player.lastMovement = 2;
            player.follow = false;
            player.drawStatus = false;
            player.controllable = true;
            player.bindCamera(world.getCamera());
        }
        
        red.players.get(0).follow = true;
        
        Perspective2D gui = new Perspective2D();
        Shader shader = null;
        try {
            shader = new Shader("res/tests/glsl/2d-perspective.vert",
                    "res/tests/glsl/2d-perspective.frag");
        } catch (IOException e) {
            e.printStackTrace();
        }
        gui.bindToShader(shader);
        
        // Draw the battle sequence
        ArrayList<Tile> tiles = new ArrayList<Tile>();
        for (int i = 0; i < Display.getWidth(); i += 96) {
            for (int j = 0; j < Display.getHeight(); j+= 96) {
                Tile tile;
                try {
                    tile = new Tile("res/game/gui/black.png", i, j, 96, 96);
                    tiles.add(tile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        double ftime = getTime();
        int drawTiles = 0;
        
        while (drawTiles < tiles.size()) {
            double ctime = getTime();
            if ((ctime - ftime) > 5) {
                drawTiles++;
                ftime = ctime;
            }
            
            shader.use();
            for (int i = 0; i < drawTiles; i++) {
                tiles.get(i).draw();
            }
            shader.release();
            Display.update();
        }
        
        if (red.players.size() == 0) {
            world.gameOver = true;
        }
    }

    
}
