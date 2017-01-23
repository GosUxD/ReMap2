package com.example.stefi.remap.view.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.stefi.remap.R;
import com.example.stefi.remap.model.adapter.TaskAdapter;
import com.example.stefi.remap.model.db.RealmController;
import com.example.stefi.remap.model.db.Task;

import java.util.ArrayList;

/**
 * Created by Stefi on 16.01.2017.
 */

public class TaskFragment extends Fragment {

    private static final String TAG = "TASK FRAGMENT";
    private RecyclerView mRecyclerView;
    private TaskAdapter mTaskAdapter;
    private ArrayList<Task> mTasks;
    private RealmController realmController;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, null, false);

        Log.i(TAG, "onCreateView: ");
        mRecyclerView = (RecyclerView) view.findViewById(R.id.tasks_recycler_view);
        mTaskAdapter = new TaskAdapter(mTasks);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mTaskAdapter);

        return view;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: ");
        realmController = new RealmController(getContext());
        mTasks = realmController.getTasks();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");
        mTaskAdapter.swap(realmController.getTasks());
    }
}
