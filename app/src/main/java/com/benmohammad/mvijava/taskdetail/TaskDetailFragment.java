package com.benmohammad.mvijava.taskdetail;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.benmohammad.mvijava.R;
import com.benmohammad.mvijava.addedittask.AddEditTaskActivity;
import com.benmohammad.mvijava.addedittask.AddEditTaskFragment;
import com.benmohammad.mvijava.mvibase.MviView;
import com.benmohammad.mvijava.util.TodoViewModelFactory;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.jakewharton.rxbinding2.view.RxView;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.PublishSubject;

public class TaskDetailFragment extends Fragment implements MviView<TaskDetailIntent, TaskDetailViewState> {

    @Nonnull
    private static final String ARGUMENT_TASK_ID = "TASK_ID";

    @Nonnull
    private static final int REQUEST_EDIT_TASK = 1;

    private TextView detailTitle;
    private TextView detailDescription;
    private CheckBox detailCompleteStatus;
    private FloatingActionButton fab;

    TaskDetailViewModel viewModel;

    private CompositeDisposable disposables = new CompositeDisposable();
    private PublishSubject<TaskDetailIntent.DeleteTask> deleteTaskIntentPublisher = PublishSubject.create();

    public static TaskDetailFragment newInstance(@Nullable String taskId) {
        Bundle arguments = new Bundle();
        arguments.putString(ARGUMENT_TASK_ID, taskId);
        TaskDetailFragment fragment = new TaskDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @androidx.annotation.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @androidx.annotation.Nullable ViewGroup container, @androidx.annotation.Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.taskdetail_frag, container, false);
        setHasOptionsMenu(true);
        detailTitle = root.findViewById(R.id.task_detail_title);
        detailDescription = root.findViewById(R.id.task_detail_description);
        detailCompleteStatus = root.findViewById(R.id.task_detail_complete);

        fab = (FloatingActionButton) getActivity().findViewById(R.id.fab_edit_task);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this, TodoViewModelFactory.getInstance(getContext())).get(TaskDetailViewModel.class);
        disposables = new CompositeDisposable();
        bind();
    }

    private void bind() {
        disposables.add(viewModel.states().subscribe(this::render));
        viewModel.processIntents(intents());
        RxView.clicks(fab).debounce(200, TimeUnit.MILLISECONDS)
                .subscribe(view -> showEditTask(getArgumentTaskId()));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposables.dispose();
    }



    @Nullable
    private String getArgumentTaskId() {
        Bundle args = getArguments();
        if(args == null) return null;
        return args.getString(ARGUMENT_TASK_ID);

    }

    private void showEditTask(@Nonnull String taskId) {
        Intent intent = new Intent(getContext(), AddEditTaskActivity.class);
        intent.putExtra(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID, taskId);
        startActivityForResult(intent, REQUEST_EDIT_TASK);

    }
    @Override
    public Observable<TaskDetailIntent> intents() {
        return Observable.merge(initialIntent(), checkBoxIntent(), deleteIntent());
    }

    private Observable<TaskDetailIntent.InitialIntent> initialIntent() {
        return Observable.just(TaskDetailIntent.InitialIntent.create(getArgumentTaskId()));
    }

    private Observable<TaskDetailIntent> checkBoxIntent() {
        return RxView.clicks(detailCompleteStatus).map(
                activated -> {
                    if(detailCompleteStatus.isChecked()) {
                        return TaskDetailIntent.CompleteTaskIntent.create(getArgumentTaskId());
                    } else {
                        return TaskDetailIntent.ActivateTaskIntent.create(getArgumentTaskId());
                    }
                }
        );
    }

    private Observable<TaskDetailIntent.DeleteTask> deleteIntent() {
        return deleteTaskIntentPublisher;
    }



    @Override
    public void render(TaskDetailViewState state) {
        setLoadingIndicator(state.loading());

        if(!state.title().isEmpty()) {
            showTitle(state.title());
        } else {
            hideTitle();
        }

        if(!state.description().isEmpty()) {
            showDescrption(state.description());
        } else {
            hideDescription();
        }

        showActive(state.active());

        if(state.taskComplete()) {
            showTaskMarkedComplete();
        }

        if(state.taskActivated()) {
            showTaskMarkedActive();
        }

        if(state.taskDeleted()) {
            getActivity().finish();

        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete:
                deleteTaskIntentPublisher.onNext(TaskDetailIntent.DeleteTask.create(getArgumentTaskId()));
                return true;
        }
        return false;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.taskdetail_fragment_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        if(requestCode == REQUEST_EDIT_TASK) {
            if(resultCode == Activity.RESULT_OK) {
                getActivity().finish();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void setLoadingIndicator(boolean active) {
        if(active) {
            detailTitle.setText("");
            detailDescription.setText(getString(R.string.loading));
        }
    }

    public void hideDescription() {
        detailDescription.setVisibility(View.GONE);
    }

    public void hideTitle() {
        detailTitle.setVisibility(View.GONE);
    }

    public void showActive(boolean isActive) {
        detailCompleteStatus.setChecked(!isActive);
    }

    public void showDescrption(@Nonnull String description) {
        detailDescription.setVisibility(View.VISIBLE);
        detailDescription.setText(description);
    }

    public void showTitle(@Nonnull String title) {
        detailTitle.setVisibility(View.VISIBLE);
        detailTitle.setText(title);
    }

    public void showTaskMarkedComplete() {
        Snackbar.make(getView(), getString(R.string.task_marked_complete), Snackbar.LENGTH_SHORT).show();
    }

    public void showTaskMarkedActive() {
        Snackbar.make(getView(), getString(R.string.task_marked_active), Snackbar.LENGTH_SHORT).show();
    }


    public void showMissingTask() {
        detailTitle.setText("");
        detailDescription.setText(getString(R.string.no_data));
    }
}
