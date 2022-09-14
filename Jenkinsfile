//git 身份id
def git_auth = "766d0a6a-b293-4029-9886-1e61fdda466b"
//git_url
def git_url = "http://10.1.3.91:18080/SDPS/SDPS_server/sdps.git"

node {
    stage('拉取代码') {
      checkout([$class: 'GitSCM', branches: [[name: "*/${branch}"]], extensions: [], userRemoteConfigs: [[credentialsId: "${git_auth}", url: "${git_url}"]]])
      echo '拉取代码'
    }

   stage('编译安装公共工程') {
      sh "mvn -f config-common clean install"
      sh "mvn -f sdps-common clean install"
      echo '编译安装公共工程'
    }

    stage('编译打包勾选服务') {
      sh "mvn -f ${project_name} clean package"
      echo '编译打包勾选服务'
    }
}