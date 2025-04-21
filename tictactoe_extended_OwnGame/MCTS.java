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

    public MCTS(Node<ExtendableTicTacToe> root, boolean isHumanFirst) {
        this.root = root;
        this.isHumanFirst = isHumanFirst;
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
        int winner = rollout(node.state());

        // Backpropagation
        for (Node<ExtendableTicTacToe> n : path) {
        	if (n instanceof ETTTNode etn) {
        		  etn.increment(winner);
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
        State<ExtendableTicTacToe> cur = state;
        int player = cur.player();
        while (!cur.isTerminal()) {
            List<Move<ExtendableTicTacToe>> moves = new ArrayList<>(cur.moves(player));
            cur = cur.next(moves.get(random.nextInt(moves.size())));
            player = 1 - player;
        }
        return cur.winner().orElse(-1);
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
                state = mcts.run(10000).state();
                System.out.println("MCTS played:");
                System.out.println(state);
            }
            humanplay = !humanplay;
        }
        scanner.close();
        
        
        
        state.winner().ifPresentOrElse(
                w -> System.out.println("Winner: " + (w == mctsPlayer ?  "Human": "MCTS" )),
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