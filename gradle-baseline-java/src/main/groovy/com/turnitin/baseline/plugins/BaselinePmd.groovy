package com.turnitin.baseline.plugins

import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.quality.Pmd
import org.gradle.api.plugins.quality.PmdPlugin
import org.gradle.plugins.ide.eclipse.EclipsePlugin

import java.nio.file.Paths

class BaselinePmd extends AbstractBaselinePlugin {

    private static final String DEFAULT_PMD_VERSION = '6.3.0'
    private static final String DEFAULT_CONSOLE_OUTPUT = true
    private static final String DEFAULT_IGNORE_FAILURES = true

    void apply(Project project) {
        this.project = project

        project.plugins.apply PmdPlugin

        project.tasks.withType(Pmd) {
            reports {
                xml.enabled = false
                html.enabled = true
            }
            ignoreFailures = DEFAULT_IGNORE_FAILURES
        }

        configurePmd()

        project.afterEvaluate { Project p ->
            configurePmdForEclipse()
        }
    }

    def configurePmd() {
        project.logger.info("Baseline: Configuring SpotBugs tasks")

        // Configure PMD
        project.pmd {
            ruleSetFiles = getRuleSetFiles()
            consoleOutput = DEFAULT_CONSOLE_OUTPUT
            toolVersion = DEFAULT_PMD_VERSION
        }
    }

    def configurePmdForEclipse() {
        if (!project.plugins.findPlugin(EclipsePlugin)) {
            project.logger.info "Baseline: Skipping configuring Eclipse for PMD (eclipse not applied)"
            return
        }
        project.logger.info "Baseline: Configuring Eclipse PMD"
        project.eclipse.project {
            natures "net.sourceforge.pmd.eclipse.runtime.builder.PMDNature"
            buildCommand "net.sourceforge.pmd.eclipse.runtime.builder.PMDBuilder"
        }
    }

    FileCollection getRuleSetFiles() {
        project.files(Paths.get(configDir, "pmd", "ruleset.xml"))
    }

}
