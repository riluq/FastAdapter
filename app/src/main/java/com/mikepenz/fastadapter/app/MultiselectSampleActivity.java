package com.mikepenz.fastadapter.app;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.app.items.SimpleItem;
import com.mikepenz.fastadapter.helpers.ActionModeHelper;
import com.mikepenz.fastadapter.helpers.UndoHelper;
import com.mikepenz.fastadapter.select.SelectExtension;
import com.mikepenz.itemanimators.SlideDownAlphaAnimator;
import com.mikepenz.materialize.MaterializeBuilder;
import com.mikepenz.materialize.util.UIUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MultiselectSampleActivity extends AppCompatActivity {
    //save our FastAdapter
    private FastAdapter<SimpleItem> mFastAdapter;

    private UndoHelper mUndoHelper;

    private ActionModeHelper<SimpleItem> mActionModeHelper;

    private SelectExtension<SimpleItem> selectExtension;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        findViewById(android.R.id.content).setSystemUiVisibility(findViewById(android.R.id.content).getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        // Handle Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.sample_multi_select);

        //style our ui
        new MaterializeBuilder().withActivity(this).build();

        //create our adapters
        final ItemAdapter<SimpleItem> headerAdapter = new ItemAdapter<>();
        ItemAdapter<SimpleItem> itemAdapter = new ItemAdapter<>();

        //create our FastAdapter
        mFastAdapter = FastAdapter.Companion.with(Arrays.asList(headerAdapter, itemAdapter));

        //configure our mFastAdapter
        //as we provide id's for the items we want the hasStableIds enabled to speed up things
        mFastAdapter.setHasStableIds(true);
        selectExtension = mFastAdapter.getOrCreateExtension(SelectExtension.class);
        selectExtension.setSelectable(true);
        selectExtension.setMultiSelect(true);
        selectExtension.setSelectOnLongClick(true);
        selectExtension.setSelectionListener((item, selected) -> Log.i("FastAdapter", "SelectedCount: " + selectExtension.getSelections().size() + " ItemsCount: " + selectExtension.getSelectedItems().size()));
        mFastAdapter.setOnPreClickListener((v, adapter, item, position) -> {
            //we handle the default onClick behavior for the actionMode. This will return null if it didn't do anything and you can handle a normal onClick
            Boolean res = mActionModeHelper.onClick(item);
            return res != null ? res : false;
        });
        mFastAdapter.setOnClickListener((v, adapter, item, position) -> {
            Toast.makeText(v.getContext(), "SelectedCount: " + selectExtension.getSelections().size() + " ItemsCount: " + selectExtension.getSelectedItems().size(), Toast.LENGTH_SHORT).show();
            return false;
        });
        mFastAdapter.setOnPreLongClickListener((v, adapter, item, position) -> {
            ActionMode actionMode = mActionModeHelper.onLongClick(MultiselectSampleActivity.this, position);

            if (actionMode != null) {
                //we want color our CAB
                findViewById(R.id.action_mode_bar).setBackgroundColor(UIUtils.getThemeColorFromAttrOrRes(MultiselectSampleActivity.this, R.attr.colorPrimary, R.color.material_drawer_primary));
            }

            //if we have no actionMode we do not consume the event
            return actionMode != null;
        });

        //
        mUndoHelper = new UndoHelper<>(mFastAdapter, (positions, removed) -> Log.e("UndoHelper", "Positions: " + positions.toString() + " Removed: " + removed.size()));

        //we init our ActionModeHelper
        mActionModeHelper = new ActionModeHelper<>(mFastAdapter, R.menu.cab, new ActionBarCallBack());

        //get our recyclerView and do basic setup
        RecyclerView rv = findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setItemAnimator(new SlideDownAlphaAnimator());
        rv.setAdapter(mFastAdapter);

        //fill with some sample data
        SimpleItem simpleItem = new SimpleItem();
        simpleItem
                .withName("Header");
        simpleItem.setIdentifier(2);
        simpleItem.setSelectable(false);
        headerAdapter.add(simpleItem);
        List<SimpleItem> items = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            SimpleItem item = new SimpleItem();
            item.withName("Test " + i);
            item.setIdentifier(100 + i);
            items.add(item);
        }
        itemAdapter.add(items);

        //restore selections (this has to be done after the items were added
        mFastAdapter.withSavedInstanceState(savedInstanceState);

        //set the back arrow in the toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(false);

        //inform that longClick is required
        Toast.makeText(this, "LongClick to enable Multi-Selection", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //add the values which need to be saved from the adapter to the bundle
        outState = mFastAdapter.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //handle the click on the back arrow click
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Our ActionBarCallBack to showcase the CAB
     */
    class ActionBarCallBack implements ActionMode.Callback {

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            mUndoHelper.remove(findViewById(android.R.id.content), "Item removed", "Undo", Snackbar.LENGTH_LONG, selectExtension.getSelections());
            //as we no longer have a selection so the actionMode can be finished
            mode.finish();
            //we consume the event
            return true;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }
    }
}
