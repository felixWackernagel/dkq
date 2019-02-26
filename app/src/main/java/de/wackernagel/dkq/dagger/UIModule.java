package de.wackernagel.dkq.dagger;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import de.wackernagel.dkq.ui.DevelopmentActivity;
import de.wackernagel.dkq.ui.MainActivity;
import de.wackernagel.dkq.ui.MessageDetailsActivity;
import de.wackernagel.dkq.ui.QuizDetailsActivity;

@Module
public abstract class UIModule {

    @ContributesAndroidInjector(modules = FragmentBuildersModule.class)
    abstract MainActivity contributeMainActivityInjector();

    @ContributesAndroidInjector(modules = FragmentBuildersModule.class)
    abstract QuizDetailsActivity contributeQuizActivityInjector();

    @ContributesAndroidInjector()
    abstract MessageDetailsActivity contributeMessageDetailsActivityInjector();

    @ContributesAndroidInjector()
    abstract DevelopmentActivity contributeDevelopmentActivityInjector();

}
