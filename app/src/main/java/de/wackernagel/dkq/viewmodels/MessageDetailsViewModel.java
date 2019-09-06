package de.wackernagel.dkq.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import de.wackernagel.dkq.repository.DkqRepository;
import de.wackernagel.dkq.room.message.Message;
import de.wackernagel.dkq.room.entities.Quiz;
import de.wackernagel.dkq.webservice.Resource;

public class MessageDetailsViewModel extends ViewModel {

    private final DkqRepository repository;

    MessageDetailsViewModel(DkqRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<Message>> loadMessage( final long messageId, final int messageNumber ) {
        return repository.loadMessage( messageId, messageNumber );
    }

    public void updateMessage( final Message message ) {
        repository.updateMessage( message );
    }

    public LiveData<Quiz> loadQuiz( final long id ) {
        return repository.loadQuiz( id );
    }
}
