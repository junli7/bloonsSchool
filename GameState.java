public class GameState {
    private int money;

    public GameState(int initialMoney) {
        this.money = initialMoney;
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
    }
}