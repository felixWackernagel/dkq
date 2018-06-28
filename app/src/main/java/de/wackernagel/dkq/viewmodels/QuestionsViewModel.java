package de.wackernagel.dkq.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import de.wackernagel.dkq.repository.DkqRepository;
import de.wackernagel.dkq.room.entities.Question;
import de.wackernagel.dkq.room.entities.Quiz;
import de.wackernagel.dkq.webservice.Resource;

public class QuestionsViewModel extends ViewModel {

    private final DkqRepository repository;

    QuestionsViewModel(DkqRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<List<Question>>> loadQuestions( long quizId) {
        return repository.loadQuestions( quizId );
    }

    public LiveData<Resource<Quiz>> loadQuiz( long quizId) {
        return repository.loadQuiz( quizId );
    }
}
