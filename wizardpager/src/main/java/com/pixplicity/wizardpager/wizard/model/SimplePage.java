package com.pixplicity.wizardpager.wizard.model;

import java.util.ArrayList;

import com.pixplicity.wizardpager.wizard.ui.SimpleFragment;
import com.pixplicity.wizardpager.wizard.ui.WizardFragment;

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

    public String getBody() {
        return null;
    }

}
