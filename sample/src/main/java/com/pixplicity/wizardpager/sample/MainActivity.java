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

package com.pixplicity.wizardpager.sample;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.widget.Button;

import com.pixplicity.wizardpager.wizard.WizardActivity;
import com.pixplicity.wizardpager.wizard.model.AbstractWizardModel;
import com.pixplicity.wizardpager.wizard.ui.StepPagerStrip;

public class MainActivity extends WizardActivity {

	private ViewPager mPager;

	private StepPagerStrip mStepPagerStrip;

	private Button mNextButton;
	private Button mPrevButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mPager = (ViewPager) findViewById(R.id.pager);
		mStepPagerStrip = (StepPagerStrip) findViewById(R.id.strip);
		mNextButton = (Button) findViewById(R.id.next_button);
		mPrevButton = (Button) findViewById(R.id.prev_button);
		setControls(mPager, mStepPagerStrip, mNextButton, mPrevButton);
	}

	@Override
	public AbstractWizardModel onCreateModel() {
		return new SandwichWizardModel(this);
	}

    @Override
    protected void onPageShow(int position, boolean finalPage) {
        if (finalPage) {
            // Submit button for review step
            mNextButton.setBackgroundResource(R.drawable.finish_background);
        } else {
            mNextButton.setBackgroundResource(R.drawable.selectable_item_background);
        }
        super.onPageShow(position, finalPage);
    }

    @Override
	public void onSubmit() {
		DialogFragment dg = new DialogFragment() {

			@Override
			public Dialog onCreateDialog(Bundle savedInstanceState) {
				return new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.submit_confirm_title)
						.setMessage(R.string.submit_confirm_message)
						.setPositiveButton(R.string.submit_confirm_button, null)
						.setNegativeButton(android.R.string.cancel, null)
						.create();
			}
		};
		dg.show(getSupportFragmentManager(), "place_order_dialog");
	}

    @Override
    public boolean useBackForPrevious() {
        return true;
    }

}
