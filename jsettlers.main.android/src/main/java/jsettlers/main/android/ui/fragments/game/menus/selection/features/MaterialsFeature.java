package jsettlers.main.android.ui.fragments.game.menus.selection.features;

import jsettlers.common.buildings.IBuilding;
import jsettlers.graphics.androidui.utils.OriginalImageProvider;
import jsettlers.graphics.map.controls.original.panel.selection.BuildingState;
import jsettlers.main.android.R;
import jsettlers.main.android.controls.ControlsAdapter;
import jsettlers.main.android.controls.DrawListener;
import jsettlers.main.android.ui.navigation.MenuNavigator;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by tompr on 11/01/2017.
 */

public class MaterialsFeature extends SelectionFeature implements DrawListener {
    private LayoutInflater layoutInflater;
    private LinearLayout materialsLayout;

    private boolean hasPostConstructionMaterials = true;

    public MaterialsFeature(IBuilding building, ControlsAdapter controls, MenuNavigator menuNavigator, View view) {
        super(building, controls, menuNavigator, view);
    }

    @Override
    public void initialize(BuildingState buildingState, ControlsAdapter controls) {
        super.initialize(buildingState, controls);
        layoutInflater = LayoutInflater.from(getView().getContext());
        materialsLayout = (LinearLayout) getView().findViewById(R.id.layout_materials);

        if (getBuilding() instanceof IBuilding.IOccupied || getBuilding() instanceof IBuilding.IStock || getBuilding() instanceof IBuilding.ITrading) {
            hasPostConstructionMaterials = false;
        }

        if (getBuildingState().isConstruction() || hasPostConstructionMaterials) {
            update();
        }

        getControls().addDrawListener(this);
    }

    @Override
    public void finish() {
        super.finish();
        getControls().removeDrawListener(this);
    }

    @Override
    public void draw() {
        //TODO would be more efficient to compare the stacks rather than the entire building state to avoid unnecessary work
        if (!getBuildingState().isStillInState(getBuilding())) {
            setBuildingState(new BuildingState(getBuilding()));

            getView().post(new Runnable() {
                @Override
                public void run() {
                    if (getBuildingState().isConstruction() || hasPostConstructionMaterials) {
                        update();
                    } else {
                        materialsLayout.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    private void update() {
        materialsLayout.setVisibility(View.VISIBLE);
        materialsLayout.removeAllViews();

        for (BuildingState.StackState materialStackState : getBuildingState().getStackStates()) {

            View materialItemView = layoutInflater.inflate(R.layout.view_material, materialsLayout, false);
            ImageView imageView = (ImageView) materialItemView.findViewById(R.id.image_view);
            TextView textView = (TextView) materialItemView.findViewById(R.id.text_view);

            textView.setText(materialStackState.getCount() + "");
            OriginalImageProvider.get(materialStackState.getType()).setAsImage(imageView);

            if (materialStackState.isOffering()) {
                materialsLayout.addView(materialItemView);
            } else {
                materialsLayout.addView(materialItemView, 0);
            }
        }
    }
}
