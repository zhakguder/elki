package de.lmu.ifi.dbs.elki.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.distance.Distance;
import de.lmu.ifi.dbs.elki.distance.distancefunction.DistanceFunction;
import de.lmu.ifi.dbs.elki.index.tree.spatial.SpatialDistanceFunction;
import de.lmu.ifi.dbs.elki.index.tree.spatial.SpatialEntry;
import de.lmu.ifi.dbs.elki.index.tree.spatial.SpatialIndex;
import de.lmu.ifi.dbs.elki.index.tree.spatial.SpatialNode;
import de.lmu.ifi.dbs.elki.properties.Properties;
import de.lmu.ifi.dbs.elki.utilities.QueryResult;
import de.lmu.ifi.dbs.elki.utilities.UnableToComplyException;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.ClassParameter;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.OptionID;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.ParameterException;
import de.lmu.ifi.dbs.elki.utilities.pairs.SimplePair;

/**
 * SpatialIndexDatabase is a database implementation which is supported by a
 * spatial index structure.
 * 
 * @author Elke Achtert
 * @param <O> the type of NumberVector as element of the database
 * @param <N> the type of SpatialNode stored in the index
 * @param <E> the type of SpatialEntry stored in the index
 */
public class SpatialIndexDatabase<O extends NumberVector<O, ?>, N extends SpatialNode<N, E>, E extends SpatialEntry> extends IndexDatabase<O> {

  /**
   * OptionID for {@link #INDEX_PARAM}
   */
  public static final OptionID INDEX_ID = OptionID.getOrCreateOptionID("spatialindexdb.index", "Classname of the spatial index to use " + Properties.KDD_FRAMEWORK_PROPERTIES.restrictionString(SpatialIndex.class) + ". ");

  /**
   * Parameter to specify the spatial index to use.
   * <p>
   * Key: {@code -spatialindexdb.index}
   * </p>
   */
  private final ClassParameter<SpatialIndex<O, N, E>> INDEX_PARAM = new ClassParameter<SpatialIndex<O, N, E>>(INDEX_ID, SpatialIndex.class);

  /**
   * The index structure storing the data.
   */
  protected SpatialIndex<O, N, E> index;

  public SpatialIndexDatabase() {
    super();
    addOption(INDEX_PARAM);
  }

  /**
   * Calls the super method and afterwards inserts the specified object into the
   * underlying index structure.
   */
  @Override
  public Integer insert(SimplePair<O, Associations> objectAndAssociations) throws UnableToComplyException {
    Integer id = super.insert(objectAndAssociations);
    O object = objectAndAssociations.getFirst();
    index.insert(object);
    return id;
  }

  /**
   * Calls the super method and afterwards inserts the specified objects into
   * the underlying index structure. If the option bulk load is enabled and the
   * index structure is empty, a bulk load will be performed. Otherwise the
   * objects will be inserted sequentially.
   */
  @Override
  public void insert(List<SimplePair<O, Associations>> objectsAndAssociationsList) throws UnableToComplyException {
    for(SimplePair<O, Associations> objectAndAssociations : objectsAndAssociationsList) {
      super.insert(objectAndAssociations);
    }
    index.insert(getObjects(objectsAndAssociationsList));
  }

  public <D extends Distance<D>> List<QueryResult<D>> rangeQuery(Integer id, String epsilon, DistanceFunction<O, D> distanceFunction) {
    if(distanceFunction.isInfiniteDistance(distanceFunction.valueOf(epsilon))) {
      final List<QueryResult<D>> result = new ArrayList<QueryResult<D>>();
      for(Iterator<Integer> it = iterator(); it.hasNext();) {
        Integer next = it.next();
        result.add(new QueryResult<D>(next, distanceFunction.distance(id, next)));
      }
      Collections.sort(result);
      return result;
    }

    if(!(distanceFunction instanceof SpatialDistanceFunction)) {
      // TODO: why is this emulated here, but not for other queries.
      List<QueryResult<D>> result = new ArrayList<QueryResult<D>>();
      D distance = distanceFunction.valueOf(epsilon);
      for(Iterator<Integer> it = iterator(); it.hasNext();) {
        Integer next = it.next();
        D currentDistance = distanceFunction.distance(id, next);
        if(currentDistance.compareTo(distance) <= 0) {
          result.add(new QueryResult<D>(next, currentDistance));
        }
      }
      Collections.sort(result);
      return result;
    }
    else {
      return index.rangeQuery(get(id), epsilon, (SpatialDistanceFunction<O, D>) distanceFunction);
    }
  }

  public <D extends Distance<D>> List<QueryResult<D>> kNNQueryForObject(O queryObject, int k, DistanceFunction<O, D> distanceFunction) {
    if(!(distanceFunction instanceof SpatialDistanceFunction)) {
      throw new IllegalArgumentException("Distance function must be an instance of SpatialDistanceFunction!");
    }
    return index.kNNQuery(queryObject, k, (SpatialDistanceFunction<O, D>) distanceFunction);
  }

  public <D extends Distance<D>> List<QueryResult<D>> kNNQueryForID(Integer id, int k, DistanceFunction<O, D> distanceFunction) {
    if(!(distanceFunction instanceof SpatialDistanceFunction)) {
      throw new IllegalArgumentException("Distance function must be an instance of SpatialDistanceFunction!");
    }
    return index.kNNQuery(get(id), k, (SpatialDistanceFunction<O, D>) distanceFunction);
  }

  public <D extends Distance<D>> List<List<QueryResult<D>>> bulkKNNQueryForID(List<Integer> ids, int k, DistanceFunction<O, D> distanceFunction) {
    if(!(distanceFunction instanceof SpatialDistanceFunction)) {
      throw new IllegalArgumentException("Distance function must be an instance of SpatialDistanceFunction!");
    }
    return index.bulkKNNQueryForIDs(ids, k, (SpatialDistanceFunction<O, D>) distanceFunction);
  }

  /**
   * Performs a reverse k-nearest neighbor query for the given object ID. The
   * query result is in ascending order to the distance to the query object.
   * 
   * @param id the ID of the query object
   * @param k the number of nearest neighbors to be returned
   * @param distanceFunction the distance function that computes the distances
   *        between the objects
   * @return a List of the query results
   */
  public <D extends Distance<D>> List<QueryResult<D>> reverseKNNQuery(Integer id, int k, DistanceFunction<O, D> distanceFunction) {
    if(!(distanceFunction instanceof SpatialDistanceFunction)) {
      throw new IllegalArgumentException("Distance function must be an instance of SpatialDistanceFunction!");
    }
    try {
      return index.reverseKNNQuery(get(id), k, (SpatialDistanceFunction<O, D>) distanceFunction);
    }
    catch(UnsupportedOperationException e) {
      List<QueryResult<D>> result = new ArrayList<QueryResult<D>>();
      for(Iterator<Integer> iter = iterator(); iter.hasNext();) {
        Integer candidateID = iter.next();
        List<QueryResult<D>> knns = this.kNNQueryForID(candidateID, k, distanceFunction);
        for(QueryResult<D> knn : knns) {
          if(knn.getID() == id) {
            result.add(new QueryResult<D>(candidateID, knn.getDistance()));
          }
        }
      }
      Collections.sort(result);
      return result;
    }
  }

  /**
   * Returns a string representation of this database.
   * 
   * @return a string representation of this database.
   */
  @Override
  public String toString() {
    return index.toString();
  }

  /**
   * Sets the values for the parameter bulk. If the parameters is not specified
   * the default value is set.
   */
  @Override
  public String[] setParameters(String[] args) throws ParameterException {
    String[] remainingParameters = super.setParameters(args);

    index = INDEX_PARAM.instantiateClass();

    remainingParameters = index.setParameters(remainingParameters);
    setParameters(args, remainingParameters);
    index.setDatabase(this);

    return remainingParameters;
  }

  /**
   * Returns a list of the leaf nodes of the underlying spatial index of this
   * database.
   * 
   * @return a list of the leaf nodes of the underlying spatial index of this
   *         database
   */
  public List<E> getLeaves() {
    return index.getLeaves();
  }

  /**
   * Returns the id of the root of the underlying index.
   * 
   * @return the id of the root of the underlying index
   */
  public E getRootEntry() {
    return index.getRootEntry();
  }

  /**
   * Returns the index of this database.
   * 
   * @return the index of this database
   */
  @Override
  public SpatialIndex<O, N, E> getIndex() {
    return index;
  }

  /**
   * Returns a short description of the database. (Such as: efficiency in space
   * and time, index structure...)
   * 
   * @return a description of the database
   */
  @Override
  public String parameterDescription() {
    StringBuffer description = new StringBuffer();
    description.append(this.getClass().getName());
    description.append(" holds all the data in a ");
    description.append(index.getClass().getName()).append(" index structure.\n");
    description.append(optionHandler.usage("", false));
    return description.toString();
  }
}