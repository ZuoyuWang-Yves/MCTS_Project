package com.phasmidsoftware.dsaipg.projects.mcts.tictactoe_extended_OwnGame;


import org.junit.Test;
import static org.junit.Assert.*;
import com.phasmidsoftware.dsaipg.projects.mcts.core.Move;

public class ExtendableMoveTest {

 @Test
 public void testIsInterface() {
     assertTrue(
    		 ExtendableMove.class.isInterface());
 	}

 @Test
 public void testExtendsMoveInterface() {
     assertTrue(
                Move.class.isAssignableFrom(ExtendableMove.class));
 	}
}
