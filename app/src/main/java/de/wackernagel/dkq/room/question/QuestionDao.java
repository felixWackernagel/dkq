package de.wackernagel.dkq.room.question;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import de.wackernagel.dkq.room.BaseDao;

@Dao
public abstract class QuestionDao extends BaseDao<Question> {

    @Query( "SELECT * FROM questions WHERE quizId = :quizId ORDER BY number ASC" )
    public abstract LiveData<List<Question>> loadQuestionsFromQuiz( long quizId );

    @Query( "SELECT * FROM questions WHERE quizId = :quizId AND number = :questionNumber" )
    public abstract Question loadQuestionByQuizAndNumber( long quizId, int questionNumber );

    @Query( "DELETE FROM questions" )
    public abstract void deleteAllQuestions();

}
