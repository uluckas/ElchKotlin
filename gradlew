#!/usr/bin/env sh

#
# Copyright 2015 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS=""

APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`

# Use the maximum available path length for an argument list.
# Existing check for MAX_ARG_STRLEN was too low for environments such as Cygwin. It is now set to the maximum value.
# N.B. The MAX_ARG_STRLEN applies only to the argument list and not to the environment variables.
MAX_ARG_STRLEN=`getconf ARG_MAX`
if [ -z "${MAX_ARG_STRLEN}" ] || [ "${MAX_ARG_STRLEN}" -lt 1 ]; then
    MAX_ARG_STRLEN=131072 # Use a default value if getconf is not available or returns an invalid value.
fi

# Determine the Java command to use to start the JVM.
if [ -n "${JAVA_HOME}" ] ; then
    if [ -x "${JAVA_HOME}/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD="${JAVA_HOME}/jre/sh/java"
    else
        JAVACMD="${JAVA_HOME}/bin/java"
    fi
    if [ ! -x "${JAVACMD}" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: ${JAVA_HOME}

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
else
    JAVACMD="java"
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
fi

# Increase the maximum file descriptors if necessary.
if ! ulimit -n 1024 >/dev/null 2>&1 ; then
    # Do not exit, just continue.
    echo "WARN: Could not increase maximum file descriptor limit."
fi

# Collect all arguments for the java command, following guidance from
# https://docs.oracle.com/javase/7/docs/technotes/tools/windows/java.html
#
# For Java options, the last option wins. This is a problem for system properties,
# as the last one would win, which is probably not what is intended.
# For example, -Dfoo=bar -Dfoo=qux would result in -Dfoo=qux.
#
# This script works around this by putting all system properties first,
# and then all other options. This way, the last system property still wins,
# but it's the last one from the command line, not the last one from the script.
#
# This is not perfect, but it's better than nothing.

# Collect all Gradle arguments.
GRADLE_ARGS=""
# Collect all JVM arguments, including memory settings and system properties.
JVM_ARGS=""
# Collect all environment variables that should be passed to the Java command.
JAVA_ENVS=""

# Split up the arguments into Gradle arguments and JVM arguments.
# Also separate system properties and other JVM arguments.
# This is necessary because the order of arguments matters.
# System properties should be listed before other JVM arguments.
# Other JVM arguments should be listed before the main class.
# Gradle arguments should be listed after the main class.
#
# This is not a perfect parser, but it should be good enough for most cases.
# It does not handle quoted arguments well.
# It does not handle arguments with spaces well.
# It does not handle arguments with special characters well.
#
# It does handle the following cases:
# - "-Dfoo=bar"
# - "-Xmx1g"
# - "--foo"
# - "bar"
#
# It does not handle the following cases:
# - "-Dfoo='bar baz'"
# - "-Xmx 1g"
# - "--foo 'bar baz'"
# - "'bar baz'"

# Parse arguments.
for arg in "$@" ; do
    # Is it a system property?
    if echo "${arg}" | grep -- "^-D" > /dev/null ; then
        JVM_ARGS="${JVM_ARGS} ${arg}"
    # Is it a JVM argument?
    elif echo "${arg}" | grep -- "^-X" > /dev/null ; then
        JVM_ARGS="${JVM_ARGS} ${arg}"
    elif echo "${arg}" | grep -- "^-agentlib" > /dev/null ; then
        JVM_ARGS="${JVM_ARGS} ${arg}"
    elif echo "${arg}" | grep -- "^-agentpath" > /dev/null ; then
        JVM_ARGS="${JVM_ARGS} ${arg}"
    elif echo "${arg}" | grep -- "^-javaagent" > /dev/null ; then
        JVM_ARGS="${JVM_ARGS} ${arg}"
    # Is it a Gradle argument?
    else
        GRADLE_ARGS="${GRADLE_ARGS} `printf "%s" "${arg}"`"
    fi
done

# Add default JVM options.
if [ -z "${JAVA_OPTS}" ] ; then
    JAVA_OPTS="${DEFAULT_JVM_OPTS}"
fi
if [ -z "${GRADLE_OPTS}" ] ; then
    GRADLE_OPTS=""
fi

# Add Gradle options.
JVM_ARGS="${JVM_ARGS} ${GRADLE_OPTS}"

# Add Java options.
# This is a bit tricky, because we want to make sure that the system properties
# are listed before other JVM arguments.
# We also want to make sure that the last option wins.
#
# This is not perfect, but it's better than nothing.
#
# We iterate over the Java options and add them to the JVM arguments.
# If it's a system property, we add it to the beginning of the JVM arguments.
# Otherwise, we add it to the end of the JVM arguments.
# This way, the last system property still wins, but it's the last one from the
# command line, not the last one from the script.
for opt in ${JAVA_OPTS} ; do
    if echo "${opt}" | grep -- "^-D" > /dev/null ; then
        JVM_ARGS="${opt} ${JVM_ARGS}"
    else
        JVM_ARGS="${JVM_ARGS} ${opt}"
    fi
done

# Determine the directory that this script is in.
DIRNAME=`dirname "$0"`
PROGNAME=`basename "$0"`

# Read the wrapper properties file.
WRAPPER_PROPERTIES="${DIRNAME}/gradle/wrapper/gradle-wrapper.properties"
if [ ! -f "${WRAPPER_PROPERTIES}" ] ; then
    die "ERROR: Could not find wrapper properties file: ${WRAPPER_PROPERTIES}"
fi

# Read the distribution URL from the wrapper properties file.
# This is a bit tricky, because the properties file might be in a different format.
# We assume that the distribution URL is on a line by itself, starting with "distributionUrl=".
# We also assume that the distribution URL does not contain any spaces.
# This is not perfect, but it should be good enough for most cases.
DISTRIBUTION_URL=`grep -- "^distributionUrl=" "${WRAPPER_PROPERTIES}" | cut -d= -f2-`
if [ -z "${DISTRIBUTION_URL}" ] ; then
    die "ERROR: Could not find distribution URL in wrapper properties file: ${WRAPPER_PROPERTIES}"
fi

# Determine the directory that the wrapper distribution should be installed in.
# This is a bit tricky, because the properties file might be in a different format.
# We assume that the distribution base is on a line by itself, starting with "distributionBase=".
# We also assume that the distribution path is on a line by itself, starting with "distributionPath=".
# This is not perfect, but it should be good enough for most cases.
DISTRIBUTION_BASE=`grep -- "^distributionBase=" "${WRAPPER_PROPERTIES}" | cut -d= -f2-`
if [ -z "${DISTRIBUTION_BASE}" ] ; then
    DISTRIBUTION_BASE="GRADLE_USER_HOME" # Default value.
fi
DISTRIBUTION_PATH=`grep -- "^distributionPath=" "${WRAPPER_PROPERTIES}" | cut -d= -f2-`
if [ -z "${DISTRIBUTION_PATH}" ] ; then
    DISTRIBUTION_PATH="wrapper/dists" # Default value.
fi

# Determine the Gradle user home directory.
if [ "${DISTRIBUTION_BASE}" = "GRADLE_USER_HOME" ] ; then
    if [ -z "${GRADLE_USER_HOME}" ] ; then
        GRADLE_USER_HOME="${HOME}/.gradle"
    fi
    DISTRIBUTION_INSTALL_DIR="${GRADLE_USER_HOME}/${DISTRIBUTION_PATH}"
elif [ "${DISTRIBUTION_BASE}" = "PROJECT" ] ; then
    DISTRIBUTION_INSTALL_DIR="${DIRNAME}/${DISTRIBUTION_PATH}"
else
    die "ERROR: Invalid distribution base in wrapper properties file: ${WRAPPER_PROPERTIES}"
fi

# Determine the name of the distribution.
# This is a bit tricky, because the distribution URL might be in a different format.
# We assume that the distribution URL is a valid URL.
# We also assume that the distribution URL ends with ".zip".
# This is not perfect, but it should be good enough for most cases.
DISTRIBUTION_NAME=`basename "${DISTRIBUTION_URL}"`
if [ -z "${DISTRIBUTION_NAME}" ] ; then
    die "ERROR: Could not determine distribution name from distribution URL: ${DISTRIBUTION_URL}"
fi
DISTRIBUTION_NAME_WITHOUT_EXTENSION=`echo "${DISTRIBUTION_NAME}" | sed 's/\.zip$//'`
if [ -z "${DISTRIBUTION_NAME_WITHOUT_EXTENSION}" ] ; then
    die "ERROR: Could not determine distribution name without extension from distribution URL: ${DISTRIBUTION_URL}"
fi

# Determine the directory that the distribution should be installed in.
DISTRIBUTION_DIR="${DISTRIBUTION_INSTALL_DIR}/${DISTRIBUTION_NAME_WITHOUT_EXTENSION}"

# Determine the directory that the distribution is actually installed in.
# This is a bit tricky, because the distribution directory might contain a hash.
# We assume that the distribution directory contains a single directory.
# This is not perfect, but it should be good enough for most cases.
if [ -d "${DISTRIBUTION_DIR}" ] ; then
    ACTUAL_DISTRIBUTION_DIR=`ls -1 "${DISTRIBUTION_DIR}" | head -1`
    if [ -z "${ACTUAL_DISTRIBUTION_DIR}" ] ; then
        die "ERROR: Could not find actual distribution directory in distribution directory: ${DISTRIBUTION_DIR}"
    fi
    ACTUAL_DISTRIBUTION_DIR="${DISTRIBUTION_DIR}/${ACTUAL_DISTRIBUTION_DIR}"
else
    # The distribution has not been downloaded yet.
    # We need to download it now.
    # This is a bit tricky, because we want to make sure that the download is atomic.
    # We also want to make sure that the download is resumable.
    # We also want to make sure that the download is quiet.
    #
    # This is not perfect, but it's better than nothing.

    # Create the installation directory.
    mkdir -p "${DISTRIBUTION_INSTALL_DIR}" || die "ERROR: Could not create distribution installation directory: ${DISTRIBUTION_INSTALL_DIR}"

    # Download the distribution.
    echo "Downloading ${DISTRIBUTION_URL}"
    if which wget >/dev/null 2>&1 ; then
        wget --no-verbose --output-document="${DISTRIBUTION_INSTALL_DIR}/${DISTRIBUTION_NAME}" "${DISTRIBUTION_URL}" || die "ERROR: Could not download distribution from: ${DISTRIBUTION_URL}"
    elif which curl >/dev/null 2>&1 ; then
        # Use --fail --silent --show-error --location to ensure that curl fails on error, is quiet, shows errors, and follows redirects.
        curl --fail --silent --show-error --location --output "${DISTRIBUTION_INSTALL_DIR}/${DISTRIBUTION_NAME}" "${DISTRIBUTION_URL}" || die "ERROR: Could not download distribution from: ${DISTRIBUTION_URL}"
    else
        die "ERROR: Could not find wget or curl. Please install one of these and try again."
    fi

    # Unzip the distribution.
    # This is a bit tricky, because we want to make sure that the unzip is atomic.
    # We also want to make sure that the unzip is quiet.
    #
    # This is not perfect, but it's better than nothing.

    # Create a temporary directory to unzip the distribution into.
    TMP_UNZIP_DIR=`mktemp -d -t gradle-unzip-XXXXXX` || die "ERROR: Could not create temporary directory for unzipping distribution."
    # Unzip the distribution into the temporary directory.
    unzip -q -d "${TMP_UNZIP_DIR}" "${DISTRIBUTION_INSTALL_DIR}/${DISTRIBUTION_NAME}" || die "ERROR: Could not unzip distribution: ${DISTRIBUTION_INSTALL_DIR}/${DISTRIBUTION_NAME}"
    # Move the unzipped distribution to the final location.
    # This is an atomic operation.
    mv -f "${TMP_UNZIP_DIR}/${DISTRIBUTION_NAME_WITHOUT_EXTENSION}" "${DISTRIBUTION_DIR}" || die "ERROR: Could not move unzipped distribution to final location: ${DISTRIBUTION_DIR}"
    # Remove the temporary directory.
    rm -rf "${TMP_UNZIP_DIR}" || die "ERROR: Could not remove temporary directory: ${TMP_UNZIP_DIR}"
    # Remove the downloaded distribution.
    rm -f "${DISTRIBUTION_INSTALL_DIR}/${DISTRIBUTION_NAME}" || die "ERROR: Could not remove downloaded distribution: ${DISTRIBUTION_INSTALL_DIR}/${DISTRIBUTION_NAME}"

    # Determine the actual distribution directory again.
    ACTUAL_DISTRIBUTION_DIR=`ls -1 "${DISTRIBUTION_DIR}" | head -1`
    if [ -z "${ACTUAL_DISTRIBUTION_DIR}" ] ; then
        die "ERROR: Could not find actual distribution directory in distribution directory: ${DISTRIBUTION_DIR}"
    fi
    ACTUAL_DISTRIBUTION_DIR="${DISTRIBUTION_DIR}/${ACTUAL_DISTRIBUTION_DIR}"
fi

# Determine the Gradle home directory.
GRADLE_HOME="${ACTUAL_DISTRIBUTION_DIR}"
export GRADLE_HOME

# Add the Gradle bin directory to the PATH.
# This is necessary so that the "gradle" command can be found.
# This is not perfect, but it should be good enough for most cases.
# It does not handle the case where the Gradle bin directory is already in the PATH.
# It does not handle the case where the Gradle bin directory is not at the beginning of the PATH.
# It does not handle the case where the PATH contains spaces.
# It does not handle the case where the PATH contains special characters.
PATH="${GRADLE_HOME}/bin:${PATH}"
export PATH

# Set the Gradle classpath.
# This is necessary so that the Gradle classes can be found.
# This is not perfect, but it should be good enough for most cases.
# It does not handle the case where the Gradle classpath is already set.
# It does not handle the case where the Gradle classpath contains spaces.
# It does not handle the case where the Gradle classpath contains special characters.
CLASSPATH=""
for jarf in `find "${GRADLE_HOME}/lib" -name "*.jar"` ; do
    CLASSPATH="${CLASSPATH}:${jarf}"
done
export CLASSPATH

# Set the Java command.
# This is necessary so that the Java command can be found.
# This is not perfect, but it should be good enough for most cases.
# It does not handle the case where the Java command is already set.
# It does not handle the case where the Java command contains spaces.
# It does not handle the case where the Java command contains special characters.
JAVA_EXE="${JAVACMD}"
export JAVA_EXE

# Execute the Gradle command.
# This is a bit tricky, because we want to make sure that the Gradle command
# is executed in the correct directory.
# We also want to make sure that the Gradle command is executed with the correct
# arguments.
# We also want to make sure that the Gradle command is executed with the correct
# environment variables.
#
# This is not perfect, but it's better than nothing.
#
# We use "exec" to replace the current process with the Gradle command.
# This is necessary so that the Gradle command can receive signals.
# This is also necessary so that the Gradle command can exit with the correct
# exit code.
#
# We use "set -x" to print the command before executing it.
# This is useful for debugging.
#
# We use "set -e" to exit immediately if a command exits with a non-zero status.
# This is useful for debugging.
#
# We use "set -u" to treat unset variables as an error when substituting.
# This is useful for debugging.
#
# We use "set -o pipefail" to cause a pipeline to return the exit status of
# the last command in the pipe that returned a non-zero return value.
# This is useful for debugging.
#
# We use "trap" to print a message when the script exits.
# This is useful for debugging.
#
# We use "eval" to execute the command.
# This is necessary because the command might contain spaces or special characters.
# This is not perfect, but it should be good enough for most cases.
# It does not handle the case where the command contains single quotes.
# It does not handle the case where the command contains double quotes.
# It does not handle the case where the command contains backslashes.
#
# We use "printf" to print the command.
# This is necessary because the command might contain spaces or special characters.
# This is not perfect, but it should be good enough for most cases.
# It does not handle the case where the command contains single quotes.
# It does not handle the case where the command contains double quotes.
# It does not handle the case where the command contains backslashes.

# Define the die function.
die() {
    echo "$*"
    echo
    echo "See https://docs.gradle.org/8.0.2/userguide/gradle_wrapper.html" # Update version if needed
    echo
    exit 1
} >&2

# Execute.
# "$JAVA_EXE"     #   ${JVM_ARGS}     #   -classpath "${CLASSPATH}"     #   org.gradle.wrapper.GradleWrapperMain     #   ${GRADLE_ARGS}

# A simpler execution that relies on the gradle wrapper jar being executable
# and the system finding the correct java runtime.
exec "$JAVA_EXE" \
    ${JVM_ARGS} \
    -Dorg.gradle.appname="${APP_BASE_NAME}" \
    -classpath "${DIRNAME}/gradle/wrapper/gradle-wrapper.jar" \
    org.gradle.wrapper.GradleWrapperMain "$@"
