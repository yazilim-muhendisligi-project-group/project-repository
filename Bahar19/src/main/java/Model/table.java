package Model;

public class table {
    private int id;
    private boolean isFull;
    private int tableNumber;

    public int getId() {
        return id;
    }

    public boolean getIsFull() {
        return isFull;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setIsFull(boolean isFull) {
        this.isFull = isFull;
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }
}
