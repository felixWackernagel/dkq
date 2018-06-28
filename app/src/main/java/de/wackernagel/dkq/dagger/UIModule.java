package de.wackernagel.dkq.dagger;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import de.wackernagel.dkq.ui.MainActivity;
import de.wackernagel.dkq.ui.QuizActivity;

@Module
public abstract class UIModule {

    @ContributesAndroidInjector(modules = FragmentBuildersModule.class)
    abstract MainActivity contributeMainActivityInjector();

    @ContributesAndroidInjector(modules = FragmentBuildersModule.class)
    abstract QuizActivity contributeQuizActivityInjector();

}
