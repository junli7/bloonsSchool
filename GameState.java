public class GameState{
    private int money;
    private int bloonsKilled;
    private int currentWave;
    private int lives;
    private final int initialLives;
    private final int initialMoney;

    public GameState(int initialMoney, int initialLives){
        this.initialMoney = initialMoney;
        this.initialLives = initialLives;
        reset();
   }

    public int getMoney(){ return money;}
    public boolean canAfford(int cost){ return money >= cost;}

    public void spendMoney(int amount){
        if (canAfford(amount)){
            money -= amount;
       }
   }
    public void addMoney(int amount){ money += amount;}
    public int getBloonsKilled(){ return bloonsKilled;}
    public void incrementBloonsKilled(int count){ this.bloonsKilled += count;}
    public int getCurrentWave(){ return currentWave; }

    public void incrementWave(){
        this.currentWave++;
   }

    public int getLives(){ return lives;}

    public void loseLife(int amount){
        this.lives -= amount;
        if (this.lives < 0){
            this.lives = 0;
       }
   }

    public boolean isGameOver(){
        return this.lives <= 0;
   }

    public void reset(){
        this.money = this.initialMoney;
        this.lives = this.initialLives;
        this.bloonsKilled = 0;
        this.currentWave = 0;
   }
}