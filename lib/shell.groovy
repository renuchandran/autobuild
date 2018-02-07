def pipe(command){
    String fileName = UUID.randomUUID().toString() + ".tmp"
    bat("${command} > ${fileName}")
    bat("type ${fileName} | more")

    def contents = readFile("${fileName}")
    bat("del ${fileName}")
    return contents
}

return this
