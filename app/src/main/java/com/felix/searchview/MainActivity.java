package com.felix.searchview;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    // 用于显示查询结果或者热门搜索词
    private ListView listView;
    private SearchView searchView;
    // 从服务器获取的热门搜索词
    private Object[] names;
    // 模拟数据
    private ArrayList<String> mAllList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_main);
        initActionbar();
        names = loadData();
        listView = findViewById(R.id.list);
        listView.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_expandable_list_item_1, names));

        listView.setTextFilterEnabled(true);
        searchView.setOnQueryTextListener(this);
        searchView.setSubmitButtonEnabled(false);
        // SearchView去掉（修改）搜索框的背景 修改光标
        setSearchViewBackground(searchView);

    }

    private Object[] loadData() {
        mAllList.add("aa");
        mAllList.add("ddfa");
        mAllList.add("qw");
        mAllList.add("sd");
        mAllList.add("fd");
        mAllList.add("cf");
        mAllList.add("re");
        return mAllList.toArray();
    }

    private void initActionbar() {
        // 将搜索框设置在标题的位置
        LayoutInflater mInflater = LayoutInflater.from(this);
        View mTitleView = mInflater.inflate(R.layout.custom_action_bar_layout, null);
        searchView = mTitleView.findViewById(R.id.search_view);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            return;
        }
        // 自定义标题栏
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(mTitleView, new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                        ActionBar.LayoutParams.WRAP_CONTENT));

    }

    @Override
    public boolean onQueryTextChange(String newText) {
        Object[] obj = searchItem(newText);
        updateLayout(obj);
        return false;
    }

    public Object[] searchItem(String name) {
        ArrayList<String> mSearchList = new ArrayList<>();
        for (int i = 0; i < mAllList.size(); i++) {
            int index = mAllList.get(i).indexOf(name);
            // 存在匹配的数据
            if (index != -1) {
                mSearchList.add(mAllList.get(i));
            }
        }
        return mSearchList.toArray();
    }

    public void updateLayout(Object[] obj) {
        listView.setAdapter(new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_expandable_list_item_1, obj));
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    // android4.0 SearchView去掉（修改）搜索框的背景 修改光标
    @SuppressWarnings("deprecation")
    public void setSearchViewBackground(SearchView searchView) {
        try {
            Class<?> argClass = searchView.getClass();
            // 指定某个私有属性
            // 注意mSearchPlate的背景是stateListDrawable(不同状态不同的图片)
            Field ownField = argClass.getDeclaredField("mSearchPlate");
            // 所以不能用BitmapDrawable
            // setAccessible
            // 它是用来设置是否有权限访问反射类中的私有属性的，只有设置为true时才可以访问，默认为false,暴力反射
            ownField.setAccessible(true);
            View mView = (View) ownField.get(searchView);
            mView.setBackgroundDrawable(getResources().getDrawable(
                    R.drawable.search_edit_bg1));
            // 指定某个私有属性
            Field mQueryTextView = argClass.getDeclaredField("mQueryTextView");
            mQueryTextView.setAccessible(true);
            Class<?> mTextViewClass = mQueryTextView.get(searchView).getClass()
                    .getSuperclass().getSuperclass().getSuperclass();

            // mCursorDrawableRes光标图片Id的属性
            // 这个属性是TextView的属性，所以要用mQueryTextView（SearchAutoComplete）的父类（AutoCompleteTextView）的父
            // 类( EditText）的父类(TextView)
            Field mCursorDrawableRes = mTextViewClass
                    .getDeclaredField("mCursorDrawableRes");

            // setAccessible 它是用来设置是否有权限访问反射类中的私有属性的，只有设置为true时才可以访问，默认为false
            mCursorDrawableRes.setAccessible(true);
            // 注意第一个参数持有这个属性(mQueryTextView)的对象(mSearchView)
            // 光标必须是一张图片不能是颜色，因为光标有两张图片，一张是第一次获得焦点的时候的闪烁的图片，一张是后边有内容时候的图片，如果用颜色填充的话，就会失去闪烁的那张图片，颜色填充的会缩短文字和光标的距离（某些字母会背光标覆盖一部分）。
            mCursorDrawableRes.set(mQueryTextView.get(searchView), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
