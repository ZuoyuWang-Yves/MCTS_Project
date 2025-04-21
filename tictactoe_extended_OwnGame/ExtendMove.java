package com.phasmidsoftware.dsaipg.projects.mcts.tictactoe_extended_OwnGame;

public class ExtendMove implements ExtendableMove {
	  private final int player;
	  private final Directions direction;
	  public ExtendMove(int player,Directions dir){this.player=player;this.direction=dir;}
	  public int player(){return player;}
	  public Directions direction(){return direction;}
	}