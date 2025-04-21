package com.phasmidsoftware.dsaipg.projects.mcts.tictactoe_extended_OwnGame;

import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

import com.phasmidsoftware.dsaipg.projects.mcts.core.State;
import com.phasmidsoftware.dsaipg.projects.mcts.core.Move;

import java.util.Collection;
import java.util.Iterator;

public class ETTTNodeTest {

 static class StubState implements State<ExtendableTicTacToe> {
     private final boolean terminal;
     private final Optional<Integer> winner;

     StubState(boolean terminal, Optional<Integer> winner) {
         this.terminal = terminal;
         this.winner  = winner;
     }

     @Override public boolean isTerminal()            { return terminal; }
     @Override public Optional<Integer> winner()      { return winner;   }
     @Override public ExtendableTicTacToe game()      { throw new UnsupportedOperationException(); }
     @Override public int player()                    { throw new UnsupportedOperationException(); }
     @Override public Collection moves(int p){ throw new UnsupportedOperationException(); }
     @Override public State<ExtendableTicTacToe> next(Move m) {
         throw new UnsupportedOperationException();
     }
     @Override public java.util.Random random()       { return new java.util.Random(); }
 }

 @Test
 public void leafWin_initializesWins2Playouts1() {
     State<ExtendableTicTacToe> s = new StubState(true, Optional.of(1));
     ETTTNode n = new ETTTNode(s);
     assertTrue(n.isLeaf());
     assertEquals(1, n.playouts());
     assertEquals(2, n.wins());
 }

 @Test
 public void leafDraw_initializesWins1Playouts1() {
     State<ExtendableTicTacToe> s = new StubState(true, Optional.empty());
     ETTTNode n = new ETTTNode(s);
     assertTrue(n.isLeaf());
     assertEquals(1, n.playouts());
     assertEquals(1, n.wins());
 }

 @Test
 public void nonLeaf_initializesZeroWinsAndPlayouts() {
     State<ExtendableTicTacToe> s = new StubState(false, Optional.empty());
     ETTTNode n = new ETTTNode(s);
     assertFalse(n.isLeaf());
     assertEquals(0, n.playouts());
     assertEquals(0, n.wins());
 }

 @Test
 public void increment_updatesWinsAndPlayouts() {
     State<ExtendableTicTacToe> s = new StubState(false, Optional.empty());
     ETTTNode n = new ETTTNode(s);
     n.increment(3);
     assertEquals(1, n.playouts());
     assertEquals(3, n.wins());
     n.increment(2);
     assertEquals(2, n.playouts());
     assertEquals(5, n.wins());
 }

 @Test
 public void backPropagate_sumsChildrenWinsAndPlayouts() {
     // parent non‑leaf
     ETTTNode parent = new ETTTNode(new StubState(false, Optional.empty()));
     // add two children
     parent.addChild(new StubState(false, Optional.empty()));
     parent.addChild(new StubState(false, Optional.empty()));
     assertEquals(2, parent.children().size());

     // increment each child with different scores
     Iterator<com.phasmidsoftware.dsaipg.projects.mcts.core.Node<ExtendableTicTacToe>> it =
         parent.children().iterator();
     ETTTNode child1 = (ETTTNode)it.next();
     ETTTNode child2 = (ETTTNode)it.next();

     child1.increment(3);  // now wins=3, playouts=1
     child2.increment(2);  // now wins=2, playouts=1

     parent.backPropagate();
     assertEquals(5, parent.wins());
     assertEquals(2, parent.playouts());
 }
}
