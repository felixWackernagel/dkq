package de.wackernagel.dkq.viewmodels;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import de.wackernagel.dkq.repository.DkqRepository;
import de.wackernagel.dkq.room.message.MessageListItem;
import de.wackernagel.dkq.webservice.Resource;

public class MessagesViewModel extends ViewModel {

    private final DkqRepository repository;

    @Inject
    MessagesViewModel(DkqRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<List<MessageListItem>>> loadMessages() {
        return repository.loadMessages();
    }
}
