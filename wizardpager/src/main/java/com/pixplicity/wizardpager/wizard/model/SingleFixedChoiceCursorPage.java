/*
 * Copyright 2013 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pixplicity.wizardpager.wizard.model;

import android.database.Cursor;
import android.os.Bundle;

import com.pixplicity.wizardpager.wizard.ui.SingleChoiceCursorFragment;
import com.pixplicity.wizardpager.wizard.ui.WizardFragment;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A page offering the user a number of mutually exclusive choices from a {@link Cursor}.
 */
public abstract class SingleFixedChoiceCursorPage extends Page {

    protected ArrayList<String> mChoices = new ArrayList<String>();
    private Cursor mCursor;

    public SingleFixedChoiceCursorPage(ModelCallbacks callbacks, String title) {
        super(callbacks, title);
    }

    @Override
    public WizardFragment createFragment() {
        return SingleChoiceCursorFragment.create(getKey());
    }

    public String getOptionAt(int position) {
        return mChoices.get(position);
    }

    public int getOptionCount() {
        return mChoices.size();
    }

    @Override
    public boolean isCompleted() {
        return mData.containsKey(SIMPLE_DATA_KEY);
    }

    public SingleFixedChoiceCursorPage setChoices(String... choices) {
        mChoices.clear();
        mChoices.addAll(Arrays.asList(choices));
        return this;
    }

    @Override
    public void notifyDataChanged() {
//        SingleChoiceCursorFragment mFragment = getFragment();
        if (mFragment != null) mFragment.notifyDataChanged();
        super.notifyDataChanged();
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public void reset() {
        mData = new Bundle();
    }

    public void setCursor(Cursor cursor) {
        this.mCursor = cursor;
        reset();
        notifyDataChanged();
    }

    public abstract String getColumnNameValue();

    public abstract String getColumnNameId();

    public long getValue() {
        return mData.getLong(Page.SIMPLE_DATA_KEY);
    }

    public void setValue(long id) {
        mData.putLong(Page.SIMPLE_DATA_KEY, id);
    }

    public String toString() {
        String name = null;
        if (mCursor != null) {
            int position = mCursor.getPosition();
            mCursor.moveToPosition(-1);
            while (mCursor.moveToNext()) {
                if (mCursor.getLong(mCursor.getColumnIndex(getColumnNameId())) == getValue()) {
                    name = mCursor.getString(mCursor.getColumnIndex(getColumnNameValue()));
                    break;
                }
            }
            mCursor.moveToPosition(position);
        }
        return name;
    }

}
