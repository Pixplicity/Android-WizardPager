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
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.pixplicity.wizardpager.R;
import com.pixplicity.wizardpager.wizard.model.SingleFixedChoicePage;

import java.util.ArrayList;

public class SingleChoiceFragment extends WizardListFragment {

    public static SingleChoiceFragment create(String key) {
        Bundle args = new Bundle();
        args.putString(ARG_KEY, key);

        SingleChoiceFragment fragment = new SingleChoiceFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public SingleChoiceFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SingleFixedChoicePage fixedChoicePage = (SingleFixedChoicePage) mPage;
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
                SingleFixedChoicePage fixedChoicePage = (SingleFixedChoicePage) mPage;
                String selection = fixedChoicePage.getValue();
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
    public ArrayAdapter<String> getAdapter() {
        return new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_single_choice,
                android.R.id.text1,
                mChoices);
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
    public void onListItemClick(AdapterView<?> l, View view, int position, long id) {
        SingleFixedChoicePage fixedChoicePage = (SingleFixedChoicePage) mPage;
        fixedChoicePage.setValue(mListView.getAdapter().getItem(position).toString());
        mPage.notifyDataChanged(true);
    }

}
