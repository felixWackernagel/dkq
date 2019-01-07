package de.wackernagel.dkq.room;

import android.util.Log;

import de.wackernagel.dkq.room.entities.Message;
import de.wackernagel.dkq.room.entities.Quiz;

public final class SampleCreator {

    private SampleCreator() {
        // no instance needed
    }

    public static void createSamples( final AppDatabase db ) {
        Log.i("DKQ", "create samples");
    }

    public static Message[] createSampleMessages() {
        return new Message[] {
            message(1, "Lorem ipsum dolor sit amet.", "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.", "https://cdn.pixabay.com/photo/2013/03/02/02/41/city-89197_960_720.jpg"),
            message(2, "Amet sit dolor ipsum Lorem.", "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.")
        };
    }

    public static Quiz createFutureSampleQuiz() {
        final Quiz quiz = new Quiz();
        quiz.number = 999;
        quiz.location = "Dorfpub, Hobbitstraße 20, 23456 Hobbingen";
        quiz.quizDate = "2099-06-12 20:00:00";
        quiz.quizMasterId = 0L;
        quiz.latitude = -37.872196;
        quiz.longitude = 175.683205;
        return quiz;
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