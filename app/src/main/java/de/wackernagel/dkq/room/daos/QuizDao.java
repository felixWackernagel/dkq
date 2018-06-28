package de.wackernagel.dkq.room.daos;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import de.wackernagel.dkq.room.entities.Quiz;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface QuizDao {

    @Query( "SELECT * FROM quizzes" )
    LiveData<List<Quiz>> loadQuizzes();

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
