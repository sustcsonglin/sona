package org.apache.spark.angelml.evaluation

class RegressionMetrics extends Serializable {
  import RegressionMetrics.RegressionPredictedResult

  var count: Long = 0
  var currLabelSum: Double = 0.0
  var currLabelSum2: Double = 0.0
  var currPredSum: Double = 0.0
  var currPredSum2: Double = 0.0
  var currPredLabelSum: Double = 0.0
  var currPredLabelDiffAbs: Double = 0.0

  def add(pres: RegressionPredictedResult): this.type = {
    val predValidate = !(pres.prediction.isNaN || pres.prediction.isInfinity)
    val labelValidate = !(pres.label.isNaN || pres.label.isInfinity)
    if (labelValidate && predValidate) {
      count += 1
      currLabelSum += pres.label
      currLabelSum2 += pres.label * pres.label
      currPredSum += pres.prediction
      currPredSum2 += pres.prediction * pres.prediction
      currPredLabelSum += pres.prediction * pres.label
      currPredLabelDiffAbs += Math.abs(pres.prediction - pres.label)
    }

    this
  }

  def merge(other: RegressionMetrics): this.type = {
    count += other.count
    currLabelSum += other.currLabelSum
    currLabelSum2 += other.currLabelSum2
    currPredSum += other.currPredSum
    currPredSum2 += other.currPredSum2
    currPredLabelSum += other.currPredLabelSum
    currPredLabelDiffAbs += other.currPredLabelDiffAbs

    this
  }

  def clear(): this.type = {
    count = 0
    currLabelSum = 0.0
    currLabelSum2 = 0.0
    currPredSum = 0.0
    currPredSum2 = 0.0
    currPredLabelSum = 0.0
    currPredLabelDiffAbs = 0.0

    this
  }

}


object RegressionMetrics {

  case class RegressionPredictedResult(prediction: Double, label: Double) extends Serializable

}