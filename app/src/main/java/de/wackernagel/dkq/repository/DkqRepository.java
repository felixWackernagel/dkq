package de.wackernagel.dkq.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.LiveData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import de.wackernagel.dkq.AppExecutors;
import de.wackernagel.dkq.DkqLog;
import de.wackernagel.dkq.receiver.NotificationReceiver;
import de.wackernagel.dkq.room.AppDatabase;
import de.wackernagel.dkq.room.SampleCreator;
import de.wackernagel.dkq.room.daos.QuizDao;
import de.wackernagel.dkq.room.daos.QuizzerDao;
import de.wackernagel.dkq.room.entities.Quiz;
import de.wackernagel.dkq.room.entities.QuizListItem;
import de.wackernagel.dkq.room.entities.Quizzer;
import de.wackernagel.dkq.room.entities.QuizzerListItem;
import de.wackernagel.dkq.room.message.Message;
import de.wackernagel.dkq.room.message.MessageDao;
import de.wackernagel.dkq.room.message.MessageListItem;
import de.wackernagel.dkq.room.question.Question;
import de.wackernagel.dkq.room.question.QuestionDao;
import de.wackernagel.dkq.utils.DateUtils;
import de.wackernagel.dkq.utils.ObjectUtils;
import de.wackernagel.dkq.utils.RateLimiter;
import de.wackernagel.dkq.viewmodels.QuizzerRole;
import de.wackernagel.dkq.webservice.ApiResponse;
import de.wackernagel.dkq.webservice.ApiResult;
import de.wackernagel.dkq.webservice.NetworkBoundResource;
import de.wackernagel.dkq.webservice.Resource;
import de.wackernagel.dkq.webservice.Webservice;
import retrofit2.Response;

public class DkqRepository {

    private static final String TAG = "DkqRepository";

    private final Context context;
    private final AppDatabase db;
    private final AppExecutors executors;
    private final Webservice webservice;
    private final QuizDao quizDao;
    private final QuestionDao questionDao;
    private final MessageDao messageDao;
    private final QuizzerDao quizzerDao;

    private RateLimiter<String> rateLimiter = new RateLimiter<>(10L, TimeUnit.MINUTES);
    private static final String LIMITER_QUIZZES = "quizzes";
    private static final String LIMITER_QUIZ = "quiz";
    private static final String LIMITER_QUESTIONS = "questions";
    private static final String LIMITER_MESSAGES = "messages";
    private static final String LIMITER_MESSAGE = "message";
    private static final String LIMITER_QUIZZERS = "quizzers";

    public DkqRepository( final Context context, final AppDatabase database, final AppExecutors executors, final Webservice webservice, final QuizDao quizDao, final QuestionDao questionDao, final MessageDao messageDao, final QuizzerDao quizzerDao) {
        this.context = context;
        this.db = database;
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
                db.beginTransaction();
                try {
                    saveQuizzesWithNotification( items );
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }

            @Override
            protected boolean shouldFetch(@Nullable List<QuizListItem> data) {
                return rateLimiter.shouldFetch(LIMITER_QUIZZES) || data == null || data.isEmpty();
            }

            @NonNull
            @Override
            protected LiveData<List<QuizListItem>> loadFromDb() {
                return quizDao.loadQuizzesForList();
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<ApiResult<List<Quiz>>>> createCall() {
                return webservice.getQuizzes();
            }

            @Override
            protected void onFetchFailed() {
                rateLimiter.reset( LIMITER_QUIZZES );
                DkqLog.e(TAG, "Fetch failed for quizzes list." );
            }
        }.getAsLiveData();
    }

    public LiveData<Quiz> loadNextQuiz() {
        return quizDao.loadNextQuiz();
    }

    public void saveQuizzesWithNotification( @NonNull final List<Quiz> items ) {
        final ArrayList<Quiz> futureQuizzesList = new ArrayList<>();
        for( Quiz onlineQuiz : items ) {
            saveQuiz( futureQuizzesList, onlineQuiz );
        }

        final int newQuizzesCount = futureQuizzesList.size();
        if( newQuizzesCount == 1 ) {
            final Quiz quiz = futureQuizzesList.get( 0 );
            NotificationReceiver.forOneFutureQuiz( context, quiz.id, quiz.number );
        } else if( newQuizzesCount > 1 ) {
            NotificationReceiver.forManyFutureQuizzes( context, newQuizzesCount );
        }
    }

    private void saveQuiz( final Quiz onlineQuiz ) {
        saveQuiz( null, onlineQuiz );
    }

    private void saveQuiz( @Nullable final ArrayList<Quiz> futureQuizzesList, final Quiz onlineQuiz) {
        if( !onlineQuiz.isInvalid() ) {
            final Quiz existingQuiz = quizDao.loadQuizByNumber( onlineQuiz.number );

            onlineQuiz.quizMasterId = saveQuizzer( onlineQuiz.quizMaster );
            if( ObjectUtils.equals( onlineQuiz.quizMaster, onlineQuiz.winner )  ) {
                onlineQuiz.winnerId = onlineQuiz.quizMasterId;
            } else {
                onlineQuiz.winnerId = saveQuizzer( onlineQuiz.winner );
            }

            final boolean isNew = existingQuiz == null;
            if( isNew ) {
                onlineQuiz.id = quizDao.insertQuiz(onlineQuiz);
                DkqLog.i(TAG, "Quiz inserted " + onlineQuiz.toString());
                if (futureQuizzesList != null && DateUtils.isJoomlaDateInFuture(onlineQuiz.quizDate)) {
                    futureQuizzesList.add(onlineQuiz);
                }
            } else {
                onlineQuiz.id = existingQuiz.id; // update is ID based so copy existing quiz ID to new one
                quizDao.updateQuiz( onlineQuiz );
                DkqLog.i(TAG, String.format( Locale.ENGLISH,"Quiz %d updated", onlineQuiz.number ) );
                if( futureQuizzesList != null && DateUtils.notEquals( existingQuiz.quizDate, onlineQuiz.quizDate ) && DateUtils.isJoomlaDateInFuture( onlineQuiz.quizDate ) ) {
                    futureQuizzesList.add( onlineQuiz );
                }
            }
        }
    }

    public LiveData<Resource<Quiz>> loadQuiz( final long quizId, final int quizNumber ) {
        return new NetworkBoundResource<Quiz,Quiz>(executors) {
            @Override
            protected void saveCallResult(@NonNull Quiz item) {
                db.beginTransaction();
                try {
                    saveQuiz( item );
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }

            @Override
            protected boolean shouldFetch(@Nullable Quiz data) {
                return rateLimiter.shouldFetch( LIMITER_QUIZ + ":" + quizNumber ) || data == null;
            }

            @NonNull
            @Override
            protected LiveData<Quiz> loadFromDb() {
                return quizDao.loadQuiz(quizId);
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<ApiResult<Quiz>>> createCall() {
                return webservice.getQuiz(quizNumber);
            }

            @Override
            protected void onFetchFailed() {
                rateLimiter.reset( LIMITER_QUIZ + ":" + quizNumber );
                DkqLog.e(TAG, String.format( Locale.ENGLISH, "Fetch failed for quiz (id=%d, number=%d)", quizId, quizNumber ) );
            }
        }.getAsLiveData();
    }

    public LiveData<Quiz> loadQuiz( final long quizId ) {
        return quizDao.loadQuiz( quizId );
    }

    public LiveData<Resource<List<Question>>> loadQuestions( final long quizId, final int quizNumber ) {
        return new NetworkBoundResource<List<Question>,List<Question>>(executors) {
            @Override
            protected void saveCallResult(@NonNull List<Question> items) {
                db.beginTransaction();
                try {
                    saveQuestions( items, quizId );
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }

            @Override
            protected boolean shouldFetch(@Nullable List<Question> data) {
                return rateLimiter.shouldFetch( quizNumber + ":" + LIMITER_QUESTIONS ) || data == null || data.isEmpty();
            }

            @NonNull
            @Override
            protected LiveData<List<Question>> loadFromDb() {
                return questionDao.loadQuestionsFromQuiz( quizId );
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<ApiResult<List<Question>>>> createCall() {
                return webservice.getQuestionsFromQuiz( quizNumber );
            }

            @Override
            protected void onFetchFailed() {
                rateLimiter.reset( quizNumber + ":" + LIMITER_QUESTIONS );
                DkqLog.e(TAG, String.format( Locale.ENGLISH, "Fetch failed for questions (id=%d, number=%d)", quizId, quizNumber ) );
            }
        }.getAsLiveData();
    }

    public void saveQuestions( final List<Question> onlineQuestions, final long quizId ) {
        for( Question onlineQuestion : onlineQuestions ) {
            if( onlineQuestion.isInvalid() ) {
                DkqLog.i( TAG, String.format( Locale.ENGLISH, "Skip invalid %s", onlineQuestion ) );
                continue;
            }

            final Question existingQuestion = questionDao.loadQuestionByQuizAndNumber( quizId, onlineQuestion.getNumber() );
            if( existingQuestion == null ) {

                onlineQuestion.setQuizId( quizId );

                final Question createdQuestion = questionDao.insert( onlineQuestion );
                final boolean success = createdQuestion.getId() > 0;
                DkqLog.i( TAG, String.format( Locale.ENGLISH, "Question[id=%d]%s inserted", createdQuestion.getId(), success ? "" : " NOT" ) );

            } else {

                existingQuestion.setAnswer( onlineQuestion.getAnswer() );
                existingQuestion.setQuestion( onlineQuestion.getQuestion() );
                existingQuestion.setImage( onlineQuestion.getImage() );
                existingQuestion.setVersion( onlineQuestion.getVersion() );
                existingQuestion.setLastUpdate( onlineQuestion.getLastUpdate() );

                final boolean success = questionDao.update( existingQuestion ) == 1;
                DkqLog.i( TAG, String.format( Locale.ENGLISH, "Question[id=%d]%s updated", existingQuestion.getId(), success ? "" : " NOT" ) );

            }
        }
    }

    public LiveData<Resource<List<MessageListItem>>> loadMessages() {
        return new NetworkBoundResource<List<MessageListItem>,List<Message>>(executors) {
            @Override
            protected void saveCallResult(@NonNull List<Message> items) {
                db.beginTransaction();
                try {
                    saveMessagesWithNotification( items );
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }

            @Override
            protected boolean shouldFetch(@Nullable List<MessageListItem> data) {
                return rateLimiter.shouldFetch( LIMITER_MESSAGES ) || data == null || data.isEmpty();
            }

            @NonNull
            @Override
            protected LiveData<List<MessageListItem>> loadFromDb() {
                return messageDao.loadMessages();
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<ApiResult<List<Message>>>> createCall() {
                return webservice.getMessages();
            }

            @Override
            protected void onFetchFailed() {
                rateLimiter.reset( LIMITER_MESSAGES );
                DkqLog.e(TAG, "Fetch failed for messages." );
            }
        }.getAsLiveData();
    }

    public void saveMessagesWithNotification(List<Message> items) {
        final ArrayList<Message> newMessagesList = new ArrayList<>();
        for( Message message : items ) {
            saveMessage( newMessagesList, message );
        }

        final int newMessagesCount = newMessagesList.size();
        if( newMessagesCount == 1 ) {
            final Message message = newMessagesList.get( 0 );
            NotificationReceiver.forOneNewMessage( context, message.getTitle(), message.getId(), message.getNumber() );
        } else if( newMessagesCount > 1 ) {
            NotificationReceiver.forManyNewMessages( context, newMessagesCount );
        }
    }

    private void saveMessage( @Nullable final List<Message> newMessagesList, final Message onlineMessage ) {
        if( onlineMessage.isInvalid() )
            return;

        onlineMessage.setQuizId( getQuizIdByNumber( onlineMessage.getQuizNumber() ) );
        onlineMessage.setType( Message.Type.ARTICLE );

        final Message existingMessage = messageDao.loadMessageByNumber( onlineMessage.getNumber() );
        if( existingMessage == null ) {

            final Message createdMessage = messageDao.insert( onlineMessage );

            final boolean success = createdMessage.getId() > 0;
            DkqLog.i( TAG, String.format( Locale.ENGLISH, "Message[id=%d]%s inserted", createdMessage.getId(), success ? "" : " NOT" ) );

            if( newMessagesList != null ) {
                newMessagesList.add( createdMessage );
            }

        } else {

            existingMessage.setTitle( onlineMessage.getTitle() );
            existingMessage.setContent( onlineMessage.getContent() );
            existingMessage.setImage( onlineMessage.getImage() );
            existingMessage.setVersion( onlineMessage.getVersion() );
            existingMessage.setLastUpdate( onlineMessage.getLastUpdate() );
            existingMessage.setQuizId( onlineMessage.getQuizId() );

            final boolean success = messageDao.update( existingMessage ) == 1;
            DkqLog.i( TAG, String.format( Locale.ENGLISH, "Message[id=%d]%s updated", existingMessage.getId(), success ? "" : " NOT" ) );

        }
    }

    private Long getQuizIdByNumber( @Nullable final Integer quizNumber) {
        if( quizNumber == null ) {
            return null;
        }

        final Quiz quiz = quizDao.loadQuizByNumber( quizNumber );
        if( quiz == null ) {
            return null;
        }
        return quiz.id;
    }

    public LiveData<Resource<Message>> loadMessage( final long messageId, final int messageNumber ) {
        return new NetworkBoundResource<Message, Message>(executors) {
            @Override
            protected void saveCallResult(@NonNull Message item) {
                saveMessage( null, item );
            }

            @Override
            protected boolean shouldFetch(@Nullable Message data) {
                return ( ( rateLimiter.shouldFetch( LIMITER_MESSAGE + ":" + messageNumber ) && data != null && Message.Type.ARTICLE.equals( data.getType() ) ) ) || data == null;
            }

            @NonNull
            @Override
            protected LiveData<Message> loadFromDb() {
                return messageDao.loadMessage( messageId );
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<ApiResult<Message>>> createCall() {
                return webservice.getMessage( messageNumber );
            }

            @Override
            protected void onFetchFailed() {
                rateLimiter.reset( LIMITER_MESSAGE + ":" + messageNumber );
                DkqLog.e(TAG, String.format( Locale.ENGLISH, "Fetch failed for message (id=%d, number=%d)", messageId, messageNumber ) );
            }

            @Override
            protected boolean onApiError(int code) {
                if( code == 404 && messageId > 0 ) {
                    return messageDao.deleteMessage( messageId ) > 0;
                }
                return super.onApiError(code);
            }
        }.getAsLiveData();
    }

    public LiveData<Resource<List<QuizzerListItem>>> loadQuizzers(final QuizzerRole criteria) {
        return new NetworkBoundResource<List<QuizzerListItem>,List<Quizzer>>(executors) {
            @Override
            protected void saveCallResult(@NonNull List<Quizzer> items) {
                db.beginTransaction();
                try {
                    saveQuizzers( items );
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }

            @Override
            protected boolean shouldFetch(@Nullable List<QuizzerListItem> data) {
                return rateLimiter.shouldFetch( LIMITER_QUIZZERS ) || data == null || data.isEmpty();
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
            protected LiveData<ApiResponse<ApiResult<List<Quizzer>>>> createCall() {
                return webservice.getQuizzers();
            }

            @Override
            protected void onFetchFailed() {
                rateLimiter.reset( LIMITER_QUIZZERS );
                DkqLog.e(TAG, "Fetch failed for quizzers." );
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
            DkqLog.i(TAG, "Insert new Quizzer " + onlineQuizzer.number + " (id=" + id + ")");
            return id;
        } else {
            onlineQuizzer.id = existingQuizzer.id; // update is ID based so copy existing ID to new one
            quizzerDao.updateQuizzer( onlineQuizzer );
            DkqLog.i(TAG, "Update Quizzer " + onlineQuizzer.number );
            return onlineQuizzer.id;
        }
    }

    public void updateMessage(final Message message ) {
        executors.diskIO().execute(() -> messageDao.update(message));
    }

    public LiveData<Integer> getNewMessagesCount() {
        return messageDao.loadNewMessagesCount();
    }

    private int getMaxMessageNumber() {
        return messageDao.loadMaxMessageNumber();
    }

    public void insertQuiz(final Quiz quiz ) {
        executors.diskIO().execute(() -> quizDao.insertQuiz(quiz));
    }

    public void deleteQuizzesByNumber( final int[] quizNumbers ) {
        executors.diskIO().execute(() -> quizDao.deleteQuizzesByNumber( quizNumbers ));
    }

    public void createSamples() {
        executors.diskIO().execute(() -> {
            db.beginTransaction();
            try {
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
                            questionDao.insert( question );
                        }
                    }
                }

                final Message[] messages = SampleCreator.createSampleMessages( quizzes[0] );
                for( Message message : messages ) {
                    messageDao.insert( message );
                }

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        });
    }

    public void dropAll() {
        executors.diskIO().execute(() -> {
            messageDao.deleteAllMessages();
            questionDao.deleteAllQuestions();
            quizDao.deleteAllQuizzes();
            quizzerDao.deleteAllQuizzers();
        });
    }

    public List<Quiz> queryQuizzes() {
        return quizDao.queryQuizzes();
    }

    public void saveUpdateLogMessage( @StringRes final int titleResId, @StringRes final int contentResId ) {
        executors.diskIO().execute(() -> {
            final Message updateLogMessage = new Message();
            updateLogMessage.setType( Message.Type.UPDATE_LOG );
            updateLogMessage.setNumber( getMaxMessageNumber() );
            updateLogMessage.setTitle( context.getString( titleResId ) );
            updateLogMessage.setContent( context.getString( contentResId ) );
            updateLogMessage.setVersion( 1 );
            updateLogMessage.setLastUpdate( DateUtils.javaDateToJoomlaDate( new Date() ) );
            updateLogMessage.setRead( false );

            final Message persistedUpdateLogMessage = messageDao.insert( updateLogMessage );
            DkqLog.i("DkqRepo", "Update log message created " + persistedUpdateLogMessage.toString());
            NotificationReceiver.forOneNewMessage( context, persistedUpdateLogMessage.getTitle(), persistedUpdateLogMessage.getId(), persistedUpdateLogMessage.getNumber() );
        });
    }

    @Nullable
    public Response<ApiResult<List<Quiz>>> requestQuizzesIfNotLimited() throws IOException {
        if( rateLimiter.shouldFetch( LIMITER_QUIZZES ) ) {
            return webservice.getQuizzesList().execute();
        }
        return null;
    }

    @Nullable
    public Response<ApiResult<List<Message>>> requestMessagesIfNotLimited() throws IOException {
        if( rateLimiter.shouldFetch( LIMITER_MESSAGES ) ) {
            return webservice.getMessagesList().execute();
        }
        return null;
    }

    @Nullable
    public Response<ApiResult<List<Question>>> requestQuestionsIfNotLimited(int quizNumber) throws IOException {
        if( rateLimiter.shouldFetch( quizNumber + ":" + LIMITER_QUESTIONS ) ) {
            return webservice.getQuestionsList( quizNumber ).execute();
        }
        return null;
    }
}