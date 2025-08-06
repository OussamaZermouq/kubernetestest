pipeline {
    agent any
    tools{
        maven 'maven'
    }
    triggers {
    	githubPush()
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
	stage('Checkmarx Analysis'){
	    steps{
		checkmarxASTScanner additionalOptions: '', baseAuthUrl: '', branchName: '', checkmarxInstallation: 'checkmarkx', credentialsId: '', projectName: 'kubernetes-test', serverUrl: '', tenantName: ''
	    }
	}
 	stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonarqube') { // Use the server name you configured
                    sh 'mvn sonar:sonar'
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

