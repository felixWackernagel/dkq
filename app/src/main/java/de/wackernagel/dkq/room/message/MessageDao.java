package de.wackernagel.dkq.room.message;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import static androidx.room.OnConflictStrategy.IGNORE;
import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public abstract class MessageDao {

    @Query( "SELECT id, number, title, content, image, read, type FROM messages ORDER BY number DESC, type DESC" )
    public abstract LiveData<List<MessageListItem>> loadMessages();

    @Query( "SELECT * FROM messages WHERE id = :messageId" )
    public abstract LiveData<Message> loadMessage(long messageId);

    @Query( "SELECT * FROM messages WHERE number = :messageNumber" )
    public abstract Message loadMessageByNumber(int messageNumber);

    @Query( "SELECT COUNT(read) FROM messages WHERE read = 0" )
    public abstract LiveData<Integer> loadNewMessagesCount();

    @Query( "SELECT MAX(number) FROM messages" )
    public abstract int loadMaxMessageNumber();

    @Query( "DELETE FROM messages" )
    public abstract void deleteAllMessages();

    @Query( "DELETE FROM messages WHERE id = :messageId" )
    public abstract int deleteMessage( long messageId );

    /**
     * By using IGNORE as onConflict value is the return value -1 on a failure.
     * By using REPLACE as onConflict value is the existing row dropped and then inserted which triggers foreign key actions.
     *
     * @param message which should be persisted
     * @return The new id of message.
     */
    @Insert( onConflict = IGNORE )
    abstract long insertOneMessage( Message message );

    public Message insertMessage( Message message )
    {
        final long id = insertOneMessage( message );
        message.setId( id );
        return message;
    }

    @Update( onConflict = IGNORE)
    public abstract int updateMessage(Message message);

}
