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

import scala.beans.BeanInfo

import edu.emory.mathcs.jtransforms.dct.DoubleDCT_1D

import org.apache.spark.angelml.linalg.{Vector, Vectors}
import org.apache.spark.angelml.util.{DefaultReadWriteTest, MLTest}
import org.apache.spark.sql.Row

@BeanInfo
case class DCTTestData(vec: Vector, wantedVec: Vector)

class DCTSuite extends MLTest with DefaultReadWriteTest {

  import testImplicits._

  test("forward transform of discrete cosine matches jTransforms result") {
    val data = Vectors.dense((0 until 128).map(_ => 2D * math.random - 1D).toArray)
    val inverse = false

    testDCT(data, inverse)
  }

  test("inverse transform of discrete cosine matches jTransforms result") {
    val data = Vectors.dense((0 until 128).map(_ => 2D * math.random - 1D).toArray)
    val inverse = true

    testDCT(data, inverse)
  }

  test("read/write") {
    val t = new DCT()
      .setInputCol("myInputCol")
      .setOutputCol("myOutputCol")
      .setInverse(true)
    testDefaultReadWrite(t)
  }

  private def testDCT(data: Vector, inverse: Boolean): Unit = {
    val expectedResultBuffer = data.toArray.clone()
    if (inverse) {
      new DoubleDCT_1D(data.size.toInt).inverse(expectedResultBuffer, true)
    } else {
      new DoubleDCT_1D(data.size.toInt).forward(expectedResultBuffer, true)
    }
    val expectedResult = Vectors.dense(expectedResultBuffer)

    val dataset = Seq(DCTTestData(data, expectedResult)).toDF()

    val transformer = new DCT()
      .setInputCol("vec")
      .setOutputCol("resultVec")
      .setInverse(inverse)

    testTransformer[(Vector, Vector)](dataset, transformer, "resultVec", "wantedVec") {
      case Row(resultVec: Vector, wantedVec: Vector) =>
        assert(Vectors.sqdist(resultVec, wantedVec) < 1e-6)
    }
  }
}
