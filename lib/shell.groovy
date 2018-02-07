def pipe(command){
    String fileName = UUID.randomUUID().toString() + ".tmp"
    bat("${command} | tee ${fileName}")
    def contents = readFile("${fileName}")
    bat("rm ${fileName}")
    return contents
}

return this
