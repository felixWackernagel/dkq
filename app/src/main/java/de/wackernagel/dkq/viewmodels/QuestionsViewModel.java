package de.wackernagel.dkq.viewmodels;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import de.wackernagel.dkq.repository.DkqRepository;
import de.wackernagel.dkq.room.entities.Question;
import de.wackernagel.dkq.room.entities.Quiz;
import de.wackernagel.dkq.room.entities.Quizzer;
import de.wackernagel.dkq.webservice.Resource;

public class QuestionsViewModel extends ViewModel {

    private final DkqRepository repository;

    QuestionsViewModel(DkqRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<List<Question>>> loadQuestions( long quizId, int quizNumber) {
        return repository.loadQuestions( quizId, quizNumber );
    }

    public LiveData<Resource<Quiz>> loadQuiz( final long quizId, final int quizNumber) {
        return repository.loadQuiz( quizId, quizNumber );
    }

    public LiveData<Resource<Quizzer>> loadQuizzer( final long quizzerId ) {
        return repository.loadQuizzer( quizzerId );
    }
}
