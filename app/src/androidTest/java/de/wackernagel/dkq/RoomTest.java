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

import de.wackernagel.dkq.room.AppDatabase;
import de.wackernagel.dkq.room.daos.MessageDao;
import de.wackernagel.dkq.room.entities.Message;

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
        Message expected = createMessage( 1, Message.Type.ARTICLE );
        long newId = messageDao.insertMessage( expected );
        expected.id = newId;

        Message numberMessage = messageDao.loadMessageByNumber( 1 );
        Assert.assertEquals( expected, numberMessage );

        LiveData<Message> idMessage = messageDao.loadMessage( newId );
        Assert.assertEquals( expected, getValue( idMessage ) );
    }

    private Message createMessage( int number, Message.Type type ) {
        final Message message = new Message();
        message.type = type;
        message.number = number;
        message.title = "A";
        message.content = "B";
        message.image = null;
        message.lastUpdate = null;
        message.version = 1;
        message.read = false;
        message.quizId = null;
        return message;
    }
}
