package com.benmohammad.mvijava.tasks;

import android.print.PageRange;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.benmohammad.mvijava.R;
import com.benmohammad.mvijava.data.Task;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

import static autovalue.shaded.com.google$.common.base.$Preconditions.checkNotNull;

public class TasksAdapter extends BaseAdapter {

    private PublishSubject<Task> taskClickObservable = PublishSubject.create();
    private PublishSubject<Task> taskToggleObservable = PublishSubject.create();
    private List<Task> tasks;

    public TasksAdapter(List<Task> tasks) {
        setList(tasks);
    }

    public void replaceData(List<Task> tasks) {
        setList(tasks);
        notifyDataSetChanged();
    }

    private void setList(List<Task> tasks) {
        this.tasks = checkNotNull(tasks);
    }

    Observable<Task> getTaskCLickObservable() {
        return taskClickObservable;
    }

    Observable<Task> getTaskToggleObservable() {
        return taskToggleObservable;
    }

    @Override
    public int getCount() {
        return tasks.size();
    }

    @Override
    public Task getItem(int position) {
        return tasks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if(rowView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            rowView = inflater.inflate(R.layout.task_item, parent, false);
        }
        final Task task = getItem(position);
        TextView titleTV = rowView.findViewById(R.id.title);
        titleTV.setText(task.getTitleForLIst());

        CheckBox completeCB = rowView.findViewById(R.id.complete);

        completeCB.setChecked(task.isCompleted());

        completeCB.setOnClickListener(ignored -> taskToggleObservable.onNext(task));

        rowView.setOnClickListener(ignored -> taskClickObservable.onNext(task));

        return rowView;

    }
}
