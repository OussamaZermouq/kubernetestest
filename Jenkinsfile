pipeline {
    agent any
    
    tools {
        maven 'Maven-3.8.1' // Configure this in Jenkins Global Tool Configuration
        jdk 'JDK-11' // Configure this in Jenkins Global Tool Configuration
    }
    
    environment {
        DOCKER_HUB_CREDENTIALS = credentials('docker-hub-credentials') // Configure in Jenkins Credentials
        DOCKER_IMAGE_NAME = 'your-dockerhub-username/your-app-name'
        DOCKER_TAG = "${BUILD_NUMBER}"
        SONAR_TOKEN = credentials('sonar-token') // Configure in Jenkins Credentials
        SONAR_HOST_URL = 'http://your-sonarqube-server:9000'
        KUBECONFIG = credentials('kubeconfig') // Configure in Jenkins Credentials
    }
    
    triggers {
        // Trigger on pull request to master branch
        githubPullRequests(
            triggerMode: 'HEAVY_HOOKS',
            events: [
                opened(), 
                synchronize()
            ],
            spec: 'H/5 * * * *',
            preStatus: true,
            cancelQueued: true
        )
    }
    
    stages {
        stage('Checkout') {
            steps {
                script {
                    // Checkout the PR branch
                    checkout scm
                }
            }
        }
        
        stage('Maven Build') {
            steps {
                script {
                    try {
                        echo 'Starting Maven build...'
                        sh 'mvn clean compile test package -DskipTests=false'
                        echo 'Maven build completed successfully'
                    } catch (Exception e) {
                        currentBuild.result = 'FAILURE'
                        sendNotification('Maven Build Failed', "Build failed with error: ${e.getMessage()}")
                        error("Maven build failed: ${e.getMessage()}")
                    }
                }
            }
            post {
                always {
                    // Archive test results
                    publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
                    archiveArtifacts artifacts: 'target/*.jar', allowEmptyArchive: false
                }
            }
        }
        
        stage('SonarQube Analysis') {
            steps {
                script {
                    try {
                        echo 'Starting SonarQube analysis...'
                        withSonarQubeEnv('SonarQube') { // Configure SonarQube server in Jenkins
                            sh '''
                                mvn sonar:sonar \
                                -Dsonar.projectKey=your-project-key \
                                -Dsonar.host.url=${SONAR_HOST_URL} \
                                -Dsonar.login=${SONAR_TOKEN}
                            '''
                        }
                        
                        // Wait for quality gate
                        timeout(time: 5, unit: 'MINUTES') {
                            def qg = waitForQualityGate()
                            if (qg.status != 'OK') {
                                currentBuild.result = 'FAILURE'
                                sendNotification('SonarQube Quality Gate Failed', "Quality gate status: ${qg.status}")
                                error("SonarQube quality gate failed: ${qg.status}")
                            }
                        }
                        echo 'SonarQube analysis completed successfully'
                    } catch (Exception e) {
                        currentBuild.result = 'FAILURE'
                        sendNotification('SonarQube Analysis Failed', "SonarQube analysis failed: ${e.getMessage()}")
                        error("SonarQube analysis failed: ${e.getMessage()}")
                    }
                }
            }
        }
        
        stage('Build Docker Image') {
            steps {
                script {
                    try {
                        echo 'Building Docker image...'
                        def dockerImage = docker.build("${DOCKER_IMAGE_NAME}:${DOCKER_TAG}")
                        env.DOCKER_IMAGE_ID = dockerImage.id
                        echo "Docker image built successfully: ${DOCKER_IMAGE_NAME}:${DOCKER_TAG}"
                    } catch (Exception e) {
                        currentBuild.result = 'FAILURE'
                        sendNotification('Docker Build Failed', "Docker image build failed: ${e.getMessage()}")
                        error("Docker image build failed: ${e.getMessage()}")
                    }
                }
            }
        }
        
        stage('Push to Docker Hub') {
            steps {
                script {
                    try {
                        echo 'Pushing Docker image to Docker Hub...'
                        docker.withRegistry('https://registry.hub.docker.com', 'docker-hub-credentials') {
                            def dockerImage = docker.image("${DOCKER_IMAGE_NAME}:${DOCKER_TAG}")
                            dockerImage.push()
                            dockerImage.push('latest')
                        }
                        echo 'Docker image pushed successfully to Docker Hub'
                    } catch (Exception e) {
                        currentBuild.result = 'FAILURE'
                        sendNotification('Docker Push Failed', "Failed to push Docker image: ${e.getMessage()}")
                        error("Failed to push Docker image: ${e.getMessage()}")
                    }
                }
            }
        }
        
        stage('Deploy to Kubernetes') {
            steps {
                script {
                    try {
                        echo 'Deploying to Kubernetes...'
                        
                        // Update deployment yaml with new image tag
                        sh """
                            sed -i 's|image: ${DOCKER_IMAGE_NAME}:.*|image: ${DOCKER_IMAGE_NAME}:${DOCKER_TAG}|g' k8s/deployment.yaml
                        """
                        
                        // Apply Kubernetes manifests
                        sh """
                            kubectl --kubeconfig=${KUBECONFIG} apply -f k8s/
                            kubectl --kubeconfig=${KUBECONFIG} set image deployment/your-app-deployment your-app-container=${DOCKER_IMAGE_NAME}:${DOCKER_TAG}
                            kubectl --kubeconfig=${KUBECONFIG} rollout status deployment/your-app-deployment --timeout=300s
                        """
                        
                        echo 'Deployment to Kubernetes completed successfully'
                        sendNotification('Deployment Successful', "Application deployed successfully to Kubernetes with image: ${DOCKER_IMAGE_NAME}:${DOCKER_TAG}")
                    } catch (Exception e) {
                        currentBuild.result = 'FAILURE'
                        sendNotification('Kubernetes Deployment Failed', "Deployment to Kubernetes failed: ${e.getMessage()}")
                        error("Deployment to Kubernetes failed: ${e.getMessage()}")
                    }
                }
            }
        }
    }
    
    post {
        always {
            // Clean up workspace
            cleanWs()
        }
        success {
            echo 'Pipeline completed successfully!'
            sendNotification('Pipeline Success', 'All stages completed successfully!')
        }
        failure {
            echo 'Pipeline failed!'
            sendNotification('Pipeline Failed', 'One or more stages failed. Check Jenkins logs for details.')
        }
    }
}

def sendNotification(String title, String message) {
    // Email notification
    emailext(
        subject: "[Jenkins] ${title} - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
        body: """
            <h3>${title}</h3>
            <p><strong>Job:</strong> ${env.JOB_NAME}</p>
            <p><strong>Build Number:</strong> ${env.BUILD_NUMBER}</p>
            <p><strong>Status:</strong> ${currentBuild.result ?: 'SUCCESS'}</p>
            <p><strong>Message:</strong> ${message}</p>
            <p><strong>Build URL:</strong> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
        """,
        to: 'your-email@company.com',
        mimeType: 'text/html'
    )
    
    // Slack notification (optional)
    /*
    slackSend(
        channel: '#deployments',
        color: currentBuild.result == 'SUCCESS' ? 'good' : 'danger',
        message: "${title}: ${message}\nJob: ${env.JOB_NAME} #${env.BUILD_NUMBER}\nURL: ${env.BUILD_URL}"
    )
    */
    
    // Teams notification (optional)
    /*
    office365ConnectorSend(
        webhookUrl: 'your-teams-webhook-url',
        message: "${title}: ${message}",
        status: currentBuild.result ?: 'SUCCESS'
    )
    */
}