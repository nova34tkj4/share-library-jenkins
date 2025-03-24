def call(String backendStaging) {
    node {
        def remote = [:]
        remote.name = '${nameservice}'
        remote.host = '1.1.1.1'
        remote.user = 'root'
        remote.identityFile = '/var/lib/jenkins/.ssh/id_rsa'
        remote.allowAnyHosts = true

        try {
                stage('Build Image') {
                    sh "echo ${nameservice}"
                }
        } catch (Exception e) {
            currentBuild.result = 'FAILURE'
            echo 'Build failed!'
            throw e
        }

        echo 'Build succeeded!'
    }
}
