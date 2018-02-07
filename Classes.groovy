
class URLConfig {

    def repo
    def url
   def pomPath

    public URLConfig(String repo, String url, String pomPath) {
        this.repo = repo
        this.url = url
        this.pomPath = pomPath
    }
    public URLConfig URLConfigItem(String repo, String url, String pomPath)
    {
        return  new URLConfig (repo,url, pomPath)
    }
}



class B{
    def greet(name){ return "greet from B: $name!" }
}

// this method just to have nice access to create class by name
Object getProperty(String name){
    return this.getClass().getClassLoader().loadClass(name).newInstance();
} 

return this

