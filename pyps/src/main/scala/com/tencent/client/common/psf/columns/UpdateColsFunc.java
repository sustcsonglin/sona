/*
 * Tencent is pleased to support the open source community by making Angel available.
 *
 * Copyright (C) 2017-2018 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/Apache-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */


package com.tencent.client.common.psf.columns;


import com.tencent.angel.exception.AngelException;
import com.tencent.angel.ml.servingmath2.vector.CompIntDoubleVector;
import com.tencent.angel.ml.servingmath2.vector.CompIntFloatVector;
import com.tencent.angel.ml.servingmath2.vector.Vector;
import com.tencent.angel.matrix.psf.update.base.PartitionUpdateParam;
import com.tencent.angel.matrix.psf.update.base.UpdateFunc;
import com.tencent.angel.ps.server.data.request.UpdateOp;
import com.tencent.angel.ps.storage.matrix.ServerPartition;
import com.tencent.angel.ps.storage.vector.ServerIntDoubleRow;
import com.tencent.angel.ps.storage.vector.ServerIntFloatRow;
import com.tencent.angel.ps.storage.vector.ServerLongDoubleRow;
import com.tencent.angel.ps.storage.vector.ServerLongFloatRow;

public class UpdateColsFunc extends UpdateFunc {

  public UpdateColsFunc(UpdateColsParam param) {
    super(param);
  }

  public UpdateColsFunc() {
    super(null);
  }

  @Override public void partitionUpdate(PartitionUpdateParam partParam) {
    PartitionUpdateColsParam param = (PartitionUpdateColsParam) partParam;
    int[] rows = param.rows;
    long[] cols = param.cols;
    Vector vector = param.vector;
    UpdateOp op = param.op;

    int matId = param.getMatrixId();
    int partitionId = param.getPartKey().getPartitionId();

    ServerPartition partition = psContext.getMatrixStorageManager().getPart(matId, partitionId);

    switch (partition.getRowType()) {
      case T_DOUBLE_DENSE:
      case T_DOUBLE_SPARSE: {
        ServerIntDoubleRow[] doubles = new ServerIntDoubleRow[rows.length];
        for (int r = 0; r < rows.length; r++)
          doubles[r] = (ServerIntDoubleRow) partition.getRow(rows[r]);
        doUpdate((CompIntDoubleVector) vector, rows, cols, doubles, op);
        return;
      }
      case T_DOUBLE_SPARSE_LONGKEY: {
        ServerLongDoubleRow[] doubles = new ServerLongDoubleRow[rows.length];
        for (int r = 0; r < rows.length; r++)
          doubles[r] = (ServerLongDoubleRow) partition.getRow(rows[r]);
        doUpdate((CompIntDoubleVector) vector, rows, cols, doubles, op);
        return;
      }
      case T_FLOAT_DENSE:
      case T_FLOAT_SPARSE: {
        ServerIntFloatRow[] floats = new ServerIntFloatRow[rows.length];
        for (int r = 0; r < rows.length; r++)
          floats[r] = (ServerIntFloatRow) partition.getRow(rows[r]);
        doUpdate((CompIntFloatVector) vector, rows, cols, floats, op);
        return;
      }
      case T_FLOAT_SPARSE_LONGKEY: {
        ServerLongFloatRow[] floats = new ServerLongFloatRow[rows.length];
        for (int r = 0; r < rows.length; r++)
          floats[r] = (ServerLongFloatRow) partition.getRow(rows[r]);
        doUpdate((CompIntFloatVector) vector, rows, cols, floats, op);
        return;
      }
      default:
        throw new AngelException("Data type should be double or float!");
    }
  }

  private void doUpdate(CompIntDoubleVector vector, int[] rows, long[] cols,
    ServerIntDoubleRow[] doubles, UpdateOp op) {
    double[][] updates = new double[cols.length][];
    for (int c = 0; c < cols.length; c++)
      updates[c] = vector.getPartitions()[c].getStorage().getValues();

    for (int r = 0; r < rows.length; r++) {
      doubles[r].startWrite();
      switch (op) {
        case PLUS:
          for (int c = 0; c < cols.length; c++) {
            doubles[r].set((int) cols[c], doubles[r].get((int) cols[c]) + updates[c][r]);
          }
        case REPLACE:
          for (int c = 0; c < cols.length; c++) {
            doubles[r].set((int) cols[c], updates[c][r]);
          }
      }
      doubles[r].endWrite();
    }
  }

  private void doUpdate(CompIntDoubleVector vector, int[] rows, long[] cols,
    ServerLongDoubleRow[] doubles, UpdateOp op) {
    double[][] updates = new double[cols.length][];
    for (int c = 0; c < cols.length; c++)
      updates[c] = vector.getPartitions()[c].getStorage().getValues();

    for (int r = 0; r < rows.length; r++) {
      doubles[r].startWrite();
      switch (op) {
        case PLUS:
          for (int c = 0; c < cols.length; c++) {
            doubles[r].set(cols[c], doubles[r].get(cols[c]) + updates[c][r]);
          }
        case REPLACE:
          for (int c = 0; c < cols.length; c++) {
            doubles[r].set(cols[c], updates[c][r]);
          }
      }
      doubles[r].endWrite();
    }
  }

  private void doUpdate(CompIntFloatVector vector, int[] rows, long[] cols,
    ServerIntFloatRow[] floats, UpdateOp op) {
    float[][] updates = new float[cols.length][];
    for (int c = 0; c < cols.length; c++)
      updates[c] = vector.getPartitions()[c].getStorage().getValues();

    for (int r = 0; r < rows.length; r++) {
      floats[r].startWrite();
      switch (op) {
        case PLUS:
          for (int c = 0; c < cols.length; c++) {
            floats[r].set((int) cols[c], floats[r].get((int) cols[c]) + updates[c][r]);
          }
        case REPLACE:
          for (int c = 0; c < cols.length; c++) {
            floats[r].set((int) cols[c], updates[c][r]);
          }
      }
      floats[r].endWrite();
    }
  }

  private void doUpdate(CompIntFloatVector vector, int[] rows, long[] cols,
    ServerLongFloatRow[] floats, UpdateOp op) {
    float[][] updates = new float[cols.length][];
    for (int c = 0; c < cols.length; c++)
      updates[c] = vector.getPartitions()[c].getStorage().getValues();

    for (int r = 0; r < rows.length; r++) {
      floats[r].startWrite();
      switch (op) {
        case PLUS:
          for (int c = 0; c < cols.length; c++) {
            floats[r].set(cols[c], floats[r].get(cols[c]) + updates[c][r]);
          }
        case REPLACE:
          for (int c = 0; c < cols.length; c++) {
            floats[r].set(cols[c], updates[c][r]);
          }
      }
      floats[r].endWrite();
    }
  }
}
