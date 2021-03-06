/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.intellij.build.pycharm

import org.codehaus.gant.GantBinding
import org.jetbrains.intellij.build.BuildContext
import org.jetbrains.intellij.build.BuildOptions
import org.jetbrains.intellij.build.BuildTasks
import org.jetbrains.intellij.build.ProprietaryBuildTools

/**
 * @author vlan
 */
class PythonCommunityPluginBuilder {
  private final GantBinding binding
  private final String home

  PythonCommunityPluginBuilder(String home, GantBinding binding) {
    this.home = home
    this.binding = binding
  }

  def build() {
    def pluginBuildNumber = System.getProperty("build.number", "SNAPSHOT")
    def options = new BuildOptions(targetOS: BuildOptions.OS_NONE, buildNumber: pluginBuildNumber, outputRootPath: "$home/out/pycharmCE")
    def buildContext = BuildContext.createContext(home, home, new PythonCommunityPluginProperties(), ProprietaryBuildTools.DUMMY, options)
    def buildTasks = BuildTasks.create(buildContext)
    buildTasks.buildDistributions()
    def pluginsPaths = new File("$buildContext.paths.buildOutputRoot/plugins-paths.txt")
    pluginsPaths.text = new File("$buildContext.paths.artifacts/plugins")
      .listFiles()
      .collect { it.toString() }
      .join("\n")
  }
}
