package de.wackernagel.dkq.viewmodels;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.wackernagel.dkq.repository.DkqRepository;

@Singleton
public class ViewModelFactory implements ViewModelProvider.Factory {

    private final DkqRepository repository;

    @Inject
    public ViewModelFactory(DkqRepository repository ) {
        this.repository = repository;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if( modelClass.isAssignableFrom( QuizzesViewModel.class ) ) {
            return (T) new QuizzesViewModel( repository );
        } else if( modelClass.isAssignableFrom( QuestionsViewModel.class ) ) {
            return (T) new QuestionsViewModel( repository );
        }
        throw new IllegalArgumentException( "Unsupported ViewModel class." );
    }
}
