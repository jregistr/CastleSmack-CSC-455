package com.jeff.game.castlesmack.system;

import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.jeff.game.castlesmack.models.gameplay.Controller;
import com.jeff.game.castlesmack.models.gameplay.Player;
import com.jeff.game.castlesmack.models.items.Entity;
import com.jeff.game.castlesmack.models.items.Island;
import com.jeff.game.castlesmack.models.items.Projectile;
import com.jeff.game.castlesmack.util.constant.Constants;
import com.jeff.game.castlesmack.util.data.Pair;
import com.jeff.game.castlesmack.util.data.ThreadLocalRandom;
import com.jeff.game.castlesmack.util.data.UiInfo;

public class GameManager {
    private World world;
    private Controller[] controllers;
    private State state;
    private boolean switchedStates;
    private Array<Island> islands;
    private int player;
    private boolean shoot;
    private Controller controller;
    private Projectile projectile;

    public GameManager(World world, Controller c1, Controller c2, Array<Island> islands, Projectile projectile) {
        // Set the world
        this.world = world;
        // Set the players
        this.controllers = new Controller[2];
        controllers[0] = c1;
        controllers[1] = c2;
        // Set the initial game state
        this.state = State.BETWEEN_TURN;
        switchedStates = true;
        this.islands = islands;
        this.player = 0;
        this.shoot = true;
        this.controller = controllers[0];
        this.projectile = projectile;
    }

    public void checkCollisions(Array<Pair<Projectile, Entity>> projCollisions) {

    }

    public void update(float delta) {
        switch (state) {
            case BETWEEN_TURN:
                betweenTurn(delta);
                break;
            case IN_TURN:
                switchedStates = true;
                inTurn(delta);
                break;
        }
    }

    private void betweenTurn(float delta) {
        // Move the islands
        if (switchedStates) {
            for (Island island : islands) {
                island.setDestination(ThreadLocalRandom.nextInt((int) (island.height / 2), (int) (((int) Constants.HEIGHT_SCREEN) - ((island.height / 2) + (island.getEntity() != null ? island.getEntity().height : 0)))));
            }
            switchedStates = false;
        }

        // Check if the islands have reached their destination
        boolean nextTurn = true;
        for (Island island : islands) {
            if(!island.hasReachedDestination()) {
                nextTurn = false;
            }
        }

        // Switch to the next turn
        if(nextTurn) {
            state = state.getNextState();
        }
    }

    private void inTurn(float delta) {
        // Make sure the controller is up to date
        controller.update();
        // Move the cannon
        controller.player.cannon.rotate(controller.cannonMoveState);

        // Shoot
        if(controller.shoot && shoot) {
            shoot = false;
            System.out.println("SHOTS FIRED!");
            controller = nextController();
            if(controller == null) {
                state = state.getNextState();
            }
        } else {
            if(!controller.shoot) {
                shoot = true;
            }
        }
    }

    public void updateUiP1Info(UiInfo info) {
        updateUiPlayerInfo(info, controllers[0].player);
    }

    public void updateUiP2Info(UiInfo info) {
        updateUiPlayerInfo(info, controllers[1].player);
    }

    private void updateUiPlayerInfo(UiInfo info, Player player) {
        info.houseHp = player.house.currentHP;
        info.houseMaxHp = player.house.maxHP;
        info.housePos = player.house.body.getPosition();
        info.cannonHp = player.cannon.currentHP;
        info.cannonMaxHp = player.cannon.maxHP;
        info.cannonForce = player.cannon.currentForce;
        info.cannonMaxForce = player.cannon.maxForce;
        info.cannonPos = player.cannon.body.getPosition();
    }

    private Controller nextController() {
        int player = this.player + 1;
        if(player == controllers.length) {
            this.player = 0;
            return null;
        } else {
            this.player = player;
            return controllers[player];
        }
    }

    private enum State {
        BETWEEN_TURN,
        IN_TURN;

        public State getNextState() {
            int ordinal = this.ordinal() + 1;
            if (ordinal == values().length) {
                return values()[0];
            } else {
                return values()[ordinal];
            }
        }
    }
}
