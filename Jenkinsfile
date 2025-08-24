pipeline {
    agent any

    tools {
        maven 'maven'
    }

    triggers {
        githubPush()
    }

    environment {
        EMAIL_RECIPIENT = 'oussamazermouq2@gmail.com'
        DOCKER_IMAGE = 'oussamazermouq/kubernetestest'
    }

    stages {
        stage('Send Start Notification') {
            steps {
                mail(
                    subject: "Build Started: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                    body: """Build Started

Project: ${env.JOB_NAME}
Build Number: ${env.BUILD_NUMBER}
Build URL: ${env.BUILD_URL}
Started At: ${new Date()}
Branch: ${env.BRANCH_NAME ?: 'master'}
Commit: ${env.GIT_COMMIT ?: 'N/A'}

Console Output: ${env.BUILD_URL}console
                    """,
                    to: "${env.EMAIL_RECIPIENT}"
                )
            }
        }

        stage('Build Maven') {
            steps {
                checkout scmGit(
                    branches: [[name: '*/master']],
                    extensions: [],
                    userRemoteConfigs: [[url: 'https://github.com/OussamaZermouq/kubernetestest']]
                )
                sh 'mvn clean install'
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    sh "docker build -t ${env.DOCKER_IMAGE} ."
                }
            }
        }

        stage('Checkmarx Analysis') {
            steps {
                checkmarxASTScanner(
                    additionalOptions: '',
                    baseAuthUrl: '',
                    branchName: '',
                    checkmarxInstallation: 'checkmarkx',
                    credentialsId: '',
                    projectName: 'kubernetes-test',
                    serverUrl: '',
                    tenantName: ''
                )
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonarqube') {
                    sh 'mvn sonar:sonar'
                }
            }
        }

        stage('Push Image to DockerHub') {
            steps {
                script {
                    withCredentials([string(credentialsId: 'dockerhubpwd', variable: 'password')]) {
                        sh 'docker login -u oussamazermouq -p ${password}'
                        sh "docker push ${env.DOCKER_IMAGE}"
                    }
                }
            }
        }
    }

    post {
        success {
            mail(
                subject: "Build Successful: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """Build Completed Successfully!

Project: ${env.JOB_NAME}
Build Number: ${env.BUILD_NUMBER}
Duration: ${currentBuild.durationString}
Build URL: ${env.BUILD_URL}
Console Output: ${env.BUILD_URL}console
Completed At: ${new Date()}
Branch: ${env.BRANCH_NAME ?: 'master'}

Docker Image: ${env.DOCKER_IMAGE}
Docker Hub: https://hub.docker.com/r/${env.DOCKER_IMAGE}

Recent Changes:
${env.GIT_COMMIT_MSG ?: 'No commit message available'}

The build has completed successfully and the Docker image has been pushed to DockerHub.
                """,
                to: "${env.EMAIL_RECIPIENT}"
            )
        }

        failure {
            mail(
                subject: "Build Failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """Build Failed!

Project: ${env.JOB_NAME}
Build Number: ${env.BUILD_NUMBER}
Duration: ${currentBuild.durationString ?: 'N/A'}
Build URL: ${env.BUILD_URL}
Console Output: ${env.BUILD_URL}console
Failed At: ${new Date()}
Branch: ${env.BRANCH_NAME ?: 'master'}

Failed Stage: ${env.STAGE_NAME ?: 'Unknown'}

Recent Changes:
${env.GIT_COMMIT_MSG ?: 'No commit message available'}

Please check the console output for detailed error information and fix the issues.

Troubleshooting:
- Console: ${env.BUILD_URL}console
- Pipeline Steps: ${env.BUILD_URL}flowGraphTable/
                """,
                to: "${env.EMAIL_RECIPIENT}"
            )
        }

        unstable {
            mail(
                subject: "Build Unstable: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """Build Completed with Warnings

Project: ${env.JOB_NAME}
Build Number: ${env.BUILD_NUMBER}
Build URL: ${env.BUILD_URL}
Console Output: ${env.BUILD_URL}console
Completed At: ${new Date()}

The build completed but may have test failures or warnings. Please review the console output.
                """,
                to: "${env.EMAIL_RECIPIENT}"
            )
        }

    }
}
