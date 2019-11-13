package de.wackernagel.dkq.room.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity( tableName = "quizzers", indices = { @Index( value = { "number" }, unique = true ) } )
public class Quizzer {
    @PrimaryKey( autoGenerate = true)
    public long id;
    public int number;
    public String name;
    public String image;
    public int version;
    public String lastUpdate;

    public boolean isInvalid() {
        return id == 0L && number == 0 && name == null && image == null && version == 0 && lastUpdate == null;
    }

    @NonNull
    @Override
    public String toString() {
        return "Quizzer{" +
                "id=" + id +
                ", number=" + number +
                ", name=" + name +
                ", image=" + image +
                ", version=" + version +
                ", lastUpdate='" + lastUpdate + '\'' +
                '}';
    }
}
