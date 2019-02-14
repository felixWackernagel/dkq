package de.wackernagel.dkq.room;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import de.wackernagel.dkq.room.entities.Message;
import de.wackernagel.dkq.room.entities.Question;
import de.wackernagel.dkq.room.entities.Quiz;
import de.wackernagel.dkq.room.entities.Quizzer;

public final class SampleCreator {

    private SampleCreator() {
        // no instance needed
    }

    public static void createSamples( final AppDatabase db ) {
        Log.i("DKQ", "create samples");
    }

    public static Quizzer[] createSampleQuizzers() {
        return new Quizzer[] {
            newQuizzer(1, "Frodo B."),
            newQuizzer(2, "Bilbo B.")
        };
    }

    public static Quiz[] createSampleQuizzes( final Quizzer quizzerOne, final Quizzer quizzerTwo ) {
        return new Quiz[] {
            newQuiz(1, "Der Grüne Drache", "Hobbitstraße 1, Hobbingen", "2018-01-15 20:00:00", quizzerOne.id, quizzerTwo.id ),
            newQuiz(2, "Smaugs Einöde", "Einsamer Berg 4, Mittelerde", "2018-02-15 20:00:00", quizzerTwo.id, quizzerOne.id ),
            newQuiz(3, "Barad-Dur", "Mordor Platz 8, Mittelerde", "2018-03-15 20:00:00", quizzerOne.id, quizzerTwo.id ),
            newQuiz(4, "Khazad-Dum", "Moria Allee 2, Mittelerde", "2019-12-15 20:00:00", quizzerTwo.id, null )
        };
    }

    public static Question[] createSampleQuestions( final Quiz quiz ) {
        final List<Question> questions = new ArrayList<>(60);
        questions.add( newQuestion( 1, quiz.id, "Wofür ist Gandalf bekannt?", "Feuerwerk" ) );
        questions.add( newQuestion( 2, quiz.id, "Was versteckt Bilbo Beutlin?", "Saurons Ring" ) );
        questions.add( newQuestion( 3, quiz.id, "Wie heißt Gollum mit richtigen Namen?", "Smeagol" ) );
        questions.add( newQuestion( 4, quiz.id, "Wieviele Hobbits gehören zu den Gefährten?", "4" ) );
        questions.add( newQuestion( 5, quiz.id, "Wieviele Ringgeister gibt es?", "9" ) );
        questions.add( newQuestion( 6, quiz.id, "Wie heißt der Herr der Pferde?", "Schattenfell" ) );
        for( int index = 7; index <= 60; index++ ) {
            questions.add( newQuestion( index, quiz.id, "", "") );
        }
        return questions.toArray( new Question[60] );
    }

    public static Message[] createSampleMessages() {
        return new Message[] {
                newMessage(1, "Wir gratulieren zu Bilbo B. Sieg.", "Nach einem langen Quiz hat Bilbo B. mit den meisten richtigen Antworten gesiegt.", null),
                newMessage(2, "Das nächste Quiz steht fest.", "Lange haben wir auf Frodo B. gewartet, doch nun kann das nächste Quiz stattfinden.", "https://cdn.pixabay.com/photo/2013/03/02/02/41/city-89197_960_720.jpg")
        };
    }

    public static Quiz createSampleFutureQuiz() {
        return newQuiz( 999, "Dorfpub", "Hobbitstraße 20, 23456 Hobbingen", "2099-06-12 20:00:00", null, null );
    }

    private static Question newQuestion( final int number, final long quizId, final String question, final String answer ) {
        final Question newQuestion = new Question();
        newQuestion.number = number;
        newQuestion.quizId = quizId;
        newQuestion.question = question;
        newQuestion.answer = answer;
        newQuestion.version = 1;
        newQuestion.lastUpdate = "2019-01-01 20:00:00";
        newQuestion.published = 1;
        return newQuestion;
    }

    private static Quizzer newQuizzer( final int number, final String name) {
        final Quizzer quizzer = new Quizzer();
        quizzer.name = name;
        quizzer.number = number;
        quizzer.image = null;
        quizzer.version = 1;
        quizzer.lastUpdate = "2019-01-01 20:00:00";
        return quizzer;
    }

    private static Quiz newQuiz( final int number, final String locationName, final String locationAddress, final String quizDate, final Long quizMasterId, @Nullable final Long winnerId ) {
        final Quiz quiz = new Quiz();
        quiz.number = number;
        quiz.location = locationName;
        quiz.address = locationAddress;
        quiz.quizDate = quizDate;
        quiz.quizMasterId = quizMasterId;
        quiz.winnerId = winnerId;
        quiz.latitude = 0.0d;
        quiz.longitude = 0.0d;
        quiz.version = 1;
        quiz.published = 1;
        quiz.lastUpdate = "2019-01-01 20:00:00";
        return quiz;
    }

    private static Message newMessage( final int number, final String title, final String content, final String image )
    {
        final Message theMessage = new Message();
        theMessage.number = number;
        theMessage.title = title;
        theMessage.content = content;
        theMessage.image = image;
        theMessage.read = 0;
        theMessage.version = 1;
        theMessage.lastUpdate = "2019-01-01 20:00:00";
        return theMessage;
    }
}