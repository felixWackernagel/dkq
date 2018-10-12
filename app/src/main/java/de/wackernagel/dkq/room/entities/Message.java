package de.wackernagel.dkq.room.entities;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity( tableName = "messages", indices = { @Index( value = { "number" }, unique = true ) } )
public class Message {
    @PrimaryKey( autoGenerate = true)
    public long id;
    public int number;
    public String title;
    public String content;
    public String image;
    public int version;
    public String lastUpdate;
    public int read;

    public boolean isInvalid() {
        return id == 0 && number == 0 && title == null && content == null && image == null && version == 0 && lastUpdate == null && read == 0;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", number=" + number +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", image='" + image + '\'' +
                ", version=" + version +
                ", lastUpdate='" + lastUpdate + '\'' +
                ", read=" + read +
                '}';
    }
}
