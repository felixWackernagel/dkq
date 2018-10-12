package de.wackernagel.dkq.room.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import de.wackernagel.dkq.room.entities.Question;

import static androidx.room.OnConflictStrategy.REPLACE;

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
