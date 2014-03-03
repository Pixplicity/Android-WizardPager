package com.example.android.wizardpager.wizard;

import java.util.List;

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

import com.example.android.wizardpager.R;
import com.example.android.wizardpager.wizard.model.AbstractWizardModel;
import com.example.android.wizardpager.wizard.model.ModelCallbacks;
import com.example.android.wizardpager.wizard.model.Page;
import com.example.android.wizardpager.wizard.ui.PageFragmentCallbacks;
import com.example.android.wizardpager.wizard.ui.ReviewFragment;
import com.example.android.wizardpager.wizard.ui.StepPagerStrip;

public abstract class WizardActivity extends FragmentActivity implements
		PageFragmentCallbacks,
		ReviewFragment.Callbacks,
		ModelCallbacks {

	private ViewPager mPager;
	private MyPagerAdapter mPagerAdapter;

	private boolean mEditingAfterReview;

	private AbstractWizardModel mWizardModel;

	private boolean mConsumePageSelectedEvent;

	private Button mNextButton;
	private Button mPrevButton;

	private List<Page> mCurrentPageSequence;
	private StepPagerStrip mStepPagerStrip;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mWizardModel = onCreateModel();

		if (savedInstanceState != null) {
			mWizardModel.load(savedInstanceState.getBundle("model"));
		}

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

	protected void setControls(ViewPager pager, StepPagerStrip stepPagerStrip, Button nextButton,
			Button prevButton) {
		mPager = pager;
		mStepPagerStrip = stepPagerStrip;
		mNextButton = nextButton;
		mPrevButton = prevButton;
		if (mPager == null) {
			throw new IllegalStateException("A ViewPager must be provided");
		}
		mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
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
					if (mPager.getCurrentItem() == mCurrentPageSequence.size()) {
						onSubmit();
					} else {
						if (mEditingAfterReview) {
							mPager.setCurrentItem(mPagerAdapter.getCount() - 1);
						} else {
							mPager.setCurrentItem(mPager.getCurrentItem() + 1);
						}
					}
				}
			});
		}
		if (mPrevButton != null) {
			mPrevButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View view) {
                    goToPrevious();
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
		mStepPagerStrip.setPageCount(mCurrentPageSequence.size() + 1); // + 1 = review step
		mPagerAdapter.notifyDataSetChanged();
		updateControls();
	}

	private void updateControls() {
		int position = mPager.getCurrentItem();
		if (position == mCurrentPageSequence.size()) {
			// Submit button for review step
			mNextButton.setText(R.string.finish);
			mNextButton.setBackgroundResource(R.drawable.finish_background);
			mNextButton.setTextAppearance(this, R.style.TextAppearanceFinish);
		} else {
			// Next button for any other step
			mNextButton.setText(mEditingAfterReview
					? R.string.review
					: R.string.next);
			mNextButton.setBackgroundResource(R.drawable.selectable_item_background);
			TypedValue v = new TypedValue();
			getTheme().resolveAttribute(android.R.attr.textAppearanceMedium, v, true);
			mNextButton.setTextAppearance(this, v.resourceId);
			mNextButton.setEnabled(position != mPagerAdapter.getCutOffPage());
		}
		// Always allow navigating to previous steps unless we're at the first one
		mPrevButton.setVisibility(position <= 0 ? View.INVISIBLE : View.VISIBLE);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBundle("model", mWizardModel.save());
	}

    @Override
    public void onBackPressed() {
        if (!useBackForPrevious() || !goToPrevious()) {
            super.onBackPressed();
        }
    }

    private boolean goToPrevious() {
        if (mPager.getCurrentItem() > 0) {
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
            return true;
        }
        return false;
    }

    public abstract AbstractWizardModel onCreateModel();

	public abstract void onSubmit();

    public abstract boolean useBackForPrevious();

	@Override
	public AbstractWizardModel onGetModel() {
		return mWizardModel;
	}

	@Override
	public void onEditScreenAfterReview(String key) {
		for (int i = mCurrentPageSequence.size() - 1; i >= 0; i--) {
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
	public void onPageDataChanged(Page page) {
		if (page.isRequired()) {
			if (recalculateCutOffPage()) {
				mPagerAdapter.notifyDataSetChanged();
				updateControls();
			}
		}
	}

	@Override
	public Page onGetPage(String key) {
		return mWizardModel.findByKey(key);
	}

	private boolean recalculateCutOffPage() {
		// Cut off the pager adapter at first required page that isn't completed
		int cutOffPage = mCurrentPageSequence.size() + 1;
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

	public class MyPagerAdapter extends FragmentStatePagerAdapter {

		private int mCutOffPage;
		private Fragment mPrimaryItem;

		public MyPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			if (i >= mCurrentPageSequence.size()) {
				return new ReviewFragment();
			}

			return mCurrentPageSequence.get(i).createFragment();
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
			return Math.min(mCutOffPage + 1, mCurrentPageSequence.size() + 1);
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
