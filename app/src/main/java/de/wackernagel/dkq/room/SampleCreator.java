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
        Log.i("DKQ", "create samples for db " + db.getOpenHelper().getDatabaseName() );
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
        questions.add( newQuestion( 1, quiz.id, "Wofür ist Gandalf bekannt?", "Feuerwerk", null ) );
        questions.add( newQuestion( 2, quiz.id, "Was versteckt Bilbo Beutlin?", "Saurons Ring", null ) );
        questions.add( newQuestion( 3, quiz.id, "Wie heißt Gollum mit richtigen Namen?", "Smeagol", null ) );
        questions.add( newQuestion( 4, quiz.id, "Wieviele Hobbits gehören zu den Gefährten?", "4", null ) );
        questions.add( newQuestion( 5, quiz.id, "Wieviele Ringgeister gibt es?", "9", null ) );
        questions.add( newQuestion( 6, quiz.id, "Wie heißt der Herr der Pferde?", "Schattenfell", null ) );
        questions.add( newQuestion( 7, quiz.id, "Wie heißt dieses Obst?", "Erdbeere", "https://cdn.pixabay.com/photo/2016/05/16/14/22/strawberries-1395771_960_720.jpg"));
        for( int index = 8; index <= 60; index++ ) {
            questions.add( newQuestion( index, quiz.id, "", "", null) );
        }
        return questions.toArray( new Question[60] );
    }

    public static Message[] createSampleMessages( @Nullable final Quiz quiz ) {
        final Long quizId = quiz != null ? quiz.id : null;
        return new Message[] {
                newMessage(1, Message.Type.ARTICLE,"Wir gratulieren zu Bilbo B. Sieg.", "Nach einem langen Quiz hat Bilbo B. mit den meisten richtigen Antworten gesiegt.", null, quizId ),
                newMessage(2, Message.Type.ARTICLE,"Das nächste Quiz steht fest.", "Lange haben wir auf Frodo B. gewartet, doch nun kann das nächste Quiz stattfinden.", "https://cdn.pixabay.com/photo/2013/03/02/02/41/city-89197_960_720.jpg", quizId ),
                newMessage(2, Message.Type.UPDATE_LOG,"Version 2.0", "Folgende Dinge haben sich geändert", null, null ),
        };
    }

    public static Quiz createSampleFutureQuiz() {
        return newQuiz( 999, "Dorfpub", "Hobbitstraße 20, 23456 Hobbingen", "2099-06-12 20:00:00", null, null );
    }

    private static Question newQuestion( final int number, final long quizId, final String question, final String answer, final String image ) {
        final Question newQuestion = new Question();
        newQuestion.number = number;
        newQuestion.quizId = quizId;
        newQuestion.question = question;
        newQuestion.answer = answer;
        newQuestion.image = image;
        newQuestion.version = 0;
        newQuestion.lastUpdate = "2018-01-01 20:00:00";
        newQuestion.published = 1;
        return newQuestion;
    }

    private static Quizzer newQuizzer( final int number, final String name) {
        final Quizzer quizzer = new Quizzer();
        quizzer.name = name;
        quizzer.number = number;
        quizzer.image = null;
        quizzer.version = 0;
        quizzer.lastUpdate = "2018-01-01 20:00:00";
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
        quiz.version = 0;
        quiz.published = 1;
        quiz.lastUpdate = "2018-01-01 20:00:00";
        return quiz;
    }

    private static Message newMessage(final int number, final Message.Type type, final String title, final String content, final String image, final Long quizId )
    {
        final Message theMessage = new Message();
        theMessage.number = number;
        theMessage.type = type;
        theMessage.title = title;
        theMessage.content = content;
        theMessage.image = image;
        theMessage.quizId = quizId;
        theMessage.read = 0;
        theMessage.version = 0;
        theMessage.lastUpdate = "2018-01-01 20:00:00";
        return theMessage;
    }
}