pipeline {
  agent any
  stages {
    stage('build') {
      steps {
        echo 'ready to compile'
        sh './mvnw -b compile'
      }
    }

  }
}