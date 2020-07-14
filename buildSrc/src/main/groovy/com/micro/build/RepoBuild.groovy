package com.micro.build

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.tasks.Exec

class RepoBuild implements Plugin<Settings> {

    @Override
    void apply(Settings settings) {
        // 配置完成之前
//        project.beforeEvaluate {
//            println "project beforeEvaluate -->" + project.name
//        }
        def project = settings.getRootProject()
        // 配置完成之后
//        project.afterEvaluate {
//            println "project afterEvaluate -->" + project.name
            File file = new File(project.getProjectDir().getAbsolutePath()+"/repo.xml")
            if (!file.exists()) {
                println "repo.xml not found"
            } else {
                println "repo.xml exists"
                def xmlSlurper = new XmlSlurper()
                def xml = xmlSlurper.parse(file)
                def remoteList = []
                // 解析repo.xml文件
                xml.remote.each { remote ->
                    def remoteInfo = new Remote()
                    remoteInfo.name = remote.@name
                    remoteInfo.branch = remote.@branch
                    remoteInfo.git = remote.@git
                    remoteInfo.source = remote.@source
                    remoteList.add(remoteInfo)
                    println "remote:${remote.text()}"
                }

                remoteList.each { remoteInfo ->
                    try {
                        // 判断文件夹是否存在
                        def moduleFile = new File(project.getRootDir().toString() + "/" + remoteInfo.name)
                        println "moduleFile.exists():${moduleFile.exists()}"
                        if (!moduleFile.exists()) {
                            def mkDirCmd = "mkdir ${remoteInfo.name}"
                            // 根据name创建文件夹

                            project.exec {
                                executable 'bash'
                                args '-c', mkDirCmd
                            }
                        }
                        def outputSteam = new ByteArrayOutputStream()
                        project.exec {
                            executable 'bash'
                            workingDir "${remoteInfo.name}/"
                            standardOutput outputSteam
                            args '-c', 'git branch'
                        }
                        // 判断git是否存在
                        def branch = outputSteam.toString()
                        println "moduleName:${remoteInfo.name} repoBranch:${remoteInfo.branch} remoteBranch:${branch}"
                        if (branch.startsWith("*")) {
                            // 如果repo文件的branch值跟当前的分支不一样，则切换分支，否则不做任何处理
                            def isExistBranch = false
                            branch.eachLine { branchLine ->
                                println "branchLine:${branchLine}"
                                if (branchLine.endsWith(remoteInfo.branch.toString())) {
                                    isExistBranch = true
                                }
                            }
                            if (!isExistBranch) {
                                def checkoutCmd = "git checkout -b ${remoteInfo.branch.toString()}"
                                project.exec {
                                    executable 'bash'
                                    workingDir "${remoteInfo.name}/"
                                    args '-c', checkoutCmd
                                }
                                println "checkout branch:${remoteInfo.branch}"
                            } else {
                                println "not checkout"
                            }

                        } else {
                            // 如果没有git、删除文件夹的内容，重新clone一次代码
                            def rmCmd = "rm -rf *"
                            project.exec {
                                executable 'bash'
                                args '-c', rmCmd
                            }
                            // git克隆工程
                            def gitCloneCmd = "git clone ${remoteInfo.git}"
                            project.exec {
                                executable 'bash'
                                args '-c', gitCloneCmd

                            }
                        }


                        println "project:${project.getConfigurations()}"
//                        project.dependencies.add("compile", project(':module1').projectDir = file('module1/'))
                        println "dependencies.add:${remoteInfo.name}"

                    } catch (GradleException e) {
                        println "exec error :${e.getMessage()}"
                    }
                }
            }
//        }
    }


}

class Remote {
    def name
    def branch
    def git
    def source
}

