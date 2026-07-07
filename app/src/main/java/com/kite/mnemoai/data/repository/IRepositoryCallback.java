package com.kite.mnemoai.data.repository;

public interface IRepositoryCallback<T> {
    void onComplete(T t);
    void onError(Throwable t);
}
