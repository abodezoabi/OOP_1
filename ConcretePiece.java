import java.util.ArrayList;
import java.util.Stack;

public abstract class ConcretePiece implements Piece {
    // Declaration of the class and its attributes
    protected ArrayList<Position> pathOfThePiece = new ArrayList<Position>(); // Stores the path taken by the piece.
    protected Player player; // Represents the player owning the piece.
    protected int num; // A number probably identifying the piece.
    protected String possessor; // A string identifier for the owner of the piece.
    protected int kills = 0; // Counts the number of "kills" made by this piece.
    protected int dist = 0; // Distance traveled by the piece.

    @Override
    public Player getOwner() {
        return this.player; // Method to get the player owning the piece.
    }

    protected void up_kills() {
        this.kills++; // Method to increase the kill count of the piece.
    }

    protected void print_path_of_the_Piece() {
        // Method to display the path traveled by the piece.
        if (this.pathOfThePiece.size() > 0) {
            // Print the path if the piece has moved.
            System.out.print(new String(this.possessor + num + ": [" + this.pathOfThePiece.get(0).toString()));
            for (int i = 1; i < this.pathOfThePiece.size(); i++) {
                System.out.print(new String(", " + this.pathOfThePiece.get(i).toString()));
            }
            System.out.print(new String("]\n"));
        }
    }

    protected void print_kills() {
        // Method to display the number of kills by the piece.
        if (this.kills > 0) {
            System.out.print(new String(this.possessor + num + ": " + this.kills + " kills\n"));
        }
    }

    public void print_distance() {
        // Method to display the distance traveled by the piece.
        if (this.dist != 0) {
            System.out.print(new String(this.possessor + num + ": " + this.dist + " squares\n"));
        }
    }
}