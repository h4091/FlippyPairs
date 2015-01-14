package org.faudroids.distributedmemory.core;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by Simon Hofmann on 13.01.15.
 */
public class GameManager {

    private ArrayList<Card> cardStack = new ArrayList<>();
    private ArrayList<Player> players = new ArrayList<>();
    private int openPairs;
    private int currentPlayer;
    private int winner;
    private Random rand;

    private enum returnValue {
        SUCCESS, FAILURE, ERROR
    }

    public enum gameStatus {
        FINISHED, RUNNING
    }

    //Set up a new game with given amount of pairs
    public GameManager(int pairsCount, int playerCount)
    {
        this.rand = new Random();
        for(int id = 0; id < pairsCount*2; id+=2) {
            int randomValue = rand.nextInt(pairsCount);
            this.cardStack.add(new Card(id, randomValue));
            Log.d("Init", "Value " + id + " : " + this.cardStack.get(id).getValue());
            this.cardStack.add(new Card(id + 1, randomValue));
            Log.d("Init", "Value " + (id+1) + " : " + this.cardStack.get(id+1).getValue());
        }
        for(int id = 0; id < playerCount; ++id) {
            this.players.add(new Player(id, "asdf"));
        }

        this.currentPlayer = 0;
        this.openPairs = pairsCount;
    }

    public returnValue matchPairs(int first, int second) {
        try {
            if (!this.cardStack.get(first).isClosed() && !this.cardStack.get(second).isClosed()) {
                if (this.cardStack.get(first).getValue() == this.cardStack.get(second).getValue()) {
                    this.cardStack.get(first).setClosed(true);
                    this.cardStack.get(second).setClosed(true);
                    --this.openPairs;
                    return returnValue.SUCCESS;
                } else {
                    return returnValue.FAILURE;
                }
            } else {
                return returnValue.FAILURE;
            }
        } catch (IndexOutOfBoundsException e)  {
            Log.e("Core", "Array index out of bounds!");
            return returnValue.ERROR;
        }
    }

    public gameStatus run(int id1, int id2) {
        Log.i("run", "Player " + this.currentPlayer);
        if(this.openPairs-1>0) {
            if(matchPairs(id1, id2) == returnValue.SUCCESS) {
                this.players.get(this.currentPlayer).setPoints(this.players.get(this.currentPlayer)
                        .getPoints()+1);
                Log.i("run", "Points: " + this.players.get(this.currentPlayer).getPoints());
                return gameStatus.RUNNING;
            } else if(matchPairs(id1, id2) == returnValue.FAILURE) {
                this.currentPlayer = getNextPlayer();
                return gameStatus.RUNNING;
            } else {
                return gameStatus.RUNNING;
            }
        } else {
            if(matchPairs(id1, id2) == returnValue.SUCCESS) {
                this.players.get(this.currentPlayer).setPoints(this.players.get(this.currentPlayer)
                        .getPoints() + 1);
                Log.i("run", "Winner: " + Collections.max(this.players).getId());
                this.winner = Collections.max(this.players).getId();
                return gameStatus.FINISHED;
            } else {
                return gameStatus.RUNNING;
            }
        }
    }

    private int getNextPlayer() {
        ++currentPlayer;
        return currentPlayer%this.players.size();
    }

    public int getWinnerId() {
        return winner;
    }

    public String getPlayerName(int id) {
        return this.players.get(id).getName();
    }
}
