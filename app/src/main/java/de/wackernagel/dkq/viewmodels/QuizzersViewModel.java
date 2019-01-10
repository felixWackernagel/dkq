package de.wackernagel.dkq.viewmodels;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import de.wackernagel.dkq.repository.DkqRepository;
import de.wackernagel.dkq.room.entities.QuizzerListItem;
import de.wackernagel.dkq.webservice.Resource;

public class QuizzersViewModel extends ViewModel {

    private final DkqRepository repository;

    QuizzersViewModel(DkqRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<List<QuizzerListItem>>> loadQuizzers( final QuizzersSearch criteria ) {
        return repository.loadQuizzers( criteria );
    }
}
