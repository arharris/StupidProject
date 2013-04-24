package StupidProject;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.util.ContextUtils;
import repast.simphony.valueLayer.GridValueLayer;

public class HabitatCell {
	
	private int x, y;
	
	private double foodProduction; 
	
	//Add a constructor for the habitat cells
	
	public HabitatCell(CellData cellData) {
		this.x = cellData.getX();
		this.y = cellData.getY();
		this.foodProduction = cellData.getFoodProduction();
	}
	
	Parameters params = RunEnvironment.getInstance().getParameters();
	
	public double foodAvailability = 0.0;
		
	public double getFood() {
		return foodAvailability;
	}

	// http://repast.sourceforge.net/docs/api/repastjava/repast/simphony/engine/schedule/ScheduledMethod.html
	//Inevitably, you will come back to this model to see what all @ScheduledMethod can do. Go to that link.
	@ScheduledMethod(start = 1, interval = 1, priority = 1)
	public void produceFood() {
		
		this.foodAvailability = foodAvailability + foodProduction;
		
		GridValueLayer valueLayer = (GridValueLayer) ContextUtils
				.getContext(this).getValueLayer("Habitat");
		
		valueLayer.set(getFood(), x, y);
	}
	
	public String foodString() {
        return String.format("Food availability is %s", this.foodAvailability);
	}
}
