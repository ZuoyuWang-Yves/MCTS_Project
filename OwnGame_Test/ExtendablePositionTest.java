// src/test/java/com/phasmidsoftware/dsaipg/projects/mcts/tictactoe_extended_OwnGame/ExtendablePositionTest.java
package com.phasmidsoftware.dsaipg.projects.mcts.tictactoe_extended_OwnGame;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;
import java.util.Optional;

public class ExtendablePositionTest {

	@Test
    public void testStartWindow() {
        ExtendablePosition p = ExtendablePosition.start();
        assertEquals(3, p.getRowMin());
        assertEquals(6, p.getRowMax());
        assertEquals(3, p.getColMin());
        assertEquals(6, p.getColMax());
        // center should be EMPTY, border ZOMBIE
        assertEquals(-1, p.get(4, 4));
        assertThrows(IndexOutOfBoundsException.class, () -> p.get(6, 4));
    }

    @Test
    public void testPlaceNextAndGet() {
        ExtendablePosition p = ExtendablePosition.start();
        // place O at (3,3)
        ExtendablePosition p2 = p.next(new PlaceMove(0, 3, 3));
        // O maps to 0
        assertEquals(0, p2.get(3, 3));
        // X should still be absent
        assertEquals(-1, p2.get(3, 4));
    }

    @Test
    public void testExtendMoves() {
    
        

        ExtendablePosition p = ExtendablePosition.start();

        List<ExtendableMove> moves = p.moves(TicTacToe.X);
        assertTrue(moves.stream().anyMatch(m -> m instanceof ExtendMove));
        
        
        // perform one extend
        ExtendablePosition p2 = p.next(new ExtendMove(0, Directions.N));
        // window should have expanded north by 3
        assertEquals(0, p2.getRowMin());
        assertEquals(6, p2.getRowMax());
    }

    @Test
    public void testWinnerHorizontalVerticalDiagonal() {
        // build a custom 3×3 block at (3..6)
        Cell[][] g = new Cell[9][9];
        for (int r=0;r<9;r++)
            for (int c=0;c<9;c++)
                g[r][c] = Cell.ZOMBIE;
        // fill 3×3 center EMPTY
        for (int r=3;r<6;r++)
            for (int c=3;c<6;c++)
                g[r][c] = Cell.EMPTY;
        // horizontal win for X (player 1) at row 3
        g[3][3]=g[3][4]=g[3][5]=Cell.X;
        ExtendablePosition pH = new ExtendablePosition(g, 0,0,3,6,3,6);
        assertEquals(Optional.of(1), pH.winner());

        // vertical win for O (player 0)
        g = g.clone();
        g[3][3]=g[4][3]=g[5][3]=Cell.O;
        ExtendablePosition pV = new ExtendablePosition(g, 1,0,3,6,3,6);
        assertEquals(Optional.of(0), pV.winner());

        // diagonal \
        g = g.clone();
        g[3][3]=g[4][4]=g[5][5]=Cell.X;
        ExtendablePosition pD = new ExtendablePosition(g, 0,0,3,6,3,6);
        assertEquals(Optional.of(1), pD.winner());
    }

    @Test
    public void testDrawCondition() {
    	// fill a 9×9 window completely with alternating X and O 
    	// rows come in pairs: two identical X,O,X,O… then two O,X,O,X…, so no three in a row exists
        Cell[][] g = new Cell[9][9];
        for (int r = 0; r < 9; r++) {
            boolean flipRow = ((r / 2) % 2) == 1;
            for (int c = 0; c < 9; c++) {
                boolean evenCol = (c % 2) == 0;
                g[r][c] = (evenCol ^ flipRow) ? Cell.X : Cell.O;
            }
        }
        // window completely extended alreaady → no extensions possible
        ExtendablePosition pos = new ExtendablePosition(
            g,0,81,0,9,0,9
        );
        
        assertFalse(pos.winner().isPresent());
        assertTrue(pos.isTerminal());
    }
    
    
}

