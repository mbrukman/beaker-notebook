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

import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

/**
 * Class containing static utility methods for Java->Clojure interop
 *
 * @author Mike
 *
 * https://github.com/mikera/clojure-utils/blob/master/src/main/java/mikera/cljutils/Clojure.java
 *
 */
public class Clojure {
  public static final Var REQUIRE=var("clojure.core", "require");
  public static final Var META=var("clojure.core", "meta");
  public static final Var EVAL=var("clojure.core", "eval");
  public static final Var READ_STRING=var("clojure.core", "read-string");

  public static Object require(String nsName) {
    return REQUIRE.invoke(Symbol.intern(nsName));
  }

  public static Object readString(String s) {
    return READ_STRING.invoke(s);
  }

  public static Var var(String varName) {
    return var("clojure.core", varName);
  }

  public static Var var(String nsName, String varName) {
    return RT.var(nsName, varName);
  }

  public static Object eval(String string) {
    return EVAL.invoke(readString(string));
  }
}
