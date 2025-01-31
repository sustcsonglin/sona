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

package org.apache.spark.angelml.util

import org.apache.spark.SparkFunSuite

class IdentifiableSuite extends SparkFunSuite {

  import IdentifiableSuite.Test

  test("Identifiable") {
    val test0 = new Test("test_0")
    assert(test0.uid === "test_0")

    val test1 = new Test
    assert(test1.uid.startsWith("test_"))
  }
}

object IdentifiableSuite {

  class Test(override val uid: String) extends Identifiable {
    def this() = this(Identifiable.randomUID("test"))
  }

}
