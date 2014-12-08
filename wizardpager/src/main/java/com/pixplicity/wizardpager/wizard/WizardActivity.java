package com.pixplicity.wizardpager.wizard;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.pixplicity.wizardpager.R;
import com.pixplicity.wizardpager.wizard.model.AbstractWizardModel;
import com.pixplicity.wizardpager.wizard.model.ModelCallbacks;
import com.pixplicity.wizardpager.wizard.model.Page;
import com.pixplicity.wizardpager.wizard.ui.PageFragmentCallbacks;
import com.pixplicity.wizardpager.wizard.ui.ReviewFragment;
import com.pixplicity.wizardpager.wizard.ui.StepPagerStrip;
import com.pixplicity.wizardpager.wizard.ui.WizardFragment;

import java.util.List;

public abstract class WizardActivity extends FragmentActivity implements
        PageFragmentCallbacks,
        ReviewFragment.Callbacks,
        ModelCallbacks {

    protected ViewPager mPager;
    protected WizardPagerAdapter mPagerAdapter;
    protected Button mNextButton;
    protected Button mSubmitButton;
    protected Button mPrevButton;
    protected StepPagerStrip mStepPagerStrip;

    private boolean mEditingAfterReview;

    private AbstractWizardModel mWizardModel;

    private boolean mConsumePageSelectedEvent;

    private List<Page> mCurrentPageSequence;
    private boolean mMaySubmit = true;

    public List<Page> getCurrentPageSequence() {
        return mCurrentPageSequence;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mWizardModel = onCreateModel();
        super.onCreate(savedInstanceState);

        mWizardModel.registerListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPager == null) {
            throw new IllegalStateException(
                    "setControls() must be called before Activity resumes for the first time; did you forget to call it in onCreate()?");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWizardModel.unregisterListener(this);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mWizardModel.load(savedInstanceState.getBundle("model"));
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBundle("model", mWizardModel.save());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (!useBackForPrevious() || !onNavigatePrevious()) {
            super.onBackPressed();
        }
    }

    protected void setControls(ViewPager pager, StepPagerStrip stepPagerStrip, Button nextButton,
                               Button prevButton, Button submitButton) {
        mPager = pager;
        mStepPagerStrip = stepPagerStrip;
        mNextButton = nextButton;
        mSubmitButton = submitButton;
        mPrevButton = prevButton;
        if (mPager == null) {
            throw new IllegalStateException("A ViewPager must be provided");
        }
        mPagerAdapter = new WizardPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                mStepPagerStrip.setCurrentPage(position);

                if (mConsumePageSelectedEvent) {
                    mConsumePageSelectedEvent = false;
                    return;
                }

                mEditingAfterReview = false;
                updateControls();
            }
        });
        if (mStepPagerStrip != null) {
            mStepPagerStrip.setHasReview(mWizardModel.hasReviewPage());
            mStepPagerStrip.setOnPageSelectedListener(new StepPagerStrip.OnPageSelectedListener() {

                @Override
                public void onPageStripSelected(int position) {
                    position = Math.min(mPagerAdapter.getCount() - 1, position);
                    if (mPager.getCurrentItem() != position) {
                        mPager.setCurrentItem(position);
                    }
                }
            });
        }
        if (mNextButton != null) {
            mNextButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    if (isFinalPage(mPager.getCurrentItem())) {
                        onSubmit();
                    } else {
                        onNavigateNext(mEditingAfterReview);
                    }
                }
            });
        }

        if(mSubmitButton != null) {
            mSubmitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isFinalPage(mPager.getCurrentItem())) {
                        onSubmit();
                    }
                }
            });
        }
        if (mPrevButton != null) {
            mPrevButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    onNavigatePrevious();
                }
            });
        }

        onPageTreeChanged();
        updateControls();
    }

    @Override
    public void onPageTreeChanged() {
        mCurrentPageSequence = mWizardModel.getCurrentPageSequence();
        recalculateCutOffPage();
        mStepPagerStrip.setPageCount(mCurrentPageSequence.size()
                + (mWizardModel.hasReviewPage() ? 1 : 0)); // + 1 = review step
        mPagerAdapter.notifyDataSetChanged();
        updateControls();
    }

    private void updateControls() {
        int position = mPager.getCurrentItem();
        if (isFinalPage(position)) {
            onPageShow(position, true);
        } else {
            onPageShow(position, false);
        }
        // Always allow navigating to previous steps unless we're at the first one
        mPrevButton.setVisibility(position <= 0 ? View.INVISIBLE : View.VISIBLE);
    }

    private boolean isFinalPage(int position) {
        return position == mCurrentPageSequence.size() - (mWizardModel.hasReviewPage() ? 0 : 1);
    }

    private void updateControlSubmit() {
        if (isFinalPage(mPager.getCurrentItem())) {
            mNextButton.setEnabled(mMaySubmit);
        }
    }

    protected void onPageShow(int position, boolean finalPage) {
        if (finalPage) {
            // Submit button for review step

            mNextButton.setVisibility(View.GONE);
            mSubmitButton.setVisibility(View.VISIBLE);

            /*
            mNextButton.setText(R.string.wizard_finish);
            mNextButton.setTextAppearance(this, R.style.TextAppearanceFinish);
            mNextButton.setBackgroundColor(getResources().getColor(R.color.submit_background));
            */
        } else {

            mNextButton.setVisibility(View.VISIBLE);
            mSubmitButton.setVisibility(View.GONE);

            // Next button for any other step
            mNextButton.setText(mEditingAfterReview
                    ? R.string.wizard_review
                    : R.string.wizard_next);
            TypedValue v = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.textAppearanceMedium, v, true);
            mNextButton.setTextAppearance(this, v.resourceId);
            mNextButton.setEnabled(position != mPagerAdapter.getCutOffPage());
            mNextButton.setBackgroundColor(0);
        }
        updateControlSubmit();
    }

    protected boolean onNavigatePrevious() {
        if (mPager.getCurrentItem() > 0) {
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
            return true;
        }
        return false;
    }

    protected boolean onNavigateNext(boolean needsReview) {
        if (needsReview) {
            mPager.setCurrentItem(mPagerAdapter.getCount() - 1);
        } else {
            mPager.setCurrentItem(mPager.getCurrentItem() + 1);
        }
        return true;
    }

    protected boolean onNavigateNext() {
        if (isFinalPage(mPager.getCurrentItem())) {
            onSubmit();
        } else {
            onNavigateNext(mEditingAfterReview);
        }
        return true;
    }

    public abstract AbstractWizardModel onCreateModel();

    public void setMaySubmit(boolean maySubmit) {
mMaySubmit = maySubmit;
    }

    public abstract void onSubmit();

    public abstract boolean useBackForPrevious();

    @Override
    public AbstractWizardModel onGetModel() {
        return mWizardModel;
    }

    @Override
    public void onEditScreenAfterReview(String key) {
        for (int i = mCurrentPageSequence.size() - (mWizardModel.hasReviewPage() ? 1 : 0); i >= 0; i--) {
            if (mCurrentPageSequence.get(i).getKey().equals(key)) {
                mConsumePageSelectedEvent = true;
                mEditingAfterReview = true;
                mPager.setCurrentItem(i);
                updateControls();
                break;
            }
        }
    }

    @Override
    public void onPageDataChanged(Page page, boolean byUser) {
        if (page.isRequired()) {
            if (recalculateCutOffPage()) {
                mPagerAdapter.notifyDataSetChanged();
                updateControls();
                return;
            }
        }
        updateControlSubmit();
    }

    @Override
    public Page onGetPage(String key) {
        return mWizardModel.findByKey(key);
    }

    private boolean recalculateCutOffPage() {
        // Cut off the pager adapter at first required page that isn't completed
        int cutOffPage = mCurrentPageSequence.size() + (mWizardModel.hasReviewPage() ? 1 : 0);
        for (int i = 0; i < mCurrentPageSequence.size(); i++) {
            Page page = mCurrentPageSequence.get(i);
            if (page.isRequired() && !page.isCompleted()) {
                cutOffPage = i;
                break;
            }
        }

        if (mPagerAdapter.getCutOffPage() != cutOffPage) {
            mPagerAdapter.setCutOffPage(cutOffPage);
            return true;
        }

        return false;
    }

    public class WizardPagerAdapter extends FragmentStatePagerAdapter {

        private int mCutOffPage;
        private Fragment mPrimaryItem;

        public WizardPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            if (fragment instanceof WizardFragment) {
                mCurrentPageSequence.get(position).setFragment((WizardFragment) fragment);
            }
            return fragment;
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment;
            if (i >= mCurrentPageSequence.size() && mWizardModel.hasReviewPage()) {
                fragment = mWizardModel.getReviewFragment();
            } else {
                fragment = mCurrentPageSequence.get(i).createFragment();
                mCurrentPageSequence.get(i).setFragment((WizardFragment) fragment);
            }
            return fragment;
        }

        @Override
        public int getItemPosition(Object object) {
            // TODO: be smarter about this
            if (object == mPrimaryItem) {
                // Re-use the current fragment (its position never changes)
                return POSITION_UNCHANGED;
            }
            return POSITION_NONE;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            mPrimaryItem = (Fragment) object;
        }

        @Override
        public int getCount() {
            if (mCurrentPageSequence == null) {
                return 0;
            }
            return Math.min(mCutOffPage + (mWizardModel.hasReviewPage() ? 1 : 0),
                    mCurrentPageSequence.size() + (mWizardModel.hasReviewPage() ? 1 : 0));
        }

        public void setCutOffPage(int cutOffPage) {
            if (cutOffPage < 0) {
                cutOffPage = Integer.MAX_VALUE;
            }
            mCutOffPage = cutOffPage;
        }

        public int getCutOffPage() {
            return mCutOffPage;
        }

    }

}
