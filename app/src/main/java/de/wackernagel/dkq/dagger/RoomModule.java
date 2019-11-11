package de.wackernagel.dkq.dagger;

import android.app.Application;

import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import javax.inject.Singleton;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;
import de.wackernagel.dkq.AppExecutors;
import de.wackernagel.dkq.BuildConfig;
import de.wackernagel.dkq.DkqConstants;
import de.wackernagel.dkq.DkqLog;
import de.wackernagel.dkq.repository.DkqRepository;
import de.wackernagel.dkq.room.AppDatabase;
import de.wackernagel.dkq.room.daos.QuizDao;
import de.wackernagel.dkq.room.daos.QuizzerDao;
import de.wackernagel.dkq.room.message.MessageDao;
import de.wackernagel.dkq.room.question.QuestionDao;
import de.wackernagel.dkq.utils.ConnectivityInterceptor;
import de.wackernagel.dkq.webservice.LiveDataCallAdapterFactory;
import de.wackernagel.dkq.webservice.Webservice;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static de.wackernagel.dkq.DkqConstants.API.BASE_URL;

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

    @Provides
    @Singleton
    AppDatabase providerAppDatabase( final Application application ) {
        return Room.databaseBuilder( application, AppDatabase.class, DkqConstants.Database.NAME)
                .addMigrations( MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4 )
                .build();
    }

    @Provides
    @Singleton
    DkqRepository provideDkqRepository( final Application application, final AppExecutors executors, final AppDatabase database, final Webservice webservice, final QuizDao quizDao, final QuestionDao questionDao, final MessageDao messageDao, final QuizzerDao quizzerDao ) {
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
    Cache provideCache(final Application application ) {
        // 10 MB response cache
        final long cacheSize = 10 * 1024 * 1024;
        return new Cache( application.getCacheDir(), cacheSize );
    }

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient( final Application application, final Cache cache ) {
        final OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.addInterceptor( new ConnectivityInterceptor( application ) );
        client.cache( cache );

        if (BuildConfig.DEBUG) {
            final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(message -> DkqLog.i("RetrofitModule", message));
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            client.addInterceptor(interceptor);
        }
        return client.build();
    }

    @Provides
    @Singleton
    Webservice provideWebservice( final Lazy<OkHttpClient> okHttpClient ) {
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory( new LiveDataCallAdapterFactory() )
                .callFactory( (request) -> okHttpClient.get().newCall( request ) )
                .build();
        return retrofit.create(Webservice.class);
    }

    @Provides
    @Singleton
    AppExecutors providerAppExecuters() {
        return new AppExecutors();
    }
}
