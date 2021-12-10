package com.github.ohle.ideaswag.services

import com.intellij.openapi.project.Project
import com.github.ohle.ideaswag.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
