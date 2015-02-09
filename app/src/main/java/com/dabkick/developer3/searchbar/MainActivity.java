package com.dabkick.developer3.searchbar;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    ListView listView;
    CustomSearchViewHandler searchBarHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //The search bar
        View searchBar = (View) findViewById(R.id.searchBar);
        searchBarHandler = new CustomSearchViewHandler(this, searchBar);
        setupSearchBar();

        //The table
        listView = (ListView) findViewById(R.id.listView);
        setupListView();

    }

    private void setupListView() {
        ArrayList<UserInfo> arrayList = new ArrayList<UserInfo>();

        for (String s : Samples.sampleStrings) {
            UserInfo user = new UserInfo();
            user.name = s;
            arrayList.add(user);
        }

        CustomAdapter mAdapter = new CustomAdapter(this,
                R.layout.custom_row, arrayList);
        listView.setAdapter(mAdapter);
        listView.setTextFilterEnabled(false);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("test", String.valueOf(position));
            }
        });

    }

    private void setupSearchBar() {

        searchBarHandler.setOnQueryTextListener(new CustomSearchViewHandler.OnQueryTextListener() {
            @Override
            public void onQueryTextChanged(CharSequence s, int start, int before, int count) {

            }

            //This function get called when the query text become empty
            @Override
            public void onQueryTextEmpty() {
                //Reset all the data
                ArrayAdapter<String> mAdapter = (ArrayAdapter<String>) listView.getAdapter();
                mAdapter.getFilter().filter("");
            }

            //This function get called when the search button pressed
            @Override
            public void onQuerySubmit(String queryText) {
                //Search data
                ArrayAdapter<String> mAdapter = (ArrayAdapter<String>) listView.getAdapter();
                mAdapter.getFilter().filter(queryText);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

class Samples {

    public static final String[] sampleStrings = {
            "hello-world", "Cats and dogs", "Dabkick", "America", "books",
            "Searchable", "magazine", "books2", "hello_world"};
}

class UserInfo {
    String name;
}

class CustomAdapter extends ArrayAdapter<UserInfo> {

    LayoutInflater mInflater;
    int mResourceID;
    List<UserInfo> mItems;
    List<UserInfo> mFullItems;
    Filter mFilter;

    public CustomAdapter(Context context, int resource, List<UserInfo> items) {
        super(context, resource, items);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mResourceID = resource;
        mItems = items;
        mFullItems = new ArrayList<>(mItems);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            v = mInflater.inflate(mResourceID, null);
            v.setMinimumHeight(250);
        }

        UserInfo p = getItem(position);

        if (p != null) {
            //assign values to row

            TextView textView = (TextView) v.findViewById(R.id.contentText);

            String s = mItems.get(position).name;
            textView.setText(s);
        }

        return v;

    }

    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new CustomFilter();
        }
        return mFilter;
    }

    private class CustomFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults result = new FilterResults();

            mItems.clear();
            mItems.addAll(mFullItems);

            if (constraint != null && constraint.toString().length() > 0) {
                constraint = constraint.toString().toLowerCase();

                ArrayList<UserInfo> filteredItems = new ArrayList<UserInfo>();

                //Here is where we do the compare
                for (UserInfo user : mItems) {
                    if (user.name.toLowerCase().contains(constraint))
                        filteredItems.add(user);
                }

                result.count = filteredItems.size();
                result.values = filteredItems;
            } else {
                result.count = mItems.size();
                result.values = mItems;
            }

            return result;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            ArrayList<UserInfo> result = (ArrayList<UserInfo>) results.values;
            notifyDataSetChanged();
            ArrayList<UserInfo> tmp = new ArrayList<UserInfo>(result);
            mItems.clear();
            mItems.addAll(tmp);
            notifyDataSetInvalidated();
        }
    }

}

class CustomSearchViewHandler{


    Activity mActivity;
    View mCustomSearchView;
    RelativeLayout mQueryHintLayout;
    RelativeLayout mQueryHintContent;
    LinearLayout mInnerRoundedRect;
    RelativeLayout mEditContentLayout;
    TextView mQueryHintText;
    ListenerEditText mQueryText;
    EditText mQueryTextDummy;
    ImageView mCloseBtn;
    ImageView mEditSearchIcon;
    ImageView mHintSearchIcon;
    AnimatorSet mDismissHintAnim;
    ImageView mMaskView;

    //Listener
    OnQueryTextListener mOnQueryTextListener;
    OnCloseListener mOnCloseListener;

    boolean isSetup;
    float hintX;
    float editX;

    CustomSearchViewHandler(Activity activity, View customSearchView) {

        mActivity = activity;
        mCustomSearchView = customSearchView;
        mQueryHintLayout = (RelativeLayout) mActivity.findViewById(R.id.queryHintLayout);
        mQueryHintContent = (RelativeLayout) mActivity.findViewById(R.id.queryHintContentLayout);
        mInnerRoundedRect = (LinearLayout) mActivity.findViewById(R.id.innerRoundedRect);
        mCloseBtn = (ImageView) mActivity.findViewById(R.id.searchCloseBtn);
        mEditContentLayout = (RelativeLayout) mActivity.findViewById(R.id.editContentLayout);
        mQueryHintText = (TextView) mActivity.findViewById(R.id.queryHintTextView);
        mEditSearchIcon = (ImageView) mActivity.findViewById(R.id.editSearchIcon);
        mHintSearchIcon = (ImageView) mActivity.findViewById(R.id.hintSearchIcon);
        mQueryTextDummy = (EditText) mActivity.findViewById(R.id.searchEditTextDummy);
        mMaskView = (ImageView) mActivity.findViewById(R.id.maskView);

        //replace edit text
        EditText tmp = (EditText) mActivity.findViewById(R.id.searchEditText);
        mQueryText = new ListenerEditText(mActivity,null,tmp);
        RelativeLayout editTextLayout = (RelativeLayout)mActivity.findViewById(R.id.editTextLayout);
        editTextLayout.removeView(tmp);
        editTextLayout.addView(mQueryText);

        isSetup = false;

        //get x coordinate after xml is layout
        mActivity.getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //prevent listener get called multiple times
                if (!isSetup) {
                    isSetup = true;

                    //get edit view global x
                    int[] location = new int[2];
                    mEditSearchIcon.getLocationOnScreen(location);
                    editX = location[0];

                    mHintSearchIcon.getLocationOnScreen(location);
                    hintX = location[0];

                    mDismissHintAnim = dismissHintAnimation();

                    setupListener();
                }
            }
        });



    }

    void setupListener() {

        mQueryHintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterEditMode();
            }
        });

        mCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closePress();

                if (mOnCloseListener != null)
                {
                    mOnCloseListener.onClosePressed(mCloseBtn);
                }

                if (mOnQueryTextListener != null)
                {
                    mOnQueryTextListener.onQueryTextEmpty();
                }
            }
        });

        mQueryText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.toString().isEmpty())
                    mCloseBtn.setVisibility(View.INVISIBLE);
                else
                    mCloseBtn.setVisibility(View.VISIBLE);

                if (mOnQueryTextListener != null)
                {
                    mOnQueryTextListener.onQueryTextChanged(s,start,before,count);

                    if (s.toString().isEmpty())
                        mOnQueryTextListener.onQueryTextEmpty();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mQueryText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                {
                    mMaskView.setAlpha(0.5f);
                    resumeQueryMode();
                    mMaskView.setClickable(true);
                    mMaskView.bringToFront();
                }
                else
                {
                    mMaskView.setAlpha(0.0f);
                    mMaskView.setClickable(false);
                    
                }
            }
        });

        mQueryText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH)
                {
                    if (mOnQueryTextListener != null)
                    {
                        mOnQueryTextListener.onQuerySubmit(mQueryText.getText().toString());
                    }

                    enterPendingQueryMode();
                    return true;
                }

                return false;
            }
        });

        mMaskView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterPendingQueryMode();
            }
        });

        mInnerRoundedRect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resumeQueryMode();
            }
        });

    }

    String getQueryText(){
        return mQueryText.getText().toString();
    }

    EditText getQueryEditText(){
        return mQueryText;
    }

    void setQueryText(String s){
        mQueryText.setText(s);
    }

    ImageView getCloseBtn(){
        return mCloseBtn;
    }

    //handle the close button pressed
    public void closePress()
    {
        if (!mQueryText.getText().toString().isEmpty())
        {
            setQueryText("");
            mCloseBtn.setVisibility(View.INVISIBLE);
        }

        if (!mQueryText.hasFocus())
        {
            resumeQueryMode();
        }

    }

    //handle the settings for searchBar lost focus
    public void enterPendingQueryMode()
    {
        if (mQueryText.getText().toString().isEmpty())
            enterHintMode();

        mQueryText.clearFocus();
        mQueryTextDummy.requestFocus();
        dismissKeyboard();

    }

    //handle the settings for searchBar has focus back
    public void resumeQueryMode()
    {
        mMaskView.setClickable(true);
        mQueryText.requestFocus();

        if (mQueryText.getText().toString().isEmpty())
            mCloseBtn.setVisibility(View.INVISIBLE);
        else
            mCloseBtn.setVisibility(View.VISIBLE);

        showKeyboard();


    }

    //Show up the query hint text
    public void enterHintMode()
    {
        mQueryHintLayout.setAlpha(1);
        mQueryHintLayout.setClickable(true);
        mInnerRoundedRect.setAlpha(0);
        mInnerRoundedRect.setClickable(false);
        mQueryTextDummy.requestFocus();
        dismissKeyboard();
    }

    //Handle the settings for searchBar get focus
    //The only difference to the resumeQueryMode() is
    //it does query hint dismissal animation
    public void enterEditMode()
    {
        mCustomSearchView.bringToFront();
        mQueryHintLayout.setClickable(false);
        mDismissHintAnim.start();
    }

    //This is the hint dismiss animation
    AnimatorSet dismissHintAnimation()
    {
        int duration = 250;

        mQueryHintLayout.setAlpha(1);
        mInnerRoundedRect.setAlpha(0);
        mQueryHintLayout.setClickable(true);

        final  float hintX = mQueryHintContent.getX();
        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(mQueryHintContent,"x",editX);
        objectAnimator1.setDuration(duration);

        final float hintTextX = mQueryHintText.getX();
        ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(mQueryHintText,"x",-100);
        objectAnimator2.setDuration(duration);

        ObjectAnimator objectAnimator3 = ObjectAnimator.ofFloat(mQueryHintText,"alpha",0);
        objectAnimator3.setDuration(duration);

        ObjectAnimator objectAnimator4 = ObjectAnimator.ofFloat(mInnerRoundedRect,"alpha",1);
        objectAnimator4.setDuration(duration);
        objectAnimator4.setStartDelay(100);

        ObjectAnimator objectAnimator5 = ObjectAnimator.ofFloat(mMaskView,"alpha",0.5f);
        objectAnimator5.setDuration(duration);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(objectAnimator1,objectAnimator2,objectAnimator3,objectAnimator4,objectAnimator5);

        objectAnimator4.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mQueryHintContent.setX(hintX);
                mQueryHintText.setX(hintTextX);
                mQueryHintText.setAlpha(1);
                mQueryHintLayout.setAlpha(0);

                mInnerRoundedRect.setClickable(true);
                mInnerRoundedRect.setAlpha(1.0f);
                mQueryText.requestFocus();


                mMaskView.setClickable(true);

                if (mOnQueryTextListener != null)
                {
                    mOnQueryTextListener.onQueryTextEmpty();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        return animatorSet;

    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mQueryText, InputMethodManager.SHOW_FORCED);

    }

    private void dismissKeyboard() {
        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mQueryText.getWindowToken(), 0);
    }

    //Listener
    public void setOnQueryTextListener(OnQueryTextListener listener)
    {
        mOnQueryTextListener = listener;
    }

    public interface OnQueryTextListener{
        void onQueryTextChanged(CharSequence s, int start, int before, int count);

        //called when query text is empty
        void onQueryTextEmpty();

        //called when search button pressed
        void onQuerySubmit(String queryText);
    }

    public void setOnCloseListener(OnCloseListener listener)
    {
        mOnCloseListener = listener;
    }

    public interface OnCloseListener{
        void onClosePressed(ImageView xButton);
    }

    //Custom editText
    //We need to override the editText to detect back button pressed
    class ListenerEditText extends EditText{

        public ListenerEditText(Context context, AttributeSet attrs,EditText editText) {
            super(context, attrs);
            clone(editText);
        }

        void clone(EditText editText){
            this.setLayoutParams(editText.getLayoutParams());
            this.setBackgroundResource(0);
            this.setCursorVisible(true);
            this.setCursorVisible(true);
            this.setFocusable(editText.isFocusable());
            this.setImeOptions(editText.getImeOptions());
            this.setInputType(editText.getInputType());
            this.setPadding(editText.getPaddingLeft(),editText.getPaddingTop(),editText.getPaddingRight(),editText.getPaddingBottom());
        }

        @Override
        public boolean onKeyPreIme (int keyCode, KeyEvent event){
            if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){
                enterPendingQueryMode();
            }
            return false;
        }
    }


}


//This is the adapter used for search while typing
//We are not using here, but keep it here for future use
class CustomAdapterTyping extends ArrayAdapter<UserInfo> {

    LayoutInflater mInflater;
    int mResourceID;
    List<UserInfo> mItems;
    Filter mFilter;

    public CustomAdapterTyping(Context context, int resource, List<UserInfo> items) {
        super(context, resource, items);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mResourceID = resource;
        mItems = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            v = mInflater.inflate(mResourceID, null);
            v.setMinimumHeight(250);
        }

        UserInfo p = getItem(position);

        if (p != null) {
            //assign values to row

            TextView textView = (TextView) v.findViewById(R.id.contentText);

            String s = mItems.get(position).name;
            textView.setText(s);
        }

        return v;

    }

    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new CustomFilter();
        }
        return mFilter;
    }

    private class CustomFilter extends Filter {

        String mConstraint;
        ArrayList<List<UserInfo>> results;

        CustomFilter() {
            mConstraint = "";
            results = new ArrayList<List<UserInfo>>();
            ArrayList<UserInfo> list = new ArrayList<UserInfo>();
            list.addAll(mItems);
            results.add(list);
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            constraint = constraint.toString().toLowerCase();
            FilterResults result = new FilterResults();

            if (constraint == null)
                return result;

            //use previous result, if query text is going back
            if (constraint.toString().length() <= mConstraint.length()) {

                int diff = Math.abs(mConstraint.length() - constraint.toString().length());
                //if diff is greater than 1, means x button is pressed
                if (diff > 1) {
                    //reset results array
                    List<UserInfo> list = results.get(0);
                    results.clear();
                    results.add(list);
                }

                mConstraint = constraint.toString();

                List<UserInfo> last = results.get(results.size() - 1);
                if (results.size() > 1) {
                    results.remove(results.size() - 1);
                    last = results.get(results.size() - 1);
                }

                synchronized (this) {
                    result.values = last;
                    result.count = last.size();
                }

            } else {
                //adding text
                mConstraint = constraint.toString();

                ArrayList<UserInfo> filteredItems = new ArrayList<UserInfo>();
                List<UserInfo> last = results.get(results.size() - 1);

                //Here is where we do the compare
                for (UserInfo user : last) {
                    if (user.name.toLowerCase().contains(constraint))
                        filteredItems.add(user);
                }

                results.add(filteredItems);

                result.count = filteredItems.size();
                result.values = filteredItems;
            }


            return result;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            ArrayList<UserInfo> result = (ArrayList<UserInfo>) results.values;
            notifyDataSetChanged();
            mItems.clear();
            mItems.addAll(result);
            notifyDataSetInvalidated();
        }
    }

}