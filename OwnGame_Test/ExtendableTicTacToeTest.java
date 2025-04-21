package com.phasmidsoftware.dsaipg.projects.mcts.tictactoe_extended_OwnGame;

import org.junit.Test;
import static org.junit.Assert.*;
import com.phasmidsoftware.dsaipg.projects.mcts.core.State;
import com.phasmidsoftware.dsaipg.projects.mcts.core.Move;
import java.util.Collection;

public class ExtendableTicTacToeTest {

    @Test
    public void testStartNotTerminal() {
        ExtendableTicTacToe game = new ExtendableTicTacToe();
        State<ExtendableTicTacToe> s0 = game.start();
        assertFalse(s0.isTerminal()); 
        assertFalse(s0.winner().isPresent());
    }

    @Test
    public void testMovesAndNext() {
        ExtendableTicTacToe game = new ExtendableTicTacToe();
        State<ExtendableTicTacToe> s0 = game.start();
        int player0 = s0.player();
        Collection<Move<ExtendableTicTacToe>> moves = s0.moves(player0);
        assertFalse(moves.isEmpty());// have at least one move
        Move<ExtendableTicTacToe> m = moves.iterator().next();
        State<ExtendableTicTacToe> s1 = s0.next(m);
        assertNotNull(s1);
        assertNotSame(s0, s1);//should return a new state
    }
}

