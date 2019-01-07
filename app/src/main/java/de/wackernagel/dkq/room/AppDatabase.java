package de.wackernagel.dkq.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import de.wackernagel.dkq.room.daos.MessageDao;
import de.wackernagel.dkq.room.daos.QuestionDao;
import de.wackernagel.dkq.room.daos.QuizDao;
import de.wackernagel.dkq.room.daos.QuizzerDao;
import de.wackernagel.dkq.room.entities.Message;
import de.wackernagel.dkq.room.entities.Question;
import de.wackernagel.dkq.room.entities.Quiz;
import de.wackernagel.dkq.room.entities.Quizzer;

@Database( entities = { Quiz.class, Question.class, Message.class, Quizzer.class}, version = 3 )
public abstract class AppDatabase extends RoomDatabase {
    public abstract QuizDao quizDao();
    public abstract QuestionDao questionDao();
    public abstract MessageDao messageDao();
    public abstract QuizzerDao quizzerDao();
}
