package model;

public class Journal extends LibraryItem {
    private String volume;
    private String field;

    public Journal(String id, String title, String author, int year, String volume, String field) {
        super(id, title, author, year);
        this.volume = volume;
        this.field = field;
    }

    @Override
    public String getItemType() {
        return "Journal";
    }

    @Override
    public String getCategory() {
        return "Journals";
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" | Vol: %s, Field: %s", volume, field);
    }
}
