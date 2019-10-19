package com.github.notyy.typeflow.util

import com.github.notyy.typeflow.domain.{Connection, Model}
import com.github.notyy.typeflow.editor.PlantUML

object Model2PlantUML {
  //shows the plantuml diagram of active flow
  def execute(model: Model): PlantUML = {
    val definitions = model.activeFlow.get.instances.map(_.definition)
    val defBlock = definitions.map(defi => s"class ${defi.name} <<${ModelUtil.definitionType(defi)}>>").mkString(System.lineSeparator)
    val connectionBlock = model.activeFlow.get.connections.map(conn => {
      val outputType = ModelUtil.findOutputType(conn.fromInstanceId, conn.outputIndex, model).get
      val decOutputType = decorateOutputType(outputType, conn, model)
      s"${conn.fromInstanceId} --> $decOutputType${System.lineSeparator}" +
        s"$decOutputType --> ${conn.toInstanceId}"
    }).mkString(System.lineSeparator).linesIterator.distinct.mkString(System.lineSeparator) //to avoid duplicated connections from same instance to it's output
    val rs = s"""
       |@startuml
       |$defBlock
       |
       |$connectionBlock
       |@enduml
       |""".stripMargin
    PlantUML(rs)
  }

  def decorateOutputType(outputType: String, connection: Connection, model: Model): String = {
    val fromInstance = model.activeFlow.get.instances.find(_.id == connection.fromInstanceId).get
    if(fromInstance.id != fromInstance.definition.name) {
      s"${fromInstance.id}::$outputType"
    } else {
      outputType
    }
  }
}
