package com.phasmidsoftware.dsaipg.projects.mcts.tictactoe_extended_OwnGame;

public class PlaceMove implements ExtendableMove {
	  private final int player, row, col;
	  public PlaceMove(int player,int row,int col){this.player=player;this.row=row;this.col=col;}
	  public int player(){return player;}
	  public int row(){return row;} public int col(){return col;}
	}