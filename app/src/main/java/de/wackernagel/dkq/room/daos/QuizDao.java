package de.wackernagel.dkq.room.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import de.wackernagel.dkq.room.entities.Quiz;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface QuizDao {

    @Query( "SELECT * FROM quizzes ORDER BY number DESC" )
    LiveData<List<Quiz>> loadQuizzes();

    @Query( "SELECT * FROM quizzes WHERE datetime( quizDate ) < datetime( 'now' ) ORDER BY number DESC" )
    LiveData<List<Quiz>> loadPastQuizzes();

    @Query( "SELECT * FROM quizzes WHERE datetime( quizDate ) > datetime( 'now' ) ORDER BY number ASC LIMIT 1" )
    LiveData<Quiz> loadNextQuiz();

    @Query( "SELECT * FROM quizzes WHERE id = :quizId" )
    LiveData<Quiz> loadQuiz( long quizId );

    @Query( "SELECT * FROM quizzes WHERE number = :quizNumber" )
    Quiz loadQuizByNumber( int quizNumber );

    @Insert( onConflict = REPLACE)
    long insertQuiz( Quiz quiz );

    @Update( onConflict = REPLACE)
    int updateQuiz( Quiz quiz );

    @Delete
    int deleteQuiz( Quiz quiz );

}
