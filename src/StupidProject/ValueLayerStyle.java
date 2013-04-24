package StupidProject;

import java.awt.Color;

import repast.simphony.valueLayer.ValueLayer;
import repast.simphony.visualizationOGL2D.ValueLayerStyleOGL;

public class ValueLayerStyle implements ValueLayerStyleOGL{

	 //Using the value layer isn't necessary, but the model runs much more quickly not having to 
//	generate 10,000 extra agents. Unfortunately, documentation doesn't much mention what these
//	methods DO, so I am borrowing heavily from the StupidModel example and trying to figure out each
//	code segment.
	
//	Guessing this pulls up our value layer - we specify this file from the repast window.
	
 private ValueLayer valueLayer = null;
 
//	Specify that we are talking about the value layer for whatever calls this method
 @Override
 public void init(ValueLayer valuelayer) {
	 this.valueLayer = valuelayer;
}
 
// 	Establish cell size
 @Override
 public float getCellSize() {
	return 15.0f;
}

//	set those colors! Unfortunately, getColor seems to be the only way in this imported package to change colors,
// 	and it seems very rigid. So... Not to original. Other ways to do this? 
 
@Override
public Color getColor(double... coordinates) {
//	Grab the available food from the spot
	double food = valueLayer.get(coordinates);
	int intensity = (int) Math.min(food*255,255);
	return new Color(0,intensity,0);
}
	
}
