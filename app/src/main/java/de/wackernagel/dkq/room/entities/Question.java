package de.wackernagel.dkq.room.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

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
    public String image;
    public int published;
    public int version;
    public String lastUpdate;

    public boolean isInvalid() {
        return id == 0L && quizId == 0L && number == 0 && question == null && answer == null && image == null
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
                ", image='" + image + '\'' +
                ", published=" + published +
                ", version=" + version +
                ", lastUpdate='" + lastUpdate + '\'' +
                '}';
    }
}
