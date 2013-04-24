package StupidProject;

import java.util.Iterator;
import java.util.List;


import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.environment.RunState;
import repast.simphony.parameter.Parameters;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.SimUtilities;

public class Bug {

	private Grid<Object> grid;
	
	//Add a constructor for the bugs
	
	public Bug(Grid<Object> grid) {
		this.grid = grid;
	}
	
	Parameters params = RunEnvironment.getInstance().getParameters();
	
	public double initialBugSizeMean = (Double)params.getValue("initial_size_mean");
	 
	public double initialBugSizeSD = (Double)params.getValue("initial_size_sd");
	
	public double size = RandomHelper.createNormal(initialBugSizeMean, initialBugSizeSD).nextDouble(); 
	
	public double maxConsumption = (Double)params.getValue("max_food_consumption");
	
	double survivalProbability = (Double)params.getValue("survival_probability");
	
	public Bug() {
		super();
	}
	
	//Now that we have a bug class, let's give them some movement.

	public void step() {
		GridPoint pt = grid.getLocation(this);
		
		// use the GridCellNgh class to create GridCells for the surrounding neighborhood
		List<GridCell<Bug>> gridCells = new GridCellNgh<Bug>(grid, pt, Bug.class, Constants.bugMovementLimit, Constants.bugMovementLimit).getNeighborhood(true);

//Originally, we randomized the list of neighborhood cells and went with the first empty one. Now, we need to remove
//any non-empty cells and iterate through to find the one with the most food. The formulation wants this done in 2 steps;
//unless there is an easy "Empty cells only bro!" trick, I am going to use 1 do loop.
		
		SimUtilities.shuffle(gridCells, RandomHelper.getUniform()); 
		
		GridPoint pointWithMostFood = null;
		double mostFood = 0;
		Iterator cell = gridCells.iterator();
		while (cell.hasNext()) {
			GridCell bCell = (GridCell) cell.next();
			if (bCell.size() > 0) {
//				We cannot use a foreach loop to eliminate the current element - we need to use an iterator for that.
				cell.remove(); // this is the proper notation to remove from the current iterating list.
//				System.out.println(String.format("gridCells size is %s", gridCells.size()));
			}
			else {
				GridPoint cellLocation = bCell.getPoint();
				Object potentialCell = grid.getObjectAt(cellLocation.getX(),cellLocation.getY());
				if(potentialCell instanceof HabitatCell) {
					if(((HabitatCell) potentialCell).foodAvailability > mostFood || pointWithMostFood == null) {
						mostFood = ((HabitatCell) potentialCell).foodAvailability;
//						System.out.println(String.format("mostFood is %s", mostFood));
						pointWithMostFood = cellLocation;
//						System.out.println(String.format("pointWithMostFood is %s", pointWithMostFood));
					}
				}
			}
		}
		//A great unit test to run would be checking to see that we only ever have 1 bug in each space.
		grid.moveTo(this, pointWithMostFood.getX(), pointWithMostFood.getY());
	}
	
	public void grow() {		
		//First things first: Where are we?
		GridPoint foodPt = grid.getLocation(this);
		
		double availableFood = 0;
		
		//And how much food is here?
		for (Object obj : grid.getObjectsAt(foodPt.getX(),foodPt.getY())) {
			if (obj instanceof HabitatCell) {
				availableFood = ((HabitatCell) obj).foodAvailability;
				if (availableFood > maxConsumption) {
					this.size = size + maxConsumption;
					((HabitatCell) obj).foodAvailability = ((HabitatCell) obj).foodAvailability - maxConsumption;
				} else {
					this.size = size + ((HabitatCell) obj).foodAvailability;
					((HabitatCell) obj).foodAvailability = 0;
				}
			}
		}
		// After the bug grows, see how big it is. If it has reached size 10 it is time to split into new bugs.
		
//		if (this.size >= 100) { With bug splitting, this is now obsolete.
//			RunEnvironment.getInstance().endRun();
//		}
		
	}
	
	public boolean needToSpawn() {
		if (this.size >= 10) {
			return true;}
		else {
			return false;
		}
	}
	
	public boolean bugSurvival() {
		double lifeOrDeath = RandomHelper.nextDoubleFromTo(0, 1);
//		System.out.println(String.format("lifeOrDeath is is %s", lifeOrDeath));
		if (lifeOrDeath <= survivalProbability) {
			return true;}
		else {
			return false;}
	}
	
	public void die() {
		if (this instanceof Bug ) {
			RunState.getInstance().getMasterContext().remove(this);
		} 
	}
	
	// This is the only I can seem to adjust the color - have G and B set to reduce with size while red stays constant.
	// Why I can't just set red to a constant when others are variable, I have no idea. Seems stupid.
	
	public GridPoint getLocation() {
		return grid.getLocation(this);
	}
	
	public Grid<Object> getGrid() {
		return grid;
	}
	
//	public ContinuousSpace<Object> getSpace()
	
	public double getBlueAndGreen() {
		return 1 - (this.size) * 0.1;
	}
	
	public double getRed() {
		return 1;
	}
	
	public double getSize() {
		return this.size;
	}
	
	public String toString() {
        return String.format("Bug @ location %s", grid.getLocation(this));
	}
		
	public String sizeString() {
        return String.format("Bug size is %s", this.size);
	}
}
