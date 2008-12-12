package net.liftweb.util

/*
 * Copyright 2006-2008 WorldWide Conferencing, LLC
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

import _root_.java.io.{InputStream, ByteArrayOutputStream, ByteArrayInputStream, Reader, File, FileInputStream, BufferedReader, InputStreamReader}
import _root_.scala.collection.mutable.{HashSet, ListBuffer}

object IoHelpers extends IoHelpers

trait IoHelpers {
  def exec(cmds: String*): Can[String] = {
    try {
      class ReadItAll(in: InputStream, done: String => Unit) extends Runnable {
        def run {
          val br = new BufferedReader(new InputStreamReader(in))
          val lines = new ListBuffer[String]
          var line = ""
          while (line != null) {
            line = br.readLine
            if (line != null) lines += line
          }
          done(lines.mkString("\n"))
        }
      }

      var stdOut = ""
      var stdErr = ""
      val proc = Runtime.getRuntime.exec(cmds.toArray)
      val t1 = new Thread(new ReadItAll(proc.getInputStream, stdOut = _))
      t1.start
      val t2 = new Thread(new ReadItAll(proc.getErrorStream, stdErr = _))
      val res = proc.waitFor
      t1.join
      t2.join
      if (res == 0) Full(stdOut)
      else Failure(stdErr, Empty, Empty)
    } catch {
      case e => Failure(e.getMessage, Full(e), Empty)
    }
  }
    def readWholeThing(in: Reader): String = {
    val bos = new StringBuilder
    val ba = new Array[Char](4096)

    def readOnce {
      val len = in.read(ba)
      if (len < 0) return
      if (len > 0) bos.append(ba, 0, len)
      readOnce
    }

    readOnce

    bos.toString
  }

  def readWholeFile(file: File): Array[Byte] = readWholeStream(new FileInputStream(file))

  def readWholeStream(in: InputStream): Array[Byte] = {
    val bos = new ByteArrayOutputStream
    val ba = new Array[Byte](4096)

    def readOnce {
      val len = in.read(ba)
      if (len > 0) bos.write(ba, 0, len)
      if (len >= 0) readOnce
    }

    readOnce

    bos.toByteArray
  }
}
