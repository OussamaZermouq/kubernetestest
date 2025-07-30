pipeline {
    agent any
    tools{
        maven 'maven'
    }
    stages {
        stage('Build Maven') {
            steps {
                checkout scmGit(branches: [[name: '*/master']], extensions: [], userRemoteConfigs: [[url: 'https://github.com/OussamaZermouq/kubernetestest']])
                sh 'mvn clean install'
            }
        }
        stage('Build docker image'){
            steps{
                script{
                    sh 'docker build -t oussamazermouq/kubernetestest .'
                }
            }
        }
        stage('Push image to dockerHub'){
            steps{
                script{
                    withCredentials([string(credentialsId: 'dockerhubpwd', variable: 'password')]) {
                        sh 'docker login -u oussamazermouq -p ${password}'
                    }
                    sh 'docker push oussamazermouq/kubernetestest'
                }
            }
        }
    }
}

