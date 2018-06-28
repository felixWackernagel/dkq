package de.wackernagel.dkq.webservice;

import android.arch.lifecycle.LiveData;

import java.util.List;

import de.wackernagel.dkq.room.entities.Question;
import de.wackernagel.dkq.room.entities.Quiz;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface Webservice {
    @GET("index.php/dkq/quizzes")
    LiveData<ApiResponse<List<Quiz>>> getQuizzes();

    @GET("index.php/dkq/quiz/{id}")
    LiveData<ApiResponse<Quiz>> getQuiz( @Path("id") long id );

    @GET("index.php/dkq/questions/{id}")
    LiveData<ApiResponse<List<Question>>> getQuestionsFromQuiz(@Path("id") long id );
}