package de.wackernagel.dkq.room.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

@Entity( tableName = "quizzes", indices = { @Index( value = { "number" }, unique = true ) } )
public class Quiz {
    @PrimaryKey( autoGenerate = true)
    public long id;
    public int number;
    public String location;
    public String address;
    public String quizDate;
    public String quizMaster;
    public double latitude;
    public double longitude;
    public int published;
    public int version;
    public String lastUpdate;

    @Override
    public String toString() {
        return "Quiz{" +
                "id=" + id +
                ", number=" + number +
                ", location='" + location + '\'' +
                ", address='" + address + '\'' +
                ", quizDate='" + quizDate + '\'' +
                ", quizMaster='" + quizMaster + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", published=" + published +
                ", version=" + version +
                ", lastUpdate='" + lastUpdate + '\'' +
                '}';
    }
}
