package de.wackernagel.dkq.repository;

import android.content.Context;
import android.util.Log;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import de.wackernagel.dkq.AppExecutors;
import de.wackernagel.dkq.receiver.NotificationReceiver;
import de.wackernagel.dkq.room.daos.MessageDao;
import de.wackernagel.dkq.room.daos.QuestionDao;
import de.wackernagel.dkq.room.daos.QuizDao;
import de.wackernagel.dkq.room.entities.Message;
import de.wackernagel.dkq.room.entities.Question;
import de.wackernagel.dkq.room.entities.Quiz;
import de.wackernagel.dkq.utils.DateUtils;
import de.wackernagel.dkq.utils.RateLimiter;
import de.wackernagel.dkq.webservice.ApiResponse;
import de.wackernagel.dkq.webservice.NetworkBoundResource;
import de.wackernagel.dkq.webservice.Resource;
import de.wackernagel.dkq.webservice.Webservice;

public class DkqRepository {

    private final Context context;
    private final AppExecutors executors;
    private final Webservice webservice;
    private final QuizDao quizDao;
    private final QuestionDao questionDao;
    private final MessageDao messageDao;

    private RateLimiter<String> rateLimiter = new RateLimiter<>(10, TimeUnit.MINUTES);
    private static final String LIMITER_QUIZZES = "quizzes";
    private static final String LIMITER_QUIZ = "quiz";
    private static final String LIMITER_QUESTIONS = "questions";
    private static final String LIMITER_MESSAGES = "messages";
    private static final String LIMITER_MESSAGE = "message";

    public DkqRepository(final Context context, final AppExecutors executors, final Webservice webservice, final QuizDao quizDao, final QuestionDao questionDao, final MessageDao messageDao ) {
        this.context = context;
        this.executors = executors;
        this.webservice = webservice;
        this.quizDao = quizDao;
        this.questionDao = questionDao;
        this.messageDao = messageDao;
    }

    public LiveData<Resource<List<Quiz>>> loadQuizzes() {
        return new NetworkBoundResource<List<Quiz>,List<Quiz>>(executors) {
            @Override
            protected void saveCallResult(@NonNull List<Quiz> items) {
                saveQuizzesWithNotification( items );
            }

            @Override
            protected boolean shouldFetch(@Nullable List<Quiz> data) {
                return data == null || data.isEmpty() || rateLimiter.shouldFetch(LIMITER_QUIZZES);
            }

            @NonNull
            @Override
            protected LiveData<List<Quiz>> loadFromDb() {
                return quizDao.loadPastQuizzes();
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<List<Quiz>>> createCall() {
                return webservice.getQuizzes();
            }

            @Override
            protected void onFetchFailed() {
                rateLimiter.reset( LIMITER_QUIZZES );
            }
        }.getAsLiveData();
    }

    public LiveData<Resource<Quiz>> loadNextQuiz() {
        return new NetworkBoundResource<Quiz,Quiz>(executors) {
            @Override
            protected void saveCallResult(@NonNull Quiz item) {
                // no-op
            }

            @Override
            protected boolean shouldFetch(@Nullable Quiz data) {
                return false;
            }

            @NonNull
            @Override
            protected LiveData<Quiz> loadFromDb() {
                return quizDao.loadNextQuiz();
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<Quiz>> createCall() {
                return webservice.getQuiz( 0 );
            }

            @Override
            protected void onFetchFailed() {
                rateLimiter.reset( LIMITER_QUIZZES );
            }
        }.getAsLiveData();
    }

    public void saveQuizzesWithNotification( @NonNull final List<Quiz> items ) {
        int quizzesInFuture = 0;
        for( Quiz onlineQuiz : items ) {
            quizzesInFuture += saveQuiz( onlineQuiz );
        }
        if( quizzesInFuture > 0 ) {
            NotificationReceiver.forNextQuiz( context, quizzesInFuture );
        }
    }

    private int saveQuiz( final Quiz onlineQuiz ) {
        if( onlineQuiz.isInvalid() ) {
            return 0;
        }
        final Quiz existingQuiz = quizDao.loadQuizByNumber( onlineQuiz.number );
        final boolean isNew = existingQuiz == null;
        if( isNew ) {
            if( onlineQuiz.published == 1 ) {
                Log.i("DKQ", "Insert new quiz " + onlineQuiz.number);
                quizDao.insertQuiz(onlineQuiz);
                return DateUtils.joomlaDateToJavaDate(onlineQuiz.quizDate, new Date()).after(new Date()) ? 1 : 0;
            }
        } else if( onlineQuiz.published == 0 ) {
            Log.i("DKQ", "Remove existing quiz " + onlineQuiz.number);
            quizDao.deleteQuiz( existingQuiz );
        } else if( existingQuiz.version < onlineQuiz.version ) {
            Log.i("DKQ", "Update quiz " + onlineQuiz.number + " from version " + existingQuiz.version + " to " + onlineQuiz.version);
            onlineQuiz.id = existingQuiz.id; // update is ID based so copy existing quiz ID to new one
            quizDao.updateQuiz( onlineQuiz );
            return !existingQuiz.quizDate.equals( onlineQuiz.quizDate ) && DateUtils.joomlaDateToJavaDate( onlineQuiz.quizDate, new Date() ).after( new Date() ) ? 1 : 0;
        } else {
            Log.i("DKQ", "No changes on Quiz " + onlineQuiz.number);
        }
        return 0;
    }

    public LiveData<Resource<Quiz>> loadQuiz( final long quizId, final int quizNumber ) {
        return new NetworkBoundResource<Quiz,Quiz>(executors) {
            @Override
            protected void saveCallResult(@NonNull Quiz item) {
                saveQuiz( item );
            }

            @Override
            protected boolean shouldFetch(@Nullable Quiz data) {
                return data == null || rateLimiter.shouldFetch( LIMITER_QUIZ + ":" + quizNumber );
            }

            @NonNull
            @Override
            protected LiveData<Quiz> loadFromDb() {
                return quizDao.loadQuiz(quizId);
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<Quiz>> createCall() {
                return webservice.getQuiz(quizNumber);
            }

            @Override
            protected void onFetchFailed() {
                rateLimiter.reset( LIMITER_QUIZ + ":" + quizNumber );
            }
        }.getAsLiveData();
    }

    public LiveData<Resource<List<Question>>> loadQuestions( final long quizId, final long quizNumber ) {
        return new NetworkBoundResource<List<Question>,List<Question>>(executors) {
            @Override
            protected void saveCallResult(@NonNull List<Question> items) {
                for( Question onlineQuestion : items ) {
                    if( onlineQuestion.isInvalid() ) {
                        continue;
                    }
                    final Question existingQuestion = questionDao.loadQuestionByQuizAndNumber( quizId, onlineQuestion.number );
                    final boolean isNew = existingQuestion == null;
                    if( isNew ) {
                        if( onlineQuestion.published == 1 ) {
                            Log.i("DKQ", "Insert new question " + onlineQuestion.number);
                            onlineQuestion.quizId = quizId;
                            questionDao.insertQuestion(onlineQuestion);
                        }
                    } else if( onlineQuestion.published == 0 ) {
                        Log.i("DKQ", "Delete existing question " + onlineQuestion.number);
                        questionDao.deleteQuestion( existingQuestion );
                    } else if( existingQuestion.version < onlineQuestion.version ) {
                        Log.i("DKQ", "Update question " + onlineQuestion.number + " from version " + existingQuestion.version + " to " + onlineQuestion.version);
                        onlineQuestion.id = existingQuestion.id; // update is ID based so copy existing quiz ID to new one
                        onlineQuestion.quizId = existingQuestion.quizId;
                        questionDao.updateQuestion( onlineQuestion );
                    } else {
                        Log.i("DKQ", "No changes on Question " + onlineQuestion.number);
                    }
                }
            }

            @Override
            protected boolean shouldFetch(@Nullable List<Question> data) {
                return data == null || data.isEmpty() || rateLimiter.shouldFetch( quizNumber + ":" + LIMITER_QUESTIONS );
            }

            @NonNull
            @Override
            protected LiveData<List<Question>> loadFromDb() {
                return questionDao.loadQuestionsFromQuiz( quizId );
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<List<Question>>> createCall() {
                return webservice.getQuestionsFromQuiz( quizNumber );
            }

            @Override
            protected void onFetchFailed() {
                rateLimiter.reset( quizNumber + ":" + LIMITER_QUESTIONS );
            }
        }.getAsLiveData();
    }

    public LiveData<Resource<List<Message>>> loadMessages() {
        return new NetworkBoundResource<List<Message>,List<Message>>(executors) {
            @Override
            protected void saveCallResult(@NonNull List<Message> items) {
                saveMessagesWithNotification( items );
            }

            @Override
            protected boolean shouldFetch(@Nullable List<Message> data) {
                return data == null || data.isEmpty() || rateLimiter.shouldFetch( LIMITER_MESSAGES );
            }

            @NonNull
            @Override
            protected LiveData<List<Message>> loadFromDb() {
                return messageDao.loadMessages();
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<List<Message>>> createCall() {
                return webservice.getMessages();
            }

            @Override
            protected void onFetchFailed() {
                rateLimiter.reset( LIMITER_MESSAGES );
            }
        }.getAsLiveData();
    }

    public void saveMessagesWithNotification(List<Message> items) {
        int newMessagesCount = 0;
        for( Message message : items ) {
            newMessagesCount += saveMessage( message );
        }
        if( newMessagesCount > 0 ) {
            NotificationReceiver.forNewMessages( context, newMessagesCount );
        }
    }

    private int saveMessage( final Message onlineMessage ) {
        if( onlineMessage.isInvalid() ) {
            return 0;
        }
        final Message existingMessage = messageDao.loadMessageByNumber( onlineMessage.number );
        final boolean isNew = existingMessage == null;
        if( isNew ) {
            Log.i("DKQ", "Insert new message " + onlineMessage.number);
            messageDao.insertMessages(onlineMessage);
            return 1;
        } else if( existingMessage.version < onlineMessage.version ) {
            Log.i("DKQ", "Update message " + onlineMessage.number + " from version " + existingMessage.version + " to " + onlineMessage.version);
            onlineMessage.id = existingMessage.id; // update is ID based so copy existing quiz ID to new one
            messageDao.updateMessage( onlineMessage );
            return 0;
        } else {
            Log.i("DKQ", "No changes on message " + onlineMessage.number);
        }
        return 0;
    }

    public LiveData<Resource<Message>> loadMessage( final long messageId, final int messageNumber ) {
        return new NetworkBoundResource<Message, Message>(executors) {
            @Override
            protected void saveCallResult(@NonNull Message item) {
                saveMessage( item );
            }

            @Override
            protected boolean shouldFetch(@Nullable Message data) {
                return data == null || rateLimiter.shouldFetch( LIMITER_MESSAGE + ":" + messageNumber );
            }

            @NonNull
            @Override
            protected LiveData<Message> loadFromDb() {
                return messageDao.loadMessage( messageId );
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<Message>> createCall() {
                return webservice.getMessage( messageNumber );
            }

            @Override
            protected void onFetchFailed() {
                rateLimiter.reset( LIMITER_MESSAGE + ":" + messageNumber );
            }
        }.getAsLiveData();
    }

    public void updateMessage(final Message message ) {
        executors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                messageDao.updateMessage(message);
            }
        });
    }

    public void insertMessages(final Message... messages ) {
        executors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                messageDao.insertMessages( messages );
            }
        });
    }

    public void deleteAllMessages() {
        executors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                messageDao.deleteAllMessages();
            }
        });
    }

    public void insertQuiz(final Quiz quiz ) {
        executors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                quizDao.insertQuiz(quiz);
            }
        } );
    }
}