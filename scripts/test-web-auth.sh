#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
mkdir -p .test-classes
java com.sun.tools.javac.Main -d .test-classes android/app/src/main/java/com/heritier/millionnaire/WebAuthPolicy.java tests/WebAuthPolicyTest.java
java -cp .test-classes com.heritier.millionnaire.WebAuthPolicyTest
node tests/native-fetch.test.cjs
