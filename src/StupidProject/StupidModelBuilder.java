package StupidProject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.environment.RunState;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.continuous.StrictBorders;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;
import repast.simphony.util.SimUtilities;
import repast.simphony.valueLayer.GridValueLayer;
import StupidProject.Bug;

public class StupidModelBuilder extends DefaultContext<Object> implements 
ContextBuilder<Object>, Comparator<Bug>{

	@Override
	public Context<Object> build(Context<Object> context) {
		context.setId("StupidProject");
		
		List<CellData> cellData = CellData.readData();
		
		int gridSizeX = Collections.max(cellData, CellData.GRID_SIZE_X).getX() + 1;
		int gridSizeY = Collections.max(cellData, CellData.GRID_SIZE_Y).getY() + 1;

		final ContinuousSpace<Object> space = ContinuousSpaceFactoryFinder
				.createContinuousSpaceFactory(null)
				.createContinuousSpace(
						"space",
						context,
						new RandomCartesianAdder<Object>(),
						new StrictBorders(),
						gridSizeX, gridSizeY);
		final Grid<Object> grid = GridFactoryFinder
				.createGridFactory(null)
				.createGrid(
					"grid", 
						context,
						new GridBuilderParameters<Object>(
								new repast.simphony.space.grid.StrictBorders(),
								new SimpleGridAdder<Object>(),
								true, // THIS MUST BE TRUE IF HABITAT CELL AND BUGS ARE TO CO-EXIST
								gridSizeX, gridSizeY));
		
		GridValueLayer habitatCellValues = new GridValueLayer("Habitat", 
				true, 
				new WrapAroundBorders(),
				gridSizeX, gridSizeY);
		
		context.addValueLayer(habitatCellValues);
		Parameters params = RunEnvironment.getInstance().getParameters();
		
		
		for (CellData in : cellData) {
			HabitatCell cell = new HabitatCell(in);
			context.add(cell);
			grid.moveTo(cell, in.getX(), in.getY());
			habitatCellValues.set(cell.getFood(), in.getX(), in.getY());
		}
		
		int bugCount = (Integer)params.getValue("bug_count");
		for (int i = 0; i < bugCount; ++i) {
			Bug bug = new Bug(grid);
			context.add(bug);
			if (bug.size < 0) {
				System.out.println("Neagtive bug size: making adjustment now");
				bug.size = bug.size * -1;
			}
			final NdPoint pt = space.getLocation(bug);
			grid.moveTo(bug, (int) pt.getX(), (int) pt.getY());
		}
		
		int predatorCount = (Integer)params.getValue("predator_count");
		for (int i = 0; i < predatorCount; i++) {
			Predator predator = new Predator(grid);
			context.add(predator);
			final NdPoint pt = space.getLocation(predator);
			grid.moveTo(predator, (int) pt.getX(), (int) pt.getY());
		}
		return context;
	}

	//	 For any scheduled method things to work, you need to add StupidModelBuilder as a class in context.xml. No, I have no idea why,
//	the internet found the solution and didn't know either: http://repast.10935.n7.nabble.com/A-problem-with-quot-Randomize-agent-actions-quot-td9079.html
	@ScheduledMethod(start = 1, interval = 1)
	public void bugActions() {
		
		if(RunEnvironment.getInstance().getCurrentSchedule().getTickCount() >= 1000) {RunEnvironment.getInstance().endRun();}
		
		List<Bug> bugList = getBugList();
		List<Predator> predatorList = getPredatorList();
		
		if(bugList.size() < 1){RunEnvironment.getInstance().endRun();}
		
		Collections.sort(bugList, new Comparator<Bug>() {
			@Override
			public int compare(Bug bug1, Bug bug2) {
//				If you want to reverse the order, reverse the </> signs. This has it set so larger bugs get placed first.
				return bug1.getSize() < bug2.getSize() ? 1
						: bug1.getSize() > bug2.getSize() ? -1
								: 0 ;
				}
		});
		
//		No longer random, but I'd like to keep a copy of that code. 
//		SimUtilities.shuffle(getBugList(), RandomHelper.getUniform());
		
		for (Bug bug : bugList) {
			bug.step();
		}
//		Maybe consider putting the bug splitting in another method and calling it here.
		for (Bug bug : bugList) {
			bug.grow();
			if (bug.needToSpawn() == true) {
				int newBugs = 0;
				GridPoint spawnPt = bug.getLocation();
				// use the GridCellNgh class (again) to create GridCells for the surrounding neighborhood
				@SuppressWarnings({ "unchecked", "rawtypes" })
				List<GridCell> spawnCells = new GridCellNgh(bug.getGrid(), spawnPt, Bug.class, Constants.spawnRange, Constants.spawnRange).getNeighborhood(false);
				Iterator cell = spawnCells.iterator();
				while (cell.hasNext()) {
					GridCell sCell = (GridCell) cell.next();
					if (sCell.size() > 0) {
						cell.remove();}
				} // while loop end
				SimUtilities.shuffle(spawnCells, RandomHelper.getUniform());
				if (spawnCells.size() < 5) {
					newBugs = spawnCells.size();}
				else {newBugs = 5;}
				for (int i = 0; i < newBugs; i++) {
					Bug newBug = new Bug(bug.getGrid());
					RunState.getInstance().getMasterContext().add(newBug);
					bug.getGrid().moveTo(newBug, (spawnCells.get(i)).getPoint().getX(),(spawnCells.get(i)).getPoint().getY());
					newBug.size = 0;
				}
				RunState.getInstance().getMasterContext().remove(bug);
			}
		} // end grow for loop. New bugs won't enter for loop until next tick.
		for (Bug bug : bugList) {
			if (bug.bugSurvival() == false) {
				RunState.getInstance().getMasterContext().remove(bug);}

		}
		
		for (Predator predator : predatorList) {
			predator.hunt();
		}
		
	}

//	All right, let's try this THEIR way.
	public List<Bug> getBugList() {
//		Get every instance of a Bug from the master context (AKA the ONLY context). Put it in an 
//		iterable so we can throw it in a foreach loop to define bugList
		Iterable<Bug> bugs = RunState.getInstance().getMasterContext().getObjects(Bug.class);
		
//		Create our actual List
		List<Bug> bugList = new ArrayList<Bug>();
		for (Bug bug : bugs) {
			bugList.add(bug);
		}
		 return bugList;
	}
	
	public List<Predator> getPredatorList() {
		Iterable<Predator> predators = RunState.getInstance().getMasterContext().getObjects(Predator.class);
		
//		Create our actual List
		List<Predator> predatorList = new ArrayList<Predator>();
		for (Predator predator : predators) {
			predatorList.add(predator);
		}
		 return predatorList;
	}	

@Override
public int compare(Bug bug1, Bug bug2) {
	
	return bug1.getSize() > bug2.getSize() ? 1
			: bug1.getSize() < bug2.getSize() ? -1
					: 0 ;
	}
}

// My original idea was to create the list of bug agents at the start, when they are created, and use this list later
// to set the order of action for the bugs. The tutorial even mentions this a possibility. My attempt is below. I tried adding
// bugs directly to the arraylist - that failed, I think because of shadowing. Tried adding the bugs to a local list, then
////passing that list to a setter to set the instance list. I think the code is a little more complicated than that. I
// probably shouldn't spend more time wrestling with this, but in the interest of curiosity and learning java, how the hell
// is this SUPPOSED to work? The list is set just fine, then it clears as soon as I leave the build method.
//public class StupidModelBuilder extends DefaultContext<Object> implements 
//ContextBuilder<Object> {
//	
////	This SHOULD be an instance variable, so I SHOULD be able to set it from any method calling the setter.
//	private List<Bug> bugList;
//
//	@Override
//	public Context<Object> build(Context<Object> context) {
//		context.setId("StupidProject");
//		
////		This LOCAL VARIABLE will be where we initially set the bugs.
//		List<Bug> allBugs = new ArrayList<Bug>();
//
//		final ContinuousSpace<Object> space = ContinuousSpaceFactoryFinder
//				.createContinuousSpaceFactory(null)
//				.createContinuousSpace(
//						"space",
//						context,
//						new RandomCartesianAdder<Object>(),
//						new repast.simphony.space.continuous.WrapAroundBorders(),
//						Constants.GridLength, Constants.GridWidth);
//		final Grid<Object> grid = GridFactoryFinder
//				.createGridFactory(null)
//				.createGrid(
//					"grid", 
//						context,
//						new GridBuilderParameters<Object>(
//								new repast.simphony.space.grid.WrapAroundBorders(),
//								new SimpleGridAdder<Object>(),
//								true, // THIS MUST BE TRUE IF HABITAT CELL AND BUGS ARE TO CO-EXIST
//								Constants.GridLength, Constants.GridWidth));
//		
//		GridValueLayer habitatCellValues = new GridValueLayer("Habitat", 
//				true, 
//				new WrapAroundBorders(),
//				Constants.GridLength, Constants.GridWidth);
//		
//		context.addValueLayer(habitatCellValues);
//		Parameters params = RunEnvironment.getInstance().getParameters();
//		
//		//If there is a way to "put a habitat cell in every grid location? Because that would be quicker.
//		
//		for (int i = 0; i < Constants.GridLength; i++) {
//			
//			for (int j = 0; j < Constants.GridWidth; j++) {
//				HabitatCell cell = new HabitatCell(i, j);
//				context.add(cell);
//				grid.moveTo(cell, i, j);
//				habitatCellValues.set(cell.getFood(), i, j);
//			}
//		}
//		
//		int bugCount = (Integer)params.getValue("bug_count");
//		for (int i = 0; i < bugCount; ++i) {
//			Bug bug = new Bug(grid);
//			context.add(bug);
//			allBugs.add(bug);
//			final NdPoint pt = space.getLocation(bug);
//			grid.moveTo(bug, (int) pt.getX(), (int) pt.getY());
//		}
//		setBugList(allBugs);
//		System.out.println(String.format("bugList size is %s", getBugList().size()));
//		return context;
//	}
//
//	public List<Bug> getBugList() {
//		return bugList;
//	}
//
//	public void setBugList(List<Bug> bugList) {
//		this.bugList = new ArrayList<Bug>();
//		System.out.println(String.format("bugList size is %s", this.bugList.size()));
//
//	}
//	
//	@ScheduledMethod(start = 1, interval = 1)
//	public void testing() {
//		System.out.println(String.format("Testing: bugList size is %s", getBugList().size()));
//	}
//
//	//	 For any scheduled method things to work, you need to add StupidModelBuilder as a class in context.xml. No, I have no idea why,
////	the internet found the solution and didn't know either: http://repast.10935.n7.nabble.com/A-problem-with-quot-Randomize-agent-actions-quot-td9079.html
//	@ScheduledMethod(start = 1, interval = 1)
//	public void bugActions() {
//		System.out.println("in bugActions");
//		setBugList(bugList);
//		System.out.println(String.format("bugList size is %s", getBugList().size()));
//		SimUtilities.shuffle(getBugList(), RandomHelper.getUniform());
//		for (Bug bug : bugList) {
//			System.out.println("stepin' time!");
//			bug.step();
//		}
//		for (Bug bug : bugList) {
//			System.out.println("growin' time!");
//			bug.grow();	
//		}
//	}
//}
