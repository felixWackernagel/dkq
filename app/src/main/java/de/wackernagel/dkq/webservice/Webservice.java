package de.wackernagel.dkq.webservice;

import androidx.lifecycle.LiveData;

import java.util.List;

import de.wackernagel.dkq.room.entities.Message;
import de.wackernagel.dkq.room.entities.Question;
import de.wackernagel.dkq.room.entities.Quiz;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface Webservice {
    @GET("v1/quizzes")
    LiveData<ApiResponse<List<Quiz>>> getQuizzes();

    @GET("v1/quizzes")
    Call<List<Quiz>> getQuizzesList();

    @GET("v1/quizzes/{quizNumber}")
    LiveData<ApiResponse<Quiz>> getQuiz( @Path("quizNumber") long quizNumber );

    @GET("v1/quizzes/{quizNumber}/questions")
    LiveData<ApiResponse<List<Question>>> getQuestionsFromQuiz( @Path("quizNumber") long quizNumber );

    @GET("v1/messages")
    LiveData<ApiResponse<List<Message>>> getMessages();

    @GET("v1/messages")
    Call<List<Message>> getMessagesList();

    @GET("v1/messages/{messageNumber}")
    LiveData<ApiResponse<Message>> getMessage( @Path("messageNumber") long messageNumber );
}