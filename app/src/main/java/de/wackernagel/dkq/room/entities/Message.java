package de.wackernagel.dkq.room.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "messages",
        indices = { @Index( value = { "number" }, unique = true ) },
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
    public int read;
    @NonNull
    public Type type;
    @Nullable
    public Long quizId = null;

    @Ignore
    public Integer quizNumber = null;

    public boolean isInvalid() {
        return id == 0 && number == 0 && title == null && content == null && image == null && version == 0 && lastUpdate == null && read == 0 && type == null && quizId == null;
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
}
