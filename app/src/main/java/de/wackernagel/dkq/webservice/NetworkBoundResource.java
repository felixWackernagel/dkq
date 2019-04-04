package de.wackernagel.dkq.webservice;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import de.wackernagel.dkq.AppExecutors;

// ResultType: Type for the Resource data
// RequestType: Type for the API response
// https://github.com/googlesamples/android-architecture-components/tree/b1a194c1ae267258cd002e2e1c102df7180be473/GithubBrowserSample
public abstract class NetworkBoundResource<ResultType, RequestType> {

    // Called to save the result of the API response into the database
    @WorkerThread
    protected abstract void saveCallResult(@NonNull RequestType item);

    // Called with the data in the database to decide whether it should be fetched from the network.
    @MainThread
    protected abstract boolean shouldFetch(@Nullable ResultType data);

    // Called to get the cached data from the database
    @NonNull @MainThread
    protected abstract LiveData<ResultType> loadFromDb();

    // Called to create the API call.
    @NonNull @MainThread
    protected abstract LiveData<ApiResponse<ApiResult<RequestType>>> createCall();

    // Called when the fetch fails. The child class may want to reset components like rate limiter.
    @MainThread
    protected void onFetchFailed() {
    }

    // Called when the fetch was successful but api returns a error. The child class may want to handle the item error.
    /**
     * @param code Error code from the API
     * @return true to reload data from database otherwise use previous loaded data.
     */
    @WorkerThread
    protected boolean onApiError( int code ) {
        return false;
    }

    private final MediatorLiveData<Resource<ResultType>> result = new MediatorLiveData<>();

    private AppExecutors executors;

    @MainThread
    protected NetworkBoundResource(final AppExecutors executors) {
        this.executors = executors;
        result.setValue(Resource.loading(null));
        final LiveData<ResultType> dbSource = loadFromDb();
        result.addSource(dbSource, data -> {
            result.removeSource(dbSource);
            if (shouldFetch(data)) {
                fetchFromNetwork(dbSource);
            } else {
                result.addSource(dbSource, newData -> result.setValue(Resource.success(newData)));
            }
        });
    }

    private void fetchFromNetwork(final LiveData<ResultType> dbSource) {
        final LiveData<ApiResponse<ApiResult<RequestType>>> apiResponse = createCall();
        // we re-attach dbSource as a new source,
        // it will dispatch its latest value quickly
        result.addSource(dbSource, newData -> result.setValue(Resource.loading(newData)));
        result.addSource(apiResponse, response -> {
            result.removeSource(apiResponse);
            result.removeSource(dbSource);
            if (response.isSuccessful()) {
                if( response.body.isStatusOk() ) {
                    saveResultAndReInit(response.body);
                } else {
                    executors.diskIO().execute(() -> {
                        if( onApiError( response.body.code ) ) {
                            reInit(false );
                        } else {
                            executors.mainThread().execute(() -> result.addSource(dbSource, newData -> result.setValue( Resource.error(response.body.message, newData))));
                        }
                    });
                }
            } else {
                onFetchFailed();
                result.addSource(dbSource, newData -> result.setValue( Resource.error(response.errorMessage, newData)));
            }
        });
    }

    @MainThread
    private void saveResultAndReInit( final ApiResult<RequestType> response) {
        executors.diskIO().execute(() -> {
            saveCallResult(response.result);
            reInit( true );
        });
    }

    @WorkerThread
    private void reInit( final boolean success ) {
        executors.mainThread().execute(() -> {
            // we specially request a new live data,
            // otherwise we will get immediately last cached value,
            // which may not be updated with latest results received from network.
            result.addSource(loadFromDb(), newData -> {
                if( success )
                    result.setValue(Resource.success(newData));
                else
                    result.setValue(Resource.error( null, newData ));
            });
        });
    }

    public final LiveData<Resource<ResultType>> getAsLiveData() {
        return result;
    }
}