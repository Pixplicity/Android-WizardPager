/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pixplicity.wizardpager.wizard.model;

import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;

import com.pixplicity.wizardpager.wizard.ui.SingleChoiceFragment;
import com.pixplicity.wizardpager.wizard.ui.WizardFragment;

/**
 * A page representing a branching point in the wizard. Depending on which choice is selected, the
 * next set of steps in the wizard may change.
 */
public class BranchPage extends SingleFixedChoicePage {

    private final List<Branch> mBranches = new ArrayList<Branch>();

    public BranchPage(ModelCallbacks callbacks, String title) {
        super(callbacks, title);
    }

    @Override
    public Page findByKey(String key) {
        if (getKey().equals(key)) {
            return this;
        }

        for (Branch branch : mBranches) {
            Page found = branch.childPageList.findByKey(key);
            if (found != null) {
                return found;
            }
        }

        return null;
    }

    @Override
    public void flattenCurrentPageSequence(ArrayList<Page> destination) {
        super.flattenCurrentPageSequence(destination);
        for (Branch branch : mBranches) {
            if (branch.choice.equals(mData.getString(Page.SIMPLE_DATA_KEY))) {
                branch.childPageList.flattenCurrentPageSequence(destination);
                break;
            }
        }
    }

    public BranchPage addBranch(String choice, Page... childPages) {
        PageList childPageList = new PageList(childPages);
        for (Page page : childPageList) {
            page.setParentKey(choice);
        }
        mBranches.add(new Branch(choice, childPageList));
        return this;
    }
    
    public BranchPage addBranch(String choice) {
        mBranches.add(new Branch(choice, new PageList()));
        return this;
    }

    public void setBranch(int index, String choice, Page... childPages) {
        Branch branch = mBranches.get(index);
        if (branch == null) {
            throw new IndexOutOfBoundsException("No branch specified for index " + index);
        }
        branch.choice = choice;
        PageList childPageList = new PageList(childPages);
        for (Page page : childPageList) {
            page.setParentKey(choice);
        }
        branch.childPageList = childPageList;
    }

    @Override
    public WizardFragment createFragment() {
        return SingleChoiceFragment.create(getKey());
    }

    @Override
    public String getOptionAt(int position) {
        return mBranches.get(position).choice;
    }

    @Override
    public int getOptionCount() {
        return mBranches.size();
    }

    @Override
    public boolean isCompleted() {
        return !TextUtils.isEmpty(mData.getString(SIMPLE_DATA_KEY));
    }

    @Override
    public void notifyDataChanged(boolean byUser) {
        mCallbacks.onPageTreeChanged();
        super.notifyDataChanged(byUser);
    }

    private static class Branch {
        public String choice;
        public PageList childPageList;

        private Branch(String choice, PageList childPageList) {
            this.choice = choice;
            this.childPageList = childPageList;
        }
    }
}
