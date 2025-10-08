pipeline {
    agent any

    parameters {
        string(name: 'SERVICE_REPO_URL', defaultValue: 'https://github.com/Tourment0412/jwtmanual-taller1-micro.git', description: 'Repo del microservicio a construir')
        string(name: 'SERVICE_BRANCH', defaultValue: 'main', description: 'Rama del microservicio')
        string(name: 'AUT_TESTS_BASE_URL', defaultValue: 'http://jwtmanual-taller1-micro:8080', description: 'Base URL del servicio bajo prueba')
    }

    tools {
        maven 'Maven-3.9'
        jdk 'jdk21'
    }

    environment {
        SONARQUBE = 'SonarQube-Server'
        MVN_HOME = tool(name: 'Maven-3.9', type: 'maven')
        JDK_HOME = tool(name: 'jdk21', type: 'jdk')
        MVN = "${MVN_HOME}/bin/mvn"
    }

    stages {
        stage('Checkout repos') {
            steps {
                dir('service') {
                    git branch: params.SERVICE_BRANCH, url: params.SERVICE_REPO_URL
                }
            }
        }

        stage('Build + Unit tests (service)') {
            steps {
                dir('service') {
                    sh 'ls -la'
                    sh "${MVN} -v"
                    sh "${MVN} clean verify"
                }
            }
            post {
                always {
                    junit 'service/target/surefire-reports/*.xml'
                    publishHTML([
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'service/target/site/jacoco',
                        reportFiles: 'index.html',
                        reportName: 'Reporte de Cobertura (service)'
                    ])
                }
            }
        }

        stage('Allure (service)') {
            steps {
                dir('service') {
                    sh "${MVN} -q -e allure:report || true"
                }
                publishHTML([
                    allowMissing: true,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'service/target/site/allure-maven-plugin',
                    reportFiles: 'index.html',
                    reportName: 'Reporte Allure (service)'
                ])
            }
        }

        stage('SonarQube (service)') {
            steps {
                dir('service') {
                    withSonarQubeEnv("${SONARQUBE}") {
                        sh "${MVN} sonar:sonar -Dsonar.projectKey=jwtgeneratortaller1 -Dsonar.host.url=http://sonarqube:9000 -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml"
                    }
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 1, unit: 'HOURS') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('E2E (automation-tests)') {
            steps {
                sh "${MVN} -q -e clean test -DbaseUrl=${params.AUT_TESTS_BASE_URL} -DbasePath=/v1"
                sh "${MVN} -q -e allure:report || true"
                publishHTML([
                    allowMissing: true,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'target/site/allure-maven-plugin',
                    reportFiles: 'index.html',
                    reportName: 'Reporte Allure (E2E)'
                ])
            }
        }
    }
}


