package de.wackernagel.dkq.room.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

@Entity( tableName = "questions", indices = { @Index( value = { "number", "quizId" }, unique = true ) }, foreignKeys = @ForeignKey(entity = Quiz.class,
        parentColumns = "id",
        childColumns = "quizId" ) )
public class Question {
    @PrimaryKey( autoGenerate = true)
    public long id;
    public long quizId;
    public int number;
    public String question;
    public String answer;
    public int published;
    public int version;
    public String lastUpdate;

    public boolean isInvalid() {
        return id == 0L && quizId == 0L && number == 0 && question == null && answer == null
                && published == 0 && version == 0 && lastUpdate == null;
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", quizId=" + quizId +
                ", number=" + number +
                ", question='" + question + '\'' +
                ", answer='" + answer + '\'' +
                ", published=" + published +
                ", version=" + version +
                ", lastUpdate='" + lastUpdate + '\'' +
                '}';
    }
}
