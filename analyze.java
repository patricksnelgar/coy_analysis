import java.util.*;
import java.io.*;

public class analyze {

	BufferedReader file_reader;

	public static void main(String[] args) {
		System.out.println();

		// Requires minimum input / output file names
		if(args.length < 2) {
			printUsage();
			return;
		}

		CoyAnalysis coy_analysis = new CoyAnalysis();
		coy_analysis.loadFile(args[args.length - 2]); // Input file should always be the second to last argument
		coy_analysis.printErrors();
		coy_analysis.setOutputFileName(args[args.length -1]);


		// Execute the options if any present
		for (int i = 0; i < args.length - 2; i++) {
			//System.out.println(args[i]);
			switch (args[i]) {
				case "-o":
					coy_analysis.printOverallCounts();
					break;
				case "-s":
					coy_analysis.outputStats();
					break;
				case "-n":
					break;
				case "-t":
					i++;
					String cane_arg = args[i];
					coy_analysis.printThinningForCane(cane_arg);
					break;
				case "-v":
					i++;
					String vine_arg = args[i];
					coy_analysis.printThinningForVine(vine_arg);
					break;
				default:
					break;
			}
		}



	}

	private static void printUsage(){
		System.out.println("Usage: java analysis [options] input_file output_file");
		System.out.println("  options:");
		System.out.println("\t-o: Output total shoot sums.");
		System.out.println("\t-s: Calculate # of canes / shoots and average flowers per shoot.");
		//System.out.println("\t-n: Compute normalizedd graph.");
		System.out.println("\t-t: Output flowers/cane after thinning to specific value. [min-max]");
		System.out.println("\t-v: Similar to '-t' but outputs per vine (IDs are vine#cane#).");
		System.out.println("\nExample: java analysis -s -t 3-4 coyvortex.csv vortext_analysis.txt");
		System.out.println("Note: the data needs to be in the format: ID,COY");
	}
}