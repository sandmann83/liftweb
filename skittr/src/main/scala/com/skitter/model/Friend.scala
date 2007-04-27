package com.skitter.model

/*                                                *\
 (c) 2007 WorldWide Conferencing, LLC
 Distributed under an Apache License
 http://www.apache.org/licenses/LICENSE-2.0
 \*                                                 */

import net.liftweb.mapper._
import DB._
import java.sql.Connection

/**
 * The singleton that has methods for accessing the database
 */
object Friend extends Friend with KeyedMetaMapper[long, Friend] {
  override def dbTableName = "friends" // define the DB table name
}

/**
 * An O-R mapped "User" class that includes first name, last name, password and we add a "Personal Essay" to it
 */
class Friend extends KeyedMapper[long, Friend] {
  def getSingleton = Friend // what's the "meta" server
  def primaryKeyField = id
  
  val id = new MappedLongIndex(this)

  val owner = new MappedLongForeignKey(this, User)
  val friend = new MappedLongForeignKey(this, User)
  val when = new MappedLong(this) {
    override def dbColumnName = "when_c"
    override def defaultValue = System.currentTimeMillis
  }
}