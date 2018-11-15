package de.wackernagel.dkq.viewmodels;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import de.wackernagel.dkq.DkqPreferences;
import de.wackernagel.dkq.repository.DkqRepository;
import de.wackernagel.dkq.room.entities.Quiz;
import de.wackernagel.dkq.room.entities.QuizListItem;
import de.wackernagel.dkq.webservice.Resource;
import de.wackernagel.dkq.workers.UpdateWorker;

public class QuizzesViewModel extends ViewModel {

    private final DkqRepository repository;

    QuizzesViewModel(DkqRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<List<QuizListItem>>> loadQuizzes() {
        return repository.loadQuizzes();
    }
}
