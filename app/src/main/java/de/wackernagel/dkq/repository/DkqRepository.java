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
import de.wackernagel.dkq.room.SampleCreator;
import de.wackernagel.dkq.room.daos.MessageDao;
import de.wackernagel.dkq.room.daos.QuestionDao;
import de.wackernagel.dkq.room.daos.QuizDao;
import de.wackernagel.dkq.room.daos.QuizzerDao;
import de.wackernagel.dkq.room.entities.Message;
import de.wackernagel.dkq.room.entities.MessageListItem;
import de.wackernagel.dkq.room.entities.Question;
import de.wackernagel.dkq.room.entities.Quiz;
import de.wackernagel.dkq.room.entities.QuizListItem;
import de.wackernagel.dkq.room.entities.Quizzer;
import de.wackernagel.dkq.room.entities.QuizzerListItem;
import de.wackernagel.dkq.utils.DateUtils;
import de.wackernagel.dkq.utils.ObjectUtils;
import de.wackernagel.dkq.utils.RateLimiter;
import de.wackernagel.dkq.viewmodels.QuizzerRole;
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
    private final QuizzerDao quizzerDao;

    private RateLimiter<String> rateLimiter = new RateLimiter<>(10, TimeUnit.MINUTES);
    private static final String LIMITER_QUIZZES = "quizzes";
    private static final String LIMITER_QUIZ = "quiz";
    private static final String LIMITER_QUESTIONS = "questions";
    private static final String LIMITER_MESSAGES = "messages";
    private static final String LIMITER_MESSAGE = "message";
    private static final String LIMITER_QUIZZERS = "quizzers";

    public DkqRepository(final Context context, final AppExecutors executors, final Webservice webservice, final QuizDao quizDao, final QuestionDao questionDao, final MessageDao messageDao, final QuizzerDao quizzerDao ) {
        this.context = context;
        this.executors = executors;
        this.webservice = webservice;
        this.quizDao = quizDao;
        this.questionDao = questionDao;
        this.messageDao = messageDao;
        this.quizzerDao = quizzerDao;
    }

    public LiveData<Resource<List<QuizListItem>>> loadQuizzes() {
        return new NetworkBoundResource<List<QuizListItem>,List<Quiz>>(executors) {
            @Override
            protected void saveCallResult(@NonNull List<Quiz> items) {
                saveQuizzesWithNotification( items );
            }

            @Override
            protected boolean shouldFetch(@Nullable List<QuizListItem> data) {
                return data == null || data.isEmpty() || rateLimiter.shouldFetch(LIMITER_QUIZZES);
            }

            @NonNull
            @Override
            protected LiveData<List<QuizListItem>> loadFromDb() {
                return quizDao.loadQuizzesForList();
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

    public LiveData<Quiz> loadNextQuiz() {
        return quizDao.loadNextQuiz();
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
        onlineQuiz.quizMasterId = saveQuizzer( onlineQuiz.quizMaster );
        onlineQuiz.winnerId = saveQuizzer( onlineQuiz.winner );
        final boolean isNew = existingQuiz == null;
        if( isNew ) {
            if( onlineQuiz.published == 1 ) {
                Log.i("DKQ", "Insert new Quiz " + onlineQuiz.number + " " + onlineQuiz.toString() );
                quizDao.insertQuiz(onlineQuiz);
                return DateUtils.joomlaDateToJavaDate(onlineQuiz.quizDate, new Date()).after(new Date()) ? 1 : 0;
            }
        } else if( onlineQuiz.published == 0 ) {
            Log.i("DKQ", "Remove existing Quiz " + onlineQuiz.number);
            quizDao.deleteQuiz( existingQuiz );
        } else if( existingQuiz.version < onlineQuiz.version || !ObjectUtils.equals( existingQuiz.winnerId, onlineQuiz.winnerId ) || !ObjectUtils.equals( existingQuiz.quizMasterId, onlineQuiz.quizMasterId ) ) {
            onlineQuiz.id = existingQuiz.id; // update is ID based so copy existing quiz ID to new one
            Log.i("DKQ", "Update Quiz " + onlineQuiz.number + " from version " + existingQuiz.version + " to " + onlineQuiz.version + " " + onlineQuiz.toString() );
            quizDao.updateQuiz( onlineQuiz );
            return DateUtils.notEquals( existingQuiz.quizDate, onlineQuiz.quizDate ) && DateUtils.joomlaDateToJavaDate( onlineQuiz.quizDate, new Date() ).after( new Date() ) ? 1 : 0;
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

    public LiveData<Resource<List<Question>>> loadQuestions( final long quizId, final int quizNumber ) {
        return new NetworkBoundResource<List<Question>,List<Question>>(executors) {
            @Override
            protected void saveCallResult(@NonNull List<Question> items) {
                saveQuestions( items, quizId );
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

    public void saveQuestions( final List<Question> items, final long quizId ) {
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

    public LiveData<Resource<List<MessageListItem>>> loadMessages() {
        return new NetworkBoundResource<List<MessageListItem>,List<Message>>(executors) {
            @Override
            protected void saveCallResult(@NonNull List<Message> items) {
                saveMessagesWithNotification( items );
            }

            @Override
            protected boolean shouldFetch(@Nullable List<MessageListItem> data) {
                return data == null || data.isEmpty() || rateLimiter.shouldFetch( LIMITER_MESSAGES );
            }

            @NonNull
            @Override
            protected LiveData<List<MessageListItem>> loadFromDb() {
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

    public LiveData<Resource<List<QuizzerListItem>>> loadQuizzers(final QuizzerRole criteria) {
        return new NetworkBoundResource<List<QuizzerListItem>,List<Quizzer>>(executors) {
            @Override
            protected void saveCallResult(@NonNull List<Quizzer> items) {
                saveQuizzers( items );
            }

            @Override
            protected boolean shouldFetch(@Nullable List<QuizzerListItem> data) {
                return data == null || data.isEmpty() || rateLimiter.shouldFetch( LIMITER_QUIZZERS );
            }

            @NonNull
            @Override
            protected LiveData<List<QuizzerListItem>> loadFromDb() {
                switch ( criteria ) {
                    case WINNER:
                        return quizzerDao.loadWinners();

                    case QUIZMASTER:
                    default:
                        return quizzerDao.loadQuizMasters();
                }
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<List<Quizzer>>> createCall() {
                return webservice.getQuizzers();
            }

            @Override
            protected void onFetchFailed() {
                rateLimiter.reset( LIMITER_QUIZZERS );
            }
        }.getAsLiveData();
    }

    public LiveData<Quizzer> loadQuizzer( final QuizzerRole role, final long id ) {
        switch ( role ) {
            case WINNER:
                return quizzerDao.loadWinnerByQuiz(id);

            case QUIZMASTER:
                return quizzerDao.loadQuizmasterByQuiz(id);

            case QUIZZER:
            default:
                return quizzerDao.loadQuizzer(id);
        }
    }

    private void saveQuizzers( final List<Quizzer> items) {
        for( Quizzer quizzer : items ) {
            saveQuizzer( quizzer );
        }
    }

    private Long saveQuizzer( final Quizzer onlineQuizzer ) {
        if( onlineQuizzer == null || onlineQuizzer.isInvalid() ) {
            return null;
        }
        final Quizzer existingQuizzer = quizzerDao.loadQuizzerByNumber( onlineQuizzer.number );
        final boolean isNew = existingQuizzer == null;
        if( isNew ) {
            final long id = quizzerDao.insertQuizzer(onlineQuizzer);
            Log.i("DKQ", "Insert new Quizzer " + onlineQuizzer.number + " (id=" + id + ")");
            return id;
        } else if( existingQuizzer.version < onlineQuizzer.version ) {
            onlineQuizzer.id = existingQuizzer.id; // update is ID based so copy existing ID to new one
            quizzerDao.updateQuizzer( onlineQuizzer );
            Log.i("DKQ", "Update Quizzer " + onlineQuizzer.number + " from version " + existingQuizzer.version + " to " + onlineQuizzer.version + " (id=" + onlineQuizzer.id + ")" );
            return onlineQuizzer.id;
        } else {
            Log.i("DKQ", "No changes on Quizzer " + existingQuizzer.number + " (id=" + existingQuizzer.id + ")" );
            return existingQuizzer.id;
        }
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

    public LiveData<Integer> getNewMessagesCount() {
        return messageDao.loadNewMessagesCount();
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

    public void deleteQuizzesByNumber( final int[] quizNumbers ) {
        executors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                quizDao.deleteQuizzesByNumber( quizNumbers );
            }
        });
    }

    public void createSamples() {
        executors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                messageDao.deleteAllMessages();
                questionDao.deleteAllQuestions();
                quizDao.deleteAllQuizzes();
                quizzerDao.deleteAllQuizzers();

                final Quizzer[] quizzers = SampleCreator.createSampleQuizzers();
                for( Quizzer quizzer : quizzers ) {
                    quizzer.id = quizzerDao.insertQuizzer( quizzer );
                }

                final Quiz[] quizzes = SampleCreator.createSampleQuizzes( quizzers[0], quizzers[1] );
                final int count = quizzes.length;
                for( int index = 0; index < count; index++ ) {
                    final Quiz quiz = quizzes[ index ];
                    quiz.id = quizDao.insertQuiz( quiz );

                    if( index + 1 < count ) {
                        final Question[] questions = SampleCreator.createSampleQuestions( quiz );
                        for( Question question : questions ) {
                            questionDao.insertQuestion( question );
                        }
                    }
                }

                final Message[] messages = SampleCreator.createSampleMessages();
                for( Message message : messages ) {
                    messageDao.insertMessages( message );
                }
            }
        });
    }

    public void dropAll() {
        executors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                messageDao.deleteAllMessages();
                questionDao.deleteAllQuestions();
                quizDao.deleteAllQuizzes();
                quizzerDao.deleteAllQuizzers();
            }
        });
    }

    public List<Quiz> queryQuizzes() {
        return quizDao.queryQuizzes();
    }
}