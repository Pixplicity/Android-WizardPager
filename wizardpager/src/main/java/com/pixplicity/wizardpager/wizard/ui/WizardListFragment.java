package com.pixplicity.wizardpager.wizard.ui;

import java.util.List;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.pixplicity.wizardpager.R;

public abstract class WizardListFragment extends WizardFragment {

    protected ListView mListView;

    protected List<String> mChoices;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        mListView = (ListView) rootView.findViewById(android.R.id.list);
        mListView.setAdapter(getAdapter());
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> l, View view, int position, long id) {
                onListItemClick(l, view, position, id);
            }

        });

        return rootView;
    }

    public abstract ListAdapter getAdapter();

    public abstract void onListItemClick(AdapterView<?> l, View view, int position, long id);

}
