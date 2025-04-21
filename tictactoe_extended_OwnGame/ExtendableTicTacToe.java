package com.phasmidsoftware.dsaipg.projects.mcts.tictactoe_extended_OwnGame;

/*
 * Copyright (c) 2024. Robin Hillyard
 */


import com.phasmidsoftware.dsaipg.projects.mcts.core.Game;
import com.phasmidsoftware.dsaipg.projects.mcts.core.Move;
import com.phasmidsoftware.dsaipg.projects.mcts.core.State;

import java.util.*;


/**
 * Class which models the game of Extended TicTacToe.
 */

public class ExtendableTicTacToe implements Game<ExtendableTicTacToe> {
  public State<ExtendableTicTacToe> start() {
    return new EState(ExtendablePosition.start());
  }
  public int opener() { return TicTacToe.X; }

  class EState implements State<ExtendableTicTacToe> {
    private final ExtendablePosition pos;
    EState(ExtendablePosition pos){ this.pos=pos; }
    
    public ExtendablePosition getPosition() {
        return pos;
    }
    public ExtendableTicTacToe game()         { return ExtendableTicTacToe.this; }
    public boolean isTerminal()               { return pos.isTerminal(); }
    public Optional<Integer> winner()         { return pos.winner(); }
    public int player()                       { return (pos.getLastPlayer()==TicTacToe.X ? TicTacToe.O : TicTacToe.X); }
    public Random random()                    { return new Random(); }

    public Collection<Move<ExtendableTicTacToe>> moves(int player){
      return List.copyOf(pos.moves(player));
    }

    public State<ExtendableTicTacToe> next(Move<ExtendableTicTacToe> m){
      return new EState(pos.next((ExtendableMove)m));
    }
    

    @Override public String toString() {
      pos.printWindow();
      return "";
    }
  }
}