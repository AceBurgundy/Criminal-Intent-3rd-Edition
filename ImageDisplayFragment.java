package com.aceburgundy.criminalintent;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.aceburgundy.criminalintent.utilities.PictureUtilities;

public class ImageDisplayFragment extends DialogFragment {

    private static final String PHOTO_KEY = "photo";

    public static ImageDisplayFragment newInstance(String photoPath) {
        Bundle args = new Bundle();
        args.putString(PHOTO_KEY, photoPath);

        ImageDisplayFragment fragment = new ImageDisplayFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ImageView imageBox;
        assert getArguments() != null;
        String photoPath = getArguments().getString(PHOTO_KEY);

        View view = LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_image, null);

        imageBox = view.findViewById(R.id.photo_zoom);
        Bitmap bitmap = PictureUtilities.getScaledBitmap(photoPath, requireActivity());

        imageBox.setImageBitmap(bitmap);
        return new AlertDialog.Builder(requireActivity()).setView(view).create();
    }

}

