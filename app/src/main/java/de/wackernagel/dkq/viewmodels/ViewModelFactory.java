package de.wackernagel.dkq.viewmodels;

import android.app.Application;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import javax.inject.Singleton;

import de.wackernagel.dkq.repository.DkqRepository;

@Singleton
public class ViewModelFactory implements ViewModelProvider.Factory {

    private final DkqRepository repository;
    private final Application application;

    public ViewModelFactory(final DkqRepository repository, final Application application) {
        this.repository = repository;
        this.application = application;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if( modelClass.isAssignableFrom( QuizzesViewModel.class ) ) {
            return (T) new QuizzesViewModel( repository );
        } else if( modelClass.isAssignableFrom( QuestionsViewModel.class ) ) {
            return (T) new QuestionsViewModel( repository );
        } else if( modelClass.isAssignableFrom( MessagesViewModel.class ) ) {
            return (T) new MessagesViewModel( repository );
        } else if( modelClass.isAssignableFrom( DevelopmentViewModel.class ) ) {
            return (T) new DevelopmentViewModel( repository );
        } else if( modelClass.isAssignableFrom( MessageDetailsViewModel.class ) ) {
            return (T) new MessageDetailsViewModel( repository );
        } else if( modelClass.isAssignableFrom( MainViewModel.class ) ) {
            return (T) new MainViewModel( application, repository );
        } else if( modelClass.isAssignableFrom( QuizzersViewModel.class ) ) {
            return (T) new QuizzersViewModel( repository );
        }
        throw new IllegalArgumentException( "Unsupported ViewModel class." );
    }
}
