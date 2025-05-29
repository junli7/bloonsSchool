
public class GameState {
    private int money;
    private int bloonsKilled;
    private int currentWave;

    public GameState(int initialMoney) {
        this.money = initialMoney;
        this.bloonsKilled = 0;
        this.currentWave = 1;
    }

    public int getMoney() {
        return money;
    }

    public boolean canAfford(int cost) {
        return money >= cost;
    }

    public void spendMoney(int amount) {
        if (canAfford(amount)) {
            money -= amount;
            System.out.println("Spent " + amount + ". Remaining money: " + money);
        } else {
            System.out.println("Attempted to spend " + amount + " but not enough money.");
        }
    }

    public void addMoney(int amount) {
        money += amount;
        System.out.println("Added " + amount + " money. Total: " + money);
    }

    public int getBloonsKilled() {
        return bloonsKilled;
    }

    public void incrementBloonsKilled(int count) {
        this.bloonsKilled += count;
    }

    public int getCurrentWave() {
        return currentWave;
    }

    public void incrementWave() {
        this.currentWave++;
        System.out.println("Advanced to wave " + this.currentWave);
    }
}