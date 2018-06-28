package de.wackernagel.dkq.room;

import de.wackernagel.dkq.room.entities.Question;
import de.wackernagel.dkq.room.entities.Quiz;

public final class SampleCreator {

    public static void createSamples( final AppDatabase db ) {
        //final long quiz39 = quiz( db, 39, "Zapfanstalt, Sebnitzer Str. 15, 01099 Dresden", "2018-03-20 20:00:00", "Bob Doe", 51.0690181,13.7555306 );
        //question( db, quiz39, 1, "Der Sinn des Lebens?", "42" );
    }

    private static long quiz( final AppDatabase database, final int number, final String location, final String quizDate, final String quizMaster, final double latitude, final double longitude ) {
        final Quiz quiz = new Quiz();
        quiz.number = number;
        quiz.location = location;
        quiz.quizDate = quizDate;
        quiz.quizMaster = quizMaster;
        quiz.latitude = latitude;
        quiz.longitude = longitude;
        return database.quizDao().insertQuiz( quiz );
    }

    private static void question( final AppDatabase database, final long quizId, final int number, final String question, final String answer )
    {
        final Question theQuestion = new Question();
        theQuestion.quizId = quizId;
        theQuestion.number = number;
        theQuestion.question = question;
        theQuestion.answer = answer;
        database.questionDao().insertQuestion( theQuestion );
    }
}