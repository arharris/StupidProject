package StupidProject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CellData {
	
	private int x, y;
	private double foodRegenRate;
	
	public static GridSizeXComparator GRID_SIZE_X = new GridSizeXComparator();
	public static GridSizeYComparator GRID_SIZE_Y = new GridSizeYComparator();
	
	public static class GridSizeXComparator implements Comparator<CellData>, Serializable{
		
//		Not entirely sure why the serialization is needed - I am under the impression that it is used to
//		make communication of values easier. Going a lot by the tutorial on this one, because my attempts at
//		filereading resulted in the entire fileread class being COMPLETELY IGNORED.  Hoping that is the result 
//		of nothing calling it...
		
		private static final long serialVersionUID = 4149351630089726905L;

		@Override
		public int compare(CellData cell1, CellData cell2) {
			return cell1.getX() - cell2.getX();
		}
		
	}
	
	public static class GridSizeYComparator implements Comparator<CellData>, Serializable{
		
		 private static final long serialVersionUID = -5955739679291874417L;

		@Override
		public int compare(CellData cell1, CellData cell2) {
			return cell1.getY() - cell2.getY();
		}
		
	}
	
	public CellData(int x, int y, double foodRegenRate) {
		super();
		this.x = x;
		this.y = y;
		this.foodRegenRate = foodRegenRate;
	}

	public static List<CellData> readData() {
//		Step 1: Create the file to read
		File inputFile = new File("Stupid_Cell.Data");
		BufferedReader input = null;
		
		final ArrayList<CellData> inputData = new ArrayList<CellData>();
		
//		All the tutorials recommend using a "try" block, because exceptions are common in this kind of thing.
		try {
//			make the new reader. According to my research, BufferedReader is the most efficient for character data.
			input = new BufferedReader(new FileReader(inputFile));
			
			String infoString;
			
//			clear the heading by reading a line to nowhere
			for (int i = 0; i < 3; i++) {
				input.readLine();
			}
			
			while ((infoString = input.readLine()) != null) {
				String[] data = infoString.split("\\s+");
//				System.out.println("HEY!");

//				Now we have the parsed infoString string into "data" - iterate through the parsings to get out the juicy info.
				
				int index = 0;
				int x = Integer.parseInt(data[index++]);
				int y = Integer.parseInt(data[index++]);
				final double foodRegenRate = Double
                        .parseDouble(data[index++]);
				inputData.add(new CellData(x, y, foodRegenRate));
			}
			input.close();
		}
		catch(FileNotFoundException oops) {
			System.out.println("File" + inputFile.getAbsolutePath() + "isn't there.");
		} catch (IOException e) {
			System.out.println("you got problems");
			e.printStackTrace();
		}
			finally {
				try {
					if(input != null) input.close();}
				catch (IOException e) {
					System.out.println("you got problems");
					e.printStackTrace();
				}
			}
			return Collections.unmodifiableList(inputData);
		}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public double getFoodProduction() {
		return foodRegenRate;
	}
	
}
