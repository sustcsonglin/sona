package com.tencent.client.sona.ml

import java.util

import com.tencent.angel.client.AngelPSClient
import com.tencent.angel.conf.AngelConf
import com.tencent.angel.matrix.MatrixContext
import com.tencent.angel.ml.core.conf.{MLCoreConf, SharedConf}
import com.tencent.angel.ml.core.network.EnvContext
import com.tencent.angel.ml.core.variable.{CILSImpl, PSVariable}
import com.tencent.angel.model.{MatrixLoadContext, MatrixSaveContext, ModelLoadContext, ModelSaveContext}
import com.tencent.angel.sona.core.SparkEnvContext


// CILS: Create, Initial, Load, Save
class SparkCILSImpl(val conf: SharedConf) extends CILSImpl{

  override def doCreate[T](mCtx: MatrixContext, envCtx: EnvContext[T]): Unit = {
    envCtx match {
      case SparkEnvContext(client: AngelPSClient) if client != null =>
        val mcList = new util.ArrayList[MatrixContext]()
        mcList.add(mCtx)
        client.createMatrices(mcList)
      case _ =>
    }
  }

  override def doInit[T](mCtx: MatrixContext, envCtx: EnvContext[T], taskFlag: Int): Unit = {

  }

  override def doLoad[T](mCtx: MatrixContext, envCtx: EnvContext[T], path: String): Unit = {
    envCtx match {
      case SparkEnvContext(client: AngelPSClient) if client != null =>
        val loadContext = new ModelLoadContext(path)
        loadContext.addMatrix(new MatrixLoadContext(mCtx.getName))
        client.load(loadContext)
      case _ =>
    }
  }

  override def doSave[T](mCtx: MatrixContext, indices: Array[Int], envCtx: EnvContext[T], path: String): Unit = {
    envCtx match {
      case SparkEnvContext(client: AngelPSClient) if client != null =>
        val saveContext: ModelSaveContext = new ModelSaveContext(path)
        val msc: MatrixSaveContext = new MatrixSaveContext(mCtx.getName,
          mCtx.getAttributes.get(MLCoreConf.ML_MATRIX_OUTPUT_FORMAT))
        msc.addIndices(indices)
        saveContext.addMatrix(msc)

        if (PSVariable.isFirstSave.getAndSet(false)) {
          val deleteExistsFile = conf.getBoolean(AngelConf.ANGEL_JOB_OUTPUT_PATH_DELETEONEXIST,
            AngelConf.DEFAULT_ANGEL_JOB_OUTPUT_PATH_DELETEONEXIST)
          client.save(saveContext, deleteExistsFile)
        } else {
          client.save(saveContext, false)
        }
      case _ =>
    }
  }
}
