def call(String backendStaging) {
    node {
        def remote = [:]
        remote.name = '${nameservice}'
        remote.host = '35.240.185.35'
        remote.user = 'root'
        remote.identityFile = '/var/lib/jenkins/.ssh/id_rsa'
        remote.allowAnyHosts = true

        try {
            withCredentials([string(credentialsId: 'key-github', variable: 'SSH_KEY')]) {
                stage('Clone the repo') {
                    checkout scmGit(
                        branches: [[name: "${branch}"]], 
                        extensions: [], 
                        userRemoteConfigs: [
                            [credentialsId: 'githubbythentoken', url: "${repo}"]
                        ]
                    )
                }

                stage('Build Image') {
                    def commit = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
                    sh "docker build -t ${registry}/${nameservice}:${commit} ."
                }

                stage('push') {
                    def commit = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
	                sh "docker push ${registry}/${nameservice}:${commit}"
	                sh "docker rmi ${registry}/${nameservice}:${commit}"
                }

                stage('Deploy To docker') {
                    def commit = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
                    sh """
                        ssh ${remote.user}@${remote.host} "docker run --name ${nameservice} -p 8088:80 -d ${registry}/${nameservice}:${commit}"
                    """
                }
            }
        } catch (Exception e) {
            currentBuild.result = 'FAILURE'
            echo 'Build failed!'
            throw e
        }

        echo 'Build succeeded!'
    }
}
