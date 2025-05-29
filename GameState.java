public class GameState {
    private int money;
    private int bloonsKilled; // Renamed from humansKilled for consistency if you prefer
    private int currentWave; // Represents the wave number (1-indexed)

    public GameState(int initialMoney) {
        this.money = initialMoney;
        this.bloonsKilled = 0;
        this.currentWave = 0; // Start at 0, meaning pre-game. First wave will be 1.
    }

    public int getMoney() { return money; }
    public boolean canAfford(int cost) { return money >= cost; }

    public void spendMoney(int amount) {
        if (canAfford(amount)) {
            money -= amount;
        } else {
            System.out.println("Attempted to spend " + amount + " but not enough money.");
        }
    }
    public void addMoney(int amount) { money += amount; }
    public int getBloonsKilled() { return bloonsKilled; }
    public void incrementBloonsKilled(int count) { this.bloonsKilled += count; }
    public int getCurrentWave() { return currentWave; }

    // Called when player initiates the next wave
    public void incrementWave() {
        this.currentWave++;
        System.out.println("GameState: Advanced to wave " + this.currentWave);
    }
}