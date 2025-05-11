package com.google.firebase.inappmessaging;


import androidx.annotation.RestrictTo;

import com.google.firebase.concurrent.UiExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
@RestrictTo(RestrictTo.Scope.TESTS)
public class TestOnlyExecutors {
    public static Executor ui() {
        return UiExecutor.INSTANCE;
    }

    public static ScheduledExecutorService background() {
        return ExecutorsRegistrar.BG_EXECUTOR.get();
    }

    public static ScheduledExecutorService blocking() {
        return ExecutorsRegistrar.BLOCKING_EXECUTOR.get();
    }

    public static ScheduledExecutorService lite() {
        return ExecutorsRegistrar.LITE_EXECUTOR.get();
    }
}
