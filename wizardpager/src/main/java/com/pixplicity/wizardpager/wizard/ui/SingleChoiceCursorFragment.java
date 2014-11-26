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
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.pixplicity.wizardpager.R;
import com.pixplicity.wizardpager.wizard.model.Page;
import com.pixplicity.wizardpager.wizard.model.SingleFixedChoiceCursorPage;

import java.util.ArrayList;

public class SingleChoiceCursorFragment extends WizardListFragment {

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
                String selection = mPage.getData().getString(Page.SIMPLE_DATA_KEY);
                for (int i = 0; i < mChoices.size(); i++) {
                    if (mChoices.get(i).equals(selection)) {
                        mListView.setItemChecked(i, true);
                        break;
                    }
                }
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
    public ListAdapter getAdapter() {
        SingleFixedChoiceCursorPage fixedChoicePage = (SingleFixedChoiceCursorPage) mPage;
        Cursor cursor = fixedChoicePage.getCursor();
        if (cursor == null) return null;
        return new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_list_item_single_choice,
                cursor,
                new String[]{
                        fixedChoicePage.getColumnName()
                },
                new int[]{android.R.id.text1},
                android.support.v4.widget.CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
    }

    @Override
    public void onListItemClick(AdapterView<?> l, View view, int position, long id) {
        mPage.getData().putString(Page.SIMPLE_DATA_KEY,
                mListView.getAdapter().getItem(position).toString());
        mPage.notifyDataChanged();
    }

}
