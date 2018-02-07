class URLConfig{
  String repo
  String url
  String pomPath
  def assign(repo,url,pomPath){  
    this.repo=repo
    this.url=url
    this.pomPath=pomPath
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

