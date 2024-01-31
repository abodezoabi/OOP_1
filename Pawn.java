public class Pawn extends ConcretePiece {

    //DATA
    private final String type1 = "♙";
    private final String type2 = "♟";

    //CONSTRUCTOR
    public Pawn(Player player, int num, String possesor) {
        this.player = player;
        this.num = num;
        this.possessor = possesor;
    }
    @Override
    public String getType() {
        if (getOwner().isPlayerOne()) {
            return type1;
        } else return type2;
    }


}