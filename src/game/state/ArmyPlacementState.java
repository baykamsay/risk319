package game.state;

import game.GameEngine;
import game.GameMap;
import game.SaveManager;
import game.SoundEngine;
import game.player.Objective;
import game.player.Player;
import game.player.Territory;
import game.player.faculties.Fen;
import game.scene.MapScene;
import javafx.event.ActionEvent;

public class ArmyPlacementState implements GameState {
    private static ArmyPlacementState instance;
    private GameEngine engine;
    private int addibleArmyNo;
    private int armyCount; //add amount per territory
    private Territory target;
    private boolean manAbilityUsed, eduAbilityUsed;
    private ArmyPlacementState() {

    }

    public static ArmyPlacementState getInstance() {
        if (instance == null) {
            synchronized (ArmyPlacementState.class) {
                if (instance == null) {
                    instance = new ArmyPlacementState();
                }
            }
        }
        return instance;
    }

    public void setArmyCount(int count){
        this.armyCount = count;
        target.setNumOfArmies(target.getNumOfArmies() + armyCount);
        addibleArmyNo = addibleArmyNo - armyCount;
        GameEngine.getInstance().getController().updateTroopNumber(addibleArmyNo);
        if (addibleArmyNo <= 0) {
            engine.switchState(AttackingState.getInstance());
        }
    }

    @Override
    public void mapSelect(int territory) {
        Territory t = engine.getMap().getTerritory(territory);
        if (manAbilityUsed && engine.getCurrentPlayer() != t.getRuler()) {
            Player p = t.getRuler();
            manAbilityUsed = false;
            t.setRuler(engine.getCurrentPlayer());
            p.setNumOfTerritory(p.getNumOfTerritory()-1);
            t.getRuler().increaseTerritory();
            if(engine.isEliminated(p)){
                engine.removePlayer(p);
            }
            engine.isGameOver();
            engine.switchState(AttackingState.getInstance());
        }
        else if (engine.getCurrentPlayer() == t.getRuler()) {
            // Call display troop selection, it will set the army no.
            engine.getController().displayTroopSelector(addibleArmyNo);
            target = t;
        }
    }

    @Override
    public void start() {

        // If players don't have objectives, generate
        for(Player p: GameEngine.getInstance().getPlayers()) {
            if (p.getObjective() == null) {
                p.setObjective(Objective.generateObjective(p));
            }
        }

        // Save the game
        try {
            SaveManager.getInstance().saveGame(GameEngine.getInstance().getSaveSlot());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to save");
        }

        manAbilityUsed = false;
        eduAbilityUsed = false;
        engine = GameEngine.getInstance();
        engine.getController().setState(0);
        calculateNumberOfArmies(engine.getCurrentPlayer());
    }

    public void calculateNumberOfArmies(Player p) {

        if (p.getNumOfTerritory() <= 9) {
            addibleArmyNo = 3;
        } else {
            addibleArmyNo = p.getNumOfTerritory() / 3;
        }

        if( GameMap.getInstance().getAreas()[0].getRuler() != null){ //east campus area = +3
            if(p.equals(GameMap.getInstance().getAreas()[0].getRuler()))
                addibleArmyNo += 3;
        }
        if( GameMap.getInstance().getAreas()[1].getRuler() != null){ //island area = +2
            if(p.equals(GameMap.getInstance().getAreas()[1].getRuler()))
                addibleArmyNo += 2;
        }
        if( GameMap.getInstance().getAreas()[2].getRuler() != null){ //upper main campus area = +5
            if(p.equals(GameMap.getInstance().getAreas()[2].getRuler()))
                addibleArmyNo += 5;
        }
        if( GameMap.getInstance().getAreas()[3].getRuler() != null){ //lower main campus area = +5
            if(p.equals(GameMap.getInstance().getAreas()[3].getRuler()))
                addibleArmyNo += 5;
        }
        int objectiveResult = p.getObjective().isDone();
        if(objectiveResult == -1){
            SoundEngine.getInstance().objectiveFailed();
            GameEngine.getInstance().getController().displayObjectiveFail();
            p.setObjective(Objective.generateObjective(p));
        } else if (objectiveResult == 1) {
                SoundEngine.getInstance().objectiveCompleted();
                addibleArmyNo += p.getObjective().getBonus(); //bonus army for a completed objective
                GameEngine.getInstance().getController().displayObjectiveSuccess(p.getObjective().getBonus());
                p.setObjective(Objective.generateObjective(p));
        }
        GameEngine.getInstance().getController().updateTroopNumber(addibleArmyNo);
    }

    public int getAddibleArmyNo(){
        return addibleArmyNo;
    }

    public void setManAbilityCanUseTrue() { manAbilityUsed = true; }
    public void setEduAbilityCanUseTrue() { eduAbilityUsed = true; }
}
