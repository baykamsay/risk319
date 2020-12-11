package game.state;

import game.GameEngine;
import game.player.Player;
import game.player.Territory;
import game.scene.MapScene;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import menu.GameMenuManager;
import java.util.ArrayList;

public class InitialArmyPlacementState implements GameState {

    private GameEngine engine;
    private int currentPlayer;

    public InitialArmyPlacementState(GameEngine engine) {
        this.engine = engine;
        currentPlayer = 0;
    }

    // When player selects a map territory
    public void mapSelect(ActionEvent e) {
        Territory[] territories = engine.getMap().getTerritories();
        ArrayList<Player> players = engine.getPlayers();
        for (Territory territory : territories) {
            // getName() can also be getId() if ids are implemented and the other part requires something to compare
            // when the map is implemented fix this
            // && territory.getRuler() == null this can also be checked but if it is not null disabling the button is
            // better
            if (territory.getName() == e.getSource().toString()) {
                territory.setRuler(players.get(currentPlayer));
            }
        }
        checkIfStateOver();
        currentPlayer++;
    }

    public void checkIfStateOver() {
        Territory[] territories = engine.getMap().getTerritories();
        boolean stateOver = true;
        for (Territory territory : territories) {
            if (territory.getRuler() == null) {
                stateOver = false;
            }
        }
        if (stateOver) {
            // fix
            engine.switchState(new ArmyPlacementState(engine));
        }
    }
}
