package de.wackernagel.dkq.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import androidx.room.TypeConverters;
import de.wackernagel.dkq.DkqConstants;
import de.wackernagel.dkq.room.converter.MessageTypeConverter;
import de.wackernagel.dkq.room.daos.QuestionDao;
import de.wackernagel.dkq.room.daos.QuizDao;
import de.wackernagel.dkq.room.daos.QuizzerDao;
import de.wackernagel.dkq.room.entities.Question;
import de.wackernagel.dkq.room.entities.Quiz;
import de.wackernagel.dkq.room.entities.Quizzer;
import de.wackernagel.dkq.room.message.Message;
import de.wackernagel.dkq.room.message.MessageDao;

@Database( entities = { Quiz.class, Question.class, Message.class, Quizzer.class}, version = DkqConstants.Database.VERSION )
@TypeConverters({MessageTypeConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract QuizDao quizDao();
    public abstract QuestionDao questionDao();
    public abstract MessageDao messageDao();
    public abstract QuizzerDao quizzerDao();
}
