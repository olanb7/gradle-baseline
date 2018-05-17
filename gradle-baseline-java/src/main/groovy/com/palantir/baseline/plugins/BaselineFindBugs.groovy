/*
 * Copyright 2015 Palantir Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.baseline.plugins

import org.gradle.api.Project
import com.github.spotbugs.SpotBugsPlugin

/**
 * Configures the Gradle 'findbugs' task with Baseline settings.
 * Even though all of the config is listed as findbugs, we actually
 * use the spotbugs plugin, which is better maintained.
 */
class BaselineFindBugs extends AbstractBaselinePlugin {

    static String DEFAULT_SPOTBUGS_VERSION = '3.1.1'
    private static final String DEFAULT_EFFORT = 'max'

    void apply(Project project) {
        this.project = project

        project.plugins.apply SpotBugsPlugin

        // Set report type.
        // We do this at 'apply' time so that they could be overridden by a user later.
        // 'html' is human-readable; 'xml' can be read by the Eclipse FindBugs plugin.
        // Only one can be enabled at a time.
        // Note: This only affects FindBugs tasks that exist when this plugin is applied.
        project.tasks.withType(com.github.spotbugs.SpotBugsTask) {
            reports {
                xml.enabled = false
                html.enabled = true
            }
        }

        configureSpotBugs()

        project.afterEvaluate { Project p ->
            configureSpotBugsEclipse()
        }
    }

    def configureSpotBugs() {
        project.logger.info("Baseline: Configuring SpotBugs tasks")

        // Configure findbugs
        project.spotbugs {
            toolVersion = DEFAULT_SPOTBUGS_VERSION
            excludeFilter = excludeFilterFile
            effort = DEFAULT_EFFORT
        }
    }

    def configureSpotBugsForEclipse() {
        if (!project.plugins.findPlugin(EclipsePlugin)) {
            project.logger.info "Baseline: Skipping configuring Eclipse for SpotBugsPlugin (eclipse not applied)"
            return
        }
        project.logger.info "Baseline: Configuring Eclipse FindBugs"
        project.eclipse.project {
            natures "edu.umd.cs.findbugs.plugin.eclipse.findbugsNature"
            buildCommand "edu.umd.cs.findbugs.plugin.eclipse.findbugsBuilder"
        }
    }

    File getExcludeFilterFile() {
        project.file(Paths.get(configDir, "findbugs", "excludeFilter.xml").toString())
    }

}
