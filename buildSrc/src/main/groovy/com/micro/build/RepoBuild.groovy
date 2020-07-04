package com.micro.build

import org.gradle.api.Plugin
import org.gradle.api.Project

class RepoBuild implements Plugin<Project> {

    @Override
    void apply(Project target) {
        println "apply project1 -->" + target.name
        println "apply project2 -->" + target.getChildProjects().size()

        target.afterEvaluate {
            println "apply project3 -->" + target.name
        }
    }


}

