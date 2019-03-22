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

import static de.wackernagel.dkq.DkqConstants.API.VERSION;

public interface Webservice {
    @GET(VERSION + "/quizzes")
    LiveData<ApiResponse<ApiResult<List<Quiz>>>> getQuizzes();

    @GET(VERSION + "/quizzes")
    Call<ApiResult<List<Quiz>>> getQuizzesList();

    @GET(VERSION + "/quizzes/{quizNumber}")
    LiveData<ApiResponse<ApiResult<Quiz>>> getQuiz( @Path("quizNumber") int quizNumber );

    @GET(VERSION + "/quizzes/{quizNumber}/questions")
    LiveData<ApiResponse<ApiResult<List<Question>>>> getQuestionsFromQuiz( @Path("quizNumber") int quizNumber );

    @GET(VERSION + "/quizzes/{quizNumber}/questions")
   Call<ApiResult<List<Question>>> getQuestionsList( @Path("quizNumber") int quizNumber );

    @GET(VERSION + "/messages")
    LiveData<ApiResponse<ApiResult<List<Message>>>> getMessages();

    @GET(VERSION + "/messages")
    Call<ApiResult<List<Message>>> getMessagesList();

    @GET(VERSION + "/messages/{messageNumber}")
    LiveData<ApiResponse<ApiResult<Message>>> getMessage( @Path("messageNumber") int messageNumber );

    @GET(VERSION + "/quizzers")
    LiveData<ApiResponse<ApiResult<List<Quizzer>>>> getQuizzers();

    // currently not used but documented
    //@GET(VERSION + "/quizzers/{quizzerNumber}")
    //LiveData<ApiResponse<Quizzer>> getQuizzer( @Path("quizzerNumber") int quizzerNumber );
}