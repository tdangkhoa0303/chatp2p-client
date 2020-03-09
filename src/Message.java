import java.time.LocalDate;

public class Message {
    private String content;
    private boolean isSender;
    private LocalDate date;

    public Message(String content, boolean isSender, LocalDate date) {
        this.content = content;
        this.isSender = isSender;
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isSender() {
        return isSender;
    }

    public void setSender(boolean sender) {
        isSender = sender;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
