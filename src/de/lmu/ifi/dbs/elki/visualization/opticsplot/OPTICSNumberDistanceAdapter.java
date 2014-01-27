package de.lmu.ifi.dbs.elki.visualization.opticsplot;

/*
 This file is part of ELKI:
 Environment for Developing KDD-Applications Supported by Index-Structures

 Copyright (C) 2014
 Ludwig-Maximilians-Universität München
 Lehr- und Forschungseinheit für Datenbanksysteme
 ELKI Development Team

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import de.lmu.ifi.dbs.elki.algorithm.clustering.optics.DoubleDistanceClusterOrderEntry;

/**
 * Adapter that will map a regular number distance to its double value.
 * 
 * @author Erich Schubert
 */
public class OPTICSNumberDistanceAdapter implements OPTICSDistanceAdapter<DoubleDistanceClusterOrderEntry> {
  /**
   * Default constructor.
   */
  public OPTICSNumberDistanceAdapter() {
    super();
  }

  @Override
  public double getDoubleForEntry(DoubleDistanceClusterOrderEntry coe) {
    return coe.getReachability();
  }

  @Override
  public boolean isInfinite(DoubleDistanceClusterOrderEntry coe) {
    return coe.getReachability() >= Double.POSITIVE_INFINITY;
  }
}