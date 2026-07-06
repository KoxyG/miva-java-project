package model;

public class Magazine extends LibraryItem {
    private int issueNumber;
    private String publisher;

    public Magazine(String id, String title, String author, int year, int issueNumber, String publisher) {
        super(id, title, author, year);
        this.issueNumber = issueNumber;
        this.publisher = publisher;
    }

    @Override
    public String getItemType() {
        return "Magazine";
    }

    @Override
    public String getCategory() {
        return "Magazines";
    }

    public int getIssueNumber() {
        return issueNumber;
    }

    public void setIssueNumber(int issueNumber) {
        this.issueNumber = issueNumber;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" | Issue: %d, Publisher: %s", issueNumber, publisher);
    }
}
