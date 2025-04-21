package com.phasmidsoftware.dsaipg.projects.mcts.tictactoe_extended_OwnGame;

import com.phasmidsoftware.dsaipg.projects.mcts.core.Node;
import com.phasmidsoftware.dsaipg.projects.mcts.core.State;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class ETTTNode implements Node<ExtendableTicTacToe> {

    private final State<ExtendableTicTacToe> state;
    private final ArrayList<Node<ExtendableTicTacToe>> children;
    private int wins;
    private int playouts;

    public ETTTNode(State<ExtendableTicTacToe> state) {
        this.state = state;
        this.children = new ArrayList<>();
        initializeNodeData();
    }
    

    private void initializeNodeData() {
        if (isLeaf()) {
            playouts = 1;
            Optional<Integer> winner = state.winner();
            wins = winner.isPresent() ? 2 : 1;  // 2 for a win, 1 for a draw
        }
    }
    
    public void increment(int score) {
        this.playouts += 1;
        this.wins     += score;
    }

    
    @Override
    public boolean isLeaf() {
        return state.isTerminal();
    }

    @Override
    public State<ExtendableTicTacToe> state() {
        return state;
    }

    @Override
    public boolean white() {
        return state.player() == state.game().opener();
    }

    @Override
    public Collection<Node<ExtendableTicTacToe>> children() {
        return children;
    }

    @Override
    public void addChild(State<ExtendableTicTacToe> newState) {
        children.add(new ETTTNode(newState));
    }

    @Override
    public void backPropagate() {
        wins = 0;
        playouts = 0;
        for (Node<ExtendableTicTacToe> c : children) {
            wins     += c.wins();
            playouts += c.playouts();
        }
    }

    @Override
    public int wins() {
        return wins;
    }

    @Override
    public int playouts() {
        return playouts;
    }
}

