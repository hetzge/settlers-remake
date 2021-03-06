/*******************************************************************************
 * Copyright (c) 2015 - 2018
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package jsettlers.logic.movable;

import jsettlers.algorithms.fogofwar.FoWTask;
import jsettlers.algorithms.path.Path;
import jsettlers.common.action.EMoveToType;
import jsettlers.common.map.shapes.HexGridArea;
import jsettlers.common.mapobject.EMapObjectType;
import jsettlers.common.material.EMaterialType;
import jsettlers.common.material.ESearchType;
import jsettlers.common.movable.EDirection;
import jsettlers.common.movable.EEffectType;
import jsettlers.common.movable.EMovableAction;
import jsettlers.common.movable.EMovableType;
import jsettlers.common.position.ShortPoint2D;
import jsettlers.common.selectable.ESelectionType;
import jsettlers.logic.constants.Constants;
import jsettlers.logic.constants.MatchConstants;
import jsettlers.logic.movable.civilian.BearerMovable;
import jsettlers.logic.movable.civilian.BricklayerMovable;
import jsettlers.logic.movable.civilian.BuildingWorkerMovable;
import jsettlers.logic.movable.civilian.DiggerMovable;
import jsettlers.logic.movable.civilian.HealerMovable;
import jsettlers.logic.movable.cargo.CargoShipMovable;
import jsettlers.logic.movable.cargo.DonkeyMovable;
import jsettlers.logic.movable.interfaces.AbstractMovableGrid;
import jsettlers.logic.movable.interfaces.IAttackableHumanMovable;
import jsettlers.logic.movable.interfaces.IFerryMovable;
import jsettlers.logic.movable.interfaces.ILogicMovable;
import jsettlers.logic.movable.military.BowmanMovable;
import jsettlers.logic.movable.military.InfantryMovable;
import jsettlers.logic.movable.military.MageMovable;
import jsettlers.logic.movable.other.FerryMovable;
import jsettlers.logic.movable.specialist.GeologistMovable;
import jsettlers.logic.movable.specialist.PioneerMovable;
import jsettlers.logic.movable.specialist.ThiefMovable;
import jsettlers.logic.player.Player;

import java.util.LinkedList;
import java.util.Objects;
/**
 * Central Movable class of JSettlers.
 *
 * @author Andreas Eberle
 */
public abstract class Movable implements ILogicMovable, FoWTask {
	private static final long serialVersionUID = -705947810059935866L;

	private static final int SHIP_PUSH_DISTANCE = 10;

	protected final AbstractMovableGrid grid;
	private final     int                 id;
	protected final   Player              player;

	private EMovableState state = EMovableState.DOING_NOTHING;

	private final EMovableType    movableType;

	private EMaterialType  materialType  = EMaterialType.NO_MATERIAL;
	private EMovableAction movableAction = EMovableAction.NO_ACTION;
	private EDirection     direction;
	private int[] effectEnd = new int[EEffectType.values().length];

	private int   animationStartTime;
	private short animationDuration;

	public ShortPoint2D oldFowPosition = null;
	protected ShortPoint2D position;

	protected ShortPoint2D requestedTargetPosition = null;
	/**
	 * Move to type of current / last path action
	 */
	private EMoveToType requestedMoveToType = EMoveToType.DEFAULT;
	protected Path path;

	protected float         health;
	private boolean       visible           = true;
	private boolean       enableNothingToDo = true;
	private ILogicMovable pushedFrom;

	private boolean isRightstep = false;
	private int     flockDelay  = 700;

	private EMaterialType takeDropMaterial;

	private transient boolean selected    = false;
	private transient boolean soundPlayed = false;

	// the following data only for ship passengers
	protected IFerryMovable ferryToEnter = null;

	protected boolean playerControlled;

	protected Movable(AbstractMovableGrid grid, EMovableType movableType, ShortPoint2D position, Player player, Movable replace) {
		this.grid = grid;
		this.position = position;
		this.player = player;
		this.movableType = movableType;

		if(replace != null) {
			this.health = replace.getHealth()/replace.getMovableType().getHealth()*movableType.getHealth();
			this.direction = replace.getDirection();
			this.materialType = replace.materialType;
		} else {
			this.health = movableType.getHealth();
			this.direction = EDirection.VALUES[MatchConstants.random().nextInt(EDirection.NUMBER_OF_DIRECTIONS)];
		}

		playerControlled = movableType.playerControllable;

		this.id = MovableManager.requestId(this, replace);
	}

	/**
	 * Tests if this movable can receive moveTo requests and if so, directs it to go to the given position.
	 *
	 * @param targetPosition
	 * 		Desired position the movable should move to
	 */
	@Override
	public void moveTo(ShortPoint2D targetPosition, EMoveToType moveToType) {
		if (playerControlled && !alreadyWalkingToPosition(targetPosition)) {
			this.requestedTargetPosition = targetPosition;
			this.requestedMoveToType = Objects.requireNonNull(moveToType);
		}
	}

	private boolean alreadyWalkingToPosition(ShortPoint2D targetPosition) {
		return this.state == EMovableState.PATHING && this.path.getTargetPosition().equals(targetPosition);
	}

	public void leavePosition() {
		if (state != EMovableState.DOING_NOTHING || !enableNothingToDo) {
			return;
		}

		int offset = MatchConstants.random().nextInt(EDirection.NUMBER_OF_DIRECTIONS);

		for (int i = 0; i < EDirection.NUMBER_OF_DIRECTIONS; i++) {
			EDirection currDir = EDirection.VALUES[(i + offset) % EDirection.NUMBER_OF_DIRECTIONS];
			if (goInDirection(currDir, EGoInDirectionMode.GO_IF_ALLOWED_AND_FREE)) {
				break;
			} else {
				ILogicMovable movableAtPos = grid.getMovableAt(currDir.getNextTileX(position.x), currDir.getNextTileY(position.y));
				if (movableAtPos != null) {
					movableAtPos.push(this);
				}
			}
		}
	}

	protected void action() {
	}

	protected void moveToPathSet(ShortPoint2D oldPosition, ShortPoint2D oldTargetPos, ShortPoint2D targetPos, EMoveToType moveToType) {
	}

	protected void tookMaterial() {
	}

	protected boolean droppingMaterial() {
		return true;
	}

	protected boolean checkPathStepPreconditions(ShortPoint2D pathTarget, int step, EMoveToType moveToType) {
		return true;
	}

	protected void pathAborted(ShortPoint2D pathTarget) {
	}

	protected Path findWayAroundObstacle(ShortPoint2D position, Path path) {
		if (!path.hasOverNextStep()) { // if path has no position left
			return path;
		}

		EDirection direction = EDirection.getApproxDirection(position, path.getOverNextPos());

		EDirection rightDir = direction.getNeighbor(-1);
		EDirection leftDir = direction.getNeighbor(1);

		ShortPoint2D straightPos = direction.getNextHexPoint(position);
		ShortPoint2D twoStraightPos = direction.getNextHexPoint(position, 2);

		ShortPoint2D rightPos = rightDir.getNextHexPoint(position);
		ShortPoint2D rightStraightPos = direction.getNextHexPoint(rightPos);
		ShortPoint2D straightRightPos = rightDir.getNextHexPoint(straightPos);

		ShortPoint2D leftPos = leftDir.getNextHexPoint(position);
		ShortPoint2D leftStraightPos = direction.getNextHexPoint(leftPos);
		ShortPoint2D straightLeftPos = leftDir.getNextHexPoint(straightPos);

		ShortPoint2D overNextPos = path.getOverNextPos();

		LinkedList<ShortPoint2D[]> possiblePaths = new LinkedList<>();

		if (twoStraightPos.equals(overNextPos)) {
			if (grid.isValidPosition(this, rightPos.x, rightPos.y) && grid.isValidPosition(this, rightStraightPos.x, rightStraightPos.y)) {
				possiblePaths.add(new ShortPoint2D[]{
						rightPos,
						rightStraightPos});
			} else if (grid.isValidPosition(this, leftPos.x, leftPos.y) && grid.isValidPosition(this, leftStraightPos.x, leftStraightPos.y)) {
				possiblePaths.add(new ShortPoint2D[]{
						leftPos,
						leftStraightPos});
			} else {
				// TODO @Andreas Eberle maybe calculate a new path
			}
		}

		if (rightStraightPos.equals(overNextPos) && grid.isValidPosition(this, rightPos.x, rightPos.y)) {
			possiblePaths.add(new ShortPoint2D[]{rightPos});
		}
		if (leftStraightPos.equals(overNextPos) && grid.isValidPosition(this, leftPos.x, leftPos.y)) {
			possiblePaths.add(new ShortPoint2D[]{leftPos});
		}

		if ((straightRightPos.equals(overNextPos) || straightLeftPos.equals(overNextPos))
				&& grid.isValidPosition(this, straightPos.x, straightPos.y) && grid.hasNoMovableAt(straightPos.x, straightPos.y)) {
			possiblePaths.add(new ShortPoint2D[]{straightPos});

		} else {
			// TODO @Andreas Eberle maybe calculate a new path
		}

		// try to find a way without a movable or with a pushable movable.
		for (ShortPoint2D[] pathPrefix : possiblePaths) { // check if any of the paths is free of movables
			ShortPoint2D firstPosition = pathPrefix[0];
			ILogicMovable movable = grid.getMovableAt(firstPosition.x, firstPosition.y);
			if (movable == null || movable.isProbablyPushable(this)) {
				path.goToNextStep();
				return new Path(path, pathPrefix);
			}
		}

		return path;
	}

	@Override
	public int timerEvent() {
		if (state == EMovableState.DEAD) {
			return -1;
		}

		switch (state) { // ensure animation is finished, if not, reschedule
			case GOING_SINGLE_STEP:
			case PLAYING_ACTION:
			case TAKE:
			case DROP:
			case PATHING:
			case WAITING:
				int remainingAnimationTime = animationStartTime + animationDuration - MatchConstants.clock().getTime();
				if (remainingAnimationTime > 0) {
					return remainingAnimationTime;
				}
				break;
			default:
				break;
		}

		switch (state) {
			case TAKE:
			case DROP:
				if (this.movableAction != EMovableAction.RAISE_UP) {
					break;
				} // TAKE and DROP are finished if we get here and we the action is RAISE_UP, otherwise continue with second part.

			case WAITING:
			case GOING_SINGLE_STEP:
			case PLAYING_ACTION:
				setState(EMovableState.DOING_NOTHING); // the action is finished, as the time passed
				movableAction = EMovableAction.NO_ACTION;

				break;
		}

		if (requestedTargetPosition != null) {
			if (playerControlled) {
				switch (state) {
					case PATHING:
						// if we're currently pathing, stop former pathing and calculate a new path
						setState(EMovableState.DOING_NOTHING);
						this.movableAction = EMovableAction.NO_ACTION;
						this.path = null;

					case DOING_NOTHING:
						ShortPoint2D oldTargetPos = path != null ? path.getTargetPosition() : null;
						ShortPoint2D oldPos = position;
						boolean foundPath = goToPos(requestedTargetPosition); // progress is reset in here
						requestedTargetPosition = null;

						if (foundPath) {
							moveToPathSet(oldPos, oldTargetPos, path.getTargetPosition(), requestedMoveToType);
							return animationDuration; // we already follow the path and initiated the walking
						} else {
							break;
						}

					default:
						break;
				}
			} else {
				requestedTargetPosition = null;
			}
		}

		switch (state) {
			case GOING_SINGLE_STEP:
			case PLAYING_ACTION:
				setState(EMovableState.DOING_NOTHING);
				this.movableAction = EMovableAction.NO_ACTION;
				break;

			case PATHING:
				pathingAction();
				break;

			case TAKE:
				grid.takeMaterial(position, takeDropMaterial);
				setMaterial(takeDropMaterial);
				playAnimation(EMovableAction.RAISE_UP, Constants.MOVABLE_BEND_DURATION);
				tookMaterial();
				break;
			case DROP:
				if (takeDropMaterial != null && takeDropMaterial.isDroppable()) {
					boolean offerMaterial = droppingMaterial();
					grid.dropMaterial(position, takeDropMaterial, offerMaterial, false);
				}
				setMaterial(EMaterialType.NO_MATERIAL);
				playAnimation(EMovableAction.RAISE_UP, Constants.MOVABLE_BEND_DURATION);
				break;

			default:
				break;
		}

		if (state == EMovableState.DOING_NOTHING) { // if movable is currently doing nothing
			action();
			if (state == EMovableState.DOING_NOTHING) { // if movable is still doing nothing after strategy, consider doingNothingAction()
				if (this.isShip()) {
					pushShips();
				}
				if (visible && enableNothingToDo) {
					return doingNothingAction();
				} else {
					return Constants.MOVABLE_INTERRUPT_PERIOD;
				}
			}
		}

		return animationDuration;
	}

	private void pathingAction() {
		if (path == null || !path.hasNextStep() || ferryToEnter == null && !checkPathStepPreconditions(path.getTargetPosition(), path.getStep(), requestedMoveToType)) {
			// if path is finished, or canceled by strategy return from here
			setState(EMovableState.DOING_NOTHING);
			movableAction = EMovableAction.NO_ACTION;
			path = null;
			if (ferryToEnter != null) {
				enterFerry();
			}
			return;
		}

		ILogicMovable blockingMovable = grid.getMovableAt(path.nextX(), path.nextY());
		if (blockingMovable == null) { // if we can go on to the next step
			if (grid.isValidNextPathPosition(this, path.getNextPos(), path.getTargetPosition())) { // next position is valid
				goSinglePathStep();

			} else { // next position is invalid
				movableAction = EMovableAction.NO_ACTION;
				animationDuration = Constants.MOVABLE_INTERRUPT_PERIOD; // recheck shortly
				Path newPath = grid.calculatePathTo(this, path.getTargetPosition()); // try to find a new path

				if (newPath == null) { // no path found
					setState(EMovableState.DOING_NOTHING);

					pathAborted(path.getTargetPosition()); // inform strategy
					path = null;
				} else {
					this.path = newPath; // continue with new path
					if (grid.hasNoMovableAt(path.nextX(), path.nextY())) { // path is valid, but maybe blocked (leaving blocked area)
						goSinglePathStep();
					}
				}
			}

		} else { // step not possible, so try it next time
			movableAction = EMovableAction.NO_ACTION;
			boolean pushedSuccessfully = blockingMovable.push(this);
			if (!pushedSuccessfully) {
				path = findWayAroundObstacle(position, path);
				animationDuration = Constants.MOVABLE_INTERRUPT_PERIOD; // recheck shortly
			} else if (movableAction == EMovableAction.NO_ACTION) {
				animationDuration = Constants.MOVABLE_INTERRUPT_PERIOD; // recheck shortly
			} // else: push initiated our next step
		}
		if (this.isShip()) { // ships need more space
			pushShips();
		}
	}

	private void enterFerry() {
		int distanceToFerry = this.getPosition().getOnGridDistTo(ferryToEnter.getPosition());
		if (distanceToFerry <= Constants.MAX_FERRY_ENTRANCE_DISTANCE) {
			if (ferryToEnter.addPassenger((IAttackableHumanMovable)this)) {
				grid.leavePosition(this.getPosition(), this);
				setState(EMovableState.ON_FERRY);
			}
		}
		ferryToEnter = null;
	}

	private void pushShips() {
		HexGridArea.stream(position.x, position.y, 1, SHIP_PUSH_DISTANCE)
				   .filterBounds(grid.getWidth(), grid.getHeight())
				   .forEach((x, y) -> {
					   ILogicMovable blockingMovable = grid.getMovableAt(x, y);
					   if (blockingMovable != null && blockingMovable.isShip()) {
						   blockingMovable.push(this);
					   }
				   });
	}

	@Override
	public void goSinglePathStep() {
		if(hasEffect(EEffectType.FROZEN)) return;

		initGoingSingleStep(path.getNextPos());
		path.goToNextStep();
	}

	@Override
	public ShortPoint2D getPosition() {
		return this.position;
	}

	@Override
	public int getViewDistance() {
		return movableType.getViewDistance();
	}

	@Override
	public ShortPoint2D getFoWPosition() {
		return position;
	}

	@Override
	public ShortPoint2D getOldFoWPosition() {
		return oldFowPosition;
	}

	@Override
	public void setOldFoWPosition(ShortPoint2D position) {
		oldFowPosition = position;
	}

	@Override
	public ILogicMovable getPushedFrom() {
		return pushedFrom;
	}

	private void initGoingSingleStep(ShortPoint2D position) {
		direction = EDirection.getDirection(this.position, position);
		playAnimation(EMovableAction.WALKING, movableType.getStepDurationMs());
		grid.leavePosition(this.position, this);
		grid.enterPosition(position, this, false);
		this.position = position;
		isRightstep = !isRightstep;
	}

	private int doingNothingAction() {
		if (this.isShip()) {
			return flockDelay;
		}
		if (grid.isBlockedOrProtected(position.x, position.y)) {
			Path newPath = grid.searchDijkstra(this, position.x, position.y, (short) 50, ESearchType.NON_BLOCKED_OR_PROTECTED);
			if (newPath == null) {
				kill();
				return -1;
			} else {
				followPath(newPath);
				return animationDuration;
			}
		} else {
			if (flockToDecentralize()) {
				return animationDuration;
			} else {
				int turnDirection = MatchConstants.random().nextInt(-8, 8);
				if (Math.abs(turnDirection) <= 1) {
					lookInDirection(direction.getNeighbor(turnDirection));
				}
			}

			return flockDelay;
		}
	}

	/**
	 * Tries to walk the movable into a position where it has a minimum distance to others.
	 *
	 * @return true if the movable moves to flock, false if no flocking is required.
	 */
	private boolean flockToDecentralize() {
		ShortPoint2D decentVector = grid.calcDecentralizeVector(position.x, position.y);

		EDirection randomDirection = direction.getNeighbor(MatchConstants.random().nextInt(-1, 1));
		int dx = randomDirection.gridDeltaX + decentVector.x;
		int dy = randomDirection.gridDeltaY + decentVector.y;

		if (ShortPoint2D.getOnGridDist(dx, dy) >= 2) {
			flockDelay = Math.max(flockDelay - 100, 500);
			return this.goInDirection(EDirection.getApproxDirection(0, 0, dx, dy), EGoInDirectionMode.GO_IF_ALLOWED_AND_FREE);
		} else {
			flockDelay = Math.min(flockDelay + 100, 1000);
			return false;
		}
	}

	/**
	 * A call to this method indicates this movable that it shall leave it's position to free the position for another movable.
	 *
	 * @param pushingMovable
	 * 		The movable pushing at this movable. This should be the movable that want's to get the position!
	 * @return true if this movable will move out of it's way in the near future <br>
	 * false if this movable doesn't move.
	 */
	@Override
	public boolean push(ILogicMovable pushingMovable) {
		if (state == EMovableState.DEAD) {
			return false;
		}

		switch (state) {
			case DOING_NOTHING:
				if (!enableNothingToDo) { // don't go to random direction if movable shouldn't do something in DOING_NOTHING
					return false;
				}

				if (goToRandomDirection(pushingMovable)) { // try to find free direction
					return true; // if we found a free direction, go there and tell the pushing one we'll move

				} else { // if we didn't find a direction, check if it's possible to exchange positions
					if (pushingMovable.getPath() == null || !pushingMovable.getPath().hasNextStep()) {
						return false; // the other movable just pushed to get space, we can't do anything for it here.

					} else if (pushingMovable.getMovableType().isPlayerControllable()
						|| grid.isValidPosition(this, position.x, position.y)) { // exchange positions
						EDirection directionToPushing = EDirection.getApproxDirection(this.position, pushingMovable.getPosition());
						pushingMovable.goSinglePathStep(); // if no free direction found, exchange the positions of the movables
						goInDirection(directionToPushing, EGoInDirectionMode.GO_IF_ALLOWED_WAIT_TILL_FREE);
						return true;

					} else { // exchange not possible, as the location is not valid.
						return false;
					}
				}

			case PATHING:
				if (path == null || pushingMovable.getPath() == null || !pushingMovable.getPath().hasNextStep()) {
					return false; // the other movable just pushed to get space, so we can't do anything for it in this state.
				}

				if (animationStartTime + animationDuration <= MatchConstants.clock().getTime() && this.path.hasNextStep()) {
					ShortPoint2D nextPos = path.getNextPos();
					if (pushingMovable.getPosition() == nextPos) { // two movables going in opposite direction and wanting to exchange positions
						pushingMovable.goSinglePathStep();
						this.goSinglePathStep();

					} else {
						if (grid.hasNoMovableAt(nextPos.x, nextPos.y)) {
							// this movable isn't blocked, so just let it's pathingAction() handle this
						} else if (pushedFrom == null) {
							try {
								this.pushedFrom = pushingMovable;
								return grid.getMovableAt(nextPos.x, nextPos.y).push(this);
							} finally {
								this.pushedFrom = null;
							}
						} else {
							while (pushingMovable != this) {
								pushingMovable.goSinglePathStep();
								pushingMovable = pushingMovable.getPushedFrom();
							}
							this.goSinglePathStep();
						}
					}
				}
				return true;

			case GOING_SINGLE_STEP:
			case PLAYING_ACTION:
			case TAKE:
			case DROP:
			case WAITING:
				return false; // we can't do anything

			case DEBUG_STATE:
				return false;

			default:
				assert false : "got pushed in unhandled state: " + state;
				return false;
		}
	}

	@Override
	public Path getPath() {
		return path;
	}

	public boolean isProbablyPushable(ILogicMovable pushingMovable) {
		switch (state) {
			case DOING_NOTHING:
				return true;
			case PATHING:
				return path != null && pushingMovable.getPath() != null && pushingMovable.getPath().hasNextStep();
			default:
				return false;
		}
	}

	private boolean goToRandomDirection(ILogicMovable pushingMovable) {
		int offset = MatchConstants.random().nextInt(EDirection.NUMBER_OF_DIRECTIONS);
		EDirection pushedFromDir = EDirection.getApproxDirection(this.getPosition(), pushingMovable.getPosition());
		if (pushedFromDir == null) {
			return false;
		}

		for (int i = 0; i < EDirection.NUMBER_OF_DIRECTIONS; i++) {
			EDirection currDir = EDirection.VALUES[(i + offset) % EDirection.NUMBER_OF_DIRECTIONS];
			if (currDir != pushedFromDir && currDir != pushedFromDir.rotateRight(1)
				&& currDir != pushedFromDir.rotateRight(EDirection.NUMBER_OF_DIRECTIONS - 1)
				&& goInDirection(currDir, EGoInDirectionMode.GO_IF_ALLOWED_AND_FREE)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Sets the material this movable is carrying to the given one.
	 *
	 * @param materialType
	 * The material type to be set
	 * @return {@link EMaterialType} that has been set before.
	 */
	public final EMaterialType setMaterial(EMaterialType materialType) {
		assert materialType != null : "MaterialType may not be null";
		EMaterialType former = this.materialType;
		this.materialType = materialType;
		return former;
	}

	/**
	 * Lets this movable execute the given action with given duration.
	 *
	 * @param movableAction
	 * 		action to be animated.
	 * @param duration
	 * 		duration the animation should last (in seconds). // TODO change to milliseconds
	 */
	public final void playAction(EMovableAction movableAction, float duration) {
		assert state == EMovableState.DOING_NOTHING : "can't do playAction() if state isn't DOING_NOTHING. curr state: " + state;

		playAnimation(movableAction, (short) (duration * 1000));
		setState(EMovableState.PLAYING_ACTION);
		this.soundPlayed = false;
	}

	private void playAnimation(EMovableAction movableAction, short duration) {
		this.animationStartTime = MatchConstants.clock().getTime();
		this.animationDuration = duration;
		this.movableAction = movableAction;
	}

	/**
	 * @param materialToTake
	 * The material type to take
	 * @return true if the animation will be executed.
	 */
	public final boolean take(EMaterialType materialToTake, boolean takeFromMap) {
		if (!takeFromMap || grid.canTakeMaterial(position, materialToTake)) {
			this.takeDropMaterial = materialToTake;

			playAnimation(EMovableAction.BEND_DOWN, Constants.MOVABLE_BEND_DURATION);
			setState(EMovableState.TAKE);
			return true;
		} else {
			return false;
		}
	}

	public final void drop(EMaterialType materialToDrop) {
		this.takeDropMaterial = materialToDrop;

		playAnimation(EMovableAction.BEND_DOWN, Constants.MOVABLE_BEND_DURATION);
		setState(EMovableState.DROP);
	}

	/**
	 * @param sleepTime
	 * 		time to sleep in milliseconds
	 */
	public final void sleep(short sleepTime) {
		assert state == EMovableState.DOING_NOTHING : "can't do sleep() if state isn't DOING_NOTHING. curr state: " + state;

		playAnimation(EMovableAction.NO_ACTION, sleepTime);
		setState(EMovableState.WAITING);
	}

	/**
	 * Lets this movable look in the given direction.
	 *
	 * @param direction
	 * The direction to look.
	 */
	public final void lookInDirection(EDirection direction) {
		if(hasEffect(EEffectType.FROZEN)) return;

		this.direction = direction;
	}

	/**
	 * Lets this movable go to the given position.
	 *
	 * @param targetPos
	 * 		position to move to.
	 * @return true if it was possible to calculate a path to the given position<br>
	 * false if it wasn't possible to get a path.
	 */
	public final boolean goToPos(ShortPoint2D targetPos) {
		assert state == EMovableState.DOING_NOTHING : "can't do goToPos() if state isn't DOING_NOTHING. curr state: " + state;

		Path path = grid.calculatePathTo(this, targetPos);
		if (path == null) {
			if (ferryToEnter != null) {
				enterFerry();
			}
			return false;
		} else {
			followPath(path);
			return this.path != null;
		}
	}

	/**
	 * Tries to go a step in the given direction.
	 *
	 * @param direction
	 * 		direction to go
	 * @param mode
	 * 		Use the given mode to go.<br>
	 * @return true if the step can and will immediately be executed. <br>
	 * false if the target position is generally blocked or a movable occupies that position.
	 */
	public final boolean goInDirection(EDirection direction, EGoInDirectionMode mode) {
		if(hasEffect(EEffectType.FROZEN)) return false;

		ShortPoint2D targetPosition = direction.getNextHexPoint(position);

		switch (mode) {
			case GO_IF_ALLOWED_WAIT_TILL_FREE: {
				this.direction = direction;
				setState(EMovableState.PATHING);
				this.followPath(new Path(targetPosition));
				return true;
			}
			case GO_IF_ALLOWED_AND_FREE:
				if ((grid.isValidPosition(this, targetPosition.x, targetPosition.y) && grid.hasNoMovableAt(targetPosition.x, targetPosition.y))) {
					initGoingSingleStep(targetPosition);
					setState(EMovableState.GOING_SINGLE_STEP);
					return true;
				} else {
					break;
				}
			case GO_IF_FREE:
				if (grid.isFreePosition(targetPosition.x, targetPosition.y)) {
					initGoingSingleStep(targetPosition);
					setState(EMovableState.GOING_SINGLE_STEP);
					return true;
				} else {
					break;
				}
		}
		return false;
	}

	@Override
	public final void setPosition(ShortPoint2D position) {
		if (visible) {
			grid.leavePosition(this.position, this);
			grid.enterPosition(position, this, true);
		}

		this.position = position;
	}

	public final void setVisible(boolean visible) {
		if (this.visible == visible) { // nothing to change
		} else if (this.visible) { // is visible and gets invisible
			grid.leavePosition(position, this);
		} else {
			grid.enterPosition(position, this, true);
		}

		this.visible = visible;
	}

	/**
	 * @param dijkstra
	 * 		if true, dijkstra algorithm is used<br>
	 * 		if false, in area finder is used.
	 * @param centerX
	 * @param centerY
	 * @param radius
	 * @param searchType
	 * @return true if a path has been found.
	 */
	public final boolean preSearchPath(boolean dijkstra, short centerX, short centerY, short radius, ESearchType searchType) {
		assert state == EMovableState.DOING_NOTHING : "this method can only be invoked in state DOING_NOTHING";

		if (dijkstra) {
			this.path = grid.searchDijkstra(this, centerX, centerY, radius, searchType);
		} else {
			this.path = grid.searchInArea(this, centerX, centerY, radius, searchType);
		}

		return path != null;
	}

	public final ShortPoint2D followPresearchedPath() {
		assert this.path != null : "path mustn't be null to be able to followPresearchedPath()!";
		followPath(this.path);
		return path.getTargetPosition();
	}

	public final void enableNothingToDoAction(boolean enable) {
		this.enableNothingToDo = enable;
	}

	public void abortPath() {
		path = null;
	}

	public boolean isOnOwnGround() {
		return grid.getPlayerAt(position) == player;
	}

	private void followPath(Path path) {
		this.path = path;
		setState(EMovableState.PATHING);
		this.movableAction = EMovableAction.NO_ACTION;
		pathingAction();
	}

	/**
	 * Sets the state to the given one and resets the movable to a clean start of this state.
	 *
	 * @param newState
	 */
	protected void setState(EMovableState newState) {
		this.state = newState;
	}

	/**
	 * kills this movable.
	 */
	@Override
	public final void kill() {
		if(state == EMovableState.DEAD) return;

		decoupleMovable();

		if (state != EMovableState.ON_FERRY) { // position of the movable on a ferry is the position it loaded into the ferry => not correct => don't show ghost
			grid.addSelfDeletingMapObject(position, EMapObjectType.GHOST, Constants.GHOST_PLAY_DURATION, player);
		}

		killMovable();
	}

	protected void decoupleMovable() {
		if (state == EMovableState.DEAD) {
			return; // this movable already died.
		}

		grid.leavePosition(this.position, this);

		MovableManager.remove(this);
	}

	protected void killMovable() {
		this.health = -200;
		this.state = EMovableState.DEAD;
		this.selected = false;
		position = null;
	}

	public boolean isOnFerry() {
		return state == EMovableState.ON_FERRY;
	}

	/**
	 * Gets the player object of this movable.
	 *
	 * @return The player object of this movable.
	 */
	public final Player getPlayer() {
		return player;
	}

	@Override
	public final boolean isSelected() {
		return selected;
	}

	@Override
	public final boolean isWounded() {
		return health < movableType.health;
	}

	@Override
	public final void setSelected(boolean selected) {
		this.selected = selected;
	}

	@Override
	public void stopOrStartWorking(boolean stop) {
	}

	@Override
	public final ESelectionType getSelectionType() {
		return movableType.selectionType;
	}

	@Override
	public final void setSoundPlayed() {
		this.soundPlayed = true;
	}

	@Override
	public final boolean isSoundPlayed() {
		return soundPlayed;
	}

	@Override
	public final EMovableType getMovableType() {
		return movableType;
	}

	@Override
	public final EMovableAction getAction() {
		return movableAction;
	}

	@Override
	public final EDirection getDirection() {
		return direction;
	}

	@Override
	public final float getMoveProgress() {
		return ((float) (MatchConstants.clock().getTime() - animationStartTime)) / animationDuration;
	}

	@Override
	public final EMaterialType getMaterial() {
		return materialType;
	}

	@Override
	public final float getHealth() {
		return health;
	}

	@Override
	public final boolean isAlive() {
		return health > 0;
	}

	@Override
	public final boolean isRightstep() {
		return isRightstep;
	}

	@Override
	public final boolean needsPlayersGround() {
		return movableType.needsPlayersGround();
	}

	@Override
	public final void debug() {
		System.out.println("debug: " + this);
	}

	@Override
	public final int getID() {
		return id;
	}

	@Override
	public String toString() {
		return "Movable: " + id + " position: " + position + " player: " + player.playerId + " movableType: " + movableType
			+ " direction: " + direction + " material: " + materialType;
	}

	protected enum EMovableState {
		PLAYING_ACTION,
		PATHING,
		DOING_NOTHING,
		GOING_SINGLE_STEP,
		WAITING,
		ON_FERRY,

		TAKE,
		DROP,

		DEAD,

		/**
		 * This state may only be used for debugging reasons!
		 */
		DEBUG_STATE
	}

	public boolean isShip() {
		return movableType.isShip();
	}

	public final void setDirection(EDirection direction) {
		this.direction = direction;
	}

	public void addEffect(EEffectType effect) {
		effectEnd[effect.ordinal()] = effect.getTime()*1000 + MatchConstants.clock().getTime();
	}

	@Override
	public boolean hasEffect(EEffectType effect) {
		return effectEnd[effect.ordinal()] >= MatchConstants.clock().getTime();
	}


	public static Movable createMovable(EMovableType movableType, Player player, ShortPoint2D position, AbstractMovableGrid grid) {
		return createMovable(movableType, player, position, grid, null);
	}

	protected static Movable createMovable(EMovableType movableType, Player player, ShortPoint2D position, AbstractMovableGrid grid, Movable replaceMovable) {
		Movable movable = chooseMovableClass(movableType, player, position, grid, replaceMovable);

		MovableManager.add(movable);
		grid.enterPosition(position, movable, true);
		return movable;
	}

	private static Movable chooseMovableClass(EMovableType movableType, Player player, ShortPoint2D position, AbstractMovableGrid grid, Movable movable) {
		if(movable != null) movable.decoupleMovable();

		switch (movableType) {
			case SWORDSMAN_L1:
			case SWORDSMAN_L2:
			case SWORDSMAN_L3:
			case PIKEMAN_L1:
			case PIKEMAN_L2:
			case PIKEMAN_L3:
				return new InfantryMovable(grid, movableType, position, player, movable);

			case BOWMAN_L1:
			case BOWMAN_L2:
			case BOWMAN_L3:
				return new BowmanMovable(grid, movableType, position, player, movable);

			case MAGE:
				return new MageMovable(grid, position, player, movable);


			case BEARER:
				return new BearerMovable(grid, position, player, movable);
			case DIGGER:
				return new DiggerMovable(grid, position, player, movable);
			case BRICKLAYER:
				return new BricklayerMovable(grid, position, player, movable);


			case THIEF:
				return new ThiefMovable(grid, position, player, movable);
			case PIONEER:
				return new PioneerMovable(grid, position, player, movable);
			case GEOLOGIST:
				return new GeologistMovable(grid, position, player, movable);

			case CARGO_SHIP:
				return new CargoShipMovable(grid, position, player, movable);
			case FERRY:
				return new FerryMovable(grid, position, player, movable);

			case DONKEY:
				return new DonkeyMovable(grid, movableType, position, player, movable);

			case BAKER:
			case CHARCOAL_BURNER:
			case FARMER:
			case FISHERMAN:
			case FORESTER:
			case MELTER:
			case MILLER:
			case MINER:
			case PIG_FARMER:
			case DONKEY_FARMER:
			case LUMBERJACK:
			case SAWMILLER:
			case SLAUGHTERER:
			case SMITH:
			case STONECUTTER:
			case WATERWORKER:
			case WINEGROWER:
			case DOCKWORKER:
				return new BuildingWorkerMovable(grid, movableType, position, player, movable);

			case HEALER:
				return new HealerMovable(grid, position, player, movable);

			default:
				throw new AssertionError("movable type " + movableType + " is not mapped to a movable class!");
		}
	}
}
