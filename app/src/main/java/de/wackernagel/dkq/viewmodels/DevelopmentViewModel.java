package de.wackernagel.dkq.viewmodels;

import androidx.lifecycle.ViewModel;
import de.wackernagel.dkq.repository.DkqRepository;
import de.wackernagel.dkq.room.entities.Message;

public class DevelopmentViewModel extends ViewModel {

    private final DkqRepository repository;

    DevelopmentViewModel(DkqRepository repository) {
        this.repository = repository;
    }

    public void insertMessages( Message... messages ) {
        repository.insertMessages( messages );
    }

    public void deleteAllMessages() {
        repository.deleteAllMessages();
    }
}