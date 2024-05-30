package com.aceburgundy.criminalintent;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.Nullable;

public class Serializable {

    private Serializable() {
        // purposely left blank
    }

    @Nullable
    public static java.io.Serializable getSerializable(@Nullable Bundle bundle, @Nullable String key) {
        if (bundle != null) {
            return getSerializableFromBundle(bundle, key);
        }
        return null;
    }

    @Nullable
    public static java.io.Serializable getSerializable(@Nullable Intent intent, @Nullable String key) {
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                return getSerializableFromBundle(bundle, key);
            }
        }
        return null;
    }

    @SuppressWarnings({"deprecation"})
    private static java.io.Serializable getSerializableFromBundle(@Nullable Bundle bundle, @Nullable String key) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            assert bundle != null;
            return bundle.getSerializable(key);
        } else {
            try {
                assert bundle != null;
                return bundle.getSerializable(key);
            } catch (Exception error) {
                Log.e("SERIALIZABLE", "Failed to get serializable from bundle", error);
            }
        }
        return null;
    }
}
