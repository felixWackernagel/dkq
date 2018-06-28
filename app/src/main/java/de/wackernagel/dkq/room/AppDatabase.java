package de.wackernagel.dkq.room;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import de.wackernagel.dkq.room.daos.QuestionDao;
import de.wackernagel.dkq.room.daos.QuizDao;
import de.wackernagel.dkq.room.entities.Question;
import de.wackernagel.dkq.room.entities.Quiz;

@Database( entities = { Quiz.class, Question.class }, version = 1 )
public abstract class AppDatabase extends RoomDatabase {
    public abstract QuizDao quizDao();
    public abstract QuestionDao questionDao();
}
