package com.phasmidsoftware.dsaipg.projects.mcts.tictactoe_extended_OwnGame;

import org.junit.Test;
import static org.junit.Assert.*;

public class PlaceMoveTest {

    @Test
    public void testGetters() {
        PlaceMove pm = new PlaceMove(0, 4, 5);
        assertEquals("player() must return the ctor value", 0, pm.player());
        assertEquals("row() must return the ctor value",    4, pm.row());
        assertEquals("col() must return the ctor value",    5, pm.col());
    }

    @Test
    public void testImplementsExtendableMove() {
        PlaceMove pm = new PlaceMove(1, 2, 3);
        assertTrue("PlaceMove should implement ExtendableMove",
                   pm instanceof ExtendableMove);
    }
}
