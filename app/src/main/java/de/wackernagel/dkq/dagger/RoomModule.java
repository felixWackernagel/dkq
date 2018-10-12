package de.wackernagel.dkq.dagger;

import android.app.Application;
import androidx.lifecycle.ViewModelProvider;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.annotation.NonNull;

import java.util.concurrent.Executors;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.wackernagel.dkq.repository.DkqRepository;
import de.wackernagel.dkq.room.AppDatabase;
import de.wackernagel.dkq.room.SampleCreator;
import de.wackernagel.dkq.room.daos.MessageDao;
import de.wackernagel.dkq.room.daos.QuestionDao;
import de.wackernagel.dkq.room.daos.QuizDao;
import de.wackernagel.dkq.webservice.Webservice;
import de.wackernagel.dkq.viewmodels.ViewModelFactory;

@Module
public class RoomModule {

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `messages` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `number` INTEGER NOT NULL, `title` TEXT, `content` TEXT, `image` TEXT, `version` INTEGER NOT NULL, `lastUpdate` TEXT, `read` INTEGER NOT NULL)");
            database.execSQL("CREATE UNIQUE INDEX `index_messages_number` ON `messages` (`number`)");
        }
    };

    private final AppDatabase database;
    private final Application application;

    public RoomModule( final Application application) {
        this.application = application;
        this.database = Room
                .databaseBuilder( application, AppDatabase.class, "dkq.db" )
                .addMigrations( MIGRATION_1_2 )
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
    DkqRepository provideDkqRepository(final Webservice webservice, final QuizDao quizDao, final QuestionDao questionDao, final MessageDao messageDao ) {
        return new DkqRepository( application.getApplicationContext(), webservice, quizDao, questionDao, messageDao );
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
    MessageDao provideMessageDao( final AppDatabase database ) {
        return database.messageDao();
    }

    @Provides
    @Singleton
    AppDatabase provideAppDatabase() {
        return database;
    }

    @Provides
    @Singleton
    Application provideApplication() {
        return application;
    }

    @Provides
    @Singleton
    ViewModelProvider.Factory providerViewModelFactory( final DkqRepository repository, final Application application ) {
        return new ViewModelFactory( repository, application );
    }
}
