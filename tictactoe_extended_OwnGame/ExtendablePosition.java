package com.phasmidsoftware.dsaipg.projects.mcts.tictactoe_extended_OwnGame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ExtendablePosition {
    private final Cell[][] grid;      // always 9×9
    private final int rowMin, rowMax, colMin, colMax;
    private final int lastPlayer, moveCount;
    private static final int MAX=9;

    public ExtendablePosition(Cell[][] grid,
                              int lastPlayer, int moveCount,
                              int rowMin, int rowMax,
                              int colMin, int colMax) {
        this.grid = grid;
        this.lastPlayer = lastPlayer;
        this.moveCount = moveCount;
        this.rowMin = rowMin; this.rowMax = rowMax;
        this.colMin = colMin; this.colMax = colMax;
    }
    
    public int getLastPlayer() {
        return lastPlayer;
    }
    public int getRowMin() {
        return rowMin;
    }
    public int getRowMax() {
        return rowMax;
    }
    public int getColMin() {
        return colMin;
    }
    public int getColMax() {
        return colMax;
    }
    
    /** start in the center 3×3 with all currently unavailable cells as ZOMBIE */
    public static ExtendablePosition start() {
      Cell[][] g = new Cell[MAX][MAX];
      for(int r=0;r<MAX;r++)
        for(int c=0;c<MAX;c++)
          g[r][c] = (r>=3&&r<6&&c>=3&&c<6) ? Cell.EMPTY : Cell.ZOMBIE;
      return new ExtendablePosition(g, /*last*/1, /*count*/0, 3,6,3,6);
    }

    /** list all PlaceMoves with valid ExtendMove's Direction*/
    public List<ExtendableMove> moves(int player) {
      if (player==lastPlayer) throw new RuntimeException("consecutive moves");
      var L = new ArrayList<ExtendableMove>();
      
      //place in empty cells
      for(int r=rowMin;r<rowMax;r++)
        for(int c=colMin;c<colMax;c++)
          if (grid[r][c]==Cell.EMPTY)
            L.add(new PlaceMove(player,r,c));
      
      //extend in any direction that stays within 9×9 limits
      for(Directions d:Directions.values())
        if (canExtend(d)) L.add(new ExtendMove(player,d));
      return L;
    }

    private boolean canExtend(Directions d) {
      int nr = rowMin   - (d.hasNorth()?3:0);
      int MR = rowMax   + (d.hasSouth()?3:0);
      int nc = colMin   - (d.hasWest()?3:0);
      int MC = colMax   + (d.hasEast()?3:0);
      return nr>=0 && MR<=MAX && nc>=0 && MC<=MAX;
    }

    /** apply either a PlaceMove or an ExtendMove */
    public ExtendablePosition next(ExtendableMove m) {
      if (m instanceof PlaceMove pm)  return place(pm.player(), pm.row(), pm.col());
      else                             return extend(m.player(), ((ExtendMove)m).direction());
    }

    private ExtendablePosition place(int pl, int r, int c) {
      if (grid[r][c]!=Cell.EMPTY) throw new RuntimeException("occupied");
      Cell[][] g2 = deepCopy(grid);
      g2[r][c] = (pl==0?Cell.O:Cell.X);
      return new ExtendablePosition(g2, pl, moveCount+1, rowMin,rowMax,colMin,colMax);
    }

    private ExtendablePosition extend(int pl, Directions d) {
      int nr = rowMin   - (d.hasNorth()?3:0);
      int MR = rowMax   + (d.hasSouth()?3:0);
      int nc = colMin   - (d.hasWest()?3:0);
      int MC = colMax   + (d.hasEast()?3:0);
      Cell[][] g2 = deepCopy(grid);
      // convert newly revealed cells from Zombie to EMPTY
      for(int r=nr; r<MR; r++)
        for(int c=nc; c<MC; c++)
          if (r<rowMin||r>=rowMax||c<colMin||c>=colMax)
            g2[r][c] = Cell.EMPTY;
      return new ExtendablePosition(g2, pl, moveCount+1, nr,MR,nc,MC);
    }

    public boolean isTerminal() {
      return winner().isPresent()
          || (noEmptyInWindow() && allExtensionsBlocked());
    }
    

    public Optional<Integer> winner() {
        //check horizontal
        for (int r = rowMin; r < rowMax; r++) {
            for (int c = colMin; c <= colMax - 3; c++) {
                Cell a = grid[r][c], b = grid[r][c+1], c2 = grid[r][c+2];
                if (a != Cell.EMPTY && a != Cell.ZOMBIE && a==b && b==c2)
                    return Optional.of(a==Cell.X ? 1 : 0);
            }
        }
        //check vertical
        for (int c = colMin; c < colMax; c++) {
            for (int r = rowMin; r <= rowMax - 3; r++) {
                Cell a = grid[r][c], b = grid[r+1][c], c2 = grid[r+2][c];
                if (a != Cell.EMPTY && a != Cell.ZOMBIE && a==b && b==c2)
                    return Optional.of(a==Cell.X ? 1 : 0);
            }
        }
        //check diagonal: \
        for (int r = rowMin; r <= rowMax - 3; r++) {
            for (int c = colMin; c <= colMax - 3; c++) {
                Cell a = grid[r][c], b = grid[r+1][c+1], c2 = grid[r+2][c+2];
                if (a != Cell.EMPTY && a != Cell.ZOMBIE && a==b && b==c2)
                    return Optional.of(a==Cell.X ? 1 : 0);
            }
        }
        // check diagonal: /
        for (int r = rowMin; r <= rowMax - 3; r++) {
            for (int c = colMin + 2; c < colMax; c++) {
                Cell a = grid[r][c], b = grid[r+1][c-1], c2 = grid[r+2][c-2];
                if (a != Cell.EMPTY && a != Cell.ZOMBIE && a==b && b==c2)
                    return Optional.of(a==Cell.X ? 1 : 0);
            }
        }
        return Optional.empty();
    }

    public void printWindow() {
      for(int r=rowMin;r<rowMax;r++) {
        for(int c=colMin;c<colMax;c++) {
          System.out.print(switch(grid[r][c]) {
            case EMPTY->'.'; case O->'O'; case X->'X'; default->'?';
          }+" ");
        }
        System.out.println();
      }
    }

    private boolean noEmptyInWindow() {
      for(int r=rowMin;r<rowMax;r++)
        for(int c=colMin;c<colMax;c++)
          if (grid[r][c]==Cell.EMPTY) return false;
      return true;
    }
    private boolean allExtensionsBlocked() {
      for(Directions d:Directions.values())
        if (canExtend(d)) return false;
      return true;
    }

    private Cell[][] deepCopy(Cell[][] src) {
      Cell[][] dest = new Cell[MAX][MAX];
      for(int i=0;i<MAX;i++) dest[i]=src[i].clone();
      return dest;
    }
    
    public String normalize() {
        // first, extract the window into a 2D array of chars
        int h = rowMax - rowMin, w = colMax - colMin;
        char[][] win = new char[h][w];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                Cell c = grid[rowMin + i][colMin + j];
                win[i][j] = (c == Cell.EMPTY ? '.' : c == Cell.O ? 'O' : c == Cell.X ? 'X' : '?');
            }
        }

        List<String> reps = new ArrayList<>();
        char[][] cur = win;
        // generate 4 rotations
        for (int r = 0; r < 4; r++) {
            reps.add(windowToString(cur));
            reps.add(windowToString(reflectHorizontal(cur)));
            // rotate 90° for next iteration
            cur = rotate90(cur);
        }
        return Collections.min(reps);
    }

    // helper to turn a char matrix into a single string
    private String windowToString(char[][] m) {
        StringBuilder sb = new StringBuilder();
        for (char[] row : m) {
            sb.append(row).append('|');
        }
        return sb.toString();
    }

    // rotate 90
    private char[][] rotate90(char[][] m) {
        int H = m.length, W = m[0].length;
        char[][] x = new char[W][H];
        for (int i = 0; i < H; i++)
            for (int j = 0; j < W; j++)
                x[j][H - 1 - i] = m[i][j];
        return x;
    }

    // reflect left‑to‑right
    private char[][] reflectHorizontal(char[][] m) {
        int H = m.length, W = m[0].length;
        char[][] x = new char[H][W];
        for (int i = 0; i < H; i++)
            for (int j = 0; j < W; j++)
                x[i][W - 1 - j] = m[i][j];
        return x;
    }
}

