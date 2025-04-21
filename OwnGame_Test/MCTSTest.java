package com.phasmidsoftware.dsaipg.projects.mcts.tictactoe_extended_OwnGame;


import com.phasmidsoftware.dsaipg.projects.mcts.core.Node;
import com.phasmidsoftware.dsaipg.projects.mcts.core.State;



import java.lang.reflect.Method;
import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

public class MCTSTest {

    private MCTS ai;
    private ETTTNode root;
    private int aiPlayer;


    @Test
    public void testTwoInARow() throws Exception {
    	root = new ETTTNode(new ExtendableTicTacToe().start());
        ai = new MCTS(root, /*isHumanFirst=*/true);
        // mctsPlayer == opener
        aiPlayer = root.state().game().opener();
        Method m = MCTS.class.getDeclaredMethod("twoInARow", int[].class, int.class);
        m.setAccessible(true);
        assertEquals(20, m.invoke(ai, new int[]{1,1,-1}, 1));
        assertEquals( 0, m.invoke(ai, new int[]{1,0,1}, 1));
    }

    @Test
    public void testTwoInARowThreatCheck() throws Exception {
    	root = new ETTTNode(new ExtendableTicTacToe().start());
        ai = new MCTS(root, /*isHumanFirst=*/true);
        // mctsPlayer == opener
        aiPlayer = root.state().game().opener();
        // build pos with Xs at (3,3),(3,4)
        ExtendablePosition p0 = ExtendablePosition.start()
            .next(new PlaceMove(1,3,3))
            .next(new PlaceMove(0,4,4))
            .next(new PlaceMove(1,3,4));
        Method m = MCTS.class.getDeclaredMethod(
            "twoInARowThreatCheck", ExtendablePosition.class, int.class);
        m.setAccessible(true);
        assertTrue((boolean)m.invoke(ai, p0, 1));
        assertFalse((boolean)m.invoke(ai, p0, 0));
    }

    @Test
    public void testEvaluateEmptyBoard() throws Exception {
    	root = new ETTTNode(new ExtendableTicTacToe().start());
        ai = new MCTS(root, /*isHumanFirst=*/true);
        // mctsPlayer == opener
        aiPlayer = root.state().game().opener();
        State<ExtendableTicTacToe> s = new ExtendableTicTacToe().start();
        Method eval = MCTS.class.getDeclaredMethod("evaluate", State.class, int.class);
        eval.setAccessible(true);
        int score = (int)eval.invoke(ai, s, aiPlayer);
        assertEquals(0, score); // empty window evaluate to 0
    }

    @Test
    public void testEvaluateWinBonus() throws Exception {
    	root = new ETTTNode(new ExtendableTicTacToe().start());
        ai = new MCTS(root, /*isHumanFirst=*/true);
        // mctsPlayer == opener
        aiPlayer = root.state().game().opener();
        // craft a terminal X‑win in the 3×3 window
        Cell[][] g = new Cell[9][9];
        for (int r=0; r<9; r++)
            Arrays.fill(g[r], Cell.ZOMBIE);
        for (int r=3; r<6; r++)
            for (int c=3; c<6; c++)
                g[r][c] = Cell.EMPTY;
        g[3][3] = g[3][4] = g[3][5] = Cell.X;
        ExtendablePosition p = new ExtendablePosition(g, /*last*/0,0,3,6,3,6);
        ExtendableTicTacToe.EState st = new ExtendableTicTacToe().new EState(p);

        Method eval = MCTS.class.getDeclaredMethod("evaluate", State.class, int.class);
        eval.setAccessible(true);
        int score = (int)eval.invoke(ai, st, /*player=*/1);
        assertTrue(score >= 100); // terminal win yield ≥100 bonus
    }

    @Test
    public void testUcb1AndSelect() throws Exception {
    	root = new ETTTNode(new ExtendableTicTacToe().start());
        ai = new MCTS(root, /*isHumanFirst=*/true);
        // mctsPlayer == opener
        aiPlayer = root.state().game().opener();
        // A:B winRate 1:2
        Node<ExtendableTicTacToe> a = new NodeStub(10, 20); // winRate=0.5
        Node<ExtendableTicTacToe> b = new NodeStub( 5,  5); // winRate=1.0
        double logN = Math.log(25);

        Method ucb1 = MCTS.class.getDeclaredMethod("ucb1", Node.class, double.class);
        ucb1.setAccessible(true);
        double uA = (double)ucb1.invoke(ai, a, logN);
        double uB = (double)ucb1.invoke(ai, b, logN);
        assertTrue(uB > uA);

        // test select
        ParentStub parent = new ParentStub(Arrays.asList(a, b));
        Method select = MCTS.class.getDeclaredMethod("select", Node.class);
        select.setAccessible(true);
        @SuppressWarnings("unchecked")
        Node<ExtendableTicTacToe> chosen = (Node<ExtendableTicTacToe>)select.invoke(ai, parent);
        assertSame(b, chosen);
    }

    @Test
    public void testExpandNoDuplicates() throws Exception {
    	root = new ETTTNode(new ExtendableTicTacToe().start());
        ai = new MCTS(root, /*isHumanFirst=*/true);
        // mctsPlayer == opener
        aiPlayer = root.state().game().opener();
        Method expand = MCTS.class.getDeclaredMethod("expand", Node.class);
        expand.setAccessible(true);
        // first expansion
        expand.invoke(ai, root);
        int c1 = root.children().size();
        // second expansion should add none
        expand.invoke(ai, root);
        assertEquals(c1, root.children().size());
    }

    @Test
    public void testRolloutOnTerminal() throws Exception {
    	root = new ETTTNode(new ExtendableTicTacToe().start());
        ai = new MCTS(root, /*isHumanFirst=*/true);
        // mctsPlayer == opener
        aiPlayer = root.state().game().opener();
        // reuse the terminal position from testEvaluateWinBonus
        Cell[][] g = new Cell[9][9];
        for (int r=0; r<9; r++)
            Arrays.fill(g[r], Cell.ZOMBIE);
        for (int r=3; r<6; r++)
            for (int c=3; c<6; c++)
                g[r][c] = Cell.EMPTY;
        g[3][3]=g[3][4]=g[3][5]=Cell.X;
        ExtendablePosition p = new ExtendablePosition(g,0,0,3,6,3,6);
        ExtendableTicTacToe.EState st = new ExtendableTicTacToe().new EState(p);

        Method rollout = MCTS.class.getDeclaredMethod("rollout", State.class);
        rollout.setAccessible(true);
        int sc = (int)rollout.invoke(ai, st);
        assertTrue(sc >= 100);// ≥100 bonus
    }

    @Test
    public void testRunReturnsChild() {
    	root = new ETTTNode(new ExtendableTicTacToe().start());
        ai = new MCTS(root, /*isHumanFirst=*/true);
        // mctsPlayer == opener
        aiPlayer = root.state().game().opener();
        Node<ExtendableTicTacToe> best = ai.run(1);
        assertNotNull(best);
        assertTrue(root.children().contains(best));
    }

    //
    //helpers
    //

    /** A minimal Node stub with fixed wins/playouts */
    static class NodeStub implements Node<ExtendableTicTacToe> {
        private final int wins, playouts;
        NodeStub(int wins, int playouts) { this.wins = wins; this.playouts = playouts; }
        public boolean isLeaf()             { return false; }
        public State<ExtendableTicTacToe> state() { return null; }
        public boolean white()              { return false; }
        public Collection<Node<ExtendableTicTacToe>> children() { return List.of(); }
        public void addChild(State<ExtendableTicTacToe> s)         { /*no-op*/ }
        public void backPropagate()         { /*no-op*/ }
        public int wins()                   { return wins; }
        public int playouts()               { return playouts; }
    }

    /** A parent stub whose children() returns a fixed list */
    static class ParentStub implements Node<ExtendableTicTacToe> {
        private final List<Node<ExtendableTicTacToe>> kids;
        ParentStub(List<Node<ExtendableTicTacToe>> kids) { this.kids = kids; }
        public boolean isLeaf() { return false; }
        public State<ExtendableTicTacToe> state() { return null; }
        public boolean white() { return false; }
        public Collection<Node<ExtendableTicTacToe>> children() { return kids; }
        public void addChild(State<ExtendableTicTacToe> s) { /*no-op*/ }
        public void backPropagate() { /*no-op*/ }
        public int wins() { return 0; }
        public int playouts() { return 0; }
    }
}

