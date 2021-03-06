
import groovy.json.JsonSlurper
import groovy.json.JsonSlurperClassic

shell = load 'lib/shell.groovy'

def push(appName, hostName, appLocation, version, cfSpace, cfOrg, cfApiEndpoint) {
     bat "set HTTP_PROXY=http://437325:KTS291648_@proxy.cognizant.com:6050"
     echo "-----1"
    authenticate(cfApiEndpoint, cfOrg, cfSpace) {
         echo "-----2"
        bat "cf push ${appName} -p ${appLocation} -n ${hostName} --no-start"
         echo "-----3"
        bat "cf set-env ${appName} VERSION ${version}"
         echo "-----4"
        bat "cf start ${appName}"
         echo "-----5"
    }
}

private authenticate(cfApiEndpoint, cfOrg=null, cfSpace=null, closure) {
    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: "cloudfoundry-credentials", usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
        bat "set HTTP_PROXY=http://437325:KTS291648_@proxy.cognizant.com:6050"
         echo "-----6"
         bat "set http_proxy=http://437325:KTS291648_@proxy.cognizant.com:6050"
        bat "set HTTPS_PROXY=http://437325:KTS291648_@proxy.cognizant.com:6050"
            bat "set https_proxy=http://437325:KTS291648_@proxy.cognizant.com:6050"
        bat("cf api ${cfApiEndpoint}")
         echo "-----7"
        bat("cf auth ${env.USERNAME} ${env.PASSWORD}")
         echo "-----8"
        if (cfOrg && cfSpace) {
            bat("cf target -o ${cfOrg}")
             echo "-----10"
            bat("cf target -s ${cfSpace}")
             echo "-----11"
        }
         echo "-----12"
        closure()
    }
}

def mapRoute(appName, host, cfSpace, cfOrg, cfApiEndpoint) {
    def activeAppName = getActiveAppNameForRoute(host, cfApiEndpoint)
    if(appName.equals(activeAppName)){
        echo("Error mapping route. ${appName} already mapped to this route")
        return
    }
    if(activeAppName){
        input message: "Canary deployment initiated. Do you want to map ${appName} to ${host} along with ${activeAppName}?"
    }
    def domains = getDomains(cfSpace, cfOrg, cfApiEndpoint)
    for(int i = 0; i < (domains.resources.size() as Integer); i++){
        authenticate(cfApiEndpoint, cfOrg, cfSpace) {
            bat("cf map-route ${appName} ${domains.resources[i].entity.name} -n ${host}")
        }
    }
    if(activeAppName){
        input message: "Do you want to remove ${host} mapping from ${activeAppName}"
        for(int i = 0; i < (domains.resources.size() as Integer); i++){
            authenticate(cfApiEndpoint, cfOrg, cfSpace) {
                bat("cf unmap-route ${activeAppName} ${domains.resources[i].entity.name} -n ${host}")
            }
        }
    }
}

def getOrganizations(cfApiEndpoint) {
    return parseJson("/v2/organizations", cfApiEndpoint)
}

def getOrganization(cfOrg, cfApiEndpoint) {
    return getEntityByName(cfOrg, "/v2/organizations", cfApiEndpoint)
}

def getSpace(cfSpace, cfOrg, cfApiEndpoint) {
    def org = getOrganization(cfOrg, cfApiEndpoint)
    getEntityByName(cfSpace, org.entity.spaces_url, cfApiEndpoint)
}

def getDomains(cfSpace, cfOrg, cfApiEndpoint) {
    def space = getSpace(cfSpace, cfOrg, cfApiEndpoint)
    parseJson(space.entity.domains_url, cfApiEndpoint)
}

def parseJson(url, cfApiEndpoint) {
    authenticate(cfApiEndpoint) {
        def contents = shell.pipe("cf curl \"${url}\"") as String
        new JsonSlurperClassic().parseText(contents)
    }
}

def getEntityByName(name, url, cfApiEndpoint){
    def result = parseJson(url,cfApiEndpoint)
    for(int i = 0; i < (result.resources.size() as Integer); i++){
        if(name.equals(result.resources[i].entity.name)){
            return result.resources[i]
        }
    }
}


def getActiveAppNameForRoute(host, cfApiEndpoint){
    def routes = parseJson("/v2/routes?q=host:${host}", cfApiEndpoint)
    if(routes.resources.size() == 0){
        return null
    }
    for(int i = 0; i < (routes.resources.size() as Integer); i++) {
        def apps = parseJson(routes.resources[i].entity.apps_url, cfApiEndpoint)
        for(int j = 0; j < (apps.resources.size() as Integer); j++) {
            if("STARTED".equals(apps.resources[j].entity.state)){
                return apps.resources[j].entity.name
            }
        }
    }
    return null
}


return this
