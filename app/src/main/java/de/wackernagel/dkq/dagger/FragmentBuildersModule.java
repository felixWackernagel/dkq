package de.wackernagel.dkq.dagger;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import de.wackernagel.dkq.ui.QuestionsListFragment;
import de.wackernagel.dkq.ui.QuizzesListFragment;

@Module
public abstract class FragmentBuildersModule {

    @ContributesAndroidInjector
    abstract QuizzesListFragment contributeQuizzesListFragment();

    @ContributesAndroidInjector
    abstract QuestionsListFragment contributeQuestionsListFragment();

}