package de.wackernagel.dkq.repository;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import de.wackernagel.dkq.receiver.NotificationReceiver;
import de.wackernagel.dkq.room.daos.MessageDao;
import de.wackernagel.dkq.room.daos.QuestionDao;
import de.wackernagel.dkq.room.daos.QuizDao;
import de.wackernagel.dkq.room.entities.Message;
import de.wackernagel.dkq.room.entities.Question;
import de.wackernagel.dkq.room.entities.Quiz;
import de.wackernagel.dkq.utils.DateUtils;
import de.wackernagel.dkq.webservice.ApiResponse;
import de.wackernagel.dkq.webservice.NetworkBoundResource;
import de.wackernagel.dkq.webservice.Resource;
import de.wackernagel.dkq.webservice.Webservice;

public class DkqRepository {

    private final Context context;
    private final Webservice webservice;
    private final QuizDao quizDao;
    private final QuestionDao questionDao;
    private final MessageDao messageDao;

    @Inject
    public DkqRepository( final Context context, final Webservice webservice, final QuizDao quizDao, final QuestionDao questionDao, final MessageDao messageDao ) {
        this.context = context;
        this.webservice = webservice;
        this.quizDao = quizDao;
        this.questionDao = questionDao;
        this.messageDao = messageDao;
    }

    public LiveData<Resource<List<Quiz>>> loadQuizzes() {
        return new NetworkBoundResource<List<Quiz>,List<Quiz>>() {
            @Override
            protected void saveCallResult(@NonNull List<Quiz> items) {
                saveQuizzesWithNotification( items );
            }

            @Override
            protected boolean shouldFetch(@Nullable List<Quiz> data) {
                return true;
            }

            @NonNull
            @Override
            protected LiveData<List<Quiz>> loadFromDb() {
                return quizDao.loadQuizzes();
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<List<Quiz>>> createCall() {
                return webservice.getQuizzes();
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
        return new NetworkBoundResource<Quiz,Quiz>() {
            @Override
            protected void saveCallResult(@NonNull Quiz item) {
                saveQuiz( item );
            }

            @Override
            protected boolean shouldFetch(@Nullable Quiz data) {
                return true;
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
        }.getAsLiveData();
    }

    public LiveData<Resource<List<Question>>> loadQuestions( final long quizId, final long quizNumber ) {
        return new NetworkBoundResource<List<Question>,List<Question>>() {
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
                return true;
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
        }.getAsLiveData();
    }

    public LiveData<Resource<List<Message>>> loadMessages() {
        return new NetworkBoundResource<List<Message>,List<Message>>() {
            @Override
            protected void saveCallResult(@NonNull List<Message> items) {
                saveMessagesWithNotification( items );
            }

            @Override
            protected boolean shouldFetch(@Nullable List<Message> data) {
                return true;
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
        return new NetworkBoundResource<Message, Message>() {
            @Override
            protected void saveCallResult(@NonNull Message item) {
                saveMessage( item );
            }

            @Override
            protected boolean shouldFetch(@Nullable Message data) {
                return true;
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
        }.getAsLiveData();
    }

    public void updateMessage( Message message ) {
        new UpdateMessagesTask(messageDao).execute( message );
    }

    public void insertMessages( Message... messages ) {
        new InsertMessagesTask(messageDao).execute( messages );
    }

    public void deleteAllMessages() {
        new DeleteAllMessagesTask(messageDao).execute();
    }

    private static class InsertMessagesTask extends AsyncTask<Message, Void , Void> {
        private final MessageDao dao;

        InsertMessagesTask( MessageDao dao ) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(Message... messages) {
            dao.insertMessages( messages );
            return null;
        }
    }

    private static class UpdateMessagesTask extends AsyncTask<Message, Void , Void> {
        private final MessageDao dao;

        UpdateMessagesTask( MessageDao dao ) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(Message... messages) {
            dao.updateMessage( messages[0] );
            return null;
        }
    }

    private static class DeleteAllMessagesTask extends AsyncTask<Void, Void , Void> {
        private final MessageDao dao;

        DeleteAllMessagesTask( MessageDao dao ) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            dao.deleteAllMessages();
            return null;
        }
    }

    public Webservice getWebService() {
        return webservice;
    }
}