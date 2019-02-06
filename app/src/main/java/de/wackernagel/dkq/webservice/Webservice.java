package de.wackernagel.dkq.webservice;

import java.util.List;

import androidx.lifecycle.LiveData;
import de.wackernagel.dkq.room.entities.Message;
import de.wackernagel.dkq.room.entities.Question;
import de.wackernagel.dkq.room.entities.Quiz;
import de.wackernagel.dkq.room.entities.Quizzer;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface Webservice {
    String VERSION = "v3";

    @GET(VERSION + "/quizzes")
    LiveData<ApiResponse<List<Quiz>>> getQuizzes();

    @GET(VERSION + "/quizzes")
    Call<List<Quiz>> getQuizzesList();

    @GET(VERSION + "/quizzes/{quizNumber}")
    LiveData<ApiResponse<Quiz>> getQuiz( @Path("quizNumber") int quizNumber );

    @GET(VERSION + "/quizzes/{quizNumber}/questions")
    LiveData<ApiResponse<List<Question>>> getQuestionsFromQuiz( @Path("quizNumber") int quizNumber );

    @GET(VERSION + "/quizzes/{quizNumber}/questions")
   Call<List<Question>> getQuestionsList( @Path("quizNumber") int quizNumber );

    @GET(VERSION + "/messages")
    LiveData<ApiResponse<List<Message>>> getMessages();

    @GET(VERSION + "/messages")
    Call<List<Message>> getMessagesList();

    @GET(VERSION + "/messages/{messageNumber}")
    LiveData<ApiResponse<Message>> getMessage( @Path("messageNumber") int messageNumber );

    @GET(VERSION + "/quizzers")
    LiveData<ApiResponse<List<Quizzer>>> getQuizzers();

    @GET(VERSION + "/quizzers/{quizzerNumber}")
    LiveData<ApiResponse<Quizzer>> getQuizzer( @Path("quizzerNumber") int quizzerNumber );
}