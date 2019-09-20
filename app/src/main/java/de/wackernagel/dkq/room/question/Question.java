package de.wackernagel.dkq.room.question;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

import java.util.Objects;

import de.wackernagel.dkq.room.BaseEntity;
import de.wackernagel.dkq.room.entities.Quiz;

@Entity( tableName = "questions",
         indices = { @Index( value = { "number", "quizId" }, unique = true ),
                     @Index( value = { "quizId" } ) },
         foreignKeys = @ForeignKey(entity = Quiz.class, parentColumns = "id", childColumns = "quizId" ) )
public class Question extends BaseEntity {
    private long quizId;
    private int number;
    private String question;
    private String answer;
    private String image;
    private int version;
    private String lastUpdate;

    @Ignore
    private static final Question INVALID = new Question();

    @Ignore
    public Question() {
        super( 0 );
        this.quizId = 0;
        this.number = 0;
        this.question = null;
        this.answer = null;
        this.image = null;
        this.version = 0;
        this.lastUpdate = null;
    }

    /*
     * This constructor is used by Room
     */
    protected Question(long id, long quizId, int number, String question, String answer, String image, int version, String lastUpdate) {
        super( id );
        this.quizId = quizId;
        this.number = number;
        this.question = question;
        this.answer = answer;
        this.image = image;
        this.version = version;
        this.lastUpdate = lastUpdate;
    }

    public long getQuizId() {
        return quizId;
    }

    public void setQuizId(long quizId) {
        this.quizId = quizId;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public boolean isInvalid() {
        return INVALID.equals( this );
    }

    @NonNull
    @Override
    public String toString() {
        return "Question{" +
                "id=" + getId() +
                ", quizId=" + quizId +
                ", number=" + number +
                ", question='" + question + '\'' +
                ", answer='" + answer + '\'' +
                ", image='" + image + '\'' +
                ", version=" + version +
                ", lastUpdate='" + lastUpdate + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Question question1 = (Question) o;
        return quizId == question1.quizId &&
                number == question1.number &&
                version == question1.version &&
                Objects.equals(question, question1.question) &&
                Objects.equals(answer, question1.answer) &&
                Objects.equals(image, question1.image) &&
                Objects.equals(lastUpdate, question1.lastUpdate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), quizId, number, question, answer, image, version, lastUpdate);
    }
}
