package de.lmu.ifi.dbs.elki.math.linearalgebra;

import java.util.BitSet;

import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.database.ids.DBID;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.utilities.DatabaseUtil;

/**
 * Centroid only using a subset of dimensions.
 * 
 * This class abstracts the mathematics of efficient and numerically stable
 * computation of projected centroids.
 * 
 * See {@link de.lmu.ifi.dbs.elki.utilities.DatabaseUtil DatabaseUtil} for
 * easier to use APIs.
 * 
 * @author Erich Schubert
 */
public class ProjectedCentroid extends Centroid {
  /**
   * Serial version
   */
  private static final long serialVersionUID = 1L;

  /**
   * The selected dimensions.
   */
  private BitSet dims;

  /**
   * Constructor for updating use.
   * 
   * @param dims Dimensions to use (indexed with 0)
   * @param dim Full dimensionality
   */
  public ProjectedCentroid(BitSet dims, int dim) {
    super(dim);
    this.dims = dims;
    assert (dims.size() <= dim);
  }

  /**
   * Add a single value with weight 1.0
   * 
   * @param val Value
   */
  @Override
  public void put(double[] val) {
    assert (val.length == elements.length);
    wsum += 1.0;
    for(int i = dims.nextSetBit(0); i >= 0; i = dims.nextSetBit(i + 1)) {
      final double delta = val[i] - elements[i];
      elements[i] += delta / wsum;
    }
  }

  /**
   * Add data with a given weight.
   * 
   * @param val data
   * @param weight weight
   */
  @Override
  public void put(double val[], double weight) {
    assert (val.length == elements.length);
    final double nwsum = weight + wsum;
    for(int i = dims.nextSetBit(0); i >= 0; i = dims.nextSetBit(i + 1)) {
      final double delta = val[i] - elements[i];
      final double rval = delta * weight / nwsum;
      elements[i] += rval;
    }
    wsum = nwsum;
  }

  /**
   * Add a single value with weight 1.0
   * 
   * @param val Value
   */
  @Override
  public void put(NumberVector<?, ?> val) {
    assert (val.getDimensionality() == elements.length);
    wsum += 1.0;
    for(int i = dims.nextSetBit(0); i >= 0; i = dims.nextSetBit(i + 1)) {
      final double delta = val.doubleValue(i + 1) - elements[i];
      elements[i] += delta / wsum;
    }
  }

  /**
   * Add data with a given weight.
   * 
   * @param val data
   * @param weight weight
   */
  @Override
  public void put(NumberVector<?, ?> val, double weight) {
    assert (val.getDimensionality() == elements.length);
    final double nwsum = weight + wsum;
    for(int i = dims.nextSetBit(0); i >= 0; i = dims.nextSetBit(i + 1)) {
      final double delta = val.doubleValue(i + 1) - elements[i];
      final double rval = delta * weight / nwsum;
      elements[i] += rval;
    }
    wsum = nwsum;
  }

  /**
   * Static Constructor from a relation.
   * 
   * @param dims Dimensions to use (indexed with 0)
   * @param relation Relation to process
   */
  public static ProjectedCentroid make(BitSet dims, Relation<? extends NumberVector<?, ?>> relation) {
    ProjectedCentroid c = new ProjectedCentroid(dims, DatabaseUtil.dimensionality(relation));
    assert (dims.size() <= DatabaseUtil.dimensionality(relation));
    for(DBID id : relation.iterDBIDs()) {
      c.put(relation.get(id));
    }
    return c;
  }

  /**
   * Static Constructor from a relation.
   * 
   * @param dims Dimensions to use (indexed with 0)
   * @param relation Relation to process
   * @param ids IDs to process
   */
  public static ProjectedCentroid make(BitSet dims, Relation<? extends NumberVector<?, ?>> relation, Iterable<DBID> ids) {
    ProjectedCentroid c = new ProjectedCentroid(dims, DatabaseUtil.dimensionality(relation));
    assert (dims.size() <= DatabaseUtil.dimensionality(relation));
    for(DBID id : ids) {
      c.put(relation.get(id));
    }
    return c;
  }
}