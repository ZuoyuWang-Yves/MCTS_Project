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
    }

    @Test(expected = RuntimeException.class)
    public void testMovesConsecutiveException() {
        // start() sets lastPlayer = 1, so moves(1) should catch an exception
        ExtendablePosition.start().moves(1);
    }

    @Test
    public void testPlaceNextAffectsMoves() {
        ExtendablePosition p0 = ExtendablePosition.start();
        // first move by player=0 at (3,3)
        PlaceMove pm = new PlaceMove(0, 3, 3);
        ExtendablePosition p1 = p0.next(pm);
        // now (3,3) is occupied, should not be found in the list
        List<ExtendableMove> moves = p1.moves(1);
        boolean found = moves.stream()
            .filter(m -> m instanceof PlaceMove)
            .map(m -> (PlaceMove)m)
            .anyMatch(m -> m.row()==3 && m.col()==3);
        assertFalse(found);
    }

    @Test
    public void testExtendNextUpdatesWindow() {
        ExtendablePosition p0 = ExtendablePosition.start();
        // extend to SE by player=0
        ExtendMove em = new ExtendMove(0, Directions.SE);
        ExtendablePosition p2 = p0.next(em);
        // SE should expand rowMax and colMax by 3
        assertEquals(3, p2.getRowMin());
        assertEquals(9, p2.getRowMax());
        assertEquals(3, p2.getColMin());
        assertEquals(9, p2.getColMax());
    }

    @Test
    public void testWinnerVertical() {
        // custom grid: vertical X win at col=4, rows=3..5
        Cell[][] g = new Cell[9][9];
        for (int r = 0; r < 9; r++)
            for (int c = 0; c < 9; c++)
                g[r][c] = Cell.ZOMBIE;
        int rm = 3, rM = 6, cm = 3, cM = 6;
        // mark window EMPTY
        for (int r = rm; r < rM; r++)
            for (int c = cm; c < cM; c++)
                g[r][c] = Cell.EMPTY;
        // place vertical three X's
        g[3][4] = Cell.X;
        g[4][4] = Cell.X;
        g[5][4] = Cell.X;
        ExtendablePosition pos = new ExtendablePosition(g, 1, 3, rm, rM, cm, cM);
        Optional<Integer> w = pos.winner();
        assertTrue(w.isPresent());
        assertEquals(1, (int)w.get()); //X map to winner==1
        assertTrue(pos.isTerminal());// terminated when one player win
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

