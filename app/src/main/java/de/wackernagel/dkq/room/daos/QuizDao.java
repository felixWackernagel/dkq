package de.wackernagel.dkq.room.daos;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import de.wackernagel.dkq.room.entities.Quiz;
import de.wackernagel.dkq.room.entities.QuizListItem;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface QuizDao {

    // get quiz with id, number and the count of all questions to each quiz
    @Query( "SELECT quizzes.id, quizzes.number, COUNT(questions.quizId) AS questionCount " +
            "FROM quizzes " +
            "LEFT JOIN questions ON quizzes.id = questions.quizId " +
            "WHERE datetime( quizzes.quizDate ) < datetime( 'now' ) " +
            "GROUP BY quizzes.id " +
            "ORDER BY quizzes.number DESC" )
    LiveData<List<QuizListItem>> loadQuizzesForList();

    @Query( "SELECT * FROM quizzes WHERE datetime( quizDate ) > datetime( 'now' ) ORDER BY number ASC LIMIT 1" )
    LiveData<Quiz> loadNextQuiz();

    @Query( "SELECT * FROM quizzes WHERE id = :quizId" )
    LiveData<Quiz> loadQuiz( long quizId );

    @Query( "SELECT * FROM quizzes WHERE number = :quizNumber" )
    Quiz loadQuizByNumber( int quizNumber );

    @Query( "SELECT * FROM quizzes" )
    List<Quiz> queryQuizzes();

    @Insert( onConflict = REPLACE)
    void insertQuiz( Quiz quiz );

    @Update( onConflict = REPLACE)
    void updateQuiz( Quiz quiz );

    @Delete
    void deleteQuiz( Quiz quiz );

    @Query( "DELETE FROM quizzes WHERE number IN  (:quizNumbers)" )
    void deleteQuizzesByNumber( final int[] quizNumbers );

}
