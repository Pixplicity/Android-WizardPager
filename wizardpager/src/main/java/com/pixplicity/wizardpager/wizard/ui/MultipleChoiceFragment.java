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

import android.os.Bundle;
import android.os.Handler;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.pixplicity.wizardpager.R;
import com.pixplicity.wizardpager.wizard.model.MultipleFixedChoicePage;
import com.pixplicity.wizardpager.wizard.model.Page;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MultipleChoiceFragment extends WizardListFragment {

    public static MultipleChoiceFragment create(String key) {
        Bundle args = new Bundle();
        args.putString(ARG_KEY, key);

        MultipleChoiceFragment fragment = new MultipleChoiceFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public MultipleChoiceFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MultipleFixedChoicePage fixedChoicePage = (MultipleFixedChoicePage) mPage;
        mChoices = new ArrayList<String>();
        for (int i = 0; i < fixedChoicePage.getOptionCount(); i++) {
            mChoices.add(fixedChoicePage.getOptionAt(i));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        ((TextView) rootView.findViewById(android.R.id.title)).setText(mPage.getTitle());

        // Pre-select currently selected items.
        new Handler().post(new Runnable() {

            @Override
            public void run() {
                MultipleFixedChoicePage fixedChoicePage = (MultipleFixedChoicePage) mPage;
                ArrayList<String> selectedItems = fixedChoicePage.getValues();
                if (selectedItems == null || selectedItems.size() == 0) {
                    return;
                }

                Set<String> selectedSet = new HashSet<String>(selectedItems);

                for (int i = 0; i < mChoices.size(); i++) {
                    if (selectedSet.contains(mChoices.get(i))) {
                        mListView.setItemChecked(i, true);
                    }
                }
            }
        });

        return rootView;
    }

    @Override
    public ArrayAdapter<String> getAdapter() {
        return new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_multiple_choice,
                android.R.id.text1,
                mChoices);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_page_list;
    }

    @Override
    public void onListItemClick(AdapterView<?> l, View v, int position, long id) {
        SparseBooleanArray checkedPositions = mListView.getCheckedItemPositions();
        ArrayList<String> selections = new ArrayList<String>();
        for (int i = 0; i < checkedPositions.size(); i++) {
            if (checkedPositions.valueAt(i)) {
                selections.add(
                        mListView.getAdapter().getItem(checkedPositions.keyAt(i)).toString());
            }
        }

        MultipleFixedChoicePage fixedChoicePage = (MultipleFixedChoicePage) mPage;
        fixedChoicePage.setValues(selections);
        mPage.notifyDataChanged(true);
    }

}
