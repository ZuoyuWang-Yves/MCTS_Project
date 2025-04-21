package com.phasmidsoftware.dsaipg.projects.mcts.tictactoe;



import org.junit.Test;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

import com.phasmidsoftware.dsaipg.projects.mcts.tictactoe.TicTacToe;
import com.phasmidsoftware.dsaipg.projects.mcts.tictactoe.TicTacToe.TicTacToeState;
import com.phasmidsoftware.dsaipg.projects.mcts.tictactoe.TicTacToeNode;
import com.phasmidsoftware.dsaipg.projects.mcts.tictactoe.MCTS;
import com.phasmidsoftware.dsaipg.projects.mcts.core.Move;
import com.phasmidsoftware.dsaipg.projects.mcts.core.Node;
import com.phasmidsoftware.dsaipg.projects.mcts.core.State;

import java.util.*;


public class MCTSTest {

	
	private TicTacToe game;
    private TicTacToe.TicTacToeState start;
    private TicTacToeNode root;
    private MCTS mcts;
	
    
    /**
     * Test running MCTS returns a legal move.
     */
	@Test
    public void testRun() {
        System.out.println("Running testRun...");
        TicTacToe game = new TicTacToe(0L);
        TicTacToeState start = (TicTacToeState) game.start();
        TicTacToeNode root = new TicTacToeNode(start);
        MCTS mcts = new MCTS(root, false);

        Node<TicTacToe> child = mcts.run(10);
        assertNotNull("run() should not return null", child);

        TicTacToeState childState = (TicTacToeState) child.state();
        Collection<Move<TicTacToe>> moves = start.moves(start.player());
        boolean legal = false;
        for (Move<TicTacToe> move : moves) {
            TicTacToeState next = (TicTacToeState) start.next(move);
            if (next.position().equals(childState.position())) {
                legal = true;
                break;
            }
        }
        assertTrue("MCTS.run must return a child correspond to a legal move", legal);
        System.out.println("Finished testRun.");
    }
	
	
	/**
	 * Test select a child node after adding children.
	 */
    @Test
    public void testSelect() {
        System.out.println("Running testSelect...");
        TicTacToe game = new TicTacToe(0L);
        TicTacToeNode root = new TicTacToeNode((TicTacToeState) game.start());
        root.addChild(game.start());
        root.addChild(game.start());

        for (Node<TicTacToe> child : root.children()) {
            if (child instanceof TicTacToeNode node) {
                node.increment(10);
            }
        }

        MCTS mcts = new MCTS(root, false);
        Node<TicTacToe> selected = mcts.run(1);
        assertNotNull(selected);
        System.out.println("Finished testSelect.");
    }
    
    
    /**
     * Test that expansion adds children to the root node.
     */
    @Test
    public void testExpand() {
        System.out.println("Running testExpand...");
        TicTacToe game = new TicTacToe(0L);
        TicTacToeNode root = new TicTacToeNode((TicTacToeState) game.start());
        MCTS mcts = new MCTS(root, false);

        mcts.run(1);
        assertFalse("After expansion, root should have children", root.children().isEmpty());
        System.out.println("Finished testExpand.");
    }
    
    /**
     * Test evaluating score after rollout.
     */
    @Test
    public void testEvaluate() {
        System.out.println("Running testEvaluate...");
        TicTacToe game = new TicTacToe(0L);
        Position pos = Position.parsePosition("X . .\n. . .\n. . .", TicTacToe.X);
        TicTacToeState state = game.new TicTacToeState(pos);
        MCTS mcts = new MCTS(new TicTacToeNode(state), false);

        int score = mcts.run(1).playouts();
        assertTrue("Score should be non-negative", score >= 0);
        System.out.println("Finished testEvaluate.");
    }
    
    
    /**
     * Test detecting if two-in-a-row threat exist correctly.
     */
    @Test
    public void testTwoInARowDetection() {
        System.out.println("Running testTwoInARowDetection...");
        Position pos = Position.parsePosition("X X .\n. . .\n. . .", TicTacToe.X);
        assertTrue(new MCTS(new TicTacToeNode(new TicTacToe(0L).new TicTacToeState(pos)), false)
            .twoInARowThreatCheck(pos, TicTacToe.X));
        System.out.println("Finished testTwoInARowDetection.");
    }

    
    /**
     * Test rollout and verify if score is valid.
     */
    @Test
    public void testRollout() {
        System.out.println("Running testRollout...");
        TicTacToe game = new TicTacToe(0L);
        TicTacToeState start = (TicTacToeState) game.start();
        TicTacToeNode node = new TicTacToeNode(start);
        MCTS mcts = new MCTS(node, false);

        int score = mcts.run(1).wins();
        assertTrue("Score should be valid", score >= 0);
        System.out.println("Finished testRollout.");
    }

    @Test
    public void testUpdate() {
        System.out.println("Running testUpdate...");
        TicTacToe game = new TicTacToe(0L);
        TicTacToeNode node = new TicTacToeNode((TicTacToeState) game.start());
        int beforeWins = node.wins();
        int beforePlayouts = node.playouts();

        node.increment(5);

        assertEquals(beforeWins + 5, node.wins());
        assertEquals(beforePlayouts + 1, node.playouts());
        System.out.println("Finished testUpdate.");
    }

    
    /**
     * Test if best child selected after MCTS.
     */
    @Test
    public void testBestChild() {
        System.out.println("Running testBestChild...");
        TicTacToe game = new TicTacToe(0L);
        TicTacToeNode root = new TicTacToeNode((TicTacToeState) game.start());
        root.addChild(game.start());
        root.addChild(game.start());

        for (Node<TicTacToe> child : root.children()) {
            if (child instanceof TicTacToeNode node) {
                node.increment(10);
            }
        }

        MCTS mcts = new MCTS(root, false);
        Node<TicTacToe> best = mcts.run(1);
        assertNotNull("Best child should not be null", best);
        System.out.println("Finished testBestChild.");
    }
    
    
    /**
     * Verify that expansion phase does not create duplicate child positions.
     */
    @Test
    public void testExpandDoesNotDuplicatePositions() {
    	TicTacToe tempGame = new TicTacToe(0L);
    	TicTacToe.TicTacToeState tempStart = (TicTacToe.TicTacToeState) tempGame.start();
    	TicTacToeNode tempRoot = new TicTacToeNode(tempStart);
    	MCTS tempMcts = new MCTS(tempRoot, false);

    	tempMcts.run(1);

    	Set<String> seen = new HashSet<>();
    	for (Node<TicTacToe> child : tempRoot.children()) {
    	    TicTacToe.TicTacToeState state = (TicTacToe.TicTacToeState) child.state();
    	    String rendered = state.position().render();
    	    assertFalse("Duplicate position detected", seen.contains(rendered));
    	    seen.add(rendered);
    	}
    }
    
    
    /**
     * Confirm that rollout finishes correctly within the allowed depth limit without crashing.
     */
    @Test
    public void testRolloutLimitedDepth() {
        TicTacToe freshGame = new TicTacToe(0L);
        TicTacToe.TicTacToeState freshStart = (TicTacToe.TicTacToeState) freshGame.start();
        TicTacToeNode freshRoot = new TicTacToeNode(freshStart);
        MCTS freshMcts = new MCTS(freshRoot, false);

        int score = freshMcts.run(1).wins();
        assertTrue("Rollout should finish normally within depth limit", score >= 0);
    }
    
    

    

	
	
	
}