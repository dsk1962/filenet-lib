@Library('pipelineLib') _

mavenJavaLibrary {
 
 
 preBuildClosure = { env ->
        withCredentials([usernamePassword(credentialsId: 'p8-jasypt_credential', passwordVariable: 'pwd', usernameVariable: 'userName')]) {
           env.JASYPT_PASSWORD = env.pwd
        }
    }
  buildNode='java17-mvn3-docker'
	
}
