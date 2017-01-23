package com.example.stefi.remap.model.adapter;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.stefi.remap.R;
import com.example.stefi.remap.model.db.Task;
import com.example.stefi.remap.view.activities.ViewTaskActivity;

import java.util.ArrayList;

/**
 * Created by Daniel on 1/22/2017.
 */

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskHolder> {

    private ArrayList<Task> mTasks;

    public TaskAdapter(ArrayList<Task> tasks) {
        this.mTasks = tasks;
    }

    public void swap(ArrayList<Task> tasks) {
        if (mTasks != null) {
            mTasks.clear();
            mTasks.addAll(tasks);
        } else {
            mTasks = tasks;
        }
        notifyDataSetChanged();
    }

    @Override
    public TaskHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_task_row, parent, false);
        return new TaskHolder(view);
    }

    @Override
    public void onBindViewHolder(TaskHolder holder, int position) {
        Task currTask = mTasks.get(position);
        holder.mDate.setText(currTask.getData());
        holder.mTime.setText(currTask.getVreme());
        holder.mDesc.setText(currTask.getDescription());
    }

    @Override
    public int getItemCount() {
        return mTasks.size();
    }

    public class TaskHolder extends RecyclerView.ViewHolder {
        private static final String TAG = "TASK ADAPTER";
        TextView mDate;
        TextView mTime;
        TextView mDesc;
        ImageView mSelectTask;

        public TaskHolder(View itemView) {
            super(itemView);

            mDate = (TextView) itemView.findViewById(R.id.task_row_date);
            mTime = (TextView) itemView.findViewById(R.id.task_row_time);
            mDesc = (TextView) itemView.findViewById(R.id.task_row_desc);
            mSelectTask = (ImageView) itemView.findViewById(R.id.task_row_button);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Task task = mTasks.get(getAdapterPosition());
                    Intent viewTask = new Intent(v.getContext(), ViewTaskActivity.class);
                    viewTask.putExtra(ViewTaskActivity.EXTRA_DATE, task.getData());
                    viewTask.putExtra(ViewTaskActivity.EXTRA_TIME, task.getVreme());
                    viewTask.putExtra(ViewTaskActivity.EXTRA_DESC, task.getDescription());
                    viewTask.putExtra(ViewTaskActivity.EXTRA_LAT, task.getLat());
                    viewTask.putExtra(ViewTaskActivity.EXTRA_LON, task.getLon());
                    viewTask.putExtra(ViewTaskActivity.EXTRA_RADIUS, task.getRange());

                    v.getContext().startActivity(viewTask);
                }
            });

        }
    }
}

