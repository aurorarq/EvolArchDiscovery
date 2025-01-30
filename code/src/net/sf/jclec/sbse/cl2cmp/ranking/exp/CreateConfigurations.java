package net.sf.jclec.sbse.cl2cmp.ranking.exp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Create configuration for the problem
 * generation all the possible combinations
 * of mutation weights. For experimentation
 * purposes.
 * @author Aurora Ramirez Quesada
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * */
public class CreateConfigurations {

	/**
	 * @param args
	 */
	@SuppressWarnings("resource")
	public static void main(String[] args) {

		String dir = "cfg-base/";
		String cfgBaseName = "datapro";
		String ext = ".xml";

		try {
			BufferedReader reader = new BufferedReader(new FileReader(dir+cfgBaseName+ext));
			String line = reader.readLine();
			StringBuffer bufferInit = new StringBuffer();
			StringBuffer bufferEnd = new StringBuffer();
			StringBuffer buffer;
			FileWriter writer;
			//System.out.println(line);
			// Copy first part of configuration
			while(!line.contains("<listener")){
				bufferInit.append(line + "\n");
				line=reader.readLine();
			}

			// Copy listener configuration
			while(line!=null){
				bufferEnd.append(line + "\n");
				line=reader.readLine();
			}

			// Set mutation configuration
			double probs [] = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6};
			String name = "";

			double d0, d1, d2, d3,d4;
			for(int i=0; i<probs.length; i++){
				for(int j=0; j<probs.length; j++){
					for(int k=0; k<probs.length; k++){
						for(int l=0; l<probs.length; l++){
							d0=probs[i];
							d1=probs[j];
							d2=probs[k];
							d3=probs[l];
							d4=1.0-d0-d1-d2-d3;
							d4=(double)(Math.round((d4*100)))/100;
							if(d4>=0.1){
								
								buffer = new StringBuffer();
								
								//	System.out.println("p0: " + d0 + " p1: " + d1 + " p2: " + d2 +
								//" p3: " + d3 + " p4: " + d4);
								//	nCombs++;

								buffer.append(bufferInit.toString()); // Copy the beginning of configuration

								/*<base-mutator type="net.sf.jclec.sbse.cl2cmp.CmpBaseMutator" probability-invalids="true">
								<mutator type="net.sf.jclec.sbse.cl2cmp.mutknow.AddComponentMutator" weight="0.2" random="false"/>
								<mutator type="net.sf.jclec.sbse.cl2cmp.mutknow.RemoveComponentMutator" weight="0.2" random="false" />
								<mutator type="net.sf.jclec.sbse.cl2cmp.mutknow.MergeComponentsMutator" weight="0.2" random="false"/>
							 	<mutator type="net.sf.jclec.sbse.cl2cmp.mutknow.SplitComponentMutator" weight="0.2" random="false"/>
								<mutator type="net.sf.jclec.sbse.cl2cmp.mutknow.MoveClassMutator" weight="0.2" random="true"/>
								</base-mutator>
								 */

								buffer.append("\t\t<base-mutator type=\"net.sf.jclec.sbse.cl2cmp.ranking.RankingMutator\" probability-invalids=\"true\">\n");
								buffer.append("\t\t\t<mutator type=\"net.sf.jclec.sbse.cl2cmp.mut.AddComponentMutator\" random=\"false\" weight=\"" + d0 + "\"/>\n");
								buffer.append("\t\t\t<mutator type=\"net.sf.jclec.sbse.cl2cmp.mut.RemoveComponentMutator\" random=\"false\" weight=\"" + d1 + "\"/>\n");
								buffer.append("\t\t\t<mutator type=\"net.sf.jclec.sbse.cl2cmp.mut.MergeComponentsMutator\" random=\"false\" weight=\"" + d2 + "\"/>\n");
								buffer.append("\t\t\t<mutator type=\"net.sf.jclec.sbse.cl2cmp.mut.SplitComponentMutator\" random=\"false\" weight=\"" + d3 + "\"/>\n");
								buffer.append("\t\t\t<mutator type=\"net.sf.jclec.sbse.cl2cmp.mut.MoveClassMutator\" random=\"true\" weight=\"" + d4 + "\"/>\n");
								buffer.append("\t\t</base-mutator>\n");

								buffer.append(bufferEnd.toString()); // Copy the end of configuration

								name = "cfg-mut/" + cfgBaseName +"/" + cfgBaseName + "-" + (int)(d0*100) + "-" + (int)(d1*100) + "-" 
										+ (int)(d2*100) + "-" + (int)(d3*100) + "-" + (int)(d4*100) + ext;
								
								// Write file
								writer = new FileWriter(name);
								writer.flush();
								writer.write(buffer.toString());
								writer.close();
								
								// Clear buffer
								buffer = null;
							}
						}
					}
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}