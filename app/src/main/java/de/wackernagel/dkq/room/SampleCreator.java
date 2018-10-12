package de.wackernagel.dkq.room;

import android.util.Log;

import de.wackernagel.dkq.room.entities.Message;
import de.wackernagel.dkq.room.entities.Question;
import de.wackernagel.dkq.room.entities.Quiz;

public final class SampleCreator {

    public static void createSamples( final AppDatabase db ) {
        Log.i("DKQ", "create samples");
        //final long quiz39 = quiz( db, 39, "Zapfanstalt, Sebnitzer Str. 15, 01099 Dresden", "2018-03-20 20:00:00", "Bob Doe", 51.0690181,13.7555306 );
        //question( db, quiz39, 1, "Der Sinn des Lebens?", "42" );
    }

    public static Message[] createSampleMessages() {
        return new Message[] {
            message(1, "Lorem ipsum dolor sit amet.", "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.", "https://cdn.pixabay.com/photo/2013/03/02/02/41/city-89197_960_720.jpg"),
            message(2, "Amet sit dolor ipsum Lorem.", "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.")
        };
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

    private static Message message( final int number, final String title, final String content )
    {
        return message( number, title, content, null);
    }

    private static Message message( final int number, final String title, final String content, final String image )
    {
        final Message theMessage = new Message();
        theMessage.number = number;
        theMessage.title = title;
        theMessage.content = content;
        theMessage.image = image;
        theMessage.read = 0;
        theMessage.version = 1;
        theMessage.lastUpdate = "2018-01-01 08:00:00";
        return theMessage;
    }
}