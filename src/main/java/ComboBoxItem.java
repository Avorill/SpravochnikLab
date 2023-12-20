public class ComboBoxItem {
    private String id;
    private String displayValue;

    public ComboBoxItem(String id, String displayValue) {
        this.id = id;
        this.displayValue = displayValue;
    }

    public String getId() {
        return id;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    @Override
    public String toString() {
        return displayValue;
    }
}
