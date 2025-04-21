package com.phasmidsoftware.dsaipg.projects.mcts.tictactoe_extended_OwnGame;

import org.junit.Test;

import static org.junit.Assert.*;

public class CellTest {

    @Test
    public void testEnumValuesAndOrder() {
        Cell[] vals = Cell.values();
        assertEquals(4, vals.length);
        assertArrayEquals(
            new Cell[]{Cell.ZOMBIE, Cell.EMPTY, Cell.O, Cell.X}, vals
        );
    }

    @Test
    public void testValueOfByName() {
        assertEquals(Cell.ZOMBIE, Cell.valueOf("ZOMBIE"));
        assertEquals(Cell.EMPTY,  Cell.valueOf("EMPTY"));
        assertEquals(Cell.O,      Cell.valueOf("O"));
        assertEquals(Cell.X,      Cell.valueOf("X"));
    }
}