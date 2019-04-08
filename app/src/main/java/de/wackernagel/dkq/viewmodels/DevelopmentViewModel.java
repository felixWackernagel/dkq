package de.wackernagel.dkq.viewmodels;

import androidx.lifecycle.ViewModel;
import de.wackernagel.dkq.repository.DkqRepository;
import de.wackernagel.dkq.room.entities.Quiz;

public class DevelopmentViewModel extends ViewModel {

    private final DkqRepository repository;

    DevelopmentViewModel(DkqRepository repository) {
        this.repository = repository;
    }

    public void insertQuiz( Quiz quiz ) {
        repository.insertQuiz(quiz);
    }

    public void deleteQuizzesByNumber( int[] quizNumbers ) {
        repository.deleteQuizzesByNumber( quizNumbers );
    }

    public void dropAllAndCreateSamples() {
        repository.createSamples();
    }

    public void dropAll() {
        repository.dropAll();
    }
}
