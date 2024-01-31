import java.util.Stack;
public class Position {
    //DATA
    // Represents the column
    private int _x;
    // Represents the row
    private int _y;
    protected Stack<Piece> stack_of_P = new Stack<Piece>();

    // CONSTRUCTOR
    public Position(int x, int y) {
        this._x = x;
        this._y = y;
    }

    // Getters
    public int get_X() {
        return this._x;
    }
    public int get_Y() {
        return this._y;
    }

    public String toString() {return new String ("(" + this._x + ", " + this._y + ")");}
    public String toString_() {
        return new String  ("(" + this._x + ", " + this._y + ")"+this.stack_of_P.size()+" pieces\n");
    }
    protected void add_to_Stack(Piece p){
        if(!(this.stack_of_P.contains(p))){this.stack_of_P.add(p);}
    }

}