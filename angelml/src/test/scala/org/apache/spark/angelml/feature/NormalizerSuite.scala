/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.angelml.feature

import org.apache.spark.angelml.linalg.{DenseVector, IntSparseVector, LongSparseVector, Vector, Vectors}
import org.apache.spark.angelml.util.{DefaultReadWriteTest, MLTest}
import org.apache.spark.angelml.util.TestingUtils._
import org.apache.spark.sql.{DataFrame, Row}


class NormalizerSuite extends MLTest with DefaultReadWriteTest {

  import testImplicits._

  @transient var data: Array[Vector] = _
  @transient var l1Normalized: Array[Vector] = _
  @transient var l2Normalized: Array[Vector] = _

  override def beforeAll(): Unit = {
    super.beforeAll()

    data = Array(
      Vectors.sparse(3, Seq((0, -2.0), (1, 2.3))),
      Vectors.dense(0.0, 0.0, 0.0),
      Vectors.dense(0.6, -1.1, -3.0),
      Vectors.sparse(3, Seq((1, 0.91), (2, 3.2))),
      Vectors.sparse(3, Seq((0, 5.7), (1, 0.72), (2, 2.7))),
      Vectors.sparse(3, Seq[(Int, Double)]()),
      Vectors.sparse( size= 3L, Seq[(Long, Double)]())
    )
    l1Normalized = Array(
      Vectors.sparse(3, Seq((0, -0.465116279), (1, 0.53488372))),
      Vectors.dense(0.0, 0.0, 0.0),
      Vectors.dense(0.12765957, -0.23404255, -0.63829787),
      Vectors.sparse(3, Seq((1, 0.22141119), (2, 0.7785888))),
      Vectors.dense(0.625, 0.07894737, 0.29605263),
      Vectors.sparse(3, Seq[(Int, Double)]()),
      Vectors.sparse(3L, Seq[(Long, Double)]())
    )
    l2Normalized = Array(
      Vectors.sparse(3, Seq((0, -0.65617871), (1, 0.75460552))),
      Vectors.dense(0.0, 0.0, 0.0),
      Vectors.dense(0.184549876, -0.3383414, -0.922749378),
      Vectors.sparse(3, Seq((1, 0.27352993), (2, 0.96186349))),
      Vectors.dense(0.897906166, 0.113419726, 0.42532397),
      Vectors.sparse(3, Seq[(Int, Double)]()),
      Vectors.sparse(3L, Seq[(Long, Double)]())
    )
  }

  def assertTypeOfVector(lhs: Vector, rhs: Vector): Unit = {
    assert((lhs, rhs) match {
      case (v1: DenseVector, v2: DenseVector) => true
      case (v1: IntSparseVector, v2: IntSparseVector) => true
      case (v1: LongSparseVector, v2: LongSparseVector) => true
      case _ => false
    }, "The vector type should be preserved after normalization.")
  }

  def assertValues(lhs: Vector, rhs: Vector): Unit = {
    assert(lhs ~== rhs absTol 1E-5, "The vector value is not correct after normalization.")
  }

  test("Normalization with default parameter") {
    val normalizer = new Normalizer().setInputCol("features").setOutputCol("normalized")
    val dataFrame: DataFrame = data.zip(l2Normalized).seq.toDF("features", "expected")

    testTransformer[(Vector, Vector)](dataFrame, normalizer, "features", "normalized", "expected") {
      case Row(features: Vector, normalized: Vector, expected: Vector) =>
        assertTypeOfVector(normalized, features)
        assertValues(normalized, expected)
    }
  }

  test("Normalization with setter") {
    val dataFrame: DataFrame = data.zip(l1Normalized).seq.toDF("features", "expected")
    val normalizer = new Normalizer().setInputCol("features").setOutputCol("normalized").setP(1)

    testTransformer[(Vector, Vector)](dataFrame, normalizer, "features", "normalized", "expected") {
      case Row(features: Vector, normalized: Vector, expected: Vector) =>
        assertTypeOfVector(normalized, features)
        assertValues(normalized, expected)
    }
  }

  test("read/write") {
    val t = new Normalizer()
      .setInputCol("myInputCol")
      .setOutputCol("myOutputCol")
      .setP(3.0)
    testDefaultReadWrite(t)
  }
}
