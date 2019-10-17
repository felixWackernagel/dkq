package de.wackernagel.dkq.room.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import de.wackernagel.dkq.room.entities.Quizzer;
import de.wackernagel.dkq.room.entities.QuizzerListItem;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface QuizzerDao {

    @Query( "SELECT quizzers.id, quizzers.name, quizzers.image, COUNT(quizzes.winnerId) AS ranking, quizzers.version " +
            "FROM quizzers " +
            "LEFT JOIN quizzes ON quizzers.id = quizzes.winnerId " +
            "WHERE quizzes.winnerId IS NOT NULL " +
            "GROUP BY quizzes.winnerId " +
            "ORDER BY ranking DESC" )
    LiveData<List<QuizzerListItem>> loadWinners();

    @Query( "SELECT quizzers.id, quizzers.name, quizzers.image, COUNT(quizzes.quizMasterId) AS ranking, quizzers.version " +
            "FROM quizzers " +
            "LEFT JOIN quizzes ON quizzers.id = quizzes.quizMasterId " +
            "WHERE quizzes.quizMasterId IS NOT NULL " +
            "GROUP BY quizzes.quizMasterId " +
            "ORDER BY ranking DESC" )
    LiveData<List<QuizzerListItem>> loadQuizMasters();

    @Query( "SELECT * FROM quizzers WHERE id = :quizzerId" )
    LiveData<Quizzer> loadQuizzer(long quizzerId);

    @Query( "SELECT * FROM quizzers WHERE id = (SELECT winnerId FROM quizzes WHERE id = :quizId)" )
    LiveData<Quizzer> loadWinnerByQuiz(long quizId);

    @Query( "SELECT * FROM quizzers WHERE id = (SELECT quizMasterId FROM quizzes WHERE id = :quizId)" )
    LiveData<Quizzer> loadQuizmasterByQuiz(long quizId);

    @Query( "SELECT * FROM quizzers WHERE number = :quizzerNumber" )
    Quizzer loadQuizzerByNumber(int quizzerNumber);

    @Query( "DELETE FROM quizzers" )
    void deleteAllQuizzers();

    @Insert( onConflict = REPLACE)
    long insertQuizzer(Quizzer quizzer);

    @Update( onConflict = REPLACE)
    void updateQuizzer(Quizzer quizzer);

}
