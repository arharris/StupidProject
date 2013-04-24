package StupidProject;

import java.util.List;
//import repast.simphony.engine.environment.RunState;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.SimUtilities;

public class Predator {
	
	private Grid<Object> grid;
	
//	Predator constructor
	public Predator(Grid<Object> grid) {
		this.grid = grid;
	}
	
	public void hunt() {
		GridPoint pt = grid.getLocation(this);
		// look familiar?
		List<GridCell<Bug>> gridCells = new GridCellNgh<Bug>(grid, pt, 
				Bug.class, 
				Constants.predatorMovementLimit, Constants.predatorMovementLimit).
				getNeighborhood(true);
		
		SimUtilities.shuffle(gridCells, RandomHelper.getUniform()); 
		
		for (GridCell<Bug> gridCell : gridCells) {
			boolean occupiedCell = false;
			if (gridCell.size() > 0) {
				GridPoint deadBugPt = gridCell.getPoint();
				for (Object obj : grid.getObjectsAt(deadBugPt.getX(), deadBugPt.getY())) {
					if (obj instanceof Predator) {
						occupiedCell = true;
						return;
					}
				}
			if (occupiedCell == false) {
				grid.moveTo(this, deadBugPt.getX(), deadBugPt.getY());
			}
			gridCell.items().iterator().next().die();
			return;
			}
		} // end for loop
		GridPoint randomMove = gridCells.get(RandomHelper.nextIntFromTo(0, gridCells.size() -1 )).getPoint();
		grid.moveTo(this, randomMove.getX(), randomMove.getY());
	}
}
