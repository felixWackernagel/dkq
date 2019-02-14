package de.wackernagel.dkq.room.daos;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import de.wackernagel.dkq.room.entities.Message;
import de.wackernagel.dkq.room.entities.MessageListItem;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface MessageDao {

    @Query( "SELECT id, number, title, content, image, read FROM messages ORDER BY number DESC" )
    LiveData<List<MessageListItem>> loadMessages();

    @Query( "SELECT * FROM messages WHERE id = :messageId" )
    LiveData<Message> loadMessage(long messageId);

    @Query( "SELECT * FROM messages WHERE number = :messageNumber" )
    Message loadMessageByNumber(int messageNumber);

    @Query( "SELECT COUNT(read) FROM messages WHERE read = 0" )
    LiveData<Integer> loadNewMessagesCount();

    @Query( "DELETE FROM messages" )
    void deleteAllMessages();

    @Insert( onConflict = REPLACE)
    void insertMessages(Message... message);

    @Update( onConflict = REPLACE)
    void updateMessage(Message message);

}
