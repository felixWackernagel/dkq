package de.wackernagel.dkq.viewmodels;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import de.wackernagel.dkq.repository.DkqRepository;
import de.wackernagel.dkq.room.entities.QuizListItem;
import de.wackernagel.dkq.webservice.Resource;

public class QuizzesViewModel extends ViewModel {

    private final DkqRepository repository;

    @Inject
    QuizzesViewModel(DkqRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<List<QuizListItem>>> loadQuizzes() {
        return repository.loadQuizzes();
    }
}
