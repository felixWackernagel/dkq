package de.wackernagel.dkq.room.message;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import de.wackernagel.dkq.room.BaseDao;

@Dao
public abstract class MessageDao extends BaseDao<Message> {

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

}
