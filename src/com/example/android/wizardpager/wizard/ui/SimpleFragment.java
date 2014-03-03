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

package com.example.android.wizardpager.wizard.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.wizardpager.R;
import com.example.android.wizardpager.wizard.model.SimplePage;

public class SimpleFragment extends WizardFragment {

    public static SimpleFragment create(String key) {
        Bundle args = new Bundle();
        args.putString(ARG_KEY, key);

        SimpleFragment fragment = new SimpleFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public SimpleFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_page, container, false);
        ((TextView) rootView.findViewById(android.R.id.title)).setText(mPage.getTitle());
        if (mPage instanceof SimplePage) {
            int imageResId = ((SimplePage) mPage).getImageResId();
            if (imageResId != 0) {
                ((ImageView) rootView.findViewById(R.id.image)).setImageResource(imageResId);
            }
            int stringResId = ((SimplePage) mPage).getStringResId();
            if (stringResId != 0) {
                ((TextView) rootView.findViewById(R.id.text)).setText(stringResId);
            }
        }
        return rootView;
    }

}
