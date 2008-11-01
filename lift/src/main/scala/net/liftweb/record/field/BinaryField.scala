/*
 * Copyright 2007-2008 WorldWide Conferencing, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.liftweb.record.field

import scala.xml._
import net.liftweb.util._
import Helpers._
import net.liftweb.http.{S, FieldError}
import S._


class BinaryField[OwnerType <: Record[OwnerType]](rec: OwnerType) extends Field[Array[Byte], OwnerType] {
  override def owner = rec

  def this(rec: OwnerType, value: Array[Byte]) = {
    this(rec)
    set(value)
  }

  /**
   * Sets the field value from an Any
   */
   override def setFromAny(f: Any): Can[Array[Byte]] = Full(this.set(
     f match {
       case null => Array()
       case arr : Array[Byte] => f.asInstanceOf[Array[Byte]];
       case _ => f.toString.getBytes("UTF-8")
     }))

   override def setFromString(s: String) : Can[Array[Byte]] = {
    try{
      Full(set(s.getBytes("UTF-8")));
    } catch {
      case e: Exception => Empty
    }
  }

  override def toForm = NodeSeq.Empty

  def asXHtml: NodeSeq = NodeSeq.Empty

  override def defaultValue = Array(0)

}

import java.sql.{ResultSet, Types}
import net.liftweb.mapper.{DriverType}

/**
 * An int field holding DB related logic
 */
abstract class DBBinaryField[OwnerType <: DBRecord[OwnerType]](rec: OwnerType) extends BinaryField[OwnerType](rec) {

  def targetSQLType = Types.BINARY

  /**
   * Given the driver type, return the string required to create the column in the database
   */
  def fieldCreatorString(dbType: DriverType, colName: String): String = colName + " " + dbType.enumColumnType

  def jdbcFriendly(field : String) : Array[Byte] = value

}
