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

package com.pixplicity.wizardpager.wizard.ui;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.pixplicity.wizardpager.R;
import com.pixplicity.wizardpager.wizard.model.SingleFixedChoiceCursorPage;

import java.util.ArrayList;

public class SingleChoiceCursorFragment extends WizardListFragment {

    private SingleChoiceCursorAdapter mSingleChoiceCursorAdapter;

    public static SingleChoiceCursorFragment create(String key) {
        Bundle args = new Bundle();
        args.putString(ARG_KEY, key);

        SingleChoiceCursorFragment fragment = new SingleChoiceCursorFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public SingleChoiceCursorFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SingleFixedChoiceCursorPage fixedChoicePage = (SingleFixedChoiceCursorPage) mPage;
        mChoices = new ArrayList<String>();
        for (int i = 0; i < fixedChoicePage.getOptionCount(); i++) {
            mChoices.add(fixedChoicePage.getOptionAt(i));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        ((TextView) rootView.findViewById(android.R.id.title)).setText(mPage.getTitle());

        // Pre-select currently selected item.
        new Handler().post(new Runnable() {

            @Override
            public void run() {
                SingleFixedChoiceCursorPage fixedChoicePage = (SingleFixedChoiceCursorPage) mPage;
                long selectionId = fixedChoicePage.getValue();
                // TODO show selection
                // FIXME write custom implementation of SimpleCursorLoader below
            }
        });

        return rootView;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_page_list;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof PageFragmentCallbacks)) {
            throw new ClassCastException("Activity must implement PageFragmentCallbacks");
        }

        mCallbacks = (PageFragmentCallbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void notifyDataChanged() {
        super.notifyDataChanged();
        if (mSingleChoiceCursorAdapter != null) {
            final SingleFixedChoiceCursorPage fixedChoicePage = (SingleFixedChoiceCursorPage) mPage;
            mSingleChoiceCursorAdapter.setCurrentSelectionId(fixedChoicePage.getValue());
        }
    }

    @Override
    public ListAdapter getAdapter() {
        final SingleFixedChoiceCursorPage fixedChoicePage = (SingleFixedChoiceCursorPage) mPage;
        Cursor cursor = fixedChoicePage.getCursor();
        if (cursor == null) {
            mSingleChoiceCursorAdapter = null;
        } else if (mSingleChoiceCursorAdapter == null) {
            mSingleChoiceCursorAdapter = new SingleChoiceCursorAdapter(getActivity(),
                    cursor,
                    fixedChoicePage.getColumnNameId(),
                    fixedChoicePage.getColumnNameValue(),
                    fixedChoicePage.getValue(),
                    CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        }
        return mSingleChoiceCursorAdapter;
    }

    @Override
    public void onListItemClick(AdapterView<?> l, View view, int position, long id) {
        SingleFixedChoiceCursorPage fixedChoicePage = (SingleFixedChoiceCursorPage) mPage;
        fixedChoicePage.setValue(id);

        mPage.notifyDataChanged();
    }

    private class SingleChoiceCursorAdapter extends SimpleCursorAdapter {
        private final String mColumnNameId, mColumnNameValue;
        private long mCurrentSelectionId;

        public SingleChoiceCursorAdapter(Context context, Cursor c, String columnNameId, String columnNameValue, long currentSelectionId, int flags) {
            super(context, android.R.layout.simple_list_item_single_choice, c, new String[]{
                    columnNameValue
            }, new int[]{android.R.id.text1}, flags);
            mColumnNameId = columnNameId;
            mColumnNameValue = columnNameValue;
            mCurrentSelectionId = currentSelectionId;
        }

        public void setCurrentSelectionId(long currentSelectionId) {
            mCurrentSelectionId = currentSelectionId;
        }

        @Override
        public long getItemId(int position) {
            mCursor.moveToPosition(position);
            return mCursor.getLong(mCursor.getColumnIndex(mColumnNameId));
        }

        public String getItemValue(int position) {
            mCursor.moveToPosition(position);
            return mCursor.getString(mCursor.getColumnIndex(mColumnNameValue));
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            super.bindView(view, context, cursor);
            int i = cursor.getPosition();
            long id = getItemId(i);
            mListView.setItemChecked(i, mCurrentSelectionId == id);
        }

    }
}
