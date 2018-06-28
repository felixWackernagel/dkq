package de.wackernagel.dkq.dagger;

import android.app.Application;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.annotation.NonNull;

import java.util.concurrent.Executors;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.wackernagel.dkq.repository.DkqRepository;
import de.wackernagel.dkq.room.AppDatabase;
import de.wackernagel.dkq.room.SampleCreator;
import de.wackernagel.dkq.room.daos.QuestionDao;
import de.wackernagel.dkq.room.daos.QuizDao;
import de.wackernagel.dkq.webservice.Webservice;
import de.wackernagel.dkq.viewmodels.ViewModelFactory;

@Module
public class RoomModule {

    private final AppDatabase database;
    private final Application application;

    public RoomModule( final Application application) {
        this.application = application;
        this.database = Room
                .databaseBuilder( application, AppDatabase.class, "dkq.db" )
                .addCallback(new RoomDatabase.Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        Executors.newSingleThreadExecutor().execute(new Runnable() {
                            @Override
                            public void run() {
                                SampleCreator.createSamples( database );
                            }
                        });
                    }
                })
                .build();
    }

    @Provides
    @Singleton
    DkqRepository provideDkqRepository(final Webservice webservice, final QuizDao quizDao, final QuestionDao questionDao ) {
        return new DkqRepository( application.getApplicationContext(), webservice, quizDao, questionDao );
    }

    @Provides
    @Singleton
    QuizDao provideQuizDao( final AppDatabase database ) {
        return database.quizDao();
    }

    @Provides
    @Singleton
    QuestionDao provideQuestionDao( final AppDatabase database ) {
        return database.questionDao();
    }

    @Provides
    @Singleton
    AppDatabase provideAppDatabase() {
        return database;
    }

    @Provides
    @Singleton
    ViewModelProvider.Factory providerViewModelFactory( final DkqRepository repository ) {
        return new ViewModelFactory( repository );
    }
}
