/**
 * Copyright (c) 2012 Todoroo Inc
 *
 * See the file "LICENSE" for the full license governing this code.
 */
package com.todoroo.astrid.gtasks;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.todoroo.andlib.data.Property;
import com.todoroo.andlib.data.TodorooCursor;
import com.todoroo.astrid.activity.TaskListFragment;
import com.todoroo.astrid.adapter.TaskAdapter;
import com.todoroo.astrid.api.GtasksFilter;
import com.todoroo.astrid.dao.MetadataDao;
import com.todoroo.astrid.dao.TaskAttachmentDao;
import com.todoroo.astrid.dao.TaskDao;
import com.todoroo.astrid.data.Task;
import com.todoroo.astrid.tags.TagService;

import org.tasks.R;
import org.tasks.dialogs.DialogBuilder;
import org.tasks.injection.ForApplication;
import org.tasks.injection.FragmentComponent;
import org.tasks.preferences.Preferences;
import org.tasks.tasklist.GtasksListFragment;
import org.tasks.themes.Theme;
import org.tasks.themes.ThemeCache;
import org.tasks.ui.CheckBoxes;

import java.util.ArrayList;
import java.util.Arrays;

import javax.inject.Inject;

public class GtasksSubtaskListFragment extends GtasksListFragment {

    public static TaskListFragment newGtasksSubtaskListFragment(GtasksFilter filter, GtasksList list) {
        GtasksSubtaskListFragment fragment = new GtasksSubtaskListFragment();
        fragment.filter = filter;
        fragment.list = list;
        return fragment;
    }

    @Inject TaskDao taskDao;
    @Inject MetadataDao metadataDao;
    @Inject GtasksTaskListUpdater gtasksTaskListUpdater;
    @Inject TaskAttachmentDao taskAttachmentDao;
    @Inject Preferences preferences;
    @Inject DialogBuilder dialogBuilder;
    @Inject CheckBoxes checkBoxes;
    @Inject TagService tagService;
    @Inject ThemeCache themeCache;
    @Inject @ForApplication Context context;
    @Inject Theme theme;

    protected OrderedMetadataListFragmentHelper helper;
    private int lastVisibleIndex = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        helper.setList(list);
    }

    @Override
    protected void onTaskDelete(Task task) {
        super.onTaskDelete(task);
        helper.onDeleteTask(task);
    }

    @Override
    public Property<?>[] taskProperties() {
        Property<?>[] baseProperties = TaskAdapter.PROPERTIES;
        ArrayList<Property<?>> properties = new ArrayList<>(Arrays.asList(baseProperties));
        properties.add(GtasksMetadata.INDENT);
        properties.add(GtasksMetadata.ORDER);
        return properties.toArray(new Property<?>[properties.size()]);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        helper = new OrderedMetadataListFragmentHelper(preferences, taskAttachmentDao, taskDao,
                metadataDao, this, gtasksTaskListUpdater, dialogBuilder, checkBoxes, tagService, themeCache);
    }

    @Override
    protected int getListBody() {
        return R.layout.task_list_body_subtasks;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        helper.setUpUiComponents();
    }

    @Override
    public void setTaskAdapter() {
        helper.setList(list);
        helper.beforeSetUpTaskList(filter);

        super.setTaskAdapter();
    }

    @Override
    public void onPause() {
        super.onPause();
        lastVisibleIndex = getListView().getFirstVisiblePosition();
    }

    @Override
    public void onResume() {
        super.onResume();
        ListView listView = getListView();
        if (lastVisibleIndex >= 0) {
            listView.setSelection(lastVisibleIndex);
        }
        unregisterForContextMenu(listView);
    }

    @Override
    public void onTaskCreated(String uuid) {
        helper.onCreateTask(uuid);
    }

    @Override
    protected TaskAdapter createTaskAdapter(TodorooCursor<Task> cursor) {
        return helper.createTaskAdapter(theme.wrap(context), cursor, taskListDataProvider.getSqlQueryTemplate());
    }

    @Override
    public void inject(FragmentComponent component) {
        component.inject(this);
    }
}
