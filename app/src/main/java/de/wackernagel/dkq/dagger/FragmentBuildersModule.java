package de.wackernagel.dkq.dagger;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import de.wackernagel.dkq.ui.MessagesListFragment;
import de.wackernagel.dkq.ui.QuestionsListFragment;
import de.wackernagel.dkq.ui.QuizzersListFragment;
import de.wackernagel.dkq.ui.QuizzesListFragment;

@Module
public abstract class FragmentBuildersModule {

    @ContributesAndroidInjector
    abstract QuizzesListFragment contributeQuizzesListFragment();

    @ContributesAndroidInjector
    abstract QuestionsListFragment contributeQuestionsListFragment();

    @ContributesAndroidInjector
    abstract MessagesListFragment contributeMessagesListFragment();

    @ContributesAndroidInjector
    abstract QuizzersListFragment contributeQuizzersListFragment();

}