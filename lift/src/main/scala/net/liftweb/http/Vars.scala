package net.liftweb.http

/*
 * Copyright 2006-2008 WorldWide Conferencing, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */

import _root_.net.liftweb.util._

/**
 * Abstract a request or a session scoped variable.
 */
abstract class AnyVar[T, MyType <: AnyVar[T, MyType]](dflt: => T) { 
  self: MyType =>
  private val name = "_lift_sv_"+getClass.getName
  protected def findFunc(name: String): Can[T]
  protected def setFunc(name: String, value: T): Unit
  protected def clearFunc(name: String): Unit

  /**
   * The current value of the variable
   */
  def is: T = findFunc(name) match {
    case Full(v) => v
    case _ => val ret = dflt
      apply(ret)
      cleanupFunc.foreach(registerCleanupFunc)
      ret
  }

  /**
  * Shadow of the 'is' method
  */
  def get: T = is

  /**
  * Shadow of the apply method
  */
  def set(what: T): Unit = apply(what)

  /**
   * Set the session variable
   *
   * @param what -- the value to set the session variable to
   */
  def apply(what: T): Unit = setFunc(name, what)

  def remove(): Unit = clearFunc(name)

  def cleanupFunc: Can[() => Unit] = Empty

  def registerCleanupFunc(in: () => Unit): Unit

  override def toString = is.toString
}

/**
 * Keep session information around without the nastiness of naming session variables
 * or the type-unsafety of casting the results.
 * SessionVars are type-safe variables that map pretty directly to
 * HttpSession attributes.  Put stuff in and they are available for the
 * life of the Session.
 *
 * SessionVar's can be used even from CometActor's as now S scope in a Cometctor is 
 * provided automatically.
 * 
 * @param dflt - the default value of the session variable
 */
abstract class SessionVar[T](dflt: => T) extends AnyVar[T, SessionVar[T]](dflt) {
  override protected def findFunc(name: String): Can[T] = S.session.flatMap(_.get(name))
  override protected def setFunc(name: String, value: T): Unit = S.session.foreach(_.set(name, value))
  override protected def clearFunc(name: String): Unit = S.session.foreach(_.unset(name))

  def registerCleanupFunc(in: () => Unit): Unit =
  S.session.foreach(_.addSessionCleanup(ignore => in()))

}

/**
 * Keep request-local information around without the nastiness of naming session variables
 * or the type-unsafety of casting the results.
 * RequestVars share their value through the scope of the current HTTP
 * request.  They have no value at the beginning of request servicing
 * and their value is discarded at the end of request processing.  They
 * are helpful to share values across many snippets.
 *
 * @param dflt - the default value of the session variable
 */
abstract class RequestVar[T](dflt: => T) extends AnyVar[T, RequestVar[T]](dflt) {
  override protected def findFunc(name: String): Can[T] = S.requestState(name)
  override protected def setFunc(name: String, value: T): Unit = S.requestState(name) = value
  override protected def clearFunc(name: String): Unit = S.requestState.clear(name)

  def registerCleanupFunc(in: () => Unit): Unit =
  S.addCleanupFunc(in)
}



object AnyVar {
  implicit def whatSessionVarIs[T](in: SessionVar[T]): T = in.is
  implicit def whatRequestVarIs[T](in: RequestVar[T]): T = in.is
}


