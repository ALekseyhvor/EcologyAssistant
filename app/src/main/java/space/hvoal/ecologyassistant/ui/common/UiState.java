package space.hvoal.ecologyassistant.ui.common;

import androidx.annotation.Nullable;

public class UiState<T> {
    public enum Status { LOADING, SUCCESS, ERROR }

    public final Status status;
    @Nullable public final T data;
    @Nullable public final String error;

    private UiState(Status status, @Nullable T data, @Nullable String error) {
        this.status = status;
        this.data = data;
        this.error = error;
    }

    public static <T> UiState<T> loading() { return new UiState<>(Status.LOADING, null, null); }
    public static <T> UiState<T> success(T data) { return new UiState<>(Status.SUCCESS, data, null); }
    public static <T> UiState<T> error(String error) { return new UiState<>(Status.ERROR, null, error); }
}
