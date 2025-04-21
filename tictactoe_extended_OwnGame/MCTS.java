/*
 * Copyright (c) 2024. Robin Hillyard
 */

package com.phasmidsoftware.dsaipg.projects.mcts.tictactoe_extended_OwnGame;

import com.phasmidsoftware.dsaipg.projects.mcts.core.Node;
import com.phasmidsoftware.dsaipg.projects.mcts.tictactoe_extended_OwnGame.ExtendableTicTacToe.EState;
import com.phasmidsoftware.dsaipg.projects.mcts.core.State;
import com.phasmidsoftware.dsaipg.projects.mcts.core.Move;
import java.util.*;
import java.util.Scanner;


/**
 * Class to represent a Monte Carlo Tree Search for Extended TicTacToe.
 */
public class MCTS {
	
    private final Node<ExtendableTicTacToe> root;
    private final boolean isHumanFirst;
    private final Random random = new Random();
    private final Set<String> expanded = new HashSet<>();
    private final int mctsPlayer;

    public MCTS(Node<ExtendableTicTacToe> root, boolean isHumanFirst) {
        this.root = root;
        this.isHumanFirst = isHumanFirst;
        this.mctsPlayer = root.state().game().opener();
    }

    /** Run N simulations and return the best child */
    public Node<ExtendableTicTacToe> run(int iterations) {
    	expanded.clear(); 
        for (int i = 0; i < iterations; i++) {
            simulate(root);
        }
        return bestChild(root);
    }

    /** One MCTS iteration: Selection, Expansion, Simulation, Backpropagation */
    private void simulate(Node<ExtendableTicTacToe> start) {
        Deque<Node<ExtendableTicTacToe>> path = new ArrayDeque<>();
        Node<ExtendableTicTacToe> node = start;

        //Selection
        while (!node.isLeaf() && !node.children().isEmpty()) {
            node = select(node);
            path.push(node);
        }

        //Expansion
        if (!node.state().isTerminal()) {
            expand(node);
            if (!node.children().isEmpty()) {
                node = select(node);
                path.push(node);
            }
        }

        //Simulation
        int rolloutScore = rollout(node.state()); 

        // Backpropagation
        
        for (Node<ExtendableTicTacToe> n : path) {
            if (n instanceof ETTTNode etn) {
                etn.increment(rolloutScore);
            } else {
                n.backPropagate();
            }
        }
    }

    /** Selection using UCB1 */
    private Node<ExtendableTicTacToe> select(Node<ExtendableTicTacToe> node) {
        double logN = Math.log(Math.max(1, node.playouts()));
        return node.children().stream()
                .max(Comparator.comparing(c -> ucb1(c, logN)))
                .orElseThrow();
    }

    private double ucb1(Node<ExtendableTicTacToe> node, double logN) {
        if (node.playouts() == 0) return Double.MAX_VALUE;
        return (double) node.wins() / node.playouts()
                + Math.sqrt(2 * logN / node.playouts());
    }

    /** Expand node, avoiding duplicates via symmetry normalization */
    private void expand(Node<ExtendableTicTacToe> node) {
        State<ExtendableTicTacToe> st = node.state();
        int player = st.player();
        for (Move<ExtendableTicTacToe> m : st.moves(player)) {
            State<ExtendableTicTacToe> nxt = st.next(m);
            // Normalize the underlying position
            EState es = (EState) nxt;
            String norm = es.getPosition().normalize();
            if (expanded.add(norm)) {
                node.addChild(nxt);
            }
        }
    }

    /** Random play to terminal, return winning player or -1 */
    private int rollout(State<ExtendableTicTacToe> state) {
    	 EState cur = (EState) state;
    	    int player = cur.player();
    	    int depth = 0;

    	    Set<String> visited = new HashSet<>();
    	    visited.add(cur.getPosition().normalize());


    	    while (!cur.isTerminal() && depth < 4) {

    	        // symmetry
    	        List<Move<ExtendableTicTacToe>> all = new ArrayList<>(cur.moves(player));
    	        List<Move<ExtendableTicTacToe>> legal = new ArrayList<>();
    	        for (Move<ExtendableTicTacToe> m : all) {
    	            EState nxt = (EState) cur.next(m);
    	            String norm = nxt.getPosition().normalize();
    	            if (!visited.contains(norm)) legal.add(m);
    	        }
    	        if (legal.isEmpty()) break;

    	        //immediate win
    	        Move<ExtendableTicTacToe> best = null;
    	        for (Move<ExtendableTicTacToe> m : legal) {
    	            EState nxt = (EState) cur.next(m);
    	            if (nxt.winner().isPresent() && nxt.winner().get() == player) {
    	                best = m;
    	                break;
    	            }
    	        }

    	        // opponent’s immediate win
    	        if (best == null) {
    	            int opp = 1 - player;
    	            for (Move<ExtendableTicTacToe> m : legal) {
    	                EState nxt = (EState) cur.next(m);
    	                for (Move<ExtendableTicTacToe> om : nxt.moves(opp)) {
    	                    EState on = (EState) nxt.next(om);
    	                    if (on.winner().isPresent() && on.winner().get() == opp) {
    	                        best = m;
    	                        break;
    	                    }
    	                }
    	                if (best != null) break;
    	            }
    	        }

    	        // 2‑in‑a‑row threats
    	        if (best == null) {
    	            int opp = 1 - player;
    	            for (Move<ExtendableTicTacToe> m : legal) {
    	                EState nxt = (EState) cur.next(m);
    	                if (twoInARowThreatCheck(nxt.getPosition(), opp)) {
    	                    best = m;
    	                    break;
    	                }
    	            }
    	        }

    	        // fallback random
    	        if (best == null) {
    	            best = legal.get(random.nextInt(legal.size()));
    	        }

    	        // advance
    	        cur = (EState) cur.next(best);
    	        visited.add(cur.getPosition().normalize());
    	        player = 1 - player;
    	        depth++;
    	    }

    	    // score bonus
    	    int before = evaluate(state, mctsPlayer);
    	    int after = evaluate(cur,   mctsPlayer);
    	    int score = after - before;
    	    if (cur.winner().isPresent() && cur.winner().get() == mctsPlayer) {
    	        score += 100;
    	    }
    	    return score;
    }
    
    
    private boolean twoInARowThreatCheck(ExtendablePosition pos, int player) {
        int r0 = pos.getRowMin(), r1 = pos.getRowMax();
        int c0 = pos.getColMin(), c1 = pos.getColMax();

        // horizontal 
        for (int r = r0; r + 2 < r1; r++) {
            for (int c = c0; c + 2 < c1; c++) {
                int[] triple = { pos.get(r, c),
                                 pos.get(r, c+1),
                                 pos.get(r, c+2) };
                if (twoInARow(triple, player) > 0) {
                    return true;
                }
            }
        }

        // vertical
        for (int c = c0; c + 2 < c1; c++) {
            for (int r = r0; r + 2 < r1; r++) {
                int[] triple = { pos.get(r,   c),
                                 pos.get(r+1, c),
                                 pos.get(r+2, c) };
                if (twoInARow(triple, player) > 0) {
                    return true;
                }
            }
        }

        // diagonal  (\ and /)
        for (int r = r0; r + 2 < r1; r++) {
            for (int c = c0; c + 2 < c1; c++) {
                int[] d1 = { pos.get(r,   c),
                             pos.get(r+1, c+1),
                             pos.get(r+2, c+2) };
                int[] d2 = { pos.get(r+2, c),
                             pos.get(r+1, c+1),
                             pos.get(r,   c+2) };
                if (twoInARow(d1, player) > 0 || twoInARow(d2, player) > 0) {
                    return true;
                }
            }
        }

        return false;
    }
    
  //mimic weights and formula we experimented in classic ttt
    private int evaluate(State<ExtendableTicTacToe> s, int player) {
    	EState st = (EState) s;
        var pos= st.getPosition();
        int rowMin = pos.getRowMin(), rowMax = pos.getRowMax();
        int colMin = pos.getColMin(), colMax = pos.getColMax();
        
        int opponent = 1 - player;
        int score = 0;
//        score -= 5 * (pos.getMoveCount() / 5);
        score -= 5 * (pos.getMoveCount());
        
        // scan every sliding window (len == 3) in rows
        for (int r = rowMin; r < rowMax; r++) {
            for (int c = colMin; c + 2 < colMax; c++) {
            	int a = pos.get(r,c);
                int b = pos.get(r,c+1);
                int d = pos.get(r,c+2);
                score += twoInARow(new int[]{a, b, d}, player);
                score -= twoInARow(new int[]{a, b, d}, player)*2;
            }
        }
        // columns
        for (int c = colMin; c < colMax; c++) {
            for (int r = rowMin; r + 2 < rowMax; r++) {
                int[] triple = {pos.get(r,c), pos.get(r+1,c), pos.get(r+2,c)};
                score += twoInARow(triple, player);
                score -= twoInARow(triple, opponent) * 2;
            }
        }
        //diagonals \ and /
        for (int r = rowMin; r + 2 < rowMax; r++) {
            for (int c = colMin; c + 2 < colMax; c++) {
                
                int[] d1 = {pos.get(r,c), pos.get(r+1,c+1), pos.get(r+2,c+2)};
                
                int[] d2 = {pos.get(r+2,c), pos.get(r+1,c+1), pos.get(r,c+2)};
                
                score += twoInARow(d1, player) + twoInARow(d2, player);
                score -= 2 * (twoInARow(d1, opponent) + twoInARow(d2, opponent));
            }
        }

        //virtual win bonus
        Optional<Integer> maybeWin = st.winner();
        if (maybeWin.isPresent()) {
            int w = maybeWin.get();
            score += (w == player ? 100 : -100);
        }
        
        return score;
    }
    
    private int twoInARow(int[] line, int player) {
        int cP = 0, cB = 0;
        for (int x : line) {
            if (x == player) cP++;
            else if (x == -1) cB++;
        }
        return (cP == 2 && cB == 1) ? 20 : 0;
    }
    
    
    
    /** Pick the child with highest win-rate */
    private Node<ExtendableTicTacToe> bestChild(Node<ExtendableTicTacToe> node) {
        return node.children().stream()
                .max(Comparator.comparing(c -> (double) c.wins() / c.playouts()))
                .orElseThrow();
    }

    /**
     * Console UI & game loop
     */
    public static void main(String[] args) {
        ExtendableTicTacToe game = new ExtendableTicTacToe();
        State<ExtendableTicTacToe> state = game.start();
        Scanner scanner = new Scanner(System.in);
        boolean humanFirst = new Random().nextBoolean();
        int mctsPlayer = humanFirst ? (1 - game.opener()) : (game.opener());
   
        if (humanFirst) {
            System.out.println("Human goes first.");
            state = humanMove(state, scanner);
        } else {
        	
            System.out.println("MCTS goes first.");
        }
        boolean humanplay = false;
        
        while (!state.isTerminal()) {
            if (humanplay) {
            	state = humanMove(state, scanner);
            }else {
            	Node<ExtendableTicTacToe> root = new ETTTNode(state);
                MCTS mcts = new MCTS(root, humanFirst);
                
                long startTime = System.currentTimeMillis();
                
                state = mcts.run(10000).state();
                
                // End timing
                long endTime = System.currentTimeMillis();

                // Calculate and print elapsed time
                System.out.println("MCTS move decision took " + (endTime - startTime) + " ms.");
                
                System.out.println("MCTS played:");
                System.out.println(state);
            }
            humanplay = !humanplay;
        }
        scanner.close();
        
        
        
        state.winner().ifPresentOrElse(
                w -> System.out.println("Winner: " + (w == mctsPlayer ?   "MCTS" : "Human")),
                () -> System.out.println("Draw!"));
        System.out.println(state);
    }

    /** Prompt human for a move index */
    private static State<ExtendableTicTacToe> humanMove(State<ExtendableTicTacToe> state, Scanner scanner) {
        
        
        ExtendableTicTacToe.EState es = (ExtendableTicTacToe.EState) state;
        ExtendablePosition pos = es.getPosition();
        int baseR = pos.getRowMin(), baseC = pos.getColMin();

        List<Move<ExtendableTicTacToe>> moves = new ArrayList<>(state.moves(state.player()));
        System.out.println("Available moves:");
        for (int i = 0; i < moves.size(); i++) {
            Move<ExtendableTicTacToe> m = moves.get(i);
            String desc;
            if (m instanceof PlaceMove pm) {
                // compute local position
                int r = pm.row() - baseR;
                int c = pm.col() - baseC;
                desc = String.format("[%d] Place (%d,%d)", i, r, c);
            }
            else if (m instanceof ExtendMove em) {
                desc = String.format("[%d] Extend %s", i, em.direction());
            }
            else {
                desc = String.format("[%d] Unknown", i);
            }
            System.out.println(desc);
        }
        
        System.out.println("Current board:");
        System.out.println(state);
        
        System.out.print("Select move index: ");
        int idx = scanner.nextInt();
        return state.next(moves.get(idx));
    }
}