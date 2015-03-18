/*
 *  Copyright 2014 TWO SIGMA OPEN SOURCE, LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

 package com.twosigma.beaker.clojure.util;

import java.util.concurrent.ArrayBlockingQueue;
import clojure.tools.nsc.Settings;
import clojure.tools.nsc.interpreter.Results.Error;
import clojure.tools.nsc.interpreter.Results.Success;
import clojure.tools._
    import nsc.interpreter.{Completion, CompletionAware, IMain,
JLineCompletion, JLineDelimiter, JList, Parsed, Results, IR}
    import Completion.{Candidates, ClojureCompleter}

import jline.console.completer.{Completer, ArgumentCompleter}
import java.util.ArrayList;
import com.twosigma.beaker.jvm.`object`.SimpleEvaluationObject;
import clojure.collection.JavaConversions._

case class ResetState(val state: String);

class ClojureEvaluatorGlue(val cl: ClassLoader, var cp: String) {
  val settings = {
    val s = new Settings();
    s.bootclasspath.value = cp;
    s.embeddedDefaults(cl);
    s;
  }
  private val baos = new java.io.ByteArrayOutputStream();

  private def clojureToJline(tc: ClojureCompleter): Completer = new Completer {
    def complete(_buf: String, cursor: Int, candidates: JList[CharSequence]): Int = {
      val buf   = if (_buf == null) "" else _buf
      val Candidates(newCursor, newCandidates) = tc.complete(buf, cursor)
      newCandidates foreach (candidates add _)
      newCursor
    }
  }

  var interpreter = {
    var i = new IMain(settings, new java.io.PrintWriter(baos));
    i.setContextClassLoader();
    i;
  }

  val completer = {
    var c = new JLineCompletion(interpreter);
    var b: ArgumentCompleter =new ArgumentCompleter(new JLineDelimiter, clojureToJline(c.completer));
    b.setStrict(false);
    b;
  }

  private def getOut: Any = {
    val lvo = interpreter.valueOfTerm(interpreter.mostRecentVar);
    lvo match {
      case None => baos.toString();
      case Some(ResetState("reset")) => baos.toString();
      case Some(value) => value;
    }
  }

  def addImport(name : String): Boolean = {
    baos.reset();
      try {
      interpreter.interpret("import "+name) match {
        case Success => true;
        case _ => false;
      }
    } catch {
      case ex: Throwable => false;
    }
  }

  def evaluate2(code: String): String = {
    baos.reset();
    try {
      interpreter.interpret(code) match {
        case Success => "";
        case _ => baos.toString();
      }
    } catch {
      case ex: Throwable => ex.toString();
    }
  }

  def evaluate(out: SimpleEvaluationObject, code: String) {
    baos.reset();
    out.started();
    try {
      interpreter.interpret(code) match {
        case Success => out.finished(getOut.asInstanceOf[java.lang.Object]);
        case _ => out.error(baos.toString());
      }
    } catch {
      case ex: Throwable => out.error(ex);
    }
  }

  def autocomplete(buf: String, len : Integer): ArrayList[CharSequence] = {
    val maybes = new java.util.ArrayList[CharSequence];
    completer.complete(buf,  len, maybes);
    maybes;
  }
}
