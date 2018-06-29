package de.wackernagel.dkq.room.daos;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import de.wackernagel.dkq.room.entities.Question;
import de.wackernagel.dkq.room.entities.Quiz;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface QuestionDao {

    @Query( "SELECT * FROM questions WHERE quizId = :quizId ORDER BY number ASC" )
    LiveData<List<Question>> loadQuestionsFromQuiz( long quizId );

    @Query( "SELECT * FROM questions WHERE quizId = :quizId AND number = :questionNumber" )
    Question loadQuestionByQuizAndNumber( long quizId, int questionNumber );

    @Insert( onConflict = REPLACE)
    void insertQuestion(Question question);

    @Update( onConflict = REPLACE)
    void updateQuestion(Question question);

    @Delete
    int deleteQuestion(Question question);

}
