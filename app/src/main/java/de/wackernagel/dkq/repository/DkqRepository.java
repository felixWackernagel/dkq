package de.wackernagel.dkq.repository;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import de.wackernagel.dkq.R;
import de.wackernagel.dkq.receiver.NotificationReceiver;
import de.wackernagel.dkq.room.daos.QuestionDao;
import de.wackernagel.dkq.room.daos.QuizDao;
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

    @Inject
    public DkqRepository( final Context context, final Webservice webservice, final QuizDao quizDao, final QuestionDao questionDao ) {
        this.context = context;
        this.webservice = webservice;
        this.quizDao = quizDao;
        this.questionDao = questionDao;
    }

    public LiveData<Resource<List<Quiz>>> loadQuizzes() {
        return new NetworkBoundResource<List<Quiz>,List<Quiz>>() {
            @Override
            protected void saveCallResult(@NonNull List<Quiz> items) {
                int quizzesInFuture = 0;
                for( Quiz onlineQuiz : items ) {
                    quizzesInFuture += saveQuiz( onlineQuiz );
                }
                if( quizzesInFuture > 0 ) {
                    NotificationReceiver.forNextQuiz( context, context.getString(R.string.next_quiz_title), context.getString(R.string.next_quiz_description, quizzesInFuture) );
                }
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
}