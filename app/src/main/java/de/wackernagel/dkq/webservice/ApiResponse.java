package de.wackernagel.dkq.webservice;

import java.io.IOException;

import androidx.annotation.Nullable;
import de.wackernagel.dkq.DkqLog;
import retrofit2.Response;

public class ApiResponse<T> {

    public final int code;

    @Nullable
    final T body;

    @Nullable
    final String errorMessage;

    ApiResponse(Throwable error) {
        code = 500;
        body = null;
        errorMessage = error.getMessage();
    }

    ApiResponse(Response<T> response) {
        code = response.code();
        if(response.isSuccessful()) {
            body = response.body();
            errorMessage = null;
        } else {
            String message = null;
            if (response.errorBody() != null) {
                try {
                    message = response.errorBody().string();
                } catch ( IOException exception ) {
                    DkqLog.e("ApiResponse", "error while parsing response", exception);
                }
            }
            if (message == null || message.trim().length() == 0) {
                message = response.message();
            }
            errorMessage = message;
            body = null;
        }
    }

    boolean isSuccessful() {
        return code >= 200 && code < 300;
    }
}
