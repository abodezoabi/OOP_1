public class ConcretePlayer implements Player {

    //DATA
    protected boolean defender;
    private int wins;

    //CONSTRUCTOR
    public ConcretePlayer(boolean defender) {
        this.defender = defender;
        wins = 0;
    }

    @Override
    public boolean isPlayerOne() {
        return this.defender;
    }
    //wins adder
    public int add_wins(){//adds number of wins
        return wins+=1;
    }
    @Override
    public int getWins() {
        return this.wins;
    }
}