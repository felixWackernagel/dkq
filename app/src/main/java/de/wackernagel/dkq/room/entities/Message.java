package de.wackernagel.dkq.room.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "messages",
        indices = { @Index( value = { "number", "type" }, unique = true ),
                    @Index( value = { "quizId" } ) },
        foreignKeys = @ForeignKey(entity = Quiz.class, parentColumns = "id", childColumns = "quizId" ) )
public class Message {

    public enum Type {
        ARTICLE(0),
        UPDATE_LOG(1);

        private final int code;

        Type( final int code ) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static Type byCode( int code ) {
            for( Type type : Type.values() ) {
                if( type.getCode() == code ) {
                    return type;
                }
            }
            return Type.ARTICLE;
        }
    }

    @PrimaryKey( autoGenerate = true)
    public long id;
    public int number;
    public String title;
    public String content;
    public String image;
    public int version;
    public String lastUpdate;
    public boolean read;
    @NonNull
    public Type type;
    @Nullable
    public Long quizId = null;

    @Ignore
    public Integer quizNumber = null;

    public boolean isInvalid() {
        return id == 0 && number == 0 && title == null && content == null && image == null && version == 0 && lastUpdate == null && type == null && quizId == null;
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
                ", read=" + read + '\'' +
                ", type=" + type + '\'' +
                ", quizId=" + quizId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return id == message.id &&
                number == message.number &&
                version == message.version &&
                read == message.read &&
                Objects.equals(title, message.title) &&
                Objects.equals(content, message.content) &&
                Objects.equals(image, message.image) &&
                Objects.equals(lastUpdate, message.lastUpdate) &&
                type == message.type &&
                Objects.equals(quizId, message.quizId) &&
                Objects.equals(quizNumber, message.quizNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, number, title, content, image, version, lastUpdate, read, type, quizId, quizNumber);
    }
}
