package jsettlers.graphics.map.controls.original.panel.content.settlers.profession;

import java.text.MessageFormat;
import java.util.List;

import go.graphics.text.EFontSize;
import jsettlers.common.action.ChangeMovableRatioAction;
import jsettlers.common.action.EActionType;
import jsettlers.common.map.IGraphicsGrid;
import jsettlers.common.map.partition.IPartitionSettings;
import jsettlers.common.map.partition.IProfessionSettings;
import jsettlers.common.movable.EMovableType;
import jsettlers.common.player.IInGamePlayer;
import jsettlers.common.player.IPlayer;
import jsettlers.common.position.ShortPoint2D;
import jsettlers.graphics.action.ActionFireable;
import jsettlers.graphics.localization.Labels;
import jsettlers.graphics.map.controls.original.panel.content.AbstractContentProvider;
import jsettlers.graphics.map.controls.original.panel.content.ESecondaryTabType;
import jsettlers.graphics.map.controls.original.panel.content.updaters.UiContentUpdater;
import jsettlers.graphics.map.controls.original.panel.content.updaters.UiLocationDependingContentUpdater;
import jsettlers.graphics.ui.CountArrows;
import jsettlers.graphics.ui.Label;
import jsettlers.graphics.ui.Label.EHorizontalAlignment;
import jsettlers.graphics.ui.UIElement;
import jsettlers.graphics.ui.UIPanel;

public class ProfessionPanel extends AbstractContentProvider implements UiContentUpdater.IUiContentReceiver<IPartitionSettings> {

	private final ContentPanel panel;
	private final UiLocationDependingContentUpdater<IPartitionSettings> uiContentUpdater;

	public ProfessionPanel() {
		this.panel = new ContentPanel();
		this.uiContentUpdater = new UiLocationDependingContentUpdater<>(ProfessionPanel::currentDistributionSettingsProvider);
		this.uiContentUpdater.addListener(this);
	}

	@Override
	public UIPanel getPanel() {
		return panel;
	}

	@Override
	public ESecondaryTabType getTabs() {
		return ESecondaryTabType.SETTLERS;
	}

	@Override
	public void showMapPosition(ShortPoint2D position, IGraphicsGrid grid) {
		this.panel.setPosition(position);
		super.showMapPosition(position, grid);
		this.uiContentUpdater.updatePosition(grid, position);
	}

	@Override
	public void contentShowing(ActionFireable actionFireable) {
		super.contentShowing(actionFireable);
		this.uiContentUpdater.start();
	}
	
	@Override
	public void contentHiding(ActionFireable actionFireable, AbstractContentProvider nextContent) {
		super.contentHiding(actionFireable, nextContent);
		this.uiContentUpdater.stop();
	}

	@Override
	public void update(IPartitionSettings partitionSettings) {
		if (partitionSettings != null) {
			this.panel.setup(partitionSettings.getProfessionSettings());
		}
	}

	public void setPlayer(IInGamePlayer player) {
		uiContentUpdater.setPlayer(player);
	}

	private static IPartitionSettings currentDistributionSettingsProvider(IGraphicsGrid grid, ShortPoint2D position) {
		IPlayer player = grid.getPlayerAt(position.x, position.y);
		return (player != null && player.getPlayerId() >= 0) ? grid.getPartitionData(position.x, position.y).getPartitionSettings() : null;
	}

	public enum EProfessionType {
		CARRIER("Carriers", true, EMovableType.BEARER),
		DIGGER("Diggers", false, EMovableType.DIGGER),
		BUILDER("Builders", false, EMovableType.BRICKLAYER);

		public final String label;
		public final boolean min;
		public final EMovableType moveableType;

		private EProfessionType(String label, boolean min, EMovableType movableType) {
			this.label = label;
			this.min = min;
			this.moveableType = movableType;
		}
	}

	public class ContentPanel extends Panel {
		private ShortPoint2D position;
		private final SettlerPanel diggerPanel;
		private final SettlerPanel carrierPanel;
		private final SettlerPanel builderPanel;

		public ContentPanel() {
			super(118f, 216f);
			this.position = new ShortPoint2D(0, 0);
			this.carrierPanel = new SettlerPanel(widthInPx, EProfessionType.CARRIER);
			this.diggerPanel = new SettlerPanel(widthInPx, EProfessionType.DIGGER);
			this.builderPanel = new SettlerPanel(widthInPx, EProfessionType.BUILDER);

			add(Panel.box(new Label(Labels.getString("settler_profession_title"), EFontSize.NORMAL), widthInPx, 20f), 0f, 0f);
			add(carrierPanel, 0f, 20f);
			add(diggerPanel, 0f, 50f);
			add(builderPanel, 0f, 80f);
		}

		public void setup(IProfessionSettings settings) {
			this.carrierPanel.setRatio(settings.getMinBearerRatio());
			this.carrierPanel.setCurrentRatio(settings.getCurrentBearerRatio());

			this.diggerPanel.setRatio(settings.getMaxDiggerRatio());
			this.diggerPanel.setCurrentRatio(settings.getCurrentDiggerRatio());

			this.builderPanel.setRatio(settings.getMaxBricklayerRatio());
			this.builderPanel.setCurrentRatio(settings.getCurrentBricklayerRatio());

			update();
		}

		public float getTotalRatio() {
			return carrierPanel.ratio + diggerPanel.ratio + builderPanel.ratio;
		}

		public void setPosition(ShortPoint2D position) {
			this.position = position;
		}

		public class SettlerPanel extends Panel {
			private float ratio;
			private float currentRatio;
			private final Label label;
			private final EProfessionType type;

			public SettlerPanel(float width, EProfessionType type) {
				super(width, 30f);
				this.ratio = 0f;
				this.currentRatio = 0f;
				this.label = new Label("...", EFontSize.NORMAL, EHorizontalAlignment.LEFT);
				this.type = type;

				add(Panel.box(new CountArrows(
						() -> new ChangeMovableRatioAction(EActionType.INCREASE_MOVABLE_RATIO, type.moveableType, position),
						() -> new ChangeMovableRatioAction(EActionType.DECREASE_MOVABLE_RATIO, type.moveableType, position)
				), 12f, 18f), 10f, 5f);
				add(Panel.box(label, width, 20f), 28f, 5f);
			}

			@Override
			public void update() {
				super.update();
				this.label.setText(MessageFormat.format("{0} {1} {2} ({3})", (this.type.min ? '>' : '<'), formatPercentage(ratio), Labels.getName(this.type.moveableType), formatPercentage(currentRatio)));
			}

			private String formatPercentage(float value) {
				return (int) (value * 100f) + "%";
			}

			public void setRatio(float ratio) {
				this.ratio = ratio;
			}

			public void setCurrentRatio(float currentRatio) {
				this.currentRatio = currentRatio;
			}
		}
	}

	public static class Panel extends UIPanel {
		final float widthInPx;
		final float heightInPx;

		public Panel(float widthInPx, float heightInPx) {
			this.widthInPx = widthInPx;
			this.heightInPx = heightInPx;
		}

		public void update() {
			List<UIElement> children = getChildren();
			for (UIElement element : children) {
				if (element instanceof Panel) {
					Panel panel = (Panel) element;
					panel.update();
				}
			}
		}

		public void add(Panel panel, float x, float y) {
			addChild(panel, x(x), 1f - y(y) - y(panel.heightInPx), x(x) + x(panel.widthInPx), 1f - y(y));
		}

		public float x(float px) {
			return px / widthInPx;
		}

		public float y(float px) {
			return px / heightInPx;
		}

		public static Panel box(UIElement element, float width, float height) {
			Panel panel = new Panel(width, height);
			panel.addChild(element, 0f, 0f, 1f, 1f);
			return panel;
		}
	}
}
