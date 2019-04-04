package de.wackernagel.dkq.webservice;

import androidx.annotation.Nullable;

public class ApiResult<T> {

    public String status;
    public int code;
    public @Nullable String message;
    public @Nullable T result;

    public boolean isStatusOk() {
        return "ok".equals( status );
    }
}
