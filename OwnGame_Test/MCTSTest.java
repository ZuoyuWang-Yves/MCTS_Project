package com.phasmidsoftware.dsaipg.projects.mcts.tictactoe_extended_OwnGame;

import org.junit.Test;
import static org.junit.Assert.*;
import com.phasmidsoftware.dsaipg.projects.mcts.core.Node;
import java.lang.reflect.Method;
import java.util.*;

public class MCTSTest {

    static class StubNode implements Node<ExtendableTicTacToe> {
        private final int wins, playouts;
        private final List<Node<ExtendableTicTacToe>> children;

        StubNode(int wins, int playouts, List<Node<ExtendableTicTacToe>> children) {
            this.wins = wins;
            this.playouts = playouts;
            this.children = children;
        }

        @Override public int wins()        { return wins; }
        @Override public int playouts()    { return playouts; }
        @Override public Collection<Node<ExtendableTicTacToe>> children() { return children; }
        @Override public boolean isLeaf()  { return false; }
        @Override public boolean white()   { return false; }
        @Override public com.phasmidsoftware.dsaipg.projects.mcts.core.State<ExtendableTicTacToe> state() { return null; }
        @Override public void addChild(com.phasmidsoftware.dsaipg.projects.mcts.core.State<ExtendableTicTacToe> s) {}
        @Override public void backPropagate() {}
    }

    @Test
    public void testUcb1_zeroPlayouts() throws Exception {
        MCTS mcts = new MCTS(null, false);
        StubNode node = new StubNode(0, 0, Collections.emptyList());
        Method ucb1 = MCTS.class.getDeclaredMethod("ucb1", Node.class, double.class);
        ucb1.setAccessible(true);
        double val = (double)ucb1.invoke(mcts, node, Math.log(1.0));
        assertEquals(Double.MAX_VALUE, val, 0.0);
    }

    @Test
    public void testUcb1_nonZero() throws Exception {
        MCTS mcts = new MCTS(null, false);
        StubNode node = new StubNode(3, 5, Collections.emptyList());
        Method ucb1 = MCTS.class.getDeclaredMethod("ucb1", Node.class, double.class);
        ucb1.setAccessible(true);
        double logN = Math.log(20);
        double expected = (3.0/5.0) + Math.sqrt(2 * logN / 5.0);
        double val = (double)ucb1.invoke(mcts, node, logN);
        assertEquals(expected, val, 1e-9);
    }

    @Test
    public void testBestChild() throws Exception {
    	// A:B win-rate .4: .7
        StubNode childA = new StubNode(2, 5, Collections.emptyList());  
        StubNode childB = new StubNode(7, 10, Collections.emptyList()); 
        List<Node<ExtendableTicTacToe>> kids = Arrays.asList(childA, childB);
        StubNode root = new StubNode(0, 0, kids);
        MCTS mcts = new MCTS(root, false);
        Method bestChild = MCTS.class.getDeclaredMethod("bestChild", Node.class);
        bestChild.setAccessible(true);
        @SuppressWarnings("unchecked")
        Node<ExtendableTicTacToe> result = (Node<ExtendableTicTacToe>)bestChild.invoke(mcts, root);
        assertSame(childB, result);
    }
}
