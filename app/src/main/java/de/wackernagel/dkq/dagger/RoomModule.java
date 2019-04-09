package de.wackernagel.dkq.dagger;

import android.app.Application;

import javax.inject.Singleton;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import dagger.Module;
import dagger.Provides;
import de.wackernagel.dkq.AppExecutors;
import de.wackernagel.dkq.repository.DkqRepository;
import de.wackernagel.dkq.room.AppDatabase;
import de.wackernagel.dkq.room.SampleCreator;
import de.wackernagel.dkq.room.daos.MessageDao;
import de.wackernagel.dkq.room.daos.QuestionDao;
import de.wackernagel.dkq.room.daos.QuizDao;
import de.wackernagel.dkq.room.daos.QuizzerDao;
import de.wackernagel.dkq.viewmodels.ViewModelFactory;
import de.wackernagel.dkq.webservice.Webservice;

@Module
public class RoomModule {

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `messages` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `number` INTEGER NOT NULL, `title` TEXT, `content` TEXT, `image` TEXT, `version` INTEGER NOT NULL, `lastUpdate` TEXT, `read` INTEGER NOT NULL)");
            database.execSQL("CREATE UNIQUE INDEX `index_messages_number` ON `messages` (`number`)");
        }
    };

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `quizzers` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `number` INTEGER NOT NULL, `name` TEXT, `image` TEXT, `version` INTEGER NOT NULL, `lastUpdate` TEXT)");
            database.execSQL("CREATE UNIQUE INDEX `index_quizzers_number` ON `quizzers` (`number`)");
            database.beginTransaction();
            try {
                database.execSQL("ALTER TABLE `quizzes` RENAME TO `temp_quizzes`");
                database.execSQL("CREATE TABLE IF NOT EXISTS `quizzes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `number` INTEGER NOT NULL, `location` TEXT, `address` TEXT, `quizDate` TEXT, `quizMasterId` INTEGER, `winnerId` INTEGER, `latitude` REAL NOT NULL, `longitude` REAL NOT NULL, `published` INTEGER NOT NULL, `version` INTEGER NOT NULL, `lastUpdate` TEXT, FOREIGN KEY(`winnerId`) REFERENCES `quizzers`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION , FOREIGN KEY(`quizMasterId`) REFERENCES `quizzers`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )");
                database.execSQL("INSERT INTO `quizzes` ( `id`, `number`, `location`, `address`, `quizDate`, `latitude`, `longitude`, `published`, `version`, `lastUpdate`, `quizMasterId`, `winnerId` ) SELECT `id`, `number`, `location`, `address`, `quizDate`, `latitude`, `longitude`, `published`, `version`, `lastUpdate`, NULL, NULL FROM `temp_quizzes`");
                database.execSQL("DROP TABLE `temp_quizzes`");
                database.execSQL("CREATE UNIQUE INDEX `index_quizzes_number` ON `quizzes` (`number`)");
                database.setTransactionSuccessful();
            } finally {
                database.endTransaction();
            }
        }
    };

    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.beginTransaction();
            try {
                database.execSQL( "ALTER TABLE `questions` ADD COLUMN `image` TEXT" );
                database.execSQL( "ALTER TABLE `messages` ADD COLUMN `type` INTEGER NOT NULL DEFAULT 0" );
                database.execSQL( "ALTER TABLE `messages` ADD COLUMN `quizId` INTEGER CONSTRAINT `fk_quiz` REFERENCES `quizzes`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION" );
                database.execSQL( "DROP INDEX IF EXISTS `index_messages_number`" );
                database.execSQL( "CREATE UNIQUE INDEX IF NOT EXISTS `index_messages_number_type` ON `messages` (`number`, `type`)" );
                database.execSQL( "CREATE INDEX `index_messages_quizId` ON `messages` (`quizId`)" );
                database.execSQL( "CREATE INDEX `index_questions_quizId` ON `questions` (`quizId`)" );
                database.execSQL( "CREATE INDEX `index_quizzes_quizMasterId` ON `quizzes` (`quizMasterId`)" );
                database.execSQL( "CREATE INDEX `index_quizzes_winnerId` ON `quizzes` (`winnerId`)" );

                // DROP COLUMN quizzes.published
                database.execSQL( "ALTER TABLE `quizzes` RENAME TO `temp_quizzes`" );
                database.execSQL( "CREATE TABLE IF NOT EXISTS `quizzes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `number` INTEGER NOT NULL, `location` TEXT, `address` TEXT, `quizDate` TEXT, `quizMasterId` INTEGER, `winnerId` INTEGER, `latitude` REAL NOT NULL, `longitude` REAL NOT NULL, `version` INTEGER NOT NULL, `lastUpdate` TEXT, FOREIGN KEY(`winnerId`) REFERENCES `quizzers`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION , FOREIGN KEY(`quizMasterId`) REFERENCES `quizzers`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )" );
                database.execSQL( "INSERT INTO `quizzes` ( `id`, `number`, `location`, `address`, `quizDate`, `latitude`, `longitude`, `version`, `lastUpdate`, `quizMasterId`, `winnerId` ) SELECT `id`, `number`, `location`, `address`, `quizDate`, `latitude`, `longitude`, `version`, `lastUpdate`, `quizMasterId`, `winnerId` FROM `temp_quizzes`");
                database.execSQL( "DROP TABLE `temp_quizzes`" );
                database.execSQL( "CREATE UNIQUE INDEX `index_quizzes_number` ON `quizzes` (`number`)" );
                database.execSQL( "CREATE INDEX `index_quizzes_quizMasterId` ON `quizzes` (`quizMasterId`)" );
                database.execSQL( "CREATE INDEX `index_quizzes_winnerId` ON `quizzes` (`winnerId`)" );

                // DROP COLUMN questions.published
                database.execSQL( "ALTER TABLE `questions` RENAME TO `temp_questions`" );
                database.execSQL( "CREATE TABLE IF NOT EXISTS `questions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `quizId` INTEGER NOT NULL, `number` INTEGER NOT NULL, `question` TEXT, `answer` TEXT, `image` TEXT, `version` INTEGER NOT NULL, `lastUpdate` TEXT, FOREIGN KEY(`quizId`) REFERENCES `quizzes`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )" );
                database.execSQL( "INSERT INTO `questions` ( `id`, `quizId`, `number`, `question`, `answer`, `image`, `version`, `lastUpdate` ) SELECT `id`, `quizId`, `number`, `question`, `answer`, `image`, `version`, `lastUpdate` FROM `temp_questions`");
                database.execSQL( "DROP TABLE `temp_questions`" );
                database.execSQL( "CREATE UNIQUE INDEX `index_questions_number_quizId` ON `questions` (`number`, `quizId`)" );
                database.execSQL( "CREATE INDEX `index_questions_quizId` ON `questions` (`quizId`)" );

                database.setTransactionSuccessful();
            } finally {
                database.endTransaction();
            }
        }
    };

    private final AppDatabase database;
    private final Application application;
    private final AppExecutors executors;

    public RoomModule( final Application application, final AppExecutors executors ) {
        this.application = application;
        this.executors = executors;
        this.database = Room
            .databaseBuilder( application, AppDatabase.class, "dkq.db" )
            .addMigrations( MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4 )
            .addCallback(new RoomDatabase.Callback() {
                @Override
                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                executors.diskIO().execute(() -> SampleCreator.createSamples( database ));
                }
            })
            .build();
    }

    @Provides
    @Singleton
    DkqRepository provideDkqRepository(final Webservice webservice, final QuizDao quizDao, final QuestionDao questionDao, final MessageDao messageDao, final QuizzerDao quizzerDao ) {
        return new DkqRepository( application.getApplicationContext(), database, executors, webservice, quizDao, questionDao, messageDao, quizzerDao );
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
    QuizzerDao provideQuizzerDao( final AppDatabase database ) {
        return database.quizzerDao();
    }

    @Provides
    @Singleton
    AppDatabase provideAppDatabase() {
        return database;
    }

    @Provides
    @Singleton
    AppExecutors provideAppExecutors() {
        return executors;
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
