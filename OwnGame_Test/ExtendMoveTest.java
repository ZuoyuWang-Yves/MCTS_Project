package com.phasmidsoftware.dsaipg.projects.mcts.tictactoe_extended_OwnGame;

import org.junit.Test;
import static org.junit.Assert.*;

public class ExtendMoveTest {

 @Test
 public void testGetters() {
     ExtendMove em = new ExtendMove(1, Directions.SE);
     assertEquals( 1, em.player());
     assertEquals(Directions.SE, em.direction());
 }

 @Test
 public void testImplementsExtendableMove() {
     ExtendMove em = new ExtendMove(0, Directions.NW);
     assertTrue(
                em instanceof ExtendableMove);
 }
}

