public class King extends ConcretePiece {

    //DATA
    private final String type = "♔";

    //CONSTRUCTOR
    public King(Player player, int num, String possesor) {
        this.player = player;
        this.num = num;
        this.possessor = possesor;
    }

    @Override
    public String getType() {
        return this.type;
    }
}
