package de.wackernagel.dkq.viewmodels;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import de.wackernagel.dkq.repository.DkqRepository;
import de.wackernagel.dkq.room.question.Question;
import de.wackernagel.dkq.room.entities.Quiz;
import de.wackernagel.dkq.room.entities.Quizzer;
import de.wackernagel.dkq.webservice.Resource;

public class QuestionsViewModel extends ViewModel {

    private final DkqRepository repository;

    @Inject
    QuestionsViewModel(DkqRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<List<Question>>> loadQuestions( long quizId, int quizNumber) {
        return repository.loadQuestions( quizId, quizNumber );
    }

    public LiveData<Resource<Quiz>> loadQuiz( final long quizId, final int quizNumber) {
        return repository.loadQuiz( quizId, quizNumber );
    }

    public LiveData<Quizzer> loadWinner( final long quizId ) {
        return repository.loadQuizzer( QuizzerRole.WINNER, quizId );
    }

    public LiveData<Quizzer> loadQuizmaster( final long quizId ) {
        return repository.loadQuizzer( QuizzerRole.QUIZMASTER, quizId );
    }
}
