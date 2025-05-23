# Extendable Tic‑Tac‑Toe with MCTS

This floder contains an Extendable-Tic‑Tac‑Toe game (ETTT), within which the players can choose to either place a sign or extend the board for each move. This implementation driven by a Monte Carlo Tree Search (MCTS). 

## Game Rules

**Starting board:** a 3×3 window in the center of a 9×9 grid, all other cells are “zombies” (unplayable).

**Players**: X and O.

**Turns**: on each turn, the player may either:

_Place_ their mark (X or O) in any empty cell of the current available board, or

_Extend_ the avilable by adding 3×3 block(s) in one of the eight compass directions, if it stays within the 9×9 bounds (with the starting board at middle); for N(North), S(South), W(West) and E (East), you extend 1 3×3 block at this direction, but for other directions like NE, you'll extend all boards that are currently not available among N(North), E(East) and NE directions, so CHOOSE WISELY!

**Board growth**: when you extend, any newly exposed cells become empty (playable).  The window slides from (rowMin..rowMax)×(colMin..colMax) to include the new block.

**Win condition:** first to get three of their marks in a row (horizontal, vertical, or diagonal) within the current window wins.

**Draw:** if the current available board is full (all cells occupied and no player extending the board) or no further extension is possible, and no three‑in‑a‑row exists, the game is a draw.

## How to Play

When the game first start, the program randomly decides who goes first (Human or MCTS).  If you go first, you will immediately be prompted to pick a move.

**Board display**: the current active window is printed as a grid of . (empty), X, and O.

**Listing moves**: you will see a numbered list:

Place (r,c) uses local coordinates (0..height-1, 0..width-1) relative to the top‑left of the window.

Extend DIRECTION to grow the board in one of N, NE, E, SE, S, SW, W, NW.

**Selecting**: type the index of your desired move and press Enter.

**MCTS turn:** the AI will run a preset number of simulations (30,000 by default) and then play its best move.

Repeat until someone wins or the game draws.

## Customization

**Simulation count:** edit the run(…) call in MCTS.java to change AI thinking depth.

**Heuristics:** the playout evaluate(...) and rollout logic live in MCTS.java—tweak these to bias the AI.

**Window size:** the maximum 9×9 and extension block size (3) are constants in ExtendablePosition.java.

