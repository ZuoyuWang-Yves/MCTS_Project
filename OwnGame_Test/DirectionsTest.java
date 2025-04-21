package com.phasmidsoftware.dsaipg.projects.mcts.tictactoe_extended_OwnGame;

import org.junit.Test;

import static org.junit.Assert.*;


public class DirectionsTest {

    @Test
    public void testHasNorth() {
        assertTrue(Directions.N .hasNorth());
        assertTrue(Directions.NE.hasNorth());
        assertTrue(Directions.NW.hasNorth());
        assertFalse(Directions.S .hasNorth());
        assertFalse(Directions.E .hasNorth());
    }

    @Test
    public void testHasSouth() {
        assertTrue(Directions.S .hasSouth());
        assertTrue(Directions.SE.hasSouth());
        assertTrue(Directions.SW.hasSouth());
        assertFalse(Directions.N .hasSouth());
        assertFalse(Directions.W .hasSouth());
    }

    @Test
    public void testHasEast() {
        assertTrue(Directions.E .hasEast());
        assertTrue(Directions.NE.hasEast());
        assertTrue(Directions.SE.hasEast());
        assertFalse(Directions.W .hasEast());
        assertFalse(Directions.SW.hasEast());
    }

    @Test
    public void testHasWest() {
        assertTrue(Directions.W .hasWest());
        assertTrue(Directions.NW.hasWest());
        assertTrue(Directions.SW.hasWest());
        assertFalse(Directions.E .hasWest());
        assertFalse(Directions.NE.hasWest());
    }
}

