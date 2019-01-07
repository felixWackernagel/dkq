package de.wackernagel.dkq.room.entities;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity( tableName = "quizzes",
         indices = { @Index( value = { "number" }, unique = true ) },
         foreignKeys = { @ForeignKey(entity = Quizzer.class, parentColumns = "id", childColumns = "winnerId" ),
                         @ForeignKey(entity = Quizzer.class, parentColumns = "id", childColumns = "quizMasterId" ) } )
public class Quiz {
    @PrimaryKey( autoGenerate = true)
    public long id;
    public int number;
    public String location;
    public String address;
    public String quizDate;
    @Nullable public Long quizMasterId = null;
    @Nullable public Long winnerId = null;
    public double latitude;
    public double longitude;
    public int published;
    public int version;
    public String lastUpdate;

    @Ignore
    public Quizzer quizMaster;
    @Ignore
    public Quizzer winner;

    public boolean isInvalid() {
        return id == 0L && number == 0 && address == null && location == null && quizMasterId == null && winnerId == null &&
                quizDate == null && version == 0 && longitude == 0.0D && latitude == 0.0D && published == 0 &&
                lastUpdate == null;
    }

    @Override
    public String toString() {
        return "Quiz{" +
                "id=" + id +
                ", number=" + number +
                ", location='" + location + '\'' +
                ", address='" + address + '\'' +
                ", quizDate='" + quizDate + '\'' +
                ", quizMasterId=" + quizMasterId +
                ", winnerId=" + winnerId +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", published=" + published +
                ", version=" + version +
                ", lastUpdate='" + lastUpdate + '\'' +
                '}';
    }
}
