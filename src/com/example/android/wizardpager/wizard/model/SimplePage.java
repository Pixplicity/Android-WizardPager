package com.example.android.wizardpager.wizard.model;

import java.util.ArrayList;

import com.example.android.wizardpager.wizard.ui.SimpleFragment;
import com.example.android.wizardpager.wizard.ui.WizardFragment;

public class SimplePage extends Page {

    protected SimplePage(ModelCallbacks callbacks, String title) {
        super(callbacks, title);
    }

    @Override
    public WizardFragment createFragment() {
        return SimpleFragment.create(getKey());
    }

    @Override
    public void getReviewItems(ArrayList<ReviewItem> dest) {
        // Nothing to add
    }

    public int getImageResId() {
        return 0;
    }

    public int getStringResId() {
        return 0;
    }

}
