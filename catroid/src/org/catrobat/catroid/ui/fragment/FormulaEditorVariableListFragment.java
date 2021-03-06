/**
 *  Catroid: An on-device visual programming system for Android devices
 *  Copyright (C) 2010-2013 The Catrobat Team
 *  (<http://developer.catrobat.org/credits>)
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *  
 *  An additional term exception under section 7 of the GNU Affero
 *  General Public License, version 3, is available at
 *  http://developer.catrobat.org/license_additional_term
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *  
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.catrobat.catroid.ui.fragment;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.R;
import org.catrobat.catroid.common.Constants;
import org.catrobat.catroid.content.Project;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.formulaeditor.FormulaEditorEditText;
import org.catrobat.catroid.formulaeditor.UserVariable;
import org.catrobat.catroid.formulaeditor.UserVariablesContainer;
import org.catrobat.catroid.ui.ScriptActivity;
import org.catrobat.catroid.ui.adapter.UserVariableAdapter;
import org.catrobat.catroid.ui.dialogs.NewVariableDialog;
import org.catrobat.catroid.ui.dialogs.NewVariableDialog.NewVariableDialogListener;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class FormulaEditorVariableListFragment extends SherlockListFragment implements Dialog.OnKeyListener,
		UserVariableAdapter.OnCheckedChangeListener, UserVariableAdapter.OnListItemClickListener,
		NewVariableDialogListener {

	public static final String VARIABLE_TAG = "variableFragment";
	private FormulaEditorEditText formulaEditorEditText;
	private String actionBarTitle;
	private ActionMode contextActionMode;
	private boolean inContextMode;
	private int deleteIndex;
	private UserVariableAdapter adapter;

	public FormulaEditorVariableListFragment(FormulaEditorEditText formulaEditorEditText, String actionBarTitle,
			String fragmentTag) {
		this.formulaEditorEditText = formulaEditorEditText;
		this.actionBarTitle = actionBarTitle;
		contextActionMode = null;
		deleteIndex = -1;
		inContextMode = false;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		initializeUserVariableAdapter();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View fragmentView = inflater.inflate(R.layout.fragment_formula_editor_variablelist, container, false);
		return fragmentView;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
		if (!inContextMode) {
			super.onCreateContextMenu(menu, view, menuInfo);
			getSherlockActivity().getMenuInflater().inflate(R.menu.menu_formulaeditor_variablelist, menu);
		}
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		for (int index = 0; index < menu.size(); index++) {
			menu.getItem(index).setVisible(false);
		}
		menu.findItem(R.id.delete).setVisible(true);

		getSherlockActivity().getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSherlockActivity().getSupportActionBar().setTitle(actionBarTitle);
		getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(false);

		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onListItemClick(int position) {

		if (!inContextMode) {
			formulaEditorEditText.handleKeyEvent(0, "" + adapter.getItem(position).getName());
			KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK);
			onKey(null, keyEvent.getKeyCode(), keyEvent);
		}

	}

	@Override
	public void onCheckedChange() {
		if (inContextMode) {
			String title = adapter.getAmountOfCheckedItems() + " "
					+ getString(R.string.formula_editor_variable_context_action_item_selected);
			contextActionMode.setTitle(title);
		}
	}

	@Override
	public void onStart() {

		this.registerForContextMenu(getListView());
		getListView().setItemsCanFocus(true);
		getListView().setLongClickable(true);
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View view, int position, long id) {
				if (!inContextMode) {
					deleteIndex = position;
					getSherlockActivity().openContextMenu(getListView());
					return true;
				}
				return false;
			}
		});

		Button bottomBar = (Button) getSherlockActivity().findViewById(R.id.formula_editor_variable_list_bottom_bar);
		bottomBar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				NewVariableDialog dialog = new NewVariableDialog();
				dialog.addVariableDialogListener(FormulaEditorVariableListFragment.this);
				dialog.show(getSherlockActivity().getSupportFragmentManager(), NewVariableDialog.DIALOG_FRAGMENT_TAG);
			}
		});
		adapter.notifyDataSetChanged();

		super.onStart();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.delete:
				inContextMode = true;
				contextActionMode = getSherlockActivity().startActionMode(mContextModeCallback);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {

		switch (item.getItemId()) {
			case R.id.menu_delete:
				if (!adapter.isEmpty()) {
					ProjectManager.getInstance().getCurrentProject().getUserVariables()
							.deleteUserVariableByName(adapter.getItem(deleteIndex).getName());
					adapter.notifyDataSetChanged();
					getActivity().sendBroadcast(new Intent(ScriptActivity.ACTION_VARIABLE_DELETED));
				}
				return true;
			default:
				return super.onContextItemSelected(item);
		}

	}

	@Override
	public void onFinishNewVariableDialog(Spinner spinnerToUpdate) {
		adapter.notifyDataSetChanged();
	}

	public void showFragment(Context context) {
		SherlockFragmentActivity activity = (SherlockFragmentActivity) context;
		FragmentManager fragmentManager = activity.getSupportFragmentManager();
		FragmentTransaction fragTransaction = fragmentManager.beginTransaction();

		Fragment formulaEditorFragment = fragmentManager
				.findFragmentByTag(FormulaEditorFragment.FORMULA_EDITOR_FRAGMENT_TAG);
		fragTransaction.hide(formulaEditorFragment);
		fragTransaction.show(this);
		fragTransaction.commit();

		if (adapter != null) {
			initializeUserVariableAdapter();
		}

	}

	private void initializeUserVariableAdapter() {
		Sprite currentSprite = ProjectManager.getInstance().getCurrentSprite();
		Project currentProject = ProjectManager.getInstance().getCurrentProject();
		UserVariablesContainer userVariableContainer = currentProject.getUserVariables();
		adapter = userVariableContainer.createUserVariableAdapter(getSherlockActivity(), currentSprite);
		setListAdapter(adapter);
		adapter.setOnCheckedChangeListener(this);
		adapter.setOnListItemClickListener(this);
	}

	@Override
	public boolean onKey(DialogInterface d, int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
				FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager()
						.beginTransaction();
				fragmentTransaction.hide(this);
				FormulaEditorFragment formulaEditorFragment = (FormulaEditorFragment) getSherlockActivity()
						.getSupportFragmentManager().findFragmentByTag(
								FormulaEditorFragment.FORMULA_EDITOR_FRAGMENT_TAG);
				formulaEditorFragment.updateBrickView();
				fragmentTransaction.show(formulaEditorFragment);
				fragmentTransaction.commit();
				return true;
			default:
				break;
		}
		return false;
	}

	private ActionMode.Callback mContextModeCallback = new ActionMode.Callback() {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			adapter.setSelectMode(Constants.MULTI_SELECT);
			adapter.notifyDataSetChanged();
			mode.setTitle("0 " + getString(R.string.formula_editor_variable_context_action_item_selected));
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			UserVariablesContainer userVariablesContainer = ProjectManager.getInstance().getCurrentProject()
					.getUserVariables();
			if (!adapter.isEmpty()) {
				for (UserVariable var : adapter.getCheckedUserVariables()) {
					userVariablesContainer.deleteUserVariableByName(var.getName());
				}
			}

			adapter.notifyDataSetChanged();
			getActivity().sendBroadcast(new Intent(ScriptActivity.ACTION_VARIABLE_DELETED));
			adapter.setSelectMode(Constants.SELECT_NONE);
			contextActionMode = null;
			inContextMode = false;
		}
	};

}
