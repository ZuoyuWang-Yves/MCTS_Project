package com.phasmidsoftware.dsaipg.projects.mcts.tictactoe_extended_OwnGame;

public enum Directions {
	  N, NE, E, SE, S, SW, W, NW;
	  public boolean hasNorth() { return this==N||this==NE||this==NW; }
	  public boolean hasSouth() { return this==S||this==SE||this==SW; }
	  public boolean hasEast()  { return this==E||this==NE||this==SE; }
	  public boolean hasWest()  { return this==W||this==NW||this==SW; }
	}
