package net.sf.jclec.sbse.cl2cmp.ranking.metrics;


/**
 * Enumeration of available metrics in the
 * 'Classes to Components' (CL2CMP) problem.
 * The values match with the name of classes that 
 * extend CmpMetric, allowing to instantiate them.
 * 
 * @author Aurora Ramirez Quesada
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 2.0
 * 
 * <p>History:
 * <ul>
 * 	<li>1.0: Creation (July 2013)
 * 	<li>2.0: Rename metrics (September 2013)
 * </ul>
 * */

public enum Metrics {
	ICD,
	ERP,
	GCR,
	CV,
	MaxDifSize
}
