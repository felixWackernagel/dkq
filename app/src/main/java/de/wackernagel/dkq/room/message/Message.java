package de.wackernagel.dkq.room.message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

import java.util.Objects;

import de.wackernagel.dkq.room.BaseEntity;
import de.wackernagel.dkq.room.entities.Quiz;

@Entity(tableName = "messages",
        indices = { @Index( value = { "number", "type" }, unique = true ),
                    @Index( value = { "quizId" } ) },
        foreignKeys = @ForeignKey(entity = Quiz.class, parentColumns = "id", childColumns = "quizId" ) )
public class Message extends BaseEntity {

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

    private int number;
    private String title;
    private String content;
    private String image;
    private int version;
    private String lastUpdate;
    private boolean read;
    @NonNull
    private Type type;
    @Nullable
    private Long quizId;

    @Nullable
    @Ignore
    private Integer quizNumber;

    @Ignore
    private static final Message INVALID = new Message();

    @Ignore
    public Message() {
        super( 0 );
        this.number = 0;
        this.title = null;
        this.content = null;
        this.image = null;
        this.version = 0;
        this.lastUpdate = null;
        this.read = false;
        this.type = Type.ARTICLE;
        this.quizId = null;
        this.quizNumber = null;
    }

    /*
     * This constructor is used by Room
     */
    protected Message( final long id, final int number, final String title, final String content, final String image, final int version, final String lastUpdate, final boolean read, @NonNull final Type type, @Nullable final Long quizId ) {
        super( id );
        this.number = number;
        this.title = title;
        this.content = content;
        this.image = image;
        this.version = version;
        this.lastUpdate = lastUpdate;
        this.read = read;
        this.type = type;
        this.quizId = quizId;
        this.quizNumber = null;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    @NonNull
    public Type getType() {
        return type;
    }

    public void setType( @NonNull Type type) {
        this.type = type;
    }

    @Nullable
    public Long getQuizId() {
        return quizId;
    }

    public void setQuizId( @Nullable Long quizId) {
        this.quizId = quizId;
    }

    /**
     * This field is only used by GSON to parse this information from a json string and is converted to the quizId field.
     *
     * @return The number of a quiz which is related to this message or null.
     */
    @Nullable
    public Integer getQuizNumber() {
        return quizNumber;
    }

    public boolean isInvalid() {
        return INVALID.equals( this );
    }

    @NonNull
    @Override
    public String toString() {
        return "Message {" +
                "id=" + getId() +
                ", number=" + number +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", image='" + image + '\'' +
                ", version=" + version +
                ", lastUpdate='" + lastUpdate + '\'' +
                ", read=" + read + '\'' +
                ", type=" + type + '\'' +
                ", quizId=" + quizId + '\'' +
                ", quizNumber=" + quizNumber +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Message message = (Message) o;
        return number == message.number &&
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
        return Objects.hash(super.hashCode(), number, title, content, image, version, lastUpdate, read, type, quizId, quizNumber);
    }
}
