package de.wackernagel.dkq;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.room.Room;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import de.wackernagel.dkq.room.AppDatabase;
import de.wackernagel.dkq.room.message.Message;
import de.wackernagel.dkq.room.message.MessageDao;

import static de.wackernagel.dkq.TestUtils.getValue;

@RunWith(AndroidJUnit4.class)
public class RoomTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private MessageDao messageDao;
    private AppDatabase db;

    @Before
    public void createDb() {
        Context context = InstrumentationRegistry.getTargetContext();
        db = Room.inMemoryDatabaseBuilder( context, AppDatabase.class ).build();
        messageDao = db.messageDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void insertAndQueryMessage() throws Exception {
        Assert.assertEquals( 0, messageDao.loadMaxMessageNumber() );

        final int messageNumber = 1;
        Message expected = messageDao.insert( createMessage( messageNumber, Message.Type.ARTICLE ) );

        Assert.assertEquals( messageNumber, messageDao.loadMaxMessageNumber() );

        Message numberMessage = messageDao.loadMessageByNumber( messageNumber );
        Assert.assertEquals( expected, numberMessage );

        LiveData<Message> idMessage = messageDao.loadMessage( expected.getId() );
        Assert.assertEquals( expected, getValue( idMessage ) );
    }

    @Test
    public void checkMessageConstraints() throws Exception {
        // create a message
        Message firstArticle = createMessage( 1, Message.Type.ARTICLE );
        messageDao.insert( firstArticle );
        Assert.assertEquals(1, firstArticle.getId() );

        // create a second message
        Message secondArticle = createMessage( 2, Message.Type.ARTICLE );
        messageDao.insert( secondArticle );
        Assert.assertEquals(2, secondArticle.getId() );

        // query all message and check size
        assertListSize( 2, messageDao.loadMessages() );

        // insertion of a already persisted entity is ignored
        messageDao.insert( firstArticle );
        Assert.assertEquals("Unique message.id constraint failed.",-1, firstArticle.getId() );
        assertListSize( 2, messageDao.loadMessages() );

        // number and type are unique so the insertion should be ignored
        Message duplicateFirstArticle = createMessage( 1, Message.Type.ARTICLE );
        messageDao.insert( duplicateFirstArticle );
        Assert.assertEquals("Unique message.type and message.number constraint failed.", -1, duplicateFirstArticle.getId() );
        assertListSize( 2, messageDao.loadMessages() );

        // create message of another type which is part of unique constraint
        Message ofOtherType = createMessage( 1, Message.Type.UPDATE_LOG );
        messageDao.insert( ofOtherType );
        // NOTE: The existing messages have the id's 1, 2 and 4.
        // 3 is skipped because the number & type constraint brokes the insert but the auto-increment id was already triggered.
        Assert.assertEquals( 4, ofOtherType.getId() );
        assertListSize( 3, messageDao.loadMessages() );
    }

    private <T> void assertListSize( final int expectedSize, final LiveData<List<T>> listLiveData ) throws InterruptedException {
        Assert.assertEquals( expectedSize, getValue( listLiveData ).size() );
    }

    private Message createMessage( int number, Message.Type type ) {
        final Message message = new Message();
        message.setType( type );
        message.setNumber( number );
        return message;
    }
}
