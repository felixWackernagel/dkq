package de.wackernagel.dkq.webservice;

import android.arch.lifecycle.LiveData;

import java.util.List;

import de.wackernagel.dkq.room.entities.Question;
import de.wackernagel.dkq.room.entities.Quiz;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface Webservice {
    @GET("index.php/dkq/quiz")
    LiveData<ApiResponse<List<Quiz>>> getQuizzes();

    @GET("index.php/dkq/quiz/{quizNumber}")
    LiveData<ApiResponse<Quiz>> getQuiz( @Path("quizNumber") long quizNumber );

    @GET("index.php/dkq/quiz/{quizNumber}/question")
    LiveData<ApiResponse<List<Question>>> getQuestionsFromQuiz(@Path("quizNumber") long quizNumber );
}