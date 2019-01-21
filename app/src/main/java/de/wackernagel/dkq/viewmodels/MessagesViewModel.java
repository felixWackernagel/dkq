package de.wackernagel.dkq.viewmodels;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import de.wackernagel.dkq.repository.DkqRepository;
import de.wackernagel.dkq.room.entities.MessageListItem;
import de.wackernagel.dkq.webservice.Resource;

public class MessagesViewModel extends ViewModel {

    private final DkqRepository repository;

    MessagesViewModel(DkqRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<List<MessageListItem>>> loadMessages() {
        return repository.loadMessages();
    }
}
