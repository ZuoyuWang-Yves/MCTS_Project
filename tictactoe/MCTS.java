/*
 * Copyright (c) 2024. Robin Hillyard
 */

package com.phasmidsoftware.dsaipg.projects.mcts.tictactoe;

import com.phasmidsoftware.dsaipg.projects.mcts.core.Node;

import com.phasmidsoftware.dsaipg.projects.mcts.core.State;
import com.phasmidsoftware.dsaipg.projects.mcts.core.Move;


import java.util.*;
import java.util.Scanner;


/**
 * Class to represent a Monte Carlo Tree Search for TicTacToe.
 */
public class MCTS {
	
    private final Node<TicTacToe> root;

    private final Set<Position> expandedPositions;
    private final Random random = new Random();
    
    private final boolean isHumanFirst;
    
    private final int mctsPlayer;

//    public static void main(String[] args) {
//        MCTS mcts = new MCTS(new TicTacToeNode(new TicTacToe().new TicTacToeState()));
//        Node<TicTacToe> root = mcts.root;
//
//        // This is where you process the MCTS to try to win the game.
//    }
	
	/*
	 * 
	 */
	public static void main(String[] args) {
        TicTacToe game = new TicTacToe();
        State<TicTacToe> state = game.start();
        Scanner scanner = new Scanner(System.in); //read human input
        
        Random random = new Random();
        boolean isHumanFirst = random.nextBoolean();
        
        if (isHumanFirst) {
            System.out.println("Random choice: Human goes first!");
            // Human makes the first move
            System.out.println("Enter your move (row and column, 0-based, separated by space):");
            int row = scanner.nextInt();
            int col = scanner.nextInt();
            Move<TicTacToe> move = new TicTacToe.TicTacToeMove(1 - game.opener(), row, col);
            state = state.next(move);
        } else {
            System.out.println("Random choice: MCTS goes first!");
            // MCTS will go first automatically in the loop
        }

        while (!state.isTerminal()) {
            System.out.println("Current board:");
            System.out.println(state);

            if (state.player() == game.opener()) {
                // MCTS's turn
//                MCTS mcts = new MCTS(new TicTacToeNode(state));
            	MCTS mcts = new MCTS(new TicTacToeNode(state), isHumanFirst);
            	
            	long startTime = System.currentTimeMillis(); // stop watch
            	
                Node<TicTacToe> bestChild = mcts.run(50000); // run 20000 simulations
                
             // End timing
                long endTime = System.currentTimeMillis();

                // Calculate and print elapsed time
                System.out.println("MCTS move decision took " + (endTime - startTime) + " ms.");
                
                state = bestChild.state();
                System.out.println("MCTS played:");
                System.out.println(state);
            } else {
                // Human's turn ask for input
                System.out.println("Your move (row and column, 0-based, separated by space):");
                int row = scanner.nextInt();
                int col = scanner.nextInt();
                Move<TicTacToe> move = new TicTacToe.TicTacToeMove(state.player(), row, col);
                state = state.next(move);
            }
        }

        if (state.winner().isPresent()) {
            System.out.println("Game over! Winner: " + (state.winner().get() == game.opener() ? "MCTS" : "Human"));
        } else {
            System.out.println("Game over! It's a draw!");
        }
    }

    public MCTS(Node<TicTacToe> root, boolean isHumanFirst) {
        this.root = root;
        this.expandedPositions = new HashSet<>();
        this.isHumanFirst = isHumanFirst; // Store already expanded board positions(prevent using duplicate symmetries)
        
        this.mctsPlayer = root.state().game().opener();
    }
    
    /*
     * run simulations(100 times) and return best child node
     */
    public Node<TicTacToe> run(int iterations) {
    	expandedPositions.clear();
        for (int i = 0; i < iterations; i++) {
            simulate(root);
        }
        return bestChild(root);
    }
    
    /*
     * 4 phase each simulation:
     *  Selection -> Expansion -> Rollout -> Back propagation
     *  Selection: use ucb1 function to pick best node
     *  Expansion: expand possible moves
     *  Rollout: move randomly up to 4 steps ahead
     *  Back propagation: give scores back up to parent node
     */
    private void simulate(Node<TicTacToe> node) {
        List<Node<TicTacToe>> path = new ArrayList<>();
        
        path.add(node);
        
        Node<TicTacToe> current = node;

        while (!current.isLeaf() && !current.children().isEmpty()) {
            current = select(current);
            path.add(current);
        }

        if (!current.state().isTerminal()) {
            expand(current);
            if (!current.children().isEmpty()) {
                current = select(current);
                path.add(current);
            }
        }

        int result = rollout(current.state(), current);

        for (Node<TicTacToe> nodeInPath : path) {
            update(nodeInPath, result);
        }
    }
    
    /*
     * select best score by using ucb1 function.
     */
    private Node<TicTacToe> select(Node<TicTacToe> node) {
        double logParent = Math.log(Math.max(1, node.playouts()));
        return node.children().stream()
                .max(Comparator.comparing(c -> ucb1(c, logParent)))
                .orElseThrow();
    }
    
    /*
     * ucb1 function
     */
    private double ucb1(Node<TicTacToe> node, double logParentPlayouts) {
        if (node.playouts() == 0) return Double.MAX_VALUE; // if node is not visited, assign large value to prioritize.
        return (double) node.wins() / node.playouts() + Math.sqrt(2 * logParentPlayouts / node.playouts());
    }

    /*
     * expand possibilities from current board, normalize(check for symmetric), expand only identical board.
     */
    private void expand(Node<TicTacToe> node) {
        int currentPlayer = node.state().player();

        for (Move<TicTacToe> move : node.state().moves(currentPlayer)) {
            State<TicTacToe> nextState = node.state().next(move);
            Position normalized = ((TicTacToe.TicTacToeState) nextState).position().normalize();
            if (!expandedPositions.contains(normalized)) {
                node.addChild(nextState);
                expandedPositions.add(normalized);
            }
        }
    }
    
    
    /*
     * 5 random moves
     */
    private int rollout(State<TicTacToe> state, Node<TicTacToe> parentNode) {
        State<TicTacToe> rolloutState = state;
        int player = rolloutState.player();
        int depth = 0;

        Set<Position> visited = new HashSet<>();
        visited.add(((TicTacToe.TicTacToeState) rolloutState).position().normalize());

        while (!rolloutState.isTerminal() && depth < 5) { // evaluate final board after 4 moves or game end
            List<Move<TicTacToe>> moves = new ArrayList<>(rolloutState.moves(player));
            List<Move<TicTacToe>> filteredMoves = new ArrayList<>();

            for (Move<TicTacToe> move : moves) {
                State<TicTacToe> potentialState = rolloutState.next(move);
                Position normalized = ((TicTacToe.TicTacToeState) potentialState).position().normalize();
                if (!visited.contains(normalized)) {
                    filteredMoves.add(move);
                }
            }

            if (filteredMoves.isEmpty()) {
                break;
            }
            
            Move<TicTacToe> bestMove = null;
            // Check if current player can win immediately
            for (Move<TicTacToe> move : filteredMoves) {
                State<TicTacToe> potentialState = rolloutState.next(move);
                if (potentialState.winner().isPresent() && potentialState.winner().get() == player) {
                    bestMove = move;
                    break;
                }
            }
            
            // Check if human will immediately win after MCTS play this move, if yes MCTS will not choose it.
            if (bestMove == null) {
                int opponent = 1 - player;
                for (Move<TicTacToe> move : filteredMoves) {
                    State<TicTacToe> potentialState = rolloutState.next(move);
                    for (Move<TicTacToe> oppMove : potentialState.moves(opponent)) {
                        State<TicTacToe> oppNextState = potentialState.next(oppMove);
                        if (oppNextState.winner().isPresent() && oppNextState.winner().get() == opponent) {
                            bestMove = move; // blocks opponent
                            break;
                        }
                    }
                    if (bestMove != null) break;
                }
            }
            
            // Check if opponent have 2 in a row already and will win soon
            if (bestMove == null) {
                int opponent = 1 - player;
                for (Move<TicTacToe> move : filteredMoves) {
                    State<TicTacToe> potentialState = rolloutState.next(move);
                    Position pos = ((TicTacToe.TicTacToeState) potentialState).position();
                    if (twoInARowThreatCheck(pos, opponent)) {
                        bestMove = move; // block the threat
                        break;
                    }
                }
            }
            
            
            // if 3 previous situations are not met, explore randomly
            if (bestMove == null) {
                bestMove = filteredMoves.get(random.nextInt(filteredMoves.size()));
            }       

//            Move move = filteredMoves.get(random.nextInt(filteredMoves.size()));
            rolloutState = rolloutState.next(bestMove);
            visited.add(((TicTacToe.TicTacToeState) rolloutState).position().normalize());
            player = 1 - player;
            depth++;
        }

//        int parentScore = evaluate(parentNode.state(), parentNode.state().player());
//        int childScore = evaluate(rolloutState, parentNode.state().player());
        int parentScore = evaluate(parentNode.state(), mctsPlayer);
        int childScore  = evaluate(rolloutState,     mctsPlayer);

        if (childScore - parentScore >= 40) { // if score increase suddenly over 40, means about to win
            childScore += 100;
        }

        return childScore;
    }
    
    /*
     * update playouts and wins
     */
    private void update(Node<TicTacToe> node, int score) {
//        node.backPropagate();
    	if (node instanceof TicTacToeNode ticTacToeNode) {
            ticTacToeNode.increment(score);
        }
    }
    
    /*
     * choose best score child after some simulations
     */
    private Node<TicTacToe> bestChild(Node<TicTacToe> node) {
        return node.children().stream()
                .max(Comparator.comparing(c -> (double) c.wins() / c.playouts()))
                .orElseThrow();
    }
    
    /*
     * Score Evaluation
     * player: MCTS 
     * Opponent: human player
     */
    private int evaluate(State<TicTacToe> state, int player) {
//        Position position = state.position();
    	Position position = ((TicTacToe.TicTacToeState) state).position();
        int score = 0;

        if (position.winner().isPresent()) {
            int winner = position.winner().get();
            if (winner == player) {
            	score += isHumanFirst ? 100 : 140; 
            } else if (winner == 1 - player) {
            	score -= isHumanFirst ? 140 : 100;
            }
        }

        if (position.projectRow(1)[1] == player) score += 10; // when MCTS player takes the center
        
        // when MCTS takes corners
        if (position.projectRow(0)[0] == player) score += 5; 
        if (position.projectRow(0)[2] == player) score += 5;
        if (position.projectRow(2)[0] == player) score += 5;
        if (position.projectRow(2)[2] == player) score += 5;
        
        // when MCTS takes 2 in a row
//        for (int i = 0; i < 3; i++) {
//            score += twoInARow(position.projectRow(i), player);
//            score += twoInARow(position.projectCol(i), player);
//        }
//        //check for diagonal
//        score += twoInARow(position.projectDiag(true), player); // top-left to bottom-right
//        score += twoInARow(position.projectDiag(false), player); // top-right to bottom-left
//
//        int opponent = 1 - player;
//        for (int i = 0; i < 3; i++) {
//            score -= twoInARow(position.projectRow(i), opponent);
//            score -= twoInARow(position.projectCol(i), opponent);
//        }
//        score -= twoInARow(position.projectDiag(true), opponent);
//        score -= twoInARow(position.projectDiag(false), opponent);
        
        /*
         * Different rewards for MCTS/human moves first
         */
        int opponent = 1 - player;

        for (int i = 0; i < 3; i++) {
            if (isHumanFirst) {
                // Defensive mode
                score += twoInARow(position.projectRow(i), player);          // normal reward for MCTS own 2-in-a-row
                score -= 2 * twoInARow(position.projectRow(i), opponent);    // double penalty for human's 2-in-a-row
                score += twoInARow(position.projectCol(i), player);
                score -= 2 * twoInARow(position.projectCol(i), opponent);
            } else {
                // Aggressive mode
                score += 2 * twoInARow(position.projectRow(i), player);       // double reward for MCTS own 2-in-a-row
                score -= twoInARow(position.projectRow(i), opponent);         // normal penalty for human's 2-in-a-row
                score += 2 * twoInARow(position.projectCol(i), player);
                score -= twoInARow(position.projectCol(i), opponent);
            }
        }

        // For diagonals
        if (isHumanFirst) {
            score += twoInARow(position.projectDiag(true), player);
            score -= 2 * twoInARow(position.projectDiag(true), opponent);
            score += twoInARow(position.projectDiag(false), player);
            score -= 2 * twoInARow(position.projectDiag(false), opponent);
        } else {
            score += 2 * twoInARow(position.projectDiag(true), player);
            score -= twoInARow(position.projectDiag(true), opponent);
            score += 2 * twoInARow(position.projectDiag(false), player);
            score -= twoInARow(position.projectDiag(false), opponent);
        }

        return score;
    }
    
    /*
     * Check if 2 in a row with 1 empty space
     */
    private int twoInARow(int[] line, int player) {
        int countPlayer = 0;
        int countBlank = 0;
        for (int x : line) {
            if (x == player) countPlayer++;
            if (x == -1) countBlank++;
        }
        if (countPlayer == 2 && countBlank == 1) {
            return 20;
        }
        return 0;
    }
    
    /*
     * helper function of threat check using twoInARow function
     */
     boolean twoInARowThreatCheck(Position position, int player) {
        for (int i = 0; i < 3; i++) {
            if (twoInARow(position.projectRow(i), player) > 0) return true;
            if (twoInARow(position.projectCol(i), player) > 0) return true;
        }
        if (twoInARow(position.projectDiag(true), player) > 0) return true;
        if (twoInARow(position.projectDiag(false), player) > 0) return true;
        return false;
    }


}