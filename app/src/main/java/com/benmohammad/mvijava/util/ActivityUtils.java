package com.benmohammad.mvijava.util;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import static kotlin.jvm.internal.Intrinsics.checkNotNull;

public class ActivityUtils {

    public static void addFragmentToActivity(@NonNull FragmentManager fm,
                                             @NonNull Fragment fragment,
                                             int frameId) {
        checkNotNull(fm);
        checkNotNull(fragment);
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.add(frameId, fragment);
        transaction.commit();
    }
}
