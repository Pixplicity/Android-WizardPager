package com.example.android.wizardpager.wizard.ui;

import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public abstract class WizardListFragment extends WizardFragment {

    protected ListView mListView;

    protected List<String> mChoices;

    @Override
    public View onCreateView(LayoutInflater _inflater, ViewGroup _container,
            Bundle _savedInstanceState) {
        View rootView = super.onCreateView(_inflater, _container, _savedInstanceState);

        mListView = (ListView) rootView.findViewById(android.R.id.list);
        mListView.setAdapter(new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_multiple_choice,
                android.R.id.text1,
                mChoices));
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> l, View view, int position, long id) {
                onListItemClick(l, view, position, id);
            }

        });

        return rootView;
    }

    public abstract void onListItemClick(AdapterView<?> l, View view, int position, long id);

}
