import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
public class GameLogic implements PlayableLogic {
    // Protected field storing the position of a specific piece, possibly the King.
    protected Position P_of_K = new Position(5, 5);

    // Data fields
    // A 2D array representing the game board with ConcretePiece objects.
    ConcretePiece[][] gameboard = new ConcretePiece[11][11];
    // A 2D array representing the positions on the game board.
    Position[][] gameboardP = new Position[11][11];
    // A list to store all the pieces present in the game.
    ArrayList<ConcretePiece> List_OfAllThe_Piece = new ArrayList<ConcretePiece>();
    // Two player objects representing the first and second players.
    private ConcretePlayer player1 = new ConcretePlayer(true);
    private ConcretePlayer player2 = new ConcretePlayer(false);
    // A boolean flag to indicate if it is the second player's turn (attack mode).
    private boolean attackmode;

    // Constructor
    public GameLogic() {
        // Initializes the game board upon creating a GameLogic object.
        this.Starting_Board();
    }

    // Implementations of PlayableLogic interface methods
    @Override
    public ConcretePiece getPieceAtPosition(Position position) {
        // Returns the piece located at a given position on the game board.
        return gameboard[position.get_X()][position.get_Y()];
    }

    @Override
    public Player getFirstPlayer() {
        // Returns the first player.
        return this.player1;
    }

    @Override
    public Player getSecondPlayer() {
        // Returns the second player.
        return this.player2;
    }

    @Override
    public int getBoardSize() {
        // Returns the size of the game board.
        return 11;
    }

    @Override
    public boolean isSecondPlayerTurn() {
        // Returns true if it's the second player's turn, based on the attack mode flag.
        return attackmode;
    }

    @Override
    public void reset() {
        // Resets the game board and re-initializes the starting state of the game.
        this.gameboard = new ConcretePiece[11][11];
        P_of_K = new Position(5,5);
        this.Starting_Board();
    }


    @Override
    public boolean move(Position a, Position b) {
        // Checks for invalid moves or if it's the wrong player's turn.
        if (invalidmoves(a, b) ||
                (isSecondPlayerTurn() && getPieceAtPosition(a).getOwner().isPlayerOne()) ||
                (!isSecondPlayerTurn() && !getPieceAtPosition(a).getOwner().isPlayerOne())) {
            return false;
        }

        // Retrieve starting position coordinates.
        int startPosX = a.get_X();
        int startPosY = a.get_Y();
        // Retrieve the piece that is moving.
        ConcretePiece movingPiece = this.gameboard[startPosX][startPosY];

        // Move the piece to the new position.
        int endPosX = b.get_X();
        int endPosY = b.get_Y();
        this.gameboard[endPosX][endPosY] = movingPiece;
        // Clear the piece from its original position.
        this.gameboard[startPosX][startPosY] = null;

        // Update the distance traveled by the piece.
        this.gameboard[endPosX][endPosY].dist += (Math.abs(startPosX - endPosX) + Math.abs(startPosY - endPosY));

        // If the piece moved is a King, update its position.
        if (getPieceAtPosition(new Position(endPosX, endPosY)).getType().equals("♔")) {
            this.P_of_K = new Position(endPosX, endPosY);
        }

        // Add the new position to the piece's path history.
        this.gameboard[endPosX][endPosY].pathOfThePiece.add(new Position(endPosX, endPosY));

        // Check and execute any killing conditions as a result of the move.
        kill(b);

        // Toggle the attackmode to change the player's turn.
        attackmode = !isSecondPlayerTurn();

        // Add the moved piece to the stack at the new position on gameboardP.
        this.gameboardP[endPosX][endPosY].add_to_Stack(this.gameboard[endPosX][endPosY]);

        // If the game is finished, print the game data.
        if(isGameFinished()){printData();}

        return true;
    }

    public boolean invalidmoves(Position a, Position b) {
        // 1 a and b are in board
        if (!in_Board(a) || !in_Board(b)) {return true;}
        // 2 check if the start or end positions are invalid
        if (this.gameboard[a.get_X()][a.get_Y()] == null || this.gameboard[b.get_X()][b.get_Y()] != null) {return true;}
        // 3 check if the move is on X-axis or Y-axis not diagonal
        if (a.get_X() != b.get_X() && a.get_Y() != b.get_Y()) {return true;}
        // 4 prevent all pieces except King from moving to corners
        if (cornerpos(b) && !(this.gameboard[a.get_X()][a.get_Y()] instanceof King)) {return true;}
        // 5 check for any pieces blocking the path between a and b
        boolean isSameRow = a.get_Y() == b.get_Y();
        int startX = a.get_X(), endX = b.get_X(), startY = a.get_Y(), endY = b.get_Y();
        if (startX > endX) {
            int temp = startX;
            startX = endX;
            endX = temp;
        }
        if (startY > endY) {
            int temp = startY;
            startY = endY;
            endY = temp;
        }
        for (int i = startX + 1; i < endX; i++) {
            if (isSameRow && this.gameboard[i][a.get_Y()] != null) return true;
        }
        for (int j = startY + 1; j < endY; j++) {
            if (!isSameRow && this.gameboard[a.get_X()][j] != null) return true;
        }
        return false;
    }

    public void kill(Position position) {
        // Check if the king is in a corner position. If so, player1 wins.
        if (cornerpos(P_of_K)) {player1.add_wins();}
        else {
            // Calculate positions around the given position
            Position up = new Position(position.get_X(), position.get_Y() - 1);
            Position up_up = new Position(position.get_X(), position.get_Y() - 2);
            Position down = new Position(position.get_X(), position.get_Y() + 1);
            Position down_down = new Position(position.get_X(), position.get_Y() + 2);
            Position right = new Position(position.get_X() + 1, position.get_Y());
            Position right_right = new Position(position.get_X() + 2, position.get_Y());
            Position left = new Position(position.get_X() - 1, position.get_Y());
            Position left_left = new Position(position.get_X() - 2, position.get_Y());

            // Check for kill conditions based on the piece type at the given position
            // and surrounding positions.
            if (getPieceAtPosition(position).getType().equals("♟")) {
                //Check down
                if (in_Board(down)) {
                    if (getPieceAtPosition(down) != null && getPieceAtPosition(down).getType().equals("♙")) {
                        if (out_of_board(down_down) ||
                                (in_Board(down_down) && getPieceAtPosition(down_down) != null && getPieceAtPosition(down_down).getType().equals("♟")) ||
                                cornerpos(down_down)) {
                            this.gameboard[down.get_X()][down.get_Y()] = null;
                            getPieceAtPosition(position).up_kills();
                        }
                    }
                }
                //Check up
                if (in_Board(up)) {
                    if (getPieceAtPosition(up) != null && getPieceAtPosition(up).getType().equals("♙")) {
                        if (out_of_board(up_up) ||
                                (in_Board(up_up) && getPieceAtPosition(up_up) != null && getPieceAtPosition(up_up).getType().equals("♟")) ||
                                cornerpos(up_up)) {
                            this.gameboard[up.get_X()][up.get_Y()] = null;
                            getPieceAtPosition(position).up_kills();
                        }
                    }
                }
                //Check right
                if (in_Board(right)) {
                    if (getPieceAtPosition(right) != null && getPieceAtPosition(right).getType().equals("♙")) {
                        if (out_of_board(right_right) ||
                                (in_Board(right_right) && getPieceAtPosition(right_right) != null && getPieceAtPosition(right_right).getType().equals("♟")) ||
                                cornerpos(right_right)) {
                            this.gameboard[right.get_X()][right.get_Y()] = null;
                            getPieceAtPosition(position).up_kills();
                        }
                    }
                }
                //Check left
                if (in_Board(left)) {
                    if (getPieceAtPosition(left) != null && getPieceAtPosition(left).getType().equals("♙")) {
                        if (out_of_board(left_left) ||
                                (in_Board(left_left) && getPieceAtPosition(left_left) != null && getPieceAtPosition(left_left).getType().equals("♟")) ||
                                cornerpos(left_left)) {
                            this.gameboard[left.get_X()][left.get_Y()] = null;
                            getPieceAtPosition(position).up_kills();
                        }
                    }
                }
            }
            if (getPieceAtPosition(position).getType().equals("♙")) {
                //Check down
                if (in_Board(down)) {
                    if (getPieceAtPosition(down) != null && getPieceAtPosition(down).getType().equals("♟")) {
                        if (out_of_board(down_down) ||
                                (in_Board(down_down) && getPieceAtPosition(down_down) != null && getPieceAtPosition(down_down).getType().equals("♙")) ||
                                cornerpos(down_down)) {
                            this.gameboard[down.get_X()][down.get_Y()] = null;
                            getPieceAtPosition(position).up_kills();
                        }
                    }
                }
                //Check up
                if (in_Board(up)) {
                    if (getPieceAtPosition(up) != null && getPieceAtPosition(up).getType().equals("♟")) {
                        if (out_of_board(up_up) ||
                                (in_Board(up_up) && getPieceAtPosition(up_up) != null && getPieceAtPosition(up_up).getType().equals("♙")) ||
                                cornerpos(up_up)) {
                            this.gameboard[up.get_X()][up.get_Y()] = null;
                            getPieceAtPosition(position).up_kills();
                        }
                    }
                }
                //Check right
                if (in_Board(right)) {
                    if (getPieceAtPosition(right) != null && getPieceAtPosition(right).getType().equals("♟")) {
                        if (out_of_board(right_right) ||
                                (in_Board(right_right) && getPieceAtPosition(right_right) != null && getPieceAtPosition(right_right).getType().equals("♙")) ||
                                cornerpos(right_right)) {
                            this.gameboard[right.get_X()][right.get_Y()] = null;
                            getPieceAtPosition(position).up_kills();
                        }
                    }
                }
                //Check left
                if (in_Board(left)) {
                    if (getPieceAtPosition(left) != null && getPieceAtPosition(left).getType().equals("♟")) {
                        if (out_of_board(left_left) ||
                                (in_Board(left_left) && getPieceAtPosition(left_left) != null && getPieceAtPosition(left_left).getType().equals("♙")) ||
                                cornerpos(left_left)) {
                            this.gameboard[left.get_X()][left.get_Y()] = null;
                            getPieceAtPosition(position).up_kills();
                        }
                    }
                }
            }
            // Additional checks for the king's position and surrounding threats.
            // Check each direction around the king for threats or escape possibilities.
            // Increment 'temp' for each blocked or threatened direction.
            Position p = P_of_K;
            int temp = 0;
            up = new Position(p.get_X(), p.get_Y() - 1);
            down = new Position(p.get_X(), p.get_Y() + 1);
            right = new Position(p.get_X() + 1, p.get_Y());
            left = new Position(p.get_X() - 1, p.get_Y());
            if (in_Board(down)) {
                if ((getPieceAtPosition(down) != null && getPieceAtPosition(down).getType().equals("♟")) || cornerpos(down)) {
                    temp++;}
            }
            if (out_of_board(down)) {temp++;}
            if (in_Board(up)) {
                if ((getPieceAtPosition(up) != null && getPieceAtPosition(up).getType().equals("♟")) || cornerpos(up)) {
                    temp++;}
            }
            if (out_of_board(up)) {temp++;}
            if (in_Board(right)) {
                if ((getPieceAtPosition(right) != null && getPieceAtPosition(right).getType().equals("♟")) || cornerpos(right)) {
                    temp++;}
            }
            if (out_of_board(right)) {temp++;}
            if (in_Board(left)) {
                if ((getPieceAtPosition(left) != null && getPieceAtPosition(left).getType().equals("♟")) || cornerpos(left)) {
                    temp++;}
            }
            if (out_of_board(left)) {temp++;}

            // If all four directions around the king are blocked/threatened, player2 wins.
            if (temp == 4) {
                this.P_of_K = null;
                player2.add_wins();
            }
        }
    }
    @Override
    public boolean isGameFinished() {
        // Check if the game is finished based on the condition of the king (P_of_K).
        // The game is considered finished if either:
        // 1. P_of_K is null, which may indicate the king has been captured or removed.
        // 2. The king (P_of_K) is in a corner position and player1 has won at least once.
        if (this.P_of_K == null || (cornerpos(P_of_K) && player1.getWins() >= 1)) {
            return true; // Game is finished
        }
        return false; // Game is not finished
    }

    @Override
    public void undoLastMove() {
        ////////////////////////
        ////////////////////////
        ////////////////////////
    }

    public boolean cornerpos(Position pos) {
        // Get the X and Y coordinates of the position.
        int x = pos.get_X();
        int y = pos.get_Y();
        // Check if both X and Y coordinates are at the edges of the board (0 or 10).
        // This is true for corner positions in a board with indices ranging from 0 to 10.
        boolean isMultOfTenX = x % 10 == 0;
        boolean isMultOfTenY = y % 10 == 0;
        // Return true if both coordinates are at the edges, indicating a corner position.
        return isMultOfTenX && isMultOfTenY;
    }

    public boolean in_Board(Position p) {
        // Check if the position's coordinates are within the bounds of the board.
        // The board is assumed to be 11x11, with valid indices from 0 to 10.
        return p.get_X() >= 0 && p.get_X() <= 10 && p.get_Y() >= 0 && p.get_Y() <= 10;
    }

    public boolean out_of_board(Position p) {
        // Check if the position's coordinates are outside the bounds of the board.
        // If any coordinate is less than 0 or greater than 10, the position is outside the board.
        if (p.get_X() > 10 || p.get_Y() > 10 || p.get_X() < 0 || p.get_Y() < 0) {
            return true;
        } else return false;
    }

    public void Starting_Board() {
        // Initializes the game board. This two-dimensional loop iterates through each cell
        // of the gameboardP array, creating a new Position object for each cell.
        // The Position object likely stores the coordinates or other relevant information for that cell.
        for (int x = 0; x < this.gameboardP.length; x++) {
            for (int y = 0; y < this.gameboardP[x].length; y++) {
                this.gameboardP[x][y] = new Position(x, y);
            }
        }

        // Sets the 'attackmode' flag to true. This could indicate a specific phase or mode
        // in the game, perhaps changing how players can interact or how pieces move.
        this.attackmode = true;

        // The following lines place various chess pieces (Pawns and a King) on the board.
        // Each piece is instantiated with specific attributes (player, number, type) and
        // placed at a specific position on the gameboard array.
        this.gameboard[0][3] = new Pawn(player2, 7, "A");
        this.gameboard[0][4] = new Pawn(player2, 9, "A");
        this.gameboard[0][5] = new Pawn(player2, 11, "A");
        this.gameboard[0][6] = new Pawn(player2, 15, "A");
        this.gameboard[0][7] = new Pawn(player2, 17, "A");
        this.gameboard[1][5] = new Pawn(player2, 12, "A");
        this.gameboard[3][0] = new Pawn(player2, 1, "A");
        this.gameboard[4][0] = new Pawn(player2, 2, "A");
        this.gameboard[5][0] = new Pawn(player2, 3, "A");
        this.gameboard[6][0] = new Pawn(player2, 4, "A");
        this.gameboard[10][3] = new Pawn(player2, 8, "A");
        this.gameboard[7][0] = new Pawn(player2, 5, "A");
        this.gameboard[5][1] = new Pawn(player2, 6, "A");
        this.gameboard[10][4] = new Pawn(player2, 10, "A");
        this.gameboard[10][5] = new Pawn(player2, 14, "A");
        this.gameboard[10][6] = new Pawn(player2, 16, "A");
        this.gameboard[10][7] = new Pawn(player2, 18, "A");
        this.gameboard[9][5] = new Pawn(player2, 13, "A");
        this.gameboard[3][10] = new Pawn(player2, 20, "A");
        this.gameboard[4][10] = new Pawn(player2, 21, "A");
        this.gameboard[5][10] = new Pawn(player2, 22, "A");
        this.gameboard[6][10] = new Pawn(player2, 23, "A");
        this.gameboard[7][10] = new Pawn(player2, 24, "A");
        this.gameboard[5][9] = new Pawn(player2, 19, "A");
        this.gameboard[5][3] = new Pawn(player1, 1, "D");
        this.gameboard[4][4] = new Pawn(player1, 2, "D");
        this.gameboard[5][4] = new Pawn(player1, 3, "D");
        this.gameboard[6][4] = new Pawn(player1, 4, "D");
        this.gameboard[3][5] = new Pawn(player1, 5, "D");
        this.gameboard[4][5] = new Pawn(player1, 6, "D");
        this.gameboard[5][5] = new King(player1, 7, "K");
        this.gameboard[6][5] = new Pawn(player1, 8, "D");
        this.gameboard[7][5] = new Pawn(player1, 9, "D");
        this.gameboard[4][6] = new Pawn(player1, 10, "D");
        this.gameboard[5][6] = new Pawn(player1, 11, "D");
        this.gameboard[6][6] = new Pawn(player1, 12, "D");
        this.gameboard[5][7] = new Pawn(player1, 13, "D");

        // These lines add the initial position to the pathOfThePiece attribute of each piece.
        // This likely keeps track of the movement or potential movement paths of the pieces.
        this.gameboard[0][3].pathOfThePiece.add(new Position(0,3 ));
        this.gameboard[0][4].pathOfThePiece.add(new Position(0,4 ));
        this.gameboard[0][5].pathOfThePiece.add(new Position(0,5 ));
        this.gameboard[0][6].pathOfThePiece.add(new Position(0,6 ));
        this.gameboard[0][7].pathOfThePiece.add(new Position(0,7 ));
        this.gameboard[1][5].pathOfThePiece.add(new Position(1, 5));
        this.gameboard[3][0].pathOfThePiece.add(new Position(3, 0));
        this.gameboard[4][0].pathOfThePiece.add(new Position(4,0 ));
        this.gameboard[5][0].pathOfThePiece.add(new Position(5, 0));
        this.gameboard[6][0].pathOfThePiece.add(new Position(6,0 ));
        this.gameboard[10][3].pathOfThePiece.add(new Position(10,3 ));
        this.gameboard[7][0].pathOfThePiece.add(new Position(7, 0));
        this.gameboard[5][1].pathOfThePiece.add(new Position(5,1 ));
        this.gameboard[10][4].pathOfThePiece.add(new Position(10,4 ));
        this.gameboard[10][5].pathOfThePiece.add(new Position(10,5 ));
        this.gameboard[10][6].pathOfThePiece.add(new Position(10,6 ));
        this.gameboard[10][7].pathOfThePiece.add(new Position(10,7 ));
        this.gameboard[9][5].pathOfThePiece.add(new Position(9,5 ));
        this.gameboard[3][10].pathOfThePiece.add(new Position(3,10 ));
        this.gameboard[4][10].pathOfThePiece.add(new Position(4, 10));
        this.gameboard[5][10].pathOfThePiece.add(new Position(5, 10));
        this.gameboard[6][10].pathOfThePiece.add(new Position(6,10));
        this.gameboard[7][10].pathOfThePiece.add(new Position(7, 10));
        this.gameboard[5][9].pathOfThePiece.add(new Position(5,9));
        this.gameboard[5][3].pathOfThePiece.add(new Position(5,3 ));
        this.gameboard[4][4].pathOfThePiece.add(new Position(4,4));
        this.gameboard[5][4].pathOfThePiece.add(new Position(5,4));
        this.gameboard[6][4].pathOfThePiece.add(new Position(6,4));
        this.gameboard[3][5].pathOfThePiece.add(new Position(3,5));
        this.gameboard[4][5].pathOfThePiece.add(new Position(4,5));
        this.gameboard[5][5].pathOfThePiece.add(new Position(5,5 ));
        this.gameboard[6][5].pathOfThePiece.add(new Position(6, 5));
        this.gameboard[7][5].pathOfThePiece.add(new Position(7,5 ));
        this.gameboard[4][6].pathOfThePiece.add(new Position(4,6));
        this.gameboard[5][6].pathOfThePiece.add(new Position(5, 6));
        this.gameboard[6][6].pathOfThePiece.add(new Position(6,6));
        this.gameboard[5][7].pathOfThePiece.add(new Position(5,7));

        // Adds each piece to a stack at its corresponding position on a different representation
        // of the game board (gameboardP). This could be used for a different layer or aspect of the game,
        // such as graphical representation or user interface.
        this.gameboardP[0][3].add_to_Stack(this.gameboard[0][3]);
        this.gameboardP[0][4].add_to_Stack(this.gameboard[0][4]);
        this.gameboardP[0][5].add_to_Stack(this.gameboard[0][5]);
        this.gameboardP[0][6].add_to_Stack(this.gameboard[0][6]);
        this.gameboardP[0][7].add_to_Stack(this.gameboard[0][7]);
        this.gameboardP[1][5].add_to_Stack(this.gameboard[1][5]);
        this.gameboardP[3][0].add_to_Stack(this.gameboard[3][0]);
        this.gameboardP[4][0].add_to_Stack(this.gameboard[4][0]);
        this.gameboardP[6][0].add_to_Stack(this.gameboard[6][0]);
        this.gameboardP[5][0].add_to_Stack(this.gameboard[5][0]);
        this.gameboardP[5][1].add_to_Stack(this.gameboard[5][1]);
        this.gameboardP[7][0].add_to_Stack(this.gameboard[7][0]);
        this.gameboardP[10][3].add_to_Stack(this.gameboard[10][3]);
        this.gameboardP[10][4].add_to_Stack(this.gameboard[10][4]);
        this.gameboardP[10][5].add_to_Stack(this.gameboard[10][5]);
        this.gameboardP[10][6].add_to_Stack(this.gameboard[10][6]);
        this.gameboardP[10][7].add_to_Stack(this.gameboard[10][7]);
        this.gameboardP[9][5].add_to_Stack(this.gameboard[9][5]);
        this.gameboardP[3][10].add_to_Stack(this.gameboard[3][10]);
        this.gameboardP[4][10].add_to_Stack(this.gameboard[4][10]);
        this.gameboardP[5][10].add_to_Stack(this.gameboard[5][10]);
        this.gameboardP[6][10].add_to_Stack(this.gameboard[6][10]);
        this.gameboardP[7][10].add_to_Stack(this.gameboard[7][10]);
        this.gameboardP[5][9].add_to_Stack(this.gameboard[5][9]);
        this.gameboardP[5][3].add_to_Stack(this.gameboard[5][3]);
        this.gameboardP[4][4].add_to_Stack(this.gameboard[4][4]);
        this.gameboardP[5][4].add_to_Stack(this.gameboard[5][4]);
        this.gameboardP[6][4].add_to_Stack(this.gameboard[6][4]);
        this.gameboardP[3][5].add_to_Stack(this.gameboard[3][5]);
        this.gameboardP[4][5].add_to_Stack(this.gameboard[4][5]);
        this.gameboardP[5][5].add_to_Stack(this.gameboard[5][5]);
        this.gameboardP[6][5].add_to_Stack(this.gameboard[6][5]);
        this.gameboardP[7][5].add_to_Stack(this.gameboard[7][5]);
        this.gameboardP[4][6].add_to_Stack(this.gameboard[4][6]);
        this.gameboardP[5][6].add_to_Stack(this.gameboard[5][6]);
        this.gameboardP[6][6].add_to_Stack(this.gameboard[6][6]);
        this.gameboardP[5][7].add_to_Stack(this.gameboard[5][7]);

        // Finally, all pieces are added to a list, List_OfAllThe_Piece. This list might be used
        // for iterating over all pieces for various game logic operations, like checking game state
        // or rendering.
        this.List_OfAllThe_Piece.add(this.gameboard[0][3]);
        this.List_OfAllThe_Piece.add(this.gameboard[0][4]);
        this.List_OfAllThe_Piece.add(this.gameboard[0][5]);
        this.List_OfAllThe_Piece.add(this.gameboard[0][6]);
        this.List_OfAllThe_Piece.add(this.gameboard[0][7]);
        this.List_OfAllThe_Piece.add(this.gameboard[1][5]);
        this.List_OfAllThe_Piece.add(this.gameboard[3][0]);
        this.List_OfAllThe_Piece.add(this.gameboard[4][0]);
        this.List_OfAllThe_Piece.add(this.gameboard[5][0]);
        this.List_OfAllThe_Piece.add(this.gameboard[6][0]);
        this.List_OfAllThe_Piece.add(this.gameboard[5][1]);
        this.List_OfAllThe_Piece.add(this.gameboard[7][0]);
        this.List_OfAllThe_Piece.add(this.gameboard[10][3]);
        this.List_OfAllThe_Piece.add(this.gameboard[10][4]);
        this.List_OfAllThe_Piece.add(this.gameboard[10][5]);
        this.List_OfAllThe_Piece.add(this.gameboard[10][6]);
        this.List_OfAllThe_Piece.add(this.gameboard[10][7]);
        this.List_OfAllThe_Piece.add(this.gameboard[9][5]);
        this.List_OfAllThe_Piece.add(this.gameboard[3][10]);
        this.List_OfAllThe_Piece.add(this.gameboard[4][10]);
        this.List_OfAllThe_Piece.add(this.gameboard[5][10]);
        this.List_OfAllThe_Piece.add(this.gameboard[6][10]);
        this.List_OfAllThe_Piece.add(this.gameboard[7][10]);
        this.List_OfAllThe_Piece.add(this.gameboard[5][9]);
        this.List_OfAllThe_Piece.add(this.gameboard[5][3]);
        this.List_OfAllThe_Piece.add(this.gameboard[4][4]);
        this.List_OfAllThe_Piece.add(this.gameboard[5][4]);
        this.List_OfAllThe_Piece.add(this.gameboard[6][4]);
        this.List_OfAllThe_Piece.add(this.gameboard[3][5]);
        this.List_OfAllThe_Piece.add(this.gameboard[4][5]);
        this.List_OfAllThe_Piece.add(this.gameboard[5][5]);
        this.List_OfAllThe_Piece.add(this.gameboard[6][5]);
        this.List_OfAllThe_Piece.add(this.gameboard[7][5]);
        this.List_OfAllThe_Piece.add(this.gameboard[4][6]);
        this.List_OfAllThe_Piece.add(this.gameboard[5][6]);
        this.List_OfAllThe_Piece.add(this.gameboard[6][6]);
        this.List_OfAllThe_Piece.add(this.gameboard[5][7]);
    }


    public void printData(){
        // Create two ArrayLists to store copies of ConcretePiece objects,
        // differentiated by their types or associations.
        ArrayList<ConcretePiece> copy_of_A_Piece = new ArrayList<ConcretePiece>();
        ArrayList<ConcretePiece> copy_of_D_Piece = new ArrayList<ConcretePiece>();

        // Iterate over all pieces in List_OfAllThe_Piece to classify them into A or D types
        // based on their player association and whether they have moved (path size > 1).
        for(int i=0; i<this.List_OfAllThe_Piece.size(); i++){
            if(this.List_OfAllThe_Piece.get(i).pathOfThePiece.size()>1) {
                if (this.List_OfAllThe_Piece.get(i).player == player1) {
                    copy_of_D_Piece.add(this.List_OfAllThe_Piece.get(i));
                } else {
                    copy_of_A_Piece.add(this.List_OfAllThe_Piece.get(i));
                }
            }
        }

        // Sort each copy list (A and D pieces) based on the size of their path
        // and a numerical attribute.
        Collections.sort(copy_of_A_Piece, new Comparator<ConcretePiece>() {
            @Override
            public int compare(ConcretePiece t1, ConcretePiece t2) {
                int result1 = Integer.compare(t1.pathOfThePiece.size(), t2.pathOfThePiece.size());
                if (result1 == 0) {
                    return Integer.compare(t1.num, t2.num);
                } else return result1;
            }
        });
        Collections.sort(copy_of_D_Piece, new Comparator<ConcretePiece>() {
            @Override
            public int compare(ConcretePiece t1, ConcretePiece t2) {
                int result1 = Integer.compare(t1.pathOfThePiece.size(), t2.pathOfThePiece.size());
                if (result1 == 0) {
                    return Integer.compare(t1.num, t2.num);
                } else return result1;
            }
        });

        // Print the paths of pieces in A and D lists based on the attack mode.
        // The order of printing changes depending on whether attack mode is active.
        if(attackmode==false){
            for (int i = 0; i < copy_of_A_Piece.size(); i++) {
                copy_of_A_Piece.get(i).print_path_of_the_Piece();
            }
            for (int i = 0; i < copy_of_D_Piece.size(); i++) {
                copy_of_D_Piece.get(i).print_path_of_the_Piece();
            }
        }
        else {
            for (int i = 0; i < copy_of_D_Piece.size(); i++) {
                copy_of_D_Piece.get(i).print_path_of_the_Piece();
            }
            for (int i = 0; i < copy_of_A_Piece.size(); i++) {
                copy_of_A_Piece.get(i).print_path_of_the_Piece();
            }
        }
        System.out.println("***************************************************************************");

        // Sort the main List_OfAllThe_Piece based on the number of kills,
        // the numerical attribute, and the player association, considering the attack mode.
        Collections.sort(this.List_OfAllThe_Piece, new Comparator<ConcretePiece>() {
            @Override
            public int compare(ConcretePiece t1, ConcretePiece t2) {
                int result1 = -Integer.compare(t1.kills, t2.kills);
                if (result1 == 0) {
                    int result2 = Integer.compare(t1.num, t2.num);
                    if (result2 == 0) {
                        if(attackmode==true){
                            return Boolean.compare(t1.player.isPlayerOne(),t2.player.isPlayerOne());
                        }
                        else{return Boolean.compare(t2.player.isPlayerOne(),t1.player.isPlayerOne());}
                    }
                    return result2;
                }
                return result1;
            }
        });
        // Print the number of kills for each piece in the List_OfAllThe_Piece.
        for (int i = 0; i < this.List_OfAllThe_Piece.size(); i++) {
            this.List_OfAllThe_Piece.get(i).print_kills();
        }
        System.out.println("***************************************************************************");

        // Sort the main List_OfAllThe_Piece based on the distance attribute,
        // the numerical attribute, and the player association, considering the attack mode.
        Collections.sort(this.List_OfAllThe_Piece, new Comparator<ConcretePiece>() {
            @Override
            public int compare(ConcretePiece t1, ConcretePiece t2) {
                int result1 = -Integer.compare(t1.dist,t2.dist);
                if (result1 == 0) {
                    int result2 = Integer.compare(t1.num, t2.num);
                    if (result2 == 0) {
                        if(attackmode==true){
                            return Boolean.compare(t1.player.isPlayerOne(),t2.player.isPlayerOne());
                        }
                        else{return Boolean.compare(t2.player.isPlayerOne(),t1.player.isPlayerOne());}
                    }
                    return result2;
                }
                return result1;
            }
        });
        // Print the distance for each piece in the List_OfAllThe_Piece.
        for (int i = 0; i < this.List_OfAllThe_Piece.size(); i++) {
            this.List_OfAllThe_Piece.get(i).print_distance();
        }
        System.out.println("***************************************************************************");

        // Create a list of positions from the game board, adding only non-null positions.
        ArrayList<Position> positionsList = new ArrayList<>();
        for (int i = 0; i < this.gameboardP.length; i++) {
            for (int j = 0; j < this.gameboardP[i].length; j++) {
                if (this.gameboardP[i][j] != null) {
                    positionsList.add(this.gameboardP[i][j]);
                }
            }
        }
        // Sort the positions based on the size of their stacks and their coordinates.
        Collections.sort(positionsList, new Comparator<Position>() {
            @Override
            public int compare(Position t1, Position t2) {
                int result1 = Integer.compare(t2.stack_of_P.size(), t1.stack_of_P.size());
                if (result1 == 0) {
                    int result2 = Integer.compare(t1.get_X(), t2.get_X());
                    if (result2 == 0) { return Integer.compare(t1.get_Y(), t2.get_Y());}
                    return result2;
                }
                return result1;
            }
        });
        // Print the details of each position in the sorted positions list.
        for (int i = 0; i < positionsList.size(); i++) {
            if(positionsList.get(i).stack_of_P.size()>1){System.out.print(positionsList.get(i).toString_());}
        }
        System.out.println("***************************************************************************");
    }

}