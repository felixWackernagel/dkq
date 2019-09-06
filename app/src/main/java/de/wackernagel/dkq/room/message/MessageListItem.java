package de.wackernagel.dkq.room.message;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * MessageListItem is a read only class to display reduced information from messages table.
 * It can only created by the MessageDao class.
 */
public class MessageListItem {
    private long id;
    private int number;
    private String title;
    private String content;
    private String image;
    private boolean read;
    @NonNull
    private Message.Type type;

    MessageListItem( long id, int number, String title, String content, String image, boolean read, @NonNull Message.Type type ) {
        this.id = id;
        this.number = number;
        this.title = title;
        this.content = content;
        this.image = image;
        this.read = read;
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public int getNumber() {
        return number;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getImage() {
        return image;
    }

    public boolean isRead() {
        return read;
    }

    @NonNull
    public Message.Type getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageListItem that = (MessageListItem) o;
        return id == that.id &&
                number == that.number &&
                read == that.read &&
                Objects.equals(title, that.title) &&
                Objects.equals(content, that.content) &&
                Objects.equals(image, that.image) &&
                type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, number, title, content, image, read, type);
    }

    @NonNull
    @Override
    public String toString() {
        return "MessageListItem{" +
                "id=" + id +
                ", number=" + number +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", image='" + image + '\'' +
                ", read=" + read +
                ", type=" + type +
                '}';
    }
}
