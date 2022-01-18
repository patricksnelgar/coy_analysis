import java.io.*;
import java.lang.String;
import java.util.*;

public class CoyAnalysis {

		private BufferedReader file_reader;
		private BufferedWriter file_writer;
		private int number_of_canes = 0;
		private int number_of_shoots = 0;
		private int laterals = 0;
		private List<String[]> cane_data = new ArrayList<>();
		private List<String> errors = new ArrayList<>();
		private int shoot_bins[] = new int[11];
		private String output_file;

		CoyAnalysis(){
		}

		public void loadFile(String file_to_load){
			if(!fileExists(file_to_load)){
				System.out.println("Could not load file <" + file_to_load + ">");
			}

			loadDataFromFile();
		}

		private boolean fileExists(String file_name){
			try {
				file_reader = new BufferedReader(new FileReader(file_name));
			} catch(FileNotFoundException e){
				return false;
			}

			return true;
		}

		private void loadDataFromFile(){

			String s;
			try {
				while((s = file_reader.readLine()) != null) {
					// Check to see if the line starts with a number
					if(s.matches("^[0-9].*")){
						String[] split = s.split(",");
						//System.out.println(split.length);

						// Length will be 3 for old style recording where coy string starts with ','
						if(split.length == 3){
							
							int id = Integer.parseInt(split[0]);
							// split[1] == "
							String cane[] = split[2].split("L");							
							if(cane.length == 2){
								//System.out.println(Arrays.toString(cane));
								String laterals = cane[1].replace("\"", "");
								String[] single_cane = {"" + id, cane[0], laterals};
								cane_data.add(single_cane);
							} else {
								errors.add(s);
							}						
							
						} else if(split.length == 2){
							int id = Integer.parseInt(split[0]);
							// split[1] == "
							String cane[] = split[1].split("L");							
							if(cane.length == 2){
								//System.out.println(Arrays.toString(cane));
								String laterals = cane[1].replace("\"", "");
								String[] single_cane = {"" + id, cane[0], laterals};
								cane_data.add(single_cane);
							} else {
								errors.add(s);
							}		
						} else {
							errors.add(s);
						}
					}
				}

				file_reader.close();
			} catch (Exception e){
				System.out.println("Error loading data: " + e.getLocalizedMessage());
			}

			//System.out.println("Data length: " + cane_data.size());
		}

		public void printErrors(){
			if(errors.size() > 0){
				System.out.println("Printing errors.");
				for (String s : errors) {
					System.out.println(s);
				}
			} else {
				System.out.println("No errors in the data.");
			}
		}

		public void setOutputFileName(String fn){
			output_file = fn;
		}

		private void calculateOverallCounts(){
			for (String[] s : cane_data) {
				//System.out.println("parsing: " + s[1]);
				parseCaneData(s[1]);
			}			
		}

		public void printOverallCounts(){
			try {
				if(number_of_canes == 0) calculateOverallCounts();
				BufferedWriter file_writer = new BufferedWriter(new FileWriter(output_file, true));
				file_writer.write("bin,count\n");
				for(int i = 0; i < shoot_bins.length -1; i++){
					file_writer.write(i+","+shoot_bins[i] + "\n");
				}

				file_writer.write(".,"+ shoot_bins[shoot_bins.length-1] + "\n");
				file_writer.flush();
				file_writer.close();
			} catch (Exception e) {
				System.out.println("Error writing to file <" + output_file + "> :" + e.getLocalizedMessage());
			}
		}	
		
		private void parseCaneData(String data){
			number_of_canes++;
			int number_of_buds = data.length();
			for(int i = 0; i < number_of_buds; i++){				
				try {
					int flowers = Integer.parseInt(data.substring(i, i+1));
					shoot_bins[flowers]++;
					if(flowers != 0)
						number_of_shoots++;

				} catch (Exception e) {
					shoot_bins[10]++;
				}
			}
		}

		private int sumBins(int[] bin) {
			int sum = 0;
			// Ignore the last bin as they are equal to 0's
			for(int i = 1; i < bin.length - 2; i++){
				sum += bin[i] * i;
			}
			//System.out.println(sum);
			return sum;
		}

		public void outputStats(){
			try{
				if(number_of_canes == 0) calculateOverallCounts();
				file_writer = new BufferedWriter(new FileWriter(output_file, true));
				file_writer.write("total_canes," + number_of_canes + "\n");
				file_writer.write("number_of_shoots," + number_of_shoots + "\n");
				//file_writer.write("average_flowers_per_shoot," + String.format("%.2f", (float)sumBins() / (float)number_of_shoots) + " \n");
				//System.out.println(String.format("%.2f", (float)sumBins(shoot_bins) / (float)number_of_shoots));
				file_writer.close();
			} catch (Exception e) {
				System.out.println("Error opening file for writing <" + output_file + ": " + e.getLocalizedMessage());
			}
		}

		private String getThinningHeaders(short s, short e){
			String headers = "";
			for(int i = s; i < e; i++){
				headers += i + ",";
			}
			return headers += e +"";
		}

		public void printThinningForCane(String arg){

			String[] split = arg.split("-");
			
			if(split.length != 2) {
				System.out.println("Incorrect thinning format: " + arg);
				System.out.println("Required format: <start>-<end>.");
				return;
			}

			short start = (short)Integer.parseInt(split[0]);
			short end = (short)Integer.parseInt(split[1]);

			try {
				file_writer = new BufferedWriter(new FileWriter(output_file, true));
				file_writer.write("cane_id,total," + getThinningHeaders(start, end) + ",#shoots\n");
			} catch (Exception e) {
				System.out.println("Could not write to file: " + e.getLocalizedMessage());
			}

			for(int i = 0; i < cane_data.size(); i++){
				int[] cane_stats = getCaneSummary(cane_data.get(i)[1]);
				short total = (short)sumBins(cane_stats);
				
				if(file_writer != null){
					String output = cane_data.get(i)[0] + "," + total + ",";
					for(int j = start; j < end; j++){
						output += (total - (short)flowersToRemoveForMax(j, cane_stats)) + ",";
					}

					output += (total - flowersToRemoveForMax((int)end, cane_stats)) + "";
					try{
						file_writer.write(output + "," + cane_stats[11] + "\n");
					} catch (Exception e) {
						
					}
				}
				//System.out.println("Cane data: " + cane_data.get(i)[1]);
				//System.out.println("total flowers: " + total + "to remove for target 2: " + flowersToRemoveForMax(2, cane_stats));
			}



			if(file_writer != null){
				try{
					file_writer.close();
				}catch (Exception e) {
					
				}
			}
		}

		private int[] getCaneSummary(String cane){
			int[] bins = new int[12];
			short num_buds = (short)cane.length();
			short num_shoots = 0;
			//System.out.println("Analyzing: " + cane + " with " + (short)cane.length() + "buds");
			for(int j = 0; j < num_buds; j++){
				try {
					int flowers = Integer.parseInt(cane.substring(j, j+1));
					bins[flowers]++;
					if(flowers != 0)
						num_shoots++;
				} catch (Exception e) {
					bins[10]++;
				}
			}
			bins[11] = num_shoots;
			//System.out.println("shoots: " + num_shoots);
			return bins;
		}

		public void printThinningForVine(String arg){
			String[] split = arg.split("-");
			
			if(split.length != 2) {
				System.out.println("\nIncorrect thinning format: " + arg);
				System.out.println("Required format: <start>-<end>.");
				return;
			}

			short start = (short)Integer.parseInt(split[0]);
			short end = (short)Integer.parseInt(split[1]);

			try {
				file_writer = new BufferedWriter(new FileWriter(output_file, true));
				file_writer.write("vine_id,total," + getThinningHeaders(start, end) + ",#shoots\n");
			} catch (Exception e) {
				System.out.println("\nCould not write to file: " + e.getLocalizedMessage());
			}

			

			for(int i = 0; i < cane_data.size(); ){
				String cane_id = cane_data.get(i)[0];
				String vine_id = cane_id.substring(0, cane_id.length() -1);
				List<String> vine_data = new ArrayList<>();

				int max =  ((i+4) >= cane_data.size()) ? (cane_data.size() - i) : 4;
				//System.out.println("max is: " + max);
				vine_data.add(cane_data.get(i)[1]);
				int stop = 4;
				for(int j = 1; j <= 3; j++){
					String next_id = cane_data.get(i+j)[0];
					//System.out.println("Testing " + vine_id + " ?= " + next_id.substring(0, next_id.length() -1) + " : " + vine_id.equals(next_id.substring(0, next_id.length() -1)));
					if(vine_id.equals(next_id.substring(0, next_id.length() -1))){
						vine_data.add(cane_data.get(i+j)[1]);

					} else {
						//System.out.println("\nId " + vine_id + " !=  " + next_id.substring(0, next_id.length() -1));
						System.out.println("\nVine " + vine_id + " only has " + vine_data.size() + " records");
						stop = j;
					}
				}

				i += stop;

				int[] vine_bins = getVineSummary(vine_data);
				short total = (short)sumBins(vine_bins);
				String output = vine_id + "," + total +",";
				for(int j = start; j < end; j++) {
					output += (total - (short)flowersToRemoveForMax(j, vine_bins)) + ",";
				}

				output += (total - (short)flowersToRemoveForMax((int)end, vine_bins)) + "";
				try{
					file_writer.write(output + "," + vine_bins[11] + "\n");
				} catch (Exception e) {
					
				}

			}

			if(file_writer != null){
				try{
					file_writer.close();
				}catch (Exception e) {
					
				}
			}
		}

		private int[] getVineSummary(List<String> data){
			int[] tally = new int[12];
			for(int i = 0; i < data.size(); i++){
				tally = combineBinCounts(tally, getCaneSummary(data.get(i)));
			}

			return tally;
		}

		private int[] combineBinCounts(int[] a, int[] b){
			if (a.length != b.length){
				System.out.println("Cannot combine bins of different lengths: " + a.length + " != " + b.length);
				return null;
			}

			for(int i = 0; i < a.length; i++){
				a[i] += b[i];
			}

			return a;
		}

		public int flowersToRemoveForMax(int target, int[] bins){
			int flowers_to_remove = 0;
			if(target < 1 || target > 9) {
				System.out.println("Thinning target of #" + target + " is not valid");
				return 0;
			}


			// Want to start the thinning OVER the max
			for(int i = target; i < bins.length -2; i++){
				flowers_to_remove += (bins[i] * (i - target));
			}

			return flowers_to_remove;

			//System.out.println(flowers_to_remove + " flowers need to be thinned to achieve max " + target + " flowers per floral shoot");
			//System.out.println("Average flowers per shoot after thinning: " + String.format("%.2f", (double)(sumBins() - flowers_to_remove) / (double) number_of_shoots));
		}		
	}