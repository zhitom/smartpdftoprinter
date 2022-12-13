package com.smartsnow.smartpdftoprinter.utils;

import java.util.concurrent.Callable;

public interface SafeCallable<V> extends Callable<V>,Exitable {
}
