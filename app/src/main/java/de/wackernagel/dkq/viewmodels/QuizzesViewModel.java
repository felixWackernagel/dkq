package de.wackernagel.dkq.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import de.wackernagel.dkq.repository.DkqRepository;
import de.wackernagel.dkq.room.entities.Quiz;
import de.wackernagel.dkq.webservice.Resource;

public class QuizzesViewModel extends ViewModel {

    private final DkqRepository repository;

    QuizzesViewModel(DkqRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<List<Quiz>>> loadQuizzes() {
        return repository.loadQuizzes();
    }
}
