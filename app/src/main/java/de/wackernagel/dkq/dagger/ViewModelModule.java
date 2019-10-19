package de.wackernagel.dkq.dagger;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import de.wackernagel.dkq.viewmodels.DevelopmentViewModel;
import de.wackernagel.dkq.viewmodels.MainViewModel;
import de.wackernagel.dkq.viewmodels.MessageDetailsViewModel;
import de.wackernagel.dkq.viewmodels.MessagesViewModel;
import de.wackernagel.dkq.viewmodels.QuestionsViewModel;
import de.wackernagel.dkq.viewmodels.QuizzersViewModel;
import de.wackernagel.dkq.viewmodels.QuizzesViewModel;

@SuppressWarnings("unused")
@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(QuizzesViewModel.class)
    abstract ViewModel bindQuizzesViewModel( QuizzesViewModel viewModel );

    @Binds
    @IntoMap
    @ViewModelKey(QuestionsViewModel.class)
    abstract ViewModel bindQuestionsViewModel( QuestionsViewModel viewModel );

    @Binds
    @IntoMap
    @ViewModelKey(MessagesViewModel.class)
    abstract ViewModel bindMessagesViewModel( MessagesViewModel viewModel );

    @Binds
    @IntoMap
    @ViewModelKey(DevelopmentViewModel.class)
    abstract ViewModel bindDevelopmentViewModel( DevelopmentViewModel viewModel );

    @Binds
    @IntoMap
    @ViewModelKey(MessageDetailsViewModel.class)
    abstract ViewModel bindMessageDetailsViewModel( MessageDetailsViewModel viewModel );

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel.class)
    abstract ViewModel bindMainViewModel( MainViewModel viewModel );

    @Binds
    @IntoMap
    @ViewModelKey(QuizzersViewModel.class)
    abstract ViewModel bindQuizzersViewModel( QuizzersViewModel viewModel );

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory( ViewModelFactory factory );

}
