import java.nio.file.Files

class Sawfiles {
	File workspace

	Sawfiles(File workspace) {
		this.workspace = workspace
	}

	def static fileTypes = [
		".tar": [{file -> ["tar", "xf", file]}, {file -> ["tar", "cf", file, "."]}],
		".zip": [{file -> ["unzip", file]}, {file -> ["zip", file, "-R", "*"]}]
	]

	void operate(String path, String extension, Closure closure) {
		def abs = new File(workspace, path).getAbsoluteFile().getPath()
		def w = Files.createTempDirectory("sawfiles-").toFile()
		new ProcessBuilder(fileTypes[extension][0](abs)).directory(w).start().waitFor()
		closure.resolveStrategy = Closure.DELEGATE_FIRST
		closure.delegate = new Sawfiles(w)
		closure()
		new ProcessBuilder(fileTypes[extension][1](abs)).directory(w).start().waitFor()
	}

	/*
	 * a work-around for following two methods
	 *  void inside(String path, Closure closure)
	 *  void inside(Map files, Closure c)
	 */
	Closure getInside() {
		return {target, closure ->
			if (target in Map) {
				target.each { path, extension ->
					operate(path, extension, closure)
				}
			} else {
				operate(target, target.substring(target.lastIndexOf(".")), closure)
			}
		}
	}

	/*
	 * a work-around for following method
	 *  void execute(String... args)
	 */
	Closure getExecute() {
		return {String... args -> new ProcessBuilder(args).directory(workspace).start().waitFor()}
	}

	String toString() {
		return "Sawfiles(" + workspace + ")"
	}

    static void main(String[] args) {
		GroovyShell shell = new GroovyShell()
		Script script = shell.parse(new java.io.FileReader(args[0]))

		script.run()
		script.operation.delegate = new Sawfiles(new File(System.getProperty("user.dir")))
		script.operation()
	}
}

